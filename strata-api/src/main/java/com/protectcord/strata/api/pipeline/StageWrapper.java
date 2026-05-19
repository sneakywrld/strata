package com.protectcord.strata.api.pipeline;

/**
 * A decorator that wraps a {@link GenerationStage} to inject behavior before and/or
 * after stage execution.
 *
 * <p>Stage wrappers enable cross-cutting concerns such as performance monitoring,
 * logging, event firing, and validation without modifying the stage implementation itself.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * StageWrapper timingWrapper = new StageWrapper() {
 *     public void before(GenerationContext context) {
 *         context.put("stage_start", System.nanoTime());
 *     }
 *     public void after(GenerationContext context) {
 *         long elapsed = System.nanoTime() - context.get("stage_start", Long.class);
 *     }
 * };
 * }</pre>
 *
 * @since 1.0.0
 * @see GenerationStage
 * @see GenerationContext
 */
public interface StageWrapper {

    /**
     * Called immediately before the wrapped stage executes.
     *
     * @param context the generation context for the current chunk
     */
    default void before(GenerationContext context) {
    }

    /**
     * Called immediately after the wrapped stage completes.
     *
     * @param context the generation context for the current chunk
     */
    default void after(GenerationContext context) {
    }

    /**
     * Wraps a generation stage, returning a new stage that invokes {@link #before(GenerationContext)}
     * and {@link #after(GenerationContext)} around the inner stage's execution.
     *
     * @param inner the stage to wrap
     * @return a decorated stage with before/after hooks applied
     */
    default GenerationStage wrap(GenerationStage inner) {
        StageWrapper self = this;
        return new GenerationStage() {
            @Override
            public int order() {
                return inner.order();
            }

            @Override
            public String toString() {
                return "Wrapped[" + inner + "]";
            }
        };
    }
}
