package com.protectcord.strata.noise.fractal;

import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.api.noise.FractalSettings;
import com.protectcord.strata.api.noise.NoiseFunction;
import com.protectcord.strata.noise.algorithm.AbstractNoiseGenerator;

/**
 * Fractal noise (octave stacking) — layers multiple samples of a base
 * noise function at increasing frequencies and decreasing amplitudes.
 */
public final class FractalNoise extends AbstractNoiseGenerator {

    private final NoiseFunction base;
    private final FractalSettings settings;

    public FractalNoise(NamespacedKey key, NoiseFunction base, FractalSettings settings) {
        super(key, 0);
        this.base = base;
        this.settings = settings;
    }

    @Override
    public double sample(double x, double z) {
        double sum = 0;
        double freq = settings.frequency();
        double amp = settings.amplitude();
        double maxAmp = 0;

        for (int i = 0; i < settings.octaves(); i++) {
            sum += base.sample(x * freq, z * freq) * amp;
            maxAmp += amp;
            freq *= settings.lacunarity();
            amp *= settings.persistence();
        }

        return sum / maxAmp;
    }

    @Override
    public double sample(double x, double y, double z) {
        double sum = 0;
        double freq = settings.frequency();
        double amp = settings.amplitude();
        double maxAmp = 0;

        for (int i = 0; i < settings.octaves(); i++) {
            sum += base.sample(x * freq, y * freq, z * freq) * amp;
            maxAmp += amp;
            freq *= settings.lacunarity();
            amp *= settings.persistence();
        }

        return sum / maxAmp;
    }
}
