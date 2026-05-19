package com.protectcord.strata.api.noise;

/**
 * Parameterization for a noise function instance.
 *
 * <p>Encapsulates the fundamental settings required to configure a procedural noise function:
 * base frequency, octave count, lacunarity (frequency multiplier per octave), persistence
 * (amplitude multiplier per octave), and the seed for deterministic generation.</p>
 *
 * @param frequency   base sampling frequency; higher values produce smaller-scale features
 * @param octaves     number of noise layers to stack (1-16)
 * @param lacunarity  frequency multiplier per octave (typically {@code 2.0})
 * @param persistence amplitude multiplier per octave (typically {@code 0.5})
 * @param seed        seed for deterministic pseudo-random generation
 * @since 1.0.0
 * @see NoiseFunction
 * @see FractalSettings
 */
public record NoiseParameters(
        double frequency,
        int octaves,
        double lacunarity,
        double persistence,
        long seed
) {

    public static NoiseParameters defaultParameters(long seed) {
        return new NoiseParameters(1.0, 4, 2.0, 0.5, seed);
    }
}
