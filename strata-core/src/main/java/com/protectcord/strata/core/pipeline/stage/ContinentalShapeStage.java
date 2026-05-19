package com.protectcord.strata.core.pipeline.stage;

import com.protectcord.strata.api.chunk.ProtoChunkAccess;
import com.protectcord.strata.api.noise.NoiseFunction;
import com.protectcord.strata.api.pipeline.GenerationContext;
import com.protectcord.strata.api.pipeline.GenerationStage;
import com.protectcord.strata.core.pipeline.PipelineStage;

/**
 * Samples continentalness noise at each column (16x16) and classifies
 * as OCEAN (< -0.3), COAST (-0.3 to 0.0), or INLAND (> 0.0).
 * Stores the raw float array and classification array in context metadata.
 */
public final class ContinentalShapeStage implements PipelineStage {

    public static final int OCEAN = 0;
    public static final int COAST = 1;
    public static final int INLAND = 2;

    private static final double OCEAN_THRESHOLD = -0.3;
    private static final double COAST_THRESHOLD = 0.0;

    private final NoiseFunction continentalnessNoise;

    public ContinentalShapeStage(NoiseFunction continentalnessNoise) {
        this.continentalnessNoise = continentalnessNoise;
    }

    @Override
    public GenerationStage stage() {
        return GenerationStage.CONTINENTAL_SHAPE;
    }

    @Override
    public void generate(GenerationContext context) {
        ProtoChunkAccess chunk = context.chunk();
        int baseX = chunk.coord().blockX();
        int baseZ = chunk.coord().blockZ();

        float[] continentalValues = new float[16 * 16];
        int[] classifications = new int[16 * 16];

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = baseX + x;
                int worldZ = baseZ + z;

                double sampleX = worldX * 0.005;
                double sampleZ = worldZ * 0.005;

                float value = (float) continentalnessNoise.sample(sampleX, sampleZ);
                int idx = x + z * 16;
                continentalValues[idx] = value;

                if (value < OCEAN_THRESHOLD) {
                    classifications[idx] = OCEAN;
                } else if (value < COAST_THRESHOLD) {
                    classifications[idx] = COAST;
                } else {
                    classifications[idx] = INLAND;
                }
            }
        }

        context.put("continental_values", continentalValues);
        context.put("continental_classifications", classifications);
    }
}
