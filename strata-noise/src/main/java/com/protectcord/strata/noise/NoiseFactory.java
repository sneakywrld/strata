package com.protectcord.strata.noise;

import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.api.noise.FractalSettings;
import com.protectcord.strata.api.noise.NoiseFunction;
import com.protectcord.strata.api.noise.NoiseType;
import com.protectcord.strata.noise.algorithm.*;
import com.protectcord.strata.noise.cache.NoiseCache;
import com.protectcord.strata.noise.fractal.FractalNoise;

/**
 * Factory for creating noise function instances from configuration parameters.
 */
public final class NoiseFactory {

    private NoiseFactory() {}

    /**
     * Creates a base noise generator of the given type.
     */
    public static NoiseFunction create(NamespacedKey key, NoiseType type, long seed) {
        return switch (type) {
            case PERLIN -> new PerlinNoise(key, seed);
            case SIMPLEX -> new SimplexNoise(key, seed);
            case OPEN_SIMPLEX_2, OPEN_SIMPLEX_2S -> new OpenSimplex2Noise(key, seed);
            case CELLULAR -> new CellularNoise(key, seed);
            case VALUE -> new ValueNoise(key, seed);
            case RIDGED_MULTI -> new RidgedMultifractalNoise(key, seed, FractalSettings.defaultSettings());
            case WHITE -> new WhiteNoise(key, seed);
            case CONSTANT -> constant(key, 0.0);
        };
    }

    /**
     * Creates a fractal noise by stacking octaves of a base noise.
     */
    public static NoiseFunction fractal(NamespacedKey key, NoiseFunction base, FractalSettings settings) {
        return new FractalNoise(key, base, settings);
    }

    /**
     * Wraps a noise function with a cache.
     */
    public static NoiseCache cached(NoiseFunction noise, int maxSize) {
        return new NoiseCache(noise, maxSize);
    }

    /**
     * Creates a constant noise function.
     */
    public static NoiseFunction constant(NamespacedKey key, double value) {
        return new NoiseFunction() {
            @Override public NamespacedKey key() { return key; }
            @Override public double sample(double x, double z) { return value; }
            @Override public double sample(double x, double y, double z) { return value; }
            @Override public double minValue() { return value; }
            @Override public double maxValue() { return value; }
        };
    }
}
