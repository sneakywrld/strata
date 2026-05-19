package com.protectcord.strata.core.terrain;

import com.protectcord.strata.api.biome.ClimateParameters;
import com.protectcord.strata.api.noise.NoiseFunction;
import com.protectcord.strata.api.pipeline.GenerationContext;
import com.protectcord.strata.api.terrain.TerrainSettings;

/**
 * Evaluates terrain density at (x,y,z). Returns positive for solid, negative for air.
 * Uses base height from splines plus 3D density noise offset.
 */
public final class DensityEngine {

    private final NoiseFunction densityNoise;
    private final SplineEvaluator splineEvaluator;

    public DensityEngine(NoiseFunction densityNoise, SplineEvaluator splineEvaluator) {
        this.densityNoise = densityNoise;
        this.splineEvaluator = splineEvaluator;
    }

    public double evaluate(int x, int y, int z, GenerationContext ctx) {
        TerrainSettings terrain = ctx.profile().terrainSettings();
        double heightScale = terrain.heightScale();
        int seaLevel = terrain.seaLevel();
        double baseHeightOffset = terrain.baseHeightOffset();

        float[] continentalValues = ctx.has("continental_values")
                ? ctx.get("continental_values", float[].class)
                : null;

        double continentalness = 0.0;
        double erosion = 0.0;
        double weirdness = 0.0;

        if (ctx.has("climate_grid")) {
            ClimateParameters[] grid =
                    ctx.get("climate_grid", ClimateParameters[].class);
            int lx = x & 15;
            int lz = z & 15;
            int sx = Math.min(lx / 4, 3);
            int sz = Math.min(lz / 4, 3);
            ClimateParameters climate = grid[sx + sz * 4];
            continentalness = climate.continentalness();
            erosion = climate.erosion();
            weirdness = climate.weirdness();
        } else if (continentalValues != null) {
            int lx = x & 15;
            int lz = z & 15;
            continentalness = continentalValues[lx + lz * 16];
        }

        double baseHeight = splineEvaluator.evaluate(continentalness, erosion, weirdness);

        double heightBias = (baseHeight + seaLevel - y) * 0.02 + baseHeightOffset;

        double noise3D = densityNoise.sample(
                x * 0.01 * heightScale,
                y * 0.015,
                z * 0.01 * heightScale
        );

        return noise3D + heightBias;
    }
}
