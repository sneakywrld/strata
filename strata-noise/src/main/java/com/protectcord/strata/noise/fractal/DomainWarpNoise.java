package com.protectcord.strata.noise.fractal;

import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.api.noise.NoiseFunction;
import com.protectcord.strata.noise.algorithm.AbstractNoiseGenerator;

/**
 * Domain warping — distorts the input coordinates of a noise function
 * using another noise function, creating organic, swirling patterns.
 */
public final class DomainWarpNoise extends AbstractNoiseGenerator {

    private final NoiseFunction source;
    private final NoiseFunction warpX;
    private final NoiseFunction warpZ;
    private final NoiseFunction warpY;
    private final double warpStrength;

    public DomainWarpNoise(NamespacedKey key, NoiseFunction source,
                            NoiseFunction warpX, NoiseFunction warpZ,
                            double warpStrength) {
        super(key, 0);
        this.source = source;
        this.warpX = warpX;
        this.warpZ = warpZ;
        this.warpY = null;
        this.warpStrength = warpStrength;
    }

    public DomainWarpNoise(NamespacedKey key, NoiseFunction source,
                            NoiseFunction warpX, NoiseFunction warpY, NoiseFunction warpZ,
                            double warpStrength) {
        super(key, 0);
        this.source = source;
        this.warpX = warpX;
        this.warpZ = warpZ;
        this.warpY = warpY;
        this.warpStrength = warpStrength;
    }

    @Override
    public double sample(double x, double z) {
        double wx = x + warpX.sample(x, z) * warpStrength;
        double wz = z + warpZ.sample(x, z) * warpStrength;
        return source.sample(wx, wz);
    }

    @Override
    public double sample(double x, double y, double z) {
        double wx = x + warpX.sample(x, y, z) * warpStrength;
        double wy = warpY != null ? y + warpY.sample(x, y, z) * warpStrength : y;
        double wz = z + warpZ.sample(x, y, z) * warpStrength;
        return source.sample(wx, wy, wz);
    }
}
