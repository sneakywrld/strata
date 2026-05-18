package com.protectcord.strata.api.terrain;

import com.protectcord.strata.api.core.NamespacedKey;

/**
 * Global terrain shaping parameters for a {@link com.protectcord.strata.api.world.WorldProfile}.
 *
 * <p>These settings control the fundamental vertical geometry of the world: Y-range,
 * sea level, the primary density function, continental and erosion splines, and global
 * height transformations. They are consumed during the
 * {@link com.protectcord.strata.api.pipeline.GenerationStage#TERRAIN_SHAPING TERRAIN_SHAPING} stage.</p>
 *
 * @param seaLevel          the Y-level of the sea surface (blocks at or below this are flooded)
 * @param minY              the minimum Y coordinate of the world (e.g., {@code -64})
 * @param maxY              the maximum Y coordinate of the world (e.g., {@code 320})
 * @param densityFunction   key of the main {@link DensityFunction} in the noise registry
 * @param continentalSpline key of the continental shape {@link Spline} controlling landmass shape
 * @param erosionSpline     key of the erosion shaping {@link Spline} controlling terrain smoothness
 * @param baseHeightOffset  global height offset applied after density computation
 * @param heightScale       vertical stretch factor applied to final terrain height
 * @since 1.0.0
 * @see DensityFunction
 * @see Spline
 */
public record TerrainSettings(
        int seaLevel,
        int minY,
        int maxY,
        NamespacedKey densityFunction,
        NamespacedKey continentalSpline,
        NamespacedKey erosionSpline,
        double baseHeightOffset,
        double heightScale
) {

    /**
     * Returns default overworld terrain settings: sea level 63, Y range -64 to 320.
     *
     * @return default overworld settings
     */
    public static TerrainSettings defaultOverworld() {
        return new TerrainSettings(
                63, -64, 320,
                NamespacedKey.strata("overworld_density"),
                NamespacedKey.strata("continental_shape"),
                NamespacedKey.strata("erosion_shape"),
                0.0, 1.0
        );
    }

    /**
     * Returns default nether terrain settings: sea level 32, Y range 0 to 128.
     *
     * @return default nether settings
     */
    public static TerrainSettings defaultNether() {
        return new TerrainSettings(
                32, 0, 128,
                NamespacedKey.strata("nether_density"),
                NamespacedKey.strata("nether_shape"),
                NamespacedKey.strata("nether_erosion"),
                0.0, 1.0
        );
    }

    /**
     * Returns default end terrain settings: sea level 0, Y range 0 to 256.
     *
     * @return default end settings
     */
    public static TerrainSettings defaultEnd() {
        return new TerrainSettings(
                0, 0, 256,
                NamespacedKey.strata("end_density"),
                NamespacedKey.strata("end_shape"),
                NamespacedKey.strata("end_erosion"),
                0.0, 1.0
        );
    }
}
