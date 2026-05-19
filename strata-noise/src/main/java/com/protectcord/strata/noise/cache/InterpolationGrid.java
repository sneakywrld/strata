package com.protectcord.strata.noise.cache;

import com.protectcord.strata.api.noise.NoiseFunction;
import com.protectcord.strata.noise.math.NoiseMath;

public final class InterpolationGrid {

    private final int gridSizeX;
    private final int gridSizeY;
    private final int gridSizeZ;
    private final double spacingX;
    private final double spacingY;
    private final double spacingZ;
    private double originX;
    private double originY;
    private double originZ;
    private double[] values;

    public InterpolationGrid(int gridSizeX, int gridSizeY, int gridSizeZ,
                             double spacingX, double spacingY, double spacingZ) {
        this.gridSizeX = gridSizeX;
        this.gridSizeY = gridSizeY;
        this.gridSizeZ = gridSizeZ;
        this.spacingX = spacingX;
        this.spacingY = spacingY;
        this.spacingZ = spacingZ;
        this.values = new double[gridSizeX * gridSizeY * gridSizeZ];
    }

    public void populate(NoiseFunction source, double originX, double originY, double originZ) {
        this.originX = originX;
        this.originY = originY;
        this.originZ = originZ;

        for (int gx = 0; gx < gridSizeX; gx++) {
            for (int gy = 0; gy < gridSizeY; gy++) {
                for (int gz = 0; gz < gridSizeZ; gz++) {
                    double wx = originX + gx * spacingX;
                    double wy = originY + gy * spacingY;
                    double wz = originZ + gz * spacingZ;
                    values[index(gx, gy, gz)] = source.sample(wx, wy, wz);
                }
            }
        }
    }

    public double sample(double x, double y, double z) {
        double localX = (x - originX) / spacingX;
        double localY = (y - originY) / spacingY;
        double localZ = (z - originZ) / spacingZ;

        int gx0 = NoiseMath.fastFloor(localX);
        int gy0 = NoiseMath.fastFloor(localY);
        int gz0 = NoiseMath.fastFloor(localZ);

        gx0 = clampGrid(gx0, gridSizeX - 2);
        gy0 = clampGrid(gy0, gridSizeY - 2);
        gz0 = clampGrid(gz0, gridSizeZ - 2);

        double fx = localX - gx0;
        double fy = localY - gy0;
        double fz = localZ - gz0;

        fx = NoiseMath.clamp(fx, 0, 1);
        fy = NoiseMath.clamp(fy, 0, 1);
        fz = NoiseMath.clamp(fz, 0, 1);

        double c000 = getGrid(gx0, gy0, gz0);
        double c100 = getGrid(gx0 + 1, gy0, gz0);
        double c010 = getGrid(gx0, gy0 + 1, gz0);
        double c110 = getGrid(gx0 + 1, gy0 + 1, gz0);
        double c001 = getGrid(gx0, gy0, gz0 + 1);
        double c101 = getGrid(gx0 + 1, gy0, gz0 + 1);
        double c011 = getGrid(gx0, gy0 + 1, gz0 + 1);
        double c111 = getGrid(gx0 + 1, gy0 + 1, gz0 + 1);

        double c00 = NoiseMath.lerp(c000, c100, fx);
        double c10 = NoiseMath.lerp(c010, c110, fx);
        double c01 = NoiseMath.lerp(c001, c101, fx);
        double c11 = NoiseMath.lerp(c011, c111, fx);

        double c0 = NoiseMath.lerp(c00, c10, fy);
        double c1 = NoiseMath.lerp(c01, c11, fy);

        return NoiseMath.lerp(c0, c1, fz);
    }

    private int index(int gx, int gy, int gz) {
        return (gx * gridSizeY + gy) * gridSizeZ + gz;
    }

    private double getGrid(int gx, int gy, int gz) {
        return values[index(gx, gy, gz)];
    }

    private static int clampGrid(int v, int max) {
        return Math.max(0, Math.min(v, max));
    }
}
