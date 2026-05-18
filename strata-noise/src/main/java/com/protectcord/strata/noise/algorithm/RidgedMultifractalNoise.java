package com.protectcord.strata.noise.algorithm;

import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.api.noise.FractalSettings;

/**
 * Ridged multifractal noise — produces sharp ridges resembling mountain ranges.
 * Built on top of simplex noise with absolute-value and inversion applied
 * before octave accumulation.
 */
public final class RidgedMultifractalNoise extends AbstractNoiseGenerator {

    private final SimplexNoise base;
    private final FractalSettings settings;
    private final double[] weights;

    public RidgedMultifractalNoise(NamespacedKey key, long seed, FractalSettings settings) {
        super(key, seed);
        this.base = new SimplexNoise(key, seed);
        this.settings = settings;

        // Pre-compute spectral weights
        this.weights = new double[settings.octaves()];
        double freq = 1.0;
        for (int i = 0; i < settings.octaves(); i++) {
            weights[i] = Math.pow(freq, -1.0);
            freq *= settings.lacunarity();
        }
    }

    @Override
    public double sample(double x, double z) {
        double sum = 0;
        double freq = settings.frequency();
        double weight = 1.0;

        for (int i = 0; i < settings.octaves(); i++) {
            double signal = base.sample(x * freq, z * freq);
            signal = 1.0 - Math.abs(signal);
            signal *= signal;
            signal *= weight;

            weight = signal * 2.0;
            if (weight > 1.0) weight = 1.0;
            if (weight < 0.0) weight = 0.0;

            sum += signal * weights[i];
            freq *= settings.lacunarity();
        }

        return (sum * 1.25) - 1.0;
    }

    @Override
    public double sample(double x, double y, double z) {
        double sum = 0;
        double freq = settings.frequency();
        double weight = 1.0;

        for (int i = 0; i < settings.octaves(); i++) {
            double signal = base.sample(x * freq, y * freq, z * freq);
            signal = 1.0 - Math.abs(signal);
            signal *= signal;
            signal *= weight;

            weight = signal * 2.0;
            if (weight > 1.0) weight = 1.0;
            if (weight < 0.0) weight = 0.0;

            sum += signal * weights[i];
            freq *= settings.lacunarity();
        }

        return (sum * 1.25) - 1.0;
    }
}
