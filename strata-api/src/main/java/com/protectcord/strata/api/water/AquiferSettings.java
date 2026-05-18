package com.protectcord.strata.api.water;

import com.protectcord.strata.api.block.StrataBlockState;
import com.protectcord.strata.api.core.NamespacedKey;

/**
 * Configuration for the aquifer simulation system, part of the
 * {@link com.protectcord.strata.api.pipeline.GenerationStage#AQUIFER_PLACEMENT AQUIFER_PLACEMENT} stage.
 *
 * <p>Aquifers create underground water and lava pockets at varying depths, producing
 * realistic underground water tables. Below the {@code lavaThreshold} Y-level, aquifers
 * fill with lava instead of water.</p>
 *
 * @param enabled       whether aquifer simulation is active
 * @param waterBlock    the {@link StrataBlockState} used for water aquifers
 * @param lavaBlock     the {@link StrataBlockState} used for lava aquifers
 * @param lavaThreshold Y-level below which aquifers use lava instead of water
 * @param noiseFunction key of the {@link com.protectcord.strata.api.noise.NoiseFunction} controlling aquifer placement
 * @param barrierWidth  width of the solid barrier between adjacent aquifers (in blocks)
 * @since 1.0.0
 * @see WaterSystemSettings
 */
public record AquiferSettings(
        boolean enabled,
        StrataBlockState waterBlock,
        StrataBlockState lavaBlock,
        int lavaThreshold,
        NamespacedKey noiseFunction,
        int barrierWidth
) {

    /**
     * Returns default aquifer settings: water and lava blocks from vanilla,
     * lava threshold at Y=-10, barrier width 2.
     *
     * @return sensible default aquifer settings
     */
    public static AquiferSettings defaults() {
        return new AquiferSettings(
                true,
                StrataBlockState.of(NamespacedKey.minecraft("water")),
                StrataBlockState.of(NamespacedKey.minecraft("lava")),
                -10, NamespacedKey.strata("aquifer_noise"), 2
        );
    }
}
