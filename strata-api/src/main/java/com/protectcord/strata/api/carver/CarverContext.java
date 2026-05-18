package com.protectcord.strata.api.carver;

import com.protectcord.strata.api.chunk.BlockAccess;
import com.protectcord.strata.api.chunk.ChunkCoord;

/**
 * Context provided to {@link Carver} implementations during the
 * {@link com.protectcord.strata.api.pipeline.GenerationStage#CARVING CARVING} pipeline stage.
 *
 * <p>Contains everything a carver needs to operate: block access for reading and writing,
 * the chunk being carved, the world seed, sea level, and Y-range bounds.</p>
 *
 * @param blocks   read-write access to the chunk's block data
 * @param chunk    the coordinate of the chunk being carved
 * @param seed     the world seed
 * @param seaLevel the sea level Y coordinate (carvers may avoid carving below water)
 * @param minY     the minimum Y coordinate of the world
 * @param maxY     the maximum Y coordinate of the world
 * @since 1.0.0
 * @see Carver#carve(CarverContext, CarvingMask)
 */
public record CarverContext(
        BlockAccess blocks,
        ChunkCoord chunk,
        long seed,
        int seaLevel,
        int minY,
        int maxY
) {

    /**
     * Returns a seed unique to this chunk, suitable for deterministic carver randomization.
     *
     * <p>Computed by mixing the world seed with the chunk coordinates using large primes,
     * ensuring different chunks produce different carving patterns.</p>
     *
     * @return a chunk-specific seed value
     */
    public long chunkSeed() {
        return seed ^ ((long) chunk.x() * 341873128712L + (long) chunk.z() * 132897987541L);
    }
}
