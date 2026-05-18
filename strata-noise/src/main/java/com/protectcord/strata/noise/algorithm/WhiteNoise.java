package com.protectcord.strata.noise.algorithm;

import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.noise.math.NoiseMath;

/**
 * White noise — pure random values at each coordinate.
 * Deterministic for the same seed + coordinates.
 */
public final class WhiteNoise extends AbstractNoiseGenerator {

    public WhiteNoise(NamespacedKey key, long seed) {
        super(key, seed);
    }

    @Override
    public double sample(double x, double z) {
        int xi = NoiseMath.fastFloor(x);
        int zi = NoiseMath.fastFloor(z);
        return NoiseMath.hashToDouble(NoiseMath.hash(seed, xi, zi));
    }

    @Override
    public double sample(double x, double y, double z) {
        int xi = NoiseMath.fastFloor(x);
        int yi = NoiseMath.fastFloor(y);
        int zi = NoiseMath.fastFloor(z);
        return NoiseMath.hashToDouble(NoiseMath.hash(seed, xi, yi, zi));
    }
}
