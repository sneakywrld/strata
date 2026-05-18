package com.protectcord.strata.core.pipeline.stage;

import com.protectcord.strata.api.biome.Biome;
import com.protectcord.strata.api.biome.ClimateParameters;
import com.protectcord.strata.api.chunk.ProtoChunkAccess;
import com.protectcord.strata.api.noise.NoiseFunction;
import com.protectcord.strata.api.pipeline.GenerationContext;
import com.protectcord.strata.api.pipeline.GenerationStage;
import com.protectcord.strata.core.biome.BiomeLookupTable;
import com.protectcord.strata.core.pipeline.PipelineStage;

/**
 * Assigns biomes to 4x4x4 cells by sampling climate noise and
 * looking up the nearest biome in the KD-tree.
 */
public final class BiomeAssignmentStage implements PipelineStage {

    private final BiomeLookupTable lookupTable;
    private final NoiseFunction temperatureNoise;
    private final NoiseFunction humidityNoise;
    private final NoiseFunction continentalnessNoise;
    private final NoiseFunction erosionNoise;
    private final NoiseFunction weirdnessNoise;

    public BiomeAssignmentStage(BiomeLookupTable lookupTable,
                                 NoiseFunction temperatureNoise,
                                 NoiseFunction humidityNoise,
                                 NoiseFunction continentalnessNoise,
                                 NoiseFunction erosionNoise,
                                 NoiseFunction weirdnessNoise) {
        this.lookupTable = lookupTable;
        this.temperatureNoise = temperatureNoise;
        this.humidityNoise = humidityNoise;
        this.continentalnessNoise = continentalnessNoise;
        this.erosionNoise = erosionNoise;
        this.weirdnessNoise = weirdnessNoise;
    }

    @Override
    public GenerationStage stage() {
        return GenerationStage.BIOME_ASSIGNMENT;
    }

    @Override
    public void generate(GenerationContext context) {
        ProtoChunkAccess chunk = context.chunk();
        int baseX = chunk.coord().blockX();
        int baseZ = chunk.coord().blockZ();

        // Biomes are assigned at 4-block resolution
        for (int bx = 0; bx < 4; bx++) {
            for (int bz = 0; bz < 4; bz++) {
                int worldX = baseX + bx * 4 + 2; // sample center of 4x4 cell
                int worldZ = baseZ + bz * 4 + 2;

                double sampleX = worldX * 0.005; // large-scale climate sampling
                double sampleZ = worldZ * 0.005;

                int biomeColumns = (chunk.maxY() - chunk.minY() + 3) / 4;
                for (int by = 0; by < biomeColumns; by++) {
                    int worldY = chunk.minY() + by * 4 + 2;
                    double sampleY = worldY * 0.01;

                    ClimateParameters climate = new ClimateParameters(
                            temperatureNoise.sample(sampleX, sampleZ),
                            humidityNoise.sample(sampleX, sampleZ),
                            continentalnessNoise.sample(sampleX, sampleZ),
                            erosionNoise.sample(sampleX, sampleZ),
                            weirdnessNoise.sample(sampleX, sampleY, sampleZ)
                    );

                    Biome biome = lookupTable.lookup(climate);
                    chunk.setBiome(baseX + bx * 4, worldY, baseZ + bz * 4, biome);
                }
            }
        }
    }
}
