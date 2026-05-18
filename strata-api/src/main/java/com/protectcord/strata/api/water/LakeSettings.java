package com.protectcord.strata.api.water;

/**
 * Configuration for inland lake generation within the
 * {@link com.protectcord.strata.api.pipeline.GenerationStage#WATER_SYSTEM WATER_SYSTEM} stage.
 *
 * <p>Controls the size, frequency, and lava-lake behavior for naturally generated lakes.
 * Water lakes form at the surface; lava lakes form underground within the configured Y range.</p>
 *
 * @param enabled   whether lake generation is active
 * @param frequency relative frequency of lake generation (0.0 = none, 1.0 = maximum)
 * @param minSize   minimum lake radius in blocks
 * @param maxSize   maximum lake radius in blocks
 * @param allowLava whether lava lakes can generate deep underground
 * @param lavaMinY  minimum Y coordinate for lava lake generation
 * @param lavaMaxY  maximum Y coordinate for lava lake generation
 * @since 1.0.0
 * @see WaterSystemSettings
 */
public record LakeSettings(
        boolean enabled,
        double frequency,
        int minSize,
        int maxSize,
        boolean allowLava,
        int lavaMinY,
        int lavaMaxY
) {

    /**
     * Returns default lake settings: frequency 0.25, radius 4-16, lava lakes enabled below Y=0.
     *
     * @return sensible default lake settings
     */
    public static LakeSettings defaults() {
        return new LakeSettings(true, 0.25, 4, 16, true, -64, 0);
    }
}
