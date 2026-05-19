package com.protectcord.strata.api.pipeline;

import java.util.List;

/**
 * Read-only view of the generation pipeline for monitoring, debugging, and performance analysis.
 *
 * <p>Third-party plugins can observe pipeline state -- which stages are enabled, their execution
 * order, and per-stage performance metrics -- but cannot modify the stage ordering or enable/disable
 * stages through this interface.</p>
 *
 * <p>Useful for diagnostic plugins, performance dashboards, and admin commands that report
 * generation statistics.</p>
 *
 * @since 1.0.0
 * @see GenerationStage
 */
public interface PipelineAccessor {

    /**
     * Returns the ordered list of currently active (enabled) pipeline stages.
     *
     * @return an unmodifiable list of active {@link GenerationStage}s in execution order,
     *         never {@code null}
     */
    List<GenerationStage> stages();

    /**
     * Returns whether a specific pipeline stage is enabled.
     *
     * <p>Disabled stages are skipped during chunk generation.</p>
     *
     * @param stage the stage to check
     * @return {@code true} if the stage is enabled and will execute during generation
     */
    boolean isStageEnabled(GenerationStage stage);

    /**
     * Returns the average execution time for a stage in milliseconds per chunk.
     *
     * <p>This is a rolling average computed over recent chunk generations. Returns {@code 0.0}
     * if the stage is disabled or no chunks have been generated yet.</p>
     *
     * @param stage the stage to query
     * @return the average execution time in milliseconds
     */
    double stageAverageMs(GenerationStage stage);

    /**
     * Returns the total average generation time per chunk across all stages, in milliseconds.
     *
     * @return the total average generation time in milliseconds
     */
    double totalAverageMs();
}
