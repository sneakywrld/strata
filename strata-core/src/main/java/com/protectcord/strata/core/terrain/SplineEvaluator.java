package com.protectcord.strata.core.terrain;

/**
 * Evaluates height splines using cubic interpolation between sorted control points.
 * Control points define the mapping from climate parameters to height factors.
 */
public final class SplineEvaluator {

    private final double[] continentalPoints;
    private final double[] continentalValues;
    private final double[] erosionPoints;
    private final double[] erosionValues;
    private final double[] weirdnessPoints;
    private final double[] weirdnessValues;

    public SplineEvaluator(double[] continentalPoints, double[] continentalValues,
                            double[] erosionPoints, double[] erosionValues,
                            double[] weirdnessPoints, double[] weirdnessValues) {
        this.continentalPoints = continentalPoints;
        this.continentalValues = continentalValues;
        this.erosionPoints = erosionPoints;
        this.erosionValues = erosionValues;
        this.weirdnessPoints = weirdnessPoints;
        this.weirdnessValues = weirdnessValues;
    }

    public double evaluate(double continentalness, double erosion, double weirdness) {
        double cHeight = interpolate(continentalPoints, continentalValues, continentalness);
        double eHeight = interpolate(erosionPoints, erosionValues, erosion);
        double wHeight = interpolate(weirdnessPoints, weirdnessValues, weirdness);
        return cHeight + eHeight + wHeight;
    }

    private static double interpolate(double[] points, double[] values, double input) {
        if (points.length == 0) return 0.0;
        if (points.length == 1) return values[0];

        if (input <= points[0]) return values[0];
        if (input >= points[points.length - 1]) return values[values.length - 1];

        int idx = 0;
        for (int i = 0; i < points.length - 1; i++) {
            if (input >= points[i] && input < points[i + 1]) {
                idx = i;
                break;
            }
        }

        int p0 = Math.max(idx - 1, 0);
        int p1 = idx;
        int p2 = idx + 1;
        int p3 = Math.min(idx + 2, points.length - 1);

        double t = (input - points[p1]) / (points[p2] - points[p1]);

        double v0 = values[p0];
        double v1 = values[p1];
        double v2 = values[p2];
        double v3 = values[p3];

        double a = -0.5 * v0 + 1.5 * v1 - 1.5 * v2 + 0.5 * v3;
        double b = v0 - 2.5 * v1 + 2.0 * v2 - 0.5 * v3;
        double c = -0.5 * v0 + 0.5 * v2;
        double d = v1;

        return a * t * t * t + b * t * t + c * t + d;
    }

    public static SplineEvaluator defaultOverworld() {
        return new SplineEvaluator(
                new double[]{-1.0, -0.4, -0.1, 0.0, 0.3, 0.7, 1.0},
                new double[]{-40.0, -10.0, -2.0, 0.0, 10.0, 40.0, 80.0},
                new double[]{-1.0, -0.5, 0.0, 0.5, 1.0},
                new double[]{20.0, 10.0, 0.0, -5.0, -15.0},
                new double[]{-1.0, -0.5, 0.0, 0.5, 1.0},
                new double[]{-5.0, -2.0, 0.0, 3.0, 8.0}
        );
    }
}
