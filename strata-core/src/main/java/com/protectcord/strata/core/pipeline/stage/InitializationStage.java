package com.protectcord.strata.core.pipeline.stage;

import com.protectcord.strata.api.chunk.ProtoChunkAccess;
import com.protectcord.strata.api.pipeline.GenerationContext;
import com.protectcord.strata.api.pipeline.GenerationStage;
import com.protectcord.strata.core.pipeline.PipelineStage;

/**
 * First pipeline stage. Allocates chunk sections, seeds per-chunk RNG
 * from world seed + chunk coordinates using MurmurHash-style mixing,
 * and sets up context noise caches.
 */
public final class InitializationStage implements PipelineStage {

    @Override
    public GenerationStage stage() {
        return GenerationStage.INITIALIZATION;
    }

    @Override
    public void generate(GenerationContext context) {
        ProtoChunkAccess chunk = context.chunk();
        int chunkX = chunk.coord().x();
        int chunkZ = chunk.coord().z();

        long chunkSeed = mixSeed(context.seed(), chunkX, chunkZ);
        context.put("chunk_seed", chunkSeed);

        int sectionCount = (chunk.maxY() - chunk.minY() + 15) / 16;
        context.put("section_count", sectionCount);

        int columns = 16 * 16;
        float[] noiseCache2D = new float[columns];
        context.put("noise_cache_2d", noiseCache2D);

        int totalBlocks = columns * (chunk.maxY() - chunk.minY());
        float[] noiseCache3D = new float[totalBlocks];
        context.put("noise_cache_3d", noiseCache3D);
    }

    private static long mixSeed(long seed, int chunkX, int chunkZ) {
        long h = seed;
        h ^= (long) chunkX * 0x6C62272E07BB0142L;
        h ^= (long) chunkZ * 0x94D049BB133111EBL;
        h ^= h >>> 33;
        h *= 0xFF51AFD7ED558CCDL;
        h ^= h >>> 33;
        h *= 0xC4CEB9FE1A85EC53L;
        h ^= h >>> 33;
        return h;
    }
}
