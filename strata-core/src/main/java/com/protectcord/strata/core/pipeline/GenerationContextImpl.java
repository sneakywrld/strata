package com.protectcord.strata.core.pipeline;

import com.protectcord.strata.api.chunk.ProtoChunkAccess;
import com.protectcord.strata.api.pipeline.GenerationContext;
import com.protectcord.strata.api.pipeline.GenerationStage;
import com.protectcord.strata.api.world.WorldProfile;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of GenerationContext that carries state through pipeline stages.
 */
public final class GenerationContextImpl implements GenerationContext {

    private final ProtoChunkAccess chunk;
    private final WorldProfile profile;
    private final long seed;
    private GenerationStage currentStage;
    private final Map<String, Object> data = new HashMap<>();

    public GenerationContextImpl(ProtoChunkAccess chunk, WorldProfile profile, long seed) {
        this.chunk = chunk;
        this.profile = profile;
        this.seed = seed;
    }

    @Override
    public ProtoChunkAccess chunk() { return chunk; }

    @Override
    public WorldProfile profile() { return profile; }

    @Override
    public long seed() { return seed; }

    @Override
    public GenerationStage currentStage() { return currentStage; }

    @Override
    public int seaLevel() { return profile.terrainSettings().seaLevel(); }

    @Override
    public void put(String key, Object value) { data.put(key, value); }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        Object val = data.get(key);
        if (val == null) return null;
        return type.cast(val);
    }

    @Override
    public boolean has(String key) { return data.containsKey(key); }

    public void setCurrentStage(GenerationStage stage) {
        this.currentStage = stage;
    }
}
