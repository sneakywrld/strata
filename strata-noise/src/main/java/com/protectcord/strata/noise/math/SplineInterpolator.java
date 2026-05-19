package com.protectcord.strata.noise.math;

import java.util.Arrays;

/**
 * Natural cubic spline interpolation through a set of control points.
 *
 * <p>Given sorted control points (x_i, y_i), computes cubic polynomial
 * coefficients using a tridiagonal matrix solver (Thomas algorithm).
 * Evaluation is O(log n) via binary search on the x values.</p>
 *
 * <p>Instances are immutable after construction.</p>
 */
public final class SplineInterpolator {

    private final double[] xs;
    private final double[] ys;
    private final double[] a;
    private final double[] b;
    private final double[] c;
    private final double[] d;

    public SplineInterpolator(double[] xs, double[] ys) {
        if (xs.length != ys.length) {
            throw new IllegalArgumentException("xs and ys must have the same length");
        }
        if (xs.length < 2) {
            throw new IllegalArgumentException("At least 2 control points required");
        }
        for (int i = 1; i < xs.length; i++) {
            if (xs[i] <= xs[i - 1]) {
                throw new IllegalArgumentException("xs must be strictly increasing");
            }
        }

        this.xs = xs.clone();
        this.ys = ys.clone();

        int n = xs.length - 1;
        this.a = ys.clone();
        this.b = new double[n];
        this.c = new double[n + 1];
        this.d = new double[n];

        computeCoefficients(n);
    }

    private void computeCoefficients(int n) {
        if (n == 1) {
            b[0] = (a[1] - a[0]) / (xs[1] - xs[0]);
            c[0] = 0;
            c[1] = 0;
            d[0] = 0;
            return;
        }

        double[] h = new double[n];
        for (int i = 0; i < n; i++) {
            h[i] = xs[i + 1] - xs[i];
        }

        double[] alpha = new double[n - 1];
        for (int i = 1; i < n; i++) {
            alpha[i - 1] = (3.0 / h[i]) * (a[i + 1] - a[i])
                         - (3.0 / h[i - 1]) * (a[i] - a[i - 1]);
        }

        // Thomas algorithm for tridiagonal system
        double[] l = new double[n + 1];
        double[] mu = new double[n + 1];
        double[] z = new double[n + 1];

        l[0] = 1;
        mu[0] = 0;
        z[0] = 0;

        for (int i = 1; i < n; i++) {
            l[i] = 2.0 * (xs[i + 1] - xs[i - 1]) - h[i - 1] * mu[i - 1];
            mu[i] = h[i] / l[i];
            z[i] = (alpha[i - 1] - h[i - 1] * z[i - 1]) / l[i];
        }

        l[n] = 1;
        z[n] = 0;
        c[n] = 0;

        for (int j = n - 1; j >= 0; j--) {
            c[j] = z[j] - mu[j] * c[j + 1];
            b[j] = (a[j + 1] - a[j]) / h[j] - h[j] * (c[j + 1] + 2.0 * c[j]) / 3.0;
            d[j] = (c[j + 1] - c[j]) / (3.0 * h[j]);
        }
    }

    /**
     * Evaluates the spline at the given x value.
     *
     * <p>For x values outside the control point range, the nearest
     * boundary segment is extrapolated.</p>
     */
    public double evaluate(double x) {
        int n = xs.length - 1;

        int i;
        if (x <= xs[0]) {
            i = 0;
        } else if (x >= xs[n]) {
            i = n - 1;
        } else {
            i = Arrays.binarySearch(xs, x);
            if (i < 0) {
                i = -i - 2;
            }
            if (i >= n) {
                i = n - 1;
            }
        }

        double dx = x - xs[i];
        return a[i] + b[i] * dx + c[i] * dx * dx + d[i] * dx * dx * dx;
    }

    public int controlPointCount() {
        return xs.length;
    }

    public double minX() {
        return xs[0];
    }

    public double maxX() {
        return xs[xs.length - 1];
    }
}
