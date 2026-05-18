package com.protectcord.strata.api.water;

/**
 * Configuration for waterfall generation within the
 * {@link com.protectcord.strata.api.pipeline.GenerationStage#WATER_SYSTEM WATER_SYSTEM} stage.
 *
 * <p>Waterfalls are detected where rivers cross significant elevation changes and are
 * rendered as flowing water blocks cascading down cliff faces.</p>
 *
 * @param enabled       whether waterfall generation is active
 * @param minHeightDrop minimum vertical drop in blocks required to qualify as a waterfall
 * @param maxWidth      maximum waterfall width in blocks
 * @param mistParticles whether to generate mist particle effects at the waterfall base
 * @param poolAtBase    whether to carve a small splash pool at the waterfall base
 * @param frequency     relative frequency of waterfall generation (0.0 = none, 1.0 = maximum)
 * @since 1.0.0
 * @see WaterSystemSettings
 */
public record WaterfallSettings(
        boolean enabled,
        int minHeightDrop,
        int maxWidth,
        boolean mistParticles,
        boolean poolAtBase,
        double frequency
) {

    /**
     * Returns default waterfall settings: minimum 4-block drop, 8-block max width,
     * mist and pool enabled, frequency 0.3.
     *
     * @return sensible default waterfall settings
     */
    public static WaterfallSettings defaults() {
        return new WaterfallSettings(true, 4, 8, true, true, 0.3);
    }
}
