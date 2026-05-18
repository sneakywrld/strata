package com.protectcord.strata.api.water;

/**
 * Top-level water system settings that aggregate all water-related sub-systems.
 *
 * <p>This record is the main configuration entry point for water generation in a
 * {@link com.protectcord.strata.api.world.WorldProfile}. It bundles settings for
 * {@linkplain RiverSettings rivers}, {@linkplain OceanSettings oceans},
 * {@linkplain WaterfallSettings waterfalls}, {@linkplain LakeSettings lakes}, and
 * {@linkplain AquiferSettings aquifers}.</p>
 *
 * @param rivers    river generation configuration
 * @param oceans    ocean generation configuration
 * @param waterfalls waterfall generation configuration
 * @param lakes     lake generation configuration
 * @param aquifers  underground aquifer configuration
 * @since 1.0.0
 * @see com.protectcord.strata.api.world.WorldProfile#waterSettings()
 */
public record WaterSystemSettings(
        RiverSettings rivers,
        OceanSettings oceans,
        WaterfallSettings waterfalls,
        LakeSettings lakes,
        AquiferSettings aquifers
) {

    /**
     * Returns default water system settings with all sub-systems at their default values.
     *
     * @return sensible default settings for all water sub-systems
     */
    public static WaterSystemSettings defaults() {
        return new WaterSystemSettings(
                RiverSettings.defaults(),
                OceanSettings.defaults(),
                WaterfallSettings.defaults(),
                LakeSettings.defaults(),
                AquiferSettings.defaults()
        );
    }
}
