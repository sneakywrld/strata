package com.protectcord.strata.core.pipeline;

import com.protectcord.strata.api.pipeline.GenerationContext;
import com.protectcord.strata.api.pipeline.GenerationStage;

/**
 * Interface for a single stage in the generation pipeline.
 * Each stage receives the generation context and performs its work.
 */
public interface PipelineStage {

    /**
     * Returns which generation stage this implements.
     */
    GenerationStage stage();

    /**
     * Executes this stage's generation logic.
     */
    void generate(GenerationContext context);
}
