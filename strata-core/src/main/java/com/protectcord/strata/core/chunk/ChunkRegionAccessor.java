package com.protectcord.strata.core.chunk;

import com.protectcord.strata.api.chunk.ChunkCoord;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public final class ChunkRegionAccessor {

    private final int centerX;
    private final int centerZ;
    private final Function<ChunkCoord, StrataProtoChunk> loader;
    private final Map<Long, Optional<StrataProtoChunk>> cache = new ConcurrentHashMap<>();

    public ChunkRegionAccessor(int centerChunkX, int centerChunkZ,
                               Function<ChunkCoord, StrataProtoChunk> loader) {
        this.centerX = centerChunkX;
        this.centerZ = centerChunkZ;
        this.loader = loader;
    }

    public Optional<StrataProtoChunk> getChunk(int chunkX, int chunkZ) {
        long key = new ChunkCoord(chunkX, chunkZ).toLong();
        return cache.computeIfAbsent(key, k -> {
            try {
                ChunkCoord coord = new ChunkCoord(chunkX, chunkZ);
                StrataProtoChunk loaded = loader.apply(coord);
                return Optional.ofNullable(loaded);
            } catch (Exception e) {
                return Optional.empty();
            }
        });
    }

    public int centerX() { return centerX; }

    public int centerZ() { return centerZ; }

    public int cachedCount() { return cache.size(); }
}
