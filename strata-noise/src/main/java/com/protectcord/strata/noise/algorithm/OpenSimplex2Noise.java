package com.protectcord.strata.noise.algorithm;

import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.noise.math.NoiseMath;

/**
 * OpenSimplex2 noise — a patent-free simplex-type noise algorithm.
 * Provides good quality with no directional artifacts.
 */
public final class OpenSimplex2Noise extends AbstractNoiseGenerator {

    private static final double SQRT3 = Math.sqrt(3.0);
    private static final double PMUL = 0x5DEECE66DL;

    private final long seedField;

    public OpenSimplex2Noise(NamespacedKey key, long seed) {
        super(key, seed);
        this.seedField = seed;
    }

    @Override
    public double sample(double x, double z) {
        // Skew to and from triangular grid
        double s = 0.366025403784439 * (x + z);
        double xs = x + s, zs = z + s;

        int xsb = NoiseMath.fastFloor(xs), zsb = NoiseMath.fastFloor(zs);
        double xsi = xs - xsb, zsi = zs - zsb;

        double t = (xsi + zsi) * -0.211324865405187;
        double dx0 = xsi + t, dz0 = zsi + t;

        double value = 0;
        double a0 = 2.0 / 3.0 - dx0 * dx0 - dz0 * dz0;
        if (a0 > 0) {
            value = (a0 * a0) * (a0 * a0) * grad(seedField, xsb, zsb, dx0, dz0);
        }

        double a1 = 2.0 * (1.0 - 2.0 * 0.211324865405187) * (1.0 / 0.211324865405187 + 2.0) * t
                + (-2.0 * (1.0 - 2.0 * 0.211324865405187) * (1.0 - 2.0 * 0.211324865405187) + a0);
        if (a1 > 0) {
            double dx1 = dx0 - (1 - 2.0 * 0.211324865405187);
            double dz1 = dz0 - (1 - 2.0 * 0.211324865405187);
            value += (a1 * a1) * (a1 * a1) * grad(seedField, xsb + 1, zsb + 1, dx1, dz1);
        }

        if (dz0 > dx0) {
            double dx2 = dx0 + 0.211324865405187;
            double dz2 = dz0 + (0.211324865405187 - 1);
            double a2 = 2.0 / 3.0 - dx2 * dx2 - dz2 * dz2;
            if (a2 > 0) {
                value += (a2 * a2) * (a2 * a2) * grad(seedField, xsb, zsb + 1, dx2, dz2);
            }
        } else {
            double dx2 = dx0 + (0.211324865405187 - 1);
            double dz2 = dz0 + 0.211324865405187;
            double a2 = 2.0 / 3.0 - dx2 * dx2 - dz2 * dz2;
            if (a2 > 0) {
                value += (a2 * a2) * (a2 * a2) * grad(seedField, xsb + 1, zsb, dx2, dz2);
            }
        }

        return value;
    }

    @Override
    public double sample(double x, double y, double z) {
        // Rotate to lattice-simplex coordinates
        double r = (2.0 / 3.0) * (x + y + z);
        double xr = r - x, yr = r - y, zr = r - z;

        int xrb = NoiseMath.fastFloor(xr), yrb = NoiseMath.fastFloor(yr), zrb = NoiseMath.fastFloor(zr);
        double xri = xr - xrb, yri = yr - yrb, zri = zr - zrb;

        int xNSign = (int) (-1.0 - xri) | 1, yNSign = (int) (-1.0 - yri) | 1, zNSign = (int) (-1.0 - zri) | 1;
        double ax0 = xNSign * -xri, ay0 = yNSign * -yri, az0 = zNSign * -zri;

        double value = 0;

        double a = 0.75 - xri * xri - yri * yri - zri * zri;
        if (a > 0) value = (a * a) * (a * a) * grad3d(seedField, xrb, yrb, zrb, xri, yri, zri);

        if (ax0 >= ay0 && ax0 >= az0) {
            double b = a + ax0 + ax0;
            if (b > 1) {
                b -= 1;
                value += (b * b) * (b * b) * grad3d(seedField, xrb - xNSign, yrb, zrb, xri + xNSign, yri, zri);
            }
        } else if (ay0 > ax0 && ay0 >= az0) {
            double b = a + ay0 + ay0;
            if (b > 1) {
                b -= 1;
                value += (b * b) * (b * b) * grad3d(seedField, xrb, yrb - yNSign, zrb, xri, yri + yNSign, zri);
            }
        } else {
            double b = a + az0 + az0;
            if (b > 1) {
                b -= 1;
                value += (b * b) * (b * b) * grad3d(seedField, xrb, yrb, zrb - zNSign, xri, yri, zri + zNSign);
            }
        }

        return value;
    }

    private static double grad(long seed, int xsv, int zsv, double dx, double dz) {
        long hash = seed ^ (xsv * 0x5205402B9270C86FL) ^ (zsv * 0x598CD327003817B5L);
        hash *= hash * 0x6C8E9CF570932BD5L;
        hash ^= hash >>> 33;
        int gi = (int) (hash) & 0xFE;
        return (gi - 127) * 0.007874015748031496 * dx + ((gi >> 1) - 63) * 0.015873015873015872 * dz;
    }

    private static double grad3d(long seed, int xrv, int yrv, int zrv, double dx, double dy, double dz) {
        long hash = seed ^ (xrv * 0x5205402B9270C86FL) ^ (yrv * 0x598CD327003817B5L) ^ (zrv * 0x6B9B47B359FDBA6DL);
        hash *= hash * 0x6C8E9CF570932BD5L;
        hash ^= hash >>> 33;
        int gi = (int) (hash) & 0xFC;
        return (gi - 127) * 0.007874015748031496 * dx
                + ((gi >> 1) - 63) * 0.015873015873015872 * dy
                + ((gi >> 2) - 31) * 0.032258064516129032 * dz;
    }
}
