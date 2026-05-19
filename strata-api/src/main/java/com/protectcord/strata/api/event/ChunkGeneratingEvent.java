package com.protectcord.strata.api.event;

import com.protectcord.strata.api.chunk.ChunkCoord;
import com.protectcord.strata.api.pipeline.GenerationStage;

/**
 * Fired before each pipeline stage begins processing a chunk.
 *
 * <p>This event is {@linkplain CancellableEvent cancellable}. If cancelled, the associated
 * pipeline stage is skipped for this chunk. This allows plugins to selectively disable
 * stages at runtime (e.g., skip feature decoration in certain worlds).</p>
 *
 * @since 1.0.0
 * @see ChunkGeneratedEvent
 * @see GenerationStage
 * @see EventBus
 */
public class ChunkGeneratingEvent extends CancellableEvent {

    private final ChunkCoord chunk;
    private final GenerationStage stage;
    private final String worldName;

    /**
     * Constructs a new chunk-generating event.
     *
     * @param chunk     the coordinate of the chunk being generated
     * @param stage     the pipeline stage about to execute
     * @param worldName the name of the world the chunk belongs to
     */
    public ChunkGeneratingEvent(ChunkCoord chunk, GenerationStage stage, String worldName) {
        this.chunk = chunk;
        this.stage = stage;
        this.worldName = worldName;
    }

    /**
     * Returns the coordinate of the chunk being generated.
     *
     * @return the chunk coordinate, never {@code null}
     */
    public ChunkCoord chunk() { return chunk; }

    /**
     * Returns the pipeline stage that is about to execute.
     *
     * @return the {@link GenerationStage}, never {@code null}
     */
    public GenerationStage stage() { return stage; }

    /**
     * Returns the name of the world this chunk belongs to.
     *
     * @return the world name, never {@code null}
     */
    public String worldName() { return worldName; }
}
