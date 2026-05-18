package com.protectcord.strata.api.pipeline;

import com.protectcord.strata.api.chunk.ProtoChunkAccess;
import com.protectcord.strata.api.world.WorldProfile;

/**
 * Context passed to each pipeline stage during chunk generation.
 * Contains everything a stage needs to do its work.
 */
public interface GenerationContext {

    /**
     * The chunk being generated.
     */
    ProtoChunkAccess chunk();

    /**
     * The world profile driving generation.
     */
    WorldProfile profile();

    /**
     * The world seed.
     */
    long seed();

    /**
     * The current pipeline stage.
     */
    GenerationStage currentStage();

    /**
     * Returns the sea level for this world.
     */
    int seaLevel();

    /**
     * Stores a named value in the context for use by later stages.
     */
    void put(String key, Object value);

    /**
     * Retrieves a value stored by an earlier stage.
     */
    <T> T get(String key, Class<T> type);

    /**
     * Checks if a key exists in the context.
     */
    boolean has(String key);
}
