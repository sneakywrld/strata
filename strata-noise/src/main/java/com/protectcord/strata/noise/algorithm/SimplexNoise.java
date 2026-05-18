package com.protectcord.strata.noise.algorithm;

import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.noise.math.NoiseMath;

/**
 * Simplex noise implementation (Ken Perlin's improved gradient noise).
 * Faster and fewer directional artifacts than classic Perlin noise.
 */
public final class SimplexNoise extends AbstractNoiseGenerator {

    private static final double F2 = 0.5 * (Math.sqrt(3.0) - 1.0);
    private static final double G2 = (3.0 - Math.sqrt(3.0)) / 6.0;
    private static final double F3 = 1.0 / 3.0;
    private static final double G3 = 1.0 / 6.0;

    private static final int[][] GRAD3 = {
            {1, 1, 0}, {-1, 1, 0}, {1, -1, 0}, {-1, -1, 0},
            {1, 0, 1}, {-1, 0, 1}, {1, 0, -1}, {-1, 0, -1},
            {0, 1, 1}, {0, -1, 1}, {0, 1, -1}, {0, -1, -1}
    };

    private final int[] perm;

    public SimplexNoise(NamespacedKey key, long seed) {
        super(key, seed);
        this.perm = createPermutationTable(seed);
    }

    @Override
    public double sample(double x, double z) {
        double s = (x + z) * F2;
        int i = NoiseMath.fastFloor(x + s);
        int j = NoiseMath.fastFloor(z + s);

        double t = (i + j) * G2;
        double x0 = x - (i - t);
        double y0 = z - (j - t);

        int i1, j1;
        if (x0 > y0) { i1 = 1; j1 = 0; }
        else { i1 = 0; j1 = 1; }

        double x1 = x0 - i1 + G2;
        double y1 = y0 - j1 + G2;
        double x2 = x0 - 1.0 + 2.0 * G2;
        double y2 = y0 - 1.0 + 2.0 * G2;

        int ii = i & 255;
        int jj = j & 255;

        double n0 = 0, n1 = 0, n2 = 0;

        double t0 = 0.5 - x0 * x0 - y0 * y0;
        if (t0 >= 0) {
            int gi0 = perm[ii + perm[jj]] % 12;
            t0 *= t0;
            n0 = t0 * t0 * dot2(GRAD3[gi0], x0, y0);
        }

        double t1 = 0.5 - x1 * x1 - y1 * y1;
        if (t1 >= 0) {
            int gi1 = perm[ii + i1 + perm[jj + j1]] % 12;
            t1 *= t1;
            n1 = t1 * t1 * dot2(GRAD3[gi1], x1, y1);
        }

        double t2 = 0.5 - x2 * x2 - y2 * y2;
        if (t2 >= 0) {
            int gi2 = perm[ii + 1 + perm[jj + 1]] % 12;
            t2 *= t2;
            n2 = t2 * t2 * dot2(GRAD3[gi2], x2, y2);
        }

        return 70.0 * (n0 + n1 + n2);
    }

    @Override
    public double sample(double x, double y, double z) {
        double s = (x + y + z) * F3;
        int i = NoiseMath.fastFloor(x + s);
        int j = NoiseMath.fastFloor(y + s);
        int k = NoiseMath.fastFloor(z + s);

        double t = (i + j + k) * G3;
        double x0 = x - (i - t);
        double y0 = y - (j - t);
        double z0 = z - (k - t);

        int i1, j1, k1, i2, j2, k2;
        if (x0 >= y0) {
            if (y0 >= z0) { i1=1; j1=0; k1=0; i2=1; j2=1; k2=0; }
            else if (x0 >= z0) { i1=1; j1=0; k1=0; i2=1; j2=0; k2=1; }
            else { i1=0; j1=0; k1=1; i2=1; j2=0; k2=1; }
        } else {
            if (y0 < z0) { i1=0; j1=0; k1=1; i2=0; j2=1; k2=1; }
            else if (x0 < z0) { i1=0; j1=1; k1=0; i2=0; j2=1; k2=1; }
            else { i1=0; j1=1; k1=0; i2=1; j2=1; k2=0; }
        }

        double x1 = x0 - i1 + G3, y1 = y0 - j1 + G3, z1 = z0 - k1 + G3;
        double x2 = x0 - i2 + 2.0*G3, y2 = y0 - j2 + 2.0*G3, z2 = z0 - k2 + 2.0*G3;
        double x3 = x0 - 1.0 + 3.0*G3, y3 = y0 - 1.0 + 3.0*G3, z3 = z0 - 1.0 + 3.0*G3;

        int ii = i & 255, jj = j & 255, kk = k & 255;

        double n0 = 0, n1 = 0, n2 = 0, n3 = 0;

        double t0 = 0.6 - x0*x0 - y0*y0 - z0*z0;
        if (t0 >= 0) { int gi = perm[ii + perm[jj + perm[kk]]] % 12; t0 *= t0; n0 = t0*t0 * dot3(GRAD3[gi], x0, y0, z0); }

        double t1 = 0.6 - x1*x1 - y1*y1 - z1*z1;
        if (t1 >= 0) { int gi = perm[ii+i1 + perm[jj+j1 + perm[kk+k1]]] % 12; t1 *= t1; n1 = t1*t1 * dot3(GRAD3[gi], x1, y1, z1); }

        double t2 = 0.6 - x2*x2 - y2*y2 - z2*z2;
        if (t2 >= 0) { int gi = perm[ii+i2 + perm[jj+j2 + perm[kk+k2]]] % 12; t2 *= t2; n2 = t2*t2 * dot3(GRAD3[gi], x2, y2, z2); }

        double t3 = 0.6 - x3*x3 - y3*y3 - z3*z3;
        if (t3 >= 0) { int gi = perm[ii+1 + perm[jj+1 + perm[kk+1]]] % 12; t3 *= t3; n3 = t3*t3 * dot3(GRAD3[gi], x3, y3, z3); }

        return 32.0 * (n0 + n1 + n2 + n3);
    }

    private static double dot2(int[] g, double x, double y) {
        return g[0] * x + g[1] * y;
    }

    private static double dot3(int[] g, double x, double y, double z) {
        return g[0] * x + g[1] * y + g[2] * z;
    }
}
