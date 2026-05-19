package com.protectcord.strata.api.pipeline;

import com.protectcord.strata.api.chunk.ProtoChunkAccess;
import com.protectcord.strata.api.world.WorldProfile;

/**
 * Context passed to each pipeline stage during chunk generation.
 *
 * <p>Contains everything a stage needs to do its work: the chunk under construction,
 * the world profile, seed, current stage, and sea level. Additionally provides a
 * key-value store for passing data between stages (e.g., the climate sampling stage
 * can store sampled values for the biome assignment stage to read).</p>
 *
 * @since 1.0.0
 * @see GenerationStage
 * @see ProtoChunkAccess
 * @see WorldProfile
 */
public interface GenerationContext {

    /**
     * Returns the chunk currently being generated.
     *
     * @return the {@link ProtoChunkAccess} for block and biome manipulation, never {@code null}
     */
    ProtoChunkAccess chunk();

    /**
     * Returns the world profile driving generation for this world.
     *
     * @return the active {@link WorldProfile}, never {@code null}
     */
    WorldProfile profile();

    /**
     * Returns the world seed for deterministic generation.
     *
     * @return the world seed
     */
    long seed();

    /**
     * Returns the pipeline stage currently being executed.
     *
     * @return the current {@link GenerationStage}, never {@code null}
     */
    GenerationStage currentStage();

    /**
     * Returns the sea level Y coordinate for this world, as defined by the profile's
     * {@link com.protectcord.strata.api.terrain.TerrainSettings}.
     *
     * @return the sea level Y coordinate
     */
    int seaLevel();

    /**
     * Stores a named value in the context for use by later pipeline stages.
     *
     * <p>This is the primary mechanism for inter-stage communication. For example, the
     * climate sampling stage might store climate arrays that the biome assignment stage reads.</p>
     *
     * @param key   the unique key for this value
     * @param value the value to store
     */
    void put(String key, Object value);

    /**
     * Retrieves a typed value previously stored by an earlier pipeline stage.
     *
     * @param key  the key that was passed to {@link #put(String, Object)}
     * @param type the expected type of the stored value
     * @param <T>  the value type
     * @return the stored value, cast to the requested type
     * @throws IllegalArgumentException if the key is not present
     * @throws ClassCastException       if the stored value is not assignable to the requested type
     */
    <T> T get(String key, Class<T> type);

    /**
     * Checks whether a value has been stored under the given key.
     *
     * @param key the key to check
     * @return {@code true} if a value exists for this key
     */
    boolean has(String key);
}
