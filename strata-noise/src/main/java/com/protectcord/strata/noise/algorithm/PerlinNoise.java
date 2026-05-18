package com.protectcord.strata.noise.algorithm;

import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.noise.math.NoiseMath;

/**
 * Classic Perlin gradient noise.
 */
public final class PerlinNoise extends AbstractNoiseGenerator {

    private final int[] perm;

    public PerlinNoise(NamespacedKey key, long seed) {
        super(key, seed);
        this.perm = createPermutationTable(seed);
    }

    @Override
    public double sample(double x, double z) {
        int X = NoiseMath.fastFloor(x);
        int Z = NoiseMath.fastFloor(z);

        double xf = x - X;
        double zf = z - Z;

        X &= 255;
        Z &= 255;

        double u = NoiseMath.smootherstep(xf);
        double v = NoiseMath.smootherstep(zf);

        int aa = perm[perm[X] + Z];
        int ab = perm[perm[X] + Z + 1];
        int ba = perm[perm[X + 1] + Z];
        int bb = perm[perm[X + 1] + Z + 1];

        double x1 = NoiseMath.lerp(NoiseMath.grad2(aa, xf, zf), NoiseMath.grad2(ba, xf - 1, zf), u);
        double x2 = NoiseMath.lerp(NoiseMath.grad2(ab, xf, zf - 1), NoiseMath.grad2(bb, xf - 1, zf - 1), u);

        return NoiseMath.lerp(x1, x2, v);
    }

    @Override
    public double sample(double x, double y, double z) {
        int X = NoiseMath.fastFloor(x);
        int Y = NoiseMath.fastFloor(y);
        int Z = NoiseMath.fastFloor(z);

        double xf = x - X;
        double yf = y - Y;
        double zf = z - Z;

        X &= 255;
        Y &= 255;
        Z &= 255;

        double u = NoiseMath.smootherstep(xf);
        double v = NoiseMath.smootherstep(yf);
        double w = NoiseMath.smootherstep(zf);

        int aaa = perm[perm[perm[X] + Y] + Z];
        int aba = perm[perm[perm[X] + Y + 1] + Z];
        int aab = perm[perm[perm[X] + Y] + Z + 1];
        int abb = perm[perm[perm[X] + Y + 1] + Z + 1];
        int baa = perm[perm[perm[X + 1] + Y] + Z];
        int bba = perm[perm[perm[X + 1] + Y + 1] + Z];
        int bab = perm[perm[perm[X + 1] + Y] + Z + 1];
        int bbb = perm[perm[perm[X + 1] + Y + 1] + Z + 1];

        double x1 = NoiseMath.lerp(NoiseMath.grad3(aaa, xf, yf, zf), NoiseMath.grad3(baa, xf - 1, yf, zf), u);
        double x2 = NoiseMath.lerp(NoiseMath.grad3(aba, xf, yf - 1, zf), NoiseMath.grad3(bba, xf - 1, yf - 1, zf), u);
        double y1 = NoiseMath.lerp(x1, x2, v);

        x1 = NoiseMath.lerp(NoiseMath.grad3(aab, xf, yf, zf - 1), NoiseMath.grad3(bab, xf - 1, yf, zf - 1), u);
        x2 = NoiseMath.lerp(NoiseMath.grad3(abb, xf, yf - 1, zf - 1), NoiseMath.grad3(bbb, xf - 1, yf - 1, zf - 1), u);
        double y2 = NoiseMath.lerp(x1, x2, v);

        return NoiseMath.lerp(y1, y2, w);
    }
}
