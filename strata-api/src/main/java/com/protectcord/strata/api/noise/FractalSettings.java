package com.protectcord.strata.api.noise;

/**
 * Settings for fractal noise generation (octave stacking / fBm).
 *
 * <p>Fractal noise works by summing multiple "octaves" of a base noise function, each
 * at increasing frequency and decreasing amplitude. This produces natural-looking
 * detail at multiple spatial scales, suitable for terrain, erosion, and climate noise.</p>
 *
 * @param octaves     number of noise layers to stack (1-16); more octaves add finer detail
 * @param frequency   base sampling frequency; higher values produce smaller-scale features
 * @param amplitude   base amplitude (output scale of the first octave)
 * @param lacunarity  frequency multiplier per octave (typically {@code 2.0})
 * @param persistence amplitude multiplier per octave (typically {@code 0.5}); controls roughness
 * @since 1.0.0
 * @see NoiseFunction
 * @see NoiseType
 */
public record FractalSettings(
        int octaves,
        double frequency,
        double amplitude,
        double lacunarity,
        double persistence
) {

    /**
     * Returns default fractal settings: 4 octaves, frequency {@code 1.0}, amplitude {@code 1.0},
     * lacunarity {@code 2.0}, persistence {@code 0.5}.
     *
     * @return sensible default fractal settings
     */
    public static FractalSettings defaultSettings() {
        return new FractalSettings(4, 1.0, 1.0, 2.0, 0.5);
    }
}
