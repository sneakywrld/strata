package com.protectcord.strata.core.pipeline;

import com.protectcord.strata.api.pipeline.GenerationStage;
import com.protectcord.strata.api.pipeline.PipelineAccessor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * The ordered generation pipeline that processes chunks through all stages.
 * Stages execute in {@link GenerationStage} order. Individual stages can
 * be disabled for debugging or custom pipeline configurations.
 */
public final class GenerationPipeline implements PipelineAccessor {

    private static final Logger LOGGER = Logger.getLogger("Strata");

    private final Map<GenerationStage, PipelineStage> stages = new EnumMap<>(GenerationStage.class);
    private final Set<GenerationStage> disabledStages = EnumSet.noneOf(GenerationStage.class);
    private final Map<GenerationStage, RunningAverage> timings = new ConcurrentHashMap<>();

    /**
     * Registers a stage implementation.
     */
    public void registerStage(PipelineStage stage) {
        stages.put(stage.stage(), stage);
        timings.put(stage.stage(), new RunningAverage());
    }

    /**
     * Disables a stage (it will be skipped during generation).
     */
    public void disableStage(GenerationStage stage) {
        disabledStages.add(stage);
    }

    /**
     * Generates a chunk by running all enabled stages in order.
     */
    public void generate(GenerationContextImpl context) {
        for (GenerationStage stage : GenerationStage.values()) {
            if (disabledStages.contains(stage)) continue;

            PipelineStage impl = stages.get(stage);
            if (impl == null) continue;

            context.setCurrentStage(stage);

            long start = System.nanoTime();
            try {
                impl.generate(context);
            } catch (Exception e) {
                LOGGER.severe("Pipeline stage " + stage + " failed at chunk "
                        + context.chunk().coord() + ": " + e.getMessage());
            }
            long elapsed = System.nanoTime() - start;
            timings.get(stage).add(elapsed / 1_000_000.0);
        }
    }

    @Override
    public List<GenerationStage> stages() {
        return Arrays.stream(GenerationStage.values())
                .filter(s -> !disabledStages.contains(s) && stages.containsKey(s))
                .toList();
    }

    @Override
    public boolean isStageEnabled(GenerationStage stage) {
        return !disabledStages.contains(stage) && stages.containsKey(stage);
    }

    @Override
    public double stageAverageMs(GenerationStage stage) {
        RunningAverage avg = timings.get(stage);
        return avg != null ? avg.average() : 0.0;
    }

    @Override
    public double totalAverageMs() {
        return timings.values().stream().mapToDouble(RunningAverage::average).sum();
    }

    /**
     * Tracks a running average with exponential decay.
     */
    private static final class RunningAverage {
        private static final double ALPHA = 0.1;
        private double average;
        private boolean initialized;

        void add(double value) {
            if (!initialized) {
                average = value;
                initialized = true;
            } else {
                average = ALPHA * value + (1.0 - ALPHA) * average;
            }
        }

        double average() {
            return average;
        }
    }
}
