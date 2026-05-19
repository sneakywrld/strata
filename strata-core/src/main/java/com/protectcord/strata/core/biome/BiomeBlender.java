package com.protectcord.strata.core.biome;

import com.protectcord.strata.api.biome.Biome;
import com.protectcord.strata.api.biome.ClimateParameters;

import java.util.HashMap;
import java.util.Map;

public final class BiomeBlender {

    private BiomeBlender() {}

    public static Map<String, Double> blend(BiomeLookupTable table, double[] climateParams,
                                            int x, int z, int blendRadius) {
        Map<String, Double> weights = new HashMap<>();
        double totalWeight = 0.0;

        for (int dx = -blendRadius; dx <= blendRadius; dx++) {
            for (int dz = -blendRadius; dz <= blendRadius; dz++) {
                double distSq = dx * dx + dz * dz;
                if (distSq > (double) blendRadius * blendRadius) continue;

                double dist = Math.sqrt(distSq);
                double weight = 1.0 - (dist / (blendRadius + 1));
                if (weight <= 0.0) continue;

                double sampleX = (x + dx * 4) * 0.005;
                double sampleZ = (z + dz * 4) * 0.005;

                double offsetScale = (dx * dx + dz * dz) * 0.001;
                double temperature = clamp(climateParams[0] + perturbation(sampleX, sampleZ, 0) * offsetScale);
                double humidity = clamp(climateParams[1] + perturbation(sampleX, sampleZ, 1) * offsetScale);
                double continentalness = clamp(climateParams[2] + perturbation(sampleX, sampleZ, 2) * offsetScale);
                double erosion = clamp(climateParams[3] + perturbation(sampleX, sampleZ, 3) * offsetScale);
                double weirdness = clamp(climateParams[4] + perturbation(sampleX, sampleZ, 4) * offsetScale);

                ClimateParameters sampleClimate = new ClimateParameters(
                        temperature, humidity, continentalness, erosion, weirdness
                );

                Biome biome = table.lookup(sampleClimate);
                String biomeKey = biome.key().toString();
                weights.merge(biomeKey, weight, Double::sum);
                totalWeight += weight;
            }
        }

        if (totalWeight > 0.0) {
            double normFactor = 1.0 / totalWeight;
            weights.replaceAll((k, v) -> v * normFactor);
        }

        return weights;
    }

    private static double perturbation(double x, double z, int channel) {
        long n = (long) (x * 3129871.0) ^ (long) (z * 116129781.0) ^ (long) channel;
        n = n * n * 42317861L + n * 11L;
        return ((double) (n >> 16 & 0xFFL)) / 127.5 - 1.0;
    }

    private static double clamp(double value) {
        return Math.max(-1.0, Math.min(1.0, value));
    }
}
