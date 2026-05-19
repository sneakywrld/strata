package com.protectcord.strata.noise;

import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.api.noise.NoiseFunction;
import com.protectcord.strata.noise.cache.InterpolationGrid;
import com.protectcord.strata.noise.math.NoiseMath;

/**
 * Coarse-grid sampling with trilinear/bilinear interpolation.
 *
 * <p>Samples the underlying noise function at regular grid intervals and
 * interpolates between grid points, trading a small amount of detail for
 * a large reduction in noise evaluations. This is particularly effective
 * for expensive noise functions used in terrain density.</p>
 */
public final class InterpolatedNoise implements NoiseFunction {

    private final NamespacedKey key;
    private final NoiseFunction base;
    private final int spacingX;
    private final int spacingY;
    private final int spacingZ;

    public InterpolatedNoise(NamespacedKey key, NoiseFunction base,
                             int spacingX, int spacingY, int spacingZ) {
        if (spacingX < 1 || spacingY < 1 || spacingZ < 1) {
            throw new IllegalArgumentException("Grid spacing must be >= 1");
        }
        this.key = key;
        this.base = base;
        this.spacingX = spacingX;
        this.spacingY = spacingY;
        this.spacingZ = spacingZ;
    }

    public InterpolatedNoise(NamespacedKey key, NoiseFunction base, int spacingXZ, int spacingY) {
        this(key, base, spacingXZ, spacingY, spacingXZ);
    }

    @Override
    public NamespacedKey key() {
        return key;
    }

    @Override
    public double sample(double x, double z) {
        double gx = x / spacingX;
        double gz = z / spacingZ;

        int x0 = NoiseMath.fastFloor(gx);
        int z0 = NoiseMath.fastFloor(gz);
        int x1 = x0 + 1;
        int z1 = z0 + 1;

        double tx = gx - x0;
        double tz = gz - z0;

        double c00 = base.sample((double) x0 * spacingX, (double) z0 * spacingZ);
        double c10 = base.sample((double) x1 * spacingX, (double) z0 * spacingZ);
        double c01 = base.sample((double) x0 * spacingX, (double) z1 * spacingZ);
        double c11 = base.sample((double) x1 * spacingX, (double) z1 * spacingZ);

        double a = NoiseMath.lerp(c00, c10, tx);
        double b = NoiseMath.lerp(c01, c11, tx);
        return NoiseMath.lerp(a, b, tz);
    }

    @Override
    public double sample(double x, double y, double z) {
        double gx = x / spacingX;
        double gy = y / spacingY;
        double gz = z / spacingZ;

        int x0 = NoiseMath.fastFloor(gx);
        int y0 = NoiseMath.fastFloor(gy);
        int z0 = NoiseMath.fastFloor(gz);
        int x1 = x0 + 1;
        int y1 = y0 + 1;
        int z1 = z0 + 1;

        double tx = gx - x0;
        double ty = gy - y0;
        double tz = gz - z0;

        double sx0 = (double) x0 * spacingX;
        double sx1 = (double) x1 * spacingX;
        double sy0 = (double) y0 * spacingY;
        double sy1 = (double) y1 * spacingY;
        double sz0 = (double) z0 * spacingZ;
        double sz1 = (double) z1 * spacingZ;

        double c000 = base.sample(sx0, sy0, sz0);
        double c100 = base.sample(sx1, sy0, sz0);
        double c010 = base.sample(sx0, sy1, sz0);
        double c110 = base.sample(sx1, sy1, sz0);
        double c001 = base.sample(sx0, sy0, sz1);
        double c101 = base.sample(sx1, sy0, sz1);
        double c011 = base.sample(sx0, sy1, sz1);
        double c111 = base.sample(sx1, sy1, sz1);

        double a0 = NoiseMath.lerp(c000, c100, tx);
        double a1 = NoiseMath.lerp(c010, c110, tx);
        double a2 = NoiseMath.lerp(c001, c101, tx);
        double a3 = NoiseMath.lerp(c011, c111, tx);

        double b0 = NoiseMath.lerp(a0, a1, ty);
        double b1 = NoiseMath.lerp(a2, a3, ty);

        return NoiseMath.lerp(b0, b1, tz);
    }

    @Override
    public double minValue() {
        return base.minValue();
    }

    @Override
    public double maxValue() {
        return base.maxValue();
    }

    public InterpolationGrid createGrid(int gridCountX, int gridCountY, int gridCountZ) {
        return new InterpolationGrid(gridCountX, gridCountY, gridCountZ, spacingX, spacingY, spacingZ);
    }
}
