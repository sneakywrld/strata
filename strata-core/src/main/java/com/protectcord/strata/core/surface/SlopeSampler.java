package com.protectcord.strata.core.surface;

/**
 * Utility class for computing terrain slope from a heightmap using finite differences.
 */
public final class SlopeSampler {

    private SlopeSampler() {}

    /**
     * Computes the terrain slope at position (x, z) in a heightmap using finite differences.
     * slope = max(abs(h[x+1]-h[x-1]), abs(h[z+1]-h[z-1])) / 2.0
     *
     * @param x         the local X coordinate within the heightmap
     * @param z         the local Z coordinate within the heightmap
     * @param heightmap flat array of height values indexed by [x + z * chunkSize]
     * @param chunkSize the side length of the heightmap (typically 16)
     * @return the slope value (0.0 = flat, higher = steeper)
     */
    public static double getSlopeAt(int x, int z, int[] heightmap, int chunkSize) {
        int xm = Math.max(x - 1, 0);
        int xp = Math.min(x + 1, chunkSize - 1);
        int zm = Math.max(z - 1, 0);
        int zp = Math.min(z + 1, chunkSize - 1);

        int hxm = heightmap[xm + z * chunkSize];
        int hxp = heightmap[xp + z * chunkSize];
        int hzm = heightmap[x + zm * chunkSize];
        int hzp = heightmap[x + zp * chunkSize];

        double dxSlope = Math.abs(hxp - hxm);
        double dzSlope = Math.abs(hzp - hzm);

        return Math.max(dxSlope, dzSlope) / 2.0;
    }
}
