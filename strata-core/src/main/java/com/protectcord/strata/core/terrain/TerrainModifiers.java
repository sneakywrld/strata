package com.protectcord.strata.core.terrain;

/**
 * Utility class with static methods for terrain density modification.
 * Provides mesa plateau clamping, mountain jaggedness, and river valley carving.
 */
public final class TerrainModifiers {

    private TerrainModifiers() {}

    /**
     * Applies mesa plateau clamping: creates a flat top by clamping density
     * above the plateau height to a fixed positive value.
     *
     * @param density       the raw density value
     * @param y             the current Y coordinate
     * @param plateauHeight the Y level at which the plateau starts
     * @return the modified density
     */
    public static double applyMesaPlateau(double density, int y, int plateauHeight) {
        if (y > plateauHeight && density > 0) {
            return Math.max(density, 0.25);
        }
        return density;
    }

    /**
     * Applies mountain jaggedness by adding noise-driven perturbation to peaks.
     * Only affects positive density (solid blocks) to create rough mountain tops.
     *
     * @param density   the raw density value
     * @param noise     a noise value at this position (typically -1 to 1)
     * @param intensity the jaggedness intensity (0.0 = none, 1.0 = full)
     * @return the modified density
     */
    public static double applyMountainJaggedness(double density, double noise, double intensity) {
        if (density > 0 && density < 1.0) {
            return density + noise * intensity * density;
        }
        return density;
    }

    /**
     * Applies a V-shaped river valley depression. Carves density based on
     * distance to the river center line: closer positions get more strongly
     * reduced density, creating a V-shaped cross section.
     *
     * @param density         the raw density value
     * @param distanceToRiver distance from the river center line (0.0 = center)
     * @param riverWidth      the full width of the river valley
     * @return the modified density
     */
    public static double applyRiverValley(double density, double distanceToRiver, double riverWidth) {
        if (distanceToRiver >= riverWidth) return density;

        double halfWidth = riverWidth * 0.5;
        double normalizedDist = distanceToRiver / halfWidth;
        double carveStrength = 1.0 - Math.min(normalizedDist, 1.0);

        carveStrength *= carveStrength;

        return density - carveStrength * 2.0;
    }
}
