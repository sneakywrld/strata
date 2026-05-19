package com.protectcord.strata.api.event;

import com.protectcord.strata.api.chunk.ChunkCoord;

/**
 * Fired after a chunk has completed all generation pipeline stages.
 *
 * <p>This is a read-only notification event; it cannot be cancelled. Use it for logging,
 * performance monitoring, or triggering post-generation actions.</p>
 *
 * @since 1.0.0
 * @see ChunkGeneratingEvent
 * @see EventBus
 */
public class ChunkGeneratedEvent implements StrataEvent {

    private final ChunkCoord chunk;
    private final String worldName;
    private final double generationTimeMs;

    /**
     * Constructs a new chunk-generated event.
     *
     * @param chunk            the coordinate of the generated chunk
     * @param worldName        the name of the world the chunk belongs to
     * @param generationTimeMs the total time spent generating the chunk, in milliseconds
     */
    public ChunkGeneratedEvent(ChunkCoord chunk, String worldName, double generationTimeMs) {
        this.chunk = chunk;
        this.worldName = worldName;
        this.generationTimeMs = generationTimeMs;
    }

    /**
     * Returns the coordinate of the generated chunk.
     *
     * @return the chunk coordinate, never {@code null}
     */
    public ChunkCoord chunk() { return chunk; }

    /**
     * Returns the name of the world this chunk belongs to.
     *
     * @return the world name, never {@code null}
     */
    public String worldName() { return worldName; }

    /**
     * Returns the total time spent generating this chunk across all pipeline stages, in milliseconds.
     *
     * @return the generation time in milliseconds
     */
    public double generationTimeMs() { return generationTimeMs; }
}
