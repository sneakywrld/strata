package com.protectcord.strata.api.biome;

import java.util.OptionalInt;

/**
 * Color properties for a biome's fog, water, and vegetation rendering.
 *
 * <p>All color values are packed RGB integers in the format {@code 0xRRGGBB}.
 * The {@link #foliageColorOverride()} field uses {@link OptionalInt#empty()} to indicate
 * the client should compute foliage color from temperature and humidity.</p>
 *
 * @param fogColor              the fog color (packed RGB)
 * @param waterColor            the water surface color (packed RGB)
 * @param waterFogColor         the underwater fog color (packed RGB)
 * @param grassColorModifier    optional modifier for grass color calculation
 * @param foliageColorOverride  optional override for foliage color, or empty for biome-calculated
 * @since 1.0.0
 */
public record BiomeColors(
        int fogColor,
        int waterColor,
        int waterFogColor,
        GrassColorModifier grassColorModifier,
        OptionalInt foliageColorOverride
) {

    public enum GrassColorModifier {
        NONE,
        DARK_FOREST,
        SWAMP
    }

    public BiomeColors {
        if (grassColorModifier == null) {
            grassColorModifier = GrassColorModifier.NONE;
        }
        if (foliageColorOverride == null) {
            foliageColorOverride = OptionalInt.empty();
        }
    }

    public static BiomeColors defaultOverworld() {
        return new BiomeColors(
                0xC0D8FF, 0x3F76E4, 0x050533,
                GrassColorModifier.NONE, OptionalInt.empty()
        );
    }

    public static BiomeColors defaultNether() {
        return new BiomeColors(
                0x330808, 0x3F76E4, 0x050533,
                GrassColorModifier.NONE, OptionalInt.empty()
        );
    }

    public static BiomeColors defaultEnd() {
        return new BiomeColors(
                0xA080A0, 0x3F76E4, 0x050533,
                GrassColorModifier.NONE, OptionalInt.empty()
        );
    }
}
