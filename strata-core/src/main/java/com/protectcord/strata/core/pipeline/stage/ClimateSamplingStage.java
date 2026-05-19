package com.protectcord.strata.core.pipeline.stage;

import com.protectcord.strata.api.biome.ClimateParameters;
import com.protectcord.strata.api.chunk.ProtoChunkAccess;
import com.protectcord.strata.api.noise.NoiseFunction;
import com.protectcord.strata.api.pipeline.GenerationContext;
import com.protectcord.strata.api.pipeline.GenerationStage;
import com.protectcord.strata.core.pipeline.PipelineStage;

/**
 * Samples temperature, humidity, erosion, and weirdness noise at 4x4 column
 * resolution (4 samples across chunk). Stores ClimateParameters array in context metadata.
 */
public final class ClimateSamplingStage implements PipelineStage {

    private static final int SAMPLE_RESOLUTION = 4;
    private static final int SAMPLES_PER_AXIS = 16 / SAMPLE_RESOLUTION;

    private final NoiseFunction temperatureNoise;
    private final NoiseFunction humidityNoise;
    private final NoiseFunction continentalnessNoise;
    private final NoiseFunction erosionNoise;
    private final NoiseFunction weirdnessNoise;

    public ClimateSamplingStage(NoiseFunction temperatureNoise,
                                NoiseFunction humidityNoise,
                                NoiseFunction continentalnessNoise,
                                NoiseFunction erosionNoise,
                                NoiseFunction weirdnessNoise) {
        this.temperatureNoise = temperatureNoise;
        this.humidityNoise = humidityNoise;
        this.continentalnessNoise = continentalnessNoise;
        this.erosionNoise = erosionNoise;
        this.weirdnessNoise = weirdnessNoise;
    }

    @Override
    public GenerationStage stage() {
        return GenerationStage.CLIMATE_SAMPLING;
    }

    @Override
    public void generate(GenerationContext context) {
        ProtoChunkAccess chunk = context.chunk();
        int baseX = chunk.coord().blockX();
        int baseZ = chunk.coord().blockZ();

        ClimateParameters[] climateGrid = new ClimateParameters[SAMPLES_PER_AXIS * SAMPLES_PER_AXIS];

        for (int sx = 0; sx < SAMPLES_PER_AXIS; sx++) {
            for (int sz = 0; sz < SAMPLES_PER_AXIS; sz++) {
                int worldX = baseX + sx * SAMPLE_RESOLUTION + SAMPLE_RESOLUTION / 2;
                int worldZ = baseZ + sz * SAMPLE_RESOLUTION + SAMPLE_RESOLUTION / 2;

                double sampleX = worldX * 0.005;
                double sampleZ = worldZ * 0.005;

                double temperature = temperatureNoise.sample(sampleX, sampleZ);
                double humidity = humidityNoise.sample(sampleX, sampleZ);
                double continentalness = continentalnessNoise.sample(sampleX, sampleZ);
                double erosion = erosionNoise.sample(sampleX, sampleZ);
                double weirdness = weirdnessNoise.sample(sampleX, sampleZ);

                climateGrid[sx + sz * SAMPLES_PER_AXIS] = new ClimateParameters(
                        temperature, humidity, continentalness, erosion, weirdness
                );
            }
        }

        context.put("climate_grid", climateGrid);
    }
}
