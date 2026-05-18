package com.protectcord.strata.api.biome;

import java.util.OptionalInt;

/**
 * Visual and ambient properties of a biome sent to the client for rendering.
 *
 * <p>All color values are packed RGB integers in the format {@code 0xRRGGBB}. Optional color
 * fields (foliage, grass, particle) use {@link OptionalInt#empty()} to indicate that the
 * client should use its default color calculation based on temperature and humidity.</p>
 *
 * @param fogColor           the fog color (packed RGB)
 * @param waterColor         the water surface color (packed RGB)
 * @param waterFogColor      the underwater fog color (packed RGB)
 * @param skyColor           the sky color (packed RGB)
 * @param foliageColor       optional override for foliage (leaf) color, or empty for biome-calculated
 * @param grassColor         optional override for grass block color, or empty for biome-calculated
 * @param grassColorModifier modifier applied to the grass color calculation
 * @param particleColor      optional ambient particle color, or empty for no particles
 * @param temperature        biome temperature affecting snow/rain boundary (0.0 = cold, 2.0 = hot)
 * @param hasPrecipitation   whether this biome has rain or snow
 * @since 1.0.0
 * @see Biome#effects()
 */
public record BiomeEffects(
        int fogColor,
        int waterColor,
        int waterFogColor,
        int skyColor,
        OptionalInt foliageColor,
        OptionalInt grassColor,
        GrassColorModifier grassColorModifier,
        OptionalInt particleColor,
        float temperature,
        boolean hasPrecipitation
) {

    /**
     * Grass color modification modes that alter how the client computes grass tint.
     *
     * @since 1.0.0
     */
    public enum GrassColorModifier {
        /** No modification; standard biome-temperature grass color. */
        NONE,
        /** Dark forest blending (averages with dark green). */
        DARK_FOREST,
        /** Swamp noise-based green/brown mottling. */
        SWAMP
    }

    /**
     * Creates default overworld biome effects with standard sky, fog, and water colors,
     * a temperature of {@code 0.8}, and precipitation enabled.
     *
     * @return default overworld effects
     */
    public static BiomeEffects defaultOverworld() {
        return new BiomeEffects(
                0xC0D8FF, 0x3F76E4, 0x050533, 0x78A7FF,
                OptionalInt.empty(), OptionalInt.empty(),
                GrassColorModifier.NONE, OptionalInt.empty(),
                0.8f, true
        );
    }

    /**
     * Creates default nether biome effects with dark red fog, a temperature of {@code 2.0},
     * and no precipitation.
     *
     * @return default nether effects
     */
    public static BiomeEffects defaultNether() {
        return new BiomeEffects(
                0x330808, 0x3F76E4, 0x050533, 0x330808,
                OptionalInt.empty(), OptionalInt.empty(),
                GrassColorModifier.NONE, OptionalInt.empty(),
                2.0f, false
        );
    }

    /**
     * Creates default end biome effects with muted purple fog, a black sky,
     * a temperature of {@code 0.5}, and no precipitation.
     *
     * @return default end effects
     */
    public static BiomeEffects defaultEnd() {
        return new BiomeEffects(
                0xA080A0, 0x3F76E4, 0x050533, 0x000000,
                OptionalInt.empty(), OptionalInt.empty(),
                GrassColorModifier.NONE, OptionalInt.empty(),
                0.5f, false
        );
    }
}
