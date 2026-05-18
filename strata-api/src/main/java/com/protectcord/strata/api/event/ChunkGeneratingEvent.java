package com.protectcord.strata.api.event;

import com.protectcord.strata.api.chunk.ChunkCoord;
import com.protectcord.strata.api.pipeline.GenerationStage;

/**
 * Fired before each pipeline stage begins processing a chunk.
 * Can be cancelled to skip the stage for this chunk.
 */
public class ChunkGeneratingEvent extends CancellableEvent {

    private final ChunkCoord chunk;
    private final GenerationStage stage;
    private final String worldName;

    public ChunkGeneratingEvent(ChunkCoord chunk, GenerationStage stage, String worldName) {
        this.chunk = chunk;
        this.stage = stage;
        this.worldName = worldName;
    }

    public ChunkCoord chunk() { return chunk; }
    public GenerationStage stage() { return stage; }
    public String worldName() { return worldName; }
}
