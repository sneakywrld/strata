package com.protectcord.strata.api.event;

import com.protectcord.strata.api.chunk.ChunkCoord;

/**
 * Fired after a chunk has completed all generation stages.
 */
public class ChunkGeneratedEvent implements StrataEvent {

    private final ChunkCoord chunk;
    private final String worldName;
    private final double generationTimeMs;

    public ChunkGeneratedEvent(ChunkCoord chunk, String worldName, double generationTimeMs) {
        this.chunk = chunk;
        this.worldName = worldName;
        this.generationTimeMs = generationTimeMs;
    }

    public ChunkCoord chunk() { return chunk; }
    public String worldName() { return worldName; }
    public double generationTimeMs() { return generationTimeMs; }
}
