package com.protectcord.strata.noise.algorithm;

import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.noise.math.NoiseMath;

/**
 * Value noise — interpolated random values at integer grid points.
 * Smoother than white noise, useful as a basis for domain warping.
 */
public final class ValueNoise extends AbstractNoiseGenerator {

    public ValueNoise(NamespacedKey key, long seed) {
        super(key, seed);
    }

    @Override
    public double sample(double x, double z) {
        int x0 = NoiseMath.fastFloor(x);
        int z0 = NoiseMath.fastFloor(z);

        double xf = NoiseMath.smootherstep(x - x0);
        double zf = NoiseMath.smootherstep(z - z0);

        double v00 = NoiseMath.hashToDouble(NoiseMath.hash(seed, x0, z0));
        double v10 = NoiseMath.hashToDouble(NoiseMath.hash(seed, x0 + 1, z0));
        double v01 = NoiseMath.hashToDouble(NoiseMath.hash(seed, x0, z0 + 1));
        double v11 = NoiseMath.hashToDouble(NoiseMath.hash(seed, x0 + 1, z0 + 1));

        return NoiseMath.lerp(
                NoiseMath.lerp(v00, v10, xf),
                NoiseMath.lerp(v01, v11, xf),
                zf
        );
    }

    @Override
    public double sample(double x, double y, double z) {
        int x0 = NoiseMath.fastFloor(x);
        int y0 = NoiseMath.fastFloor(y);
        int z0 = NoiseMath.fastFloor(z);

        double xf = NoiseMath.smootherstep(x - x0);
        double yf = NoiseMath.smootherstep(y - y0);
        double zf = NoiseMath.smootherstep(z - z0);

        double v000 = NoiseMath.hashToDouble(NoiseMath.hash(seed, x0, y0, z0));
        double v100 = NoiseMath.hashToDouble(NoiseMath.hash(seed, x0+1, y0, z0));
        double v010 = NoiseMath.hashToDouble(NoiseMath.hash(seed, x0, y0+1, z0));
        double v110 = NoiseMath.hashToDouble(NoiseMath.hash(seed, x0+1, y0+1, z0));
        double v001 = NoiseMath.hashToDouble(NoiseMath.hash(seed, x0, y0, z0+1));
        double v101 = NoiseMath.hashToDouble(NoiseMath.hash(seed, x0+1, y0, z0+1));
        double v011 = NoiseMath.hashToDouble(NoiseMath.hash(seed, x0, y0+1, z0+1));
        double v111 = NoiseMath.hashToDouble(NoiseMath.hash(seed, x0+1, y0+1, z0+1));

        return NoiseMath.lerp(
                NoiseMath.lerp(NoiseMath.lerp(v000, v100, xf), NoiseMath.lerp(v010, v110, xf), yf),
                NoiseMath.lerp(NoiseMath.lerp(v001, v101, xf), NoiseMath.lerp(v011, v111, xf), yf),
                zf
        );
    }
}
