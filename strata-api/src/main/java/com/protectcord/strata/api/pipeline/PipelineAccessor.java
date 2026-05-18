package com.protectcord.strata.api.pipeline;

import java.util.List;

/**
 * Read-only view of the generation pipeline for monitoring and debugging.
 * Third-party plugins can observe pipeline state but cannot modify the
 * stage ordering.
 */
public interface PipelineAccessor {

    /**
     * Returns the ordered list of active stages.
     */
    List<GenerationStage> stages();

    /**
     * Returns whether a specific stage is enabled.
     */
    boolean isStageEnabled(GenerationStage stage);

    /**
     * Returns performance metrics for a stage (average ms per chunk).
     */
    double stageAverageMs(GenerationStage stage);

    /**
     * Returns the total average generation time per chunk in milliseconds.
     */
    double totalAverageMs();
}
