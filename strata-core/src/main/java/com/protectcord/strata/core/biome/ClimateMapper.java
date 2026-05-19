package com.protectcord.strata.core.biome;

import com.protectcord.strata.api.biome.ClimateParameters;
import com.protectcord.strata.api.noise.NoiseFunction;

public final class ClimateMapper {

    private ClimateMapper() {}

    public static ClimateParameters sampleClimate(int x, int z,
                                                   NoiseFunction tempNoise,
                                                   NoiseFunction humidNoise,
                                                   NoiseFunction erosionNoise,
                                                   NoiseFunction weirdNoise) {
        double sampleX = x * 0.005;
        double sampleZ = z * 0.005;

        double rawTemp = tempNoise.sample(sampleX, sampleZ);
        double rawHumid = humidNoise.sample(sampleX, sampleZ);
        double rawErosion = erosionNoise.sample(sampleX, sampleZ);
        double rawWeird = weirdNoise.sample(sampleX, sampleZ);

        double temperature = mapToClimateRange(rawTemp, tempNoise.minValue(), tempNoise.maxValue());
        double humidity = mapToClimateRange(rawHumid, humidNoise.minValue(), humidNoise.maxValue());
        double erosion = mapToClimateRange(rawErosion, erosionNoise.minValue(), erosionNoise.maxValue());
        double weirdness = mapToClimateRange(rawWeird, weirdNoise.minValue(), weirdNoise.maxValue());

        double continentalness = deriveContinentalness(temperature, erosion);

        return new ClimateParameters(temperature, humidity, continentalness, erosion, weirdness);
    }

    private static double mapToClimateRange(double raw, double minVal, double maxVal) {
        double range = maxVal - minVal;
        if (range <= 0.0) return 0.0;
        return ((raw - minVal) / range) * 2.0 - 1.0;
    }

    private static double deriveContinentalness(double temperature, double erosion) {
        return Math.max(-1.0, Math.min(1.0, (temperature * 0.3 + erosion * 0.7)));
    }
}
