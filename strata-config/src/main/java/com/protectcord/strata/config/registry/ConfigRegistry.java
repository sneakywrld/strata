package com.protectcord.strata.config.registry;

import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.config.model.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central registry of all loaded configuration definitions.
 * Stores parsed config models keyed by NamespacedKey for each subsystem.
 */
public final class ConfigRegistry {

    private final Map<NamespacedKey, ProfileConfig> profiles = new ConcurrentHashMap<>();
    private final Map<NamespacedKey, BiomeConfig> biomes = new ConcurrentHashMap<>();
    private final Map<NamespacedKey, NoiseConfig> noises = new ConcurrentHashMap<>();
    private final Map<NamespacedKey, TerrainConfig> terrains = new ConcurrentHashMap<>();
    private final Map<NamespacedKey, WaterConfig> waters = new ConcurrentHashMap<>();

    public void registerProfile(NamespacedKey key, ProfileConfig config) {
        profiles.put(key, config);
    }

    public void registerBiome(NamespacedKey key, BiomeConfig config) {
        biomes.put(key, config);
    }

    public void registerNoise(NamespacedKey key, NoiseConfig config) {
        noises.put(key, config);
    }

    public void registerTerrain(NamespacedKey key, TerrainConfig config) {
        terrains.put(key, config);
    }

    public void registerWater(NamespacedKey key, WaterConfig config) {
        waters.put(key, config);
    }

    public Optional<ProfileConfig> getProfile(NamespacedKey key) {
        return Optional.ofNullable(profiles.get(key));
    }

    public Optional<BiomeConfig> getBiome(NamespacedKey key) {
        return Optional.ofNullable(biomes.get(key));
    }

    public Optional<NoiseConfig> getNoise(NamespacedKey key) {
        return Optional.ofNullable(noises.get(key));
    }

    public Optional<TerrainConfig> getTerrain(NamespacedKey key) {
        return Optional.ofNullable(terrains.get(key));
    }

    public Optional<WaterConfig> getWater(NamespacedKey key) {
        return Optional.ofNullable(waters.get(key));
    }

    public Collection<NamespacedKey> profileKeys() {
        return Collections.unmodifiableSet(profiles.keySet());
    }

    public Collection<NamespacedKey> biomeKeys() {
        return Collections.unmodifiableSet(biomes.keySet());
    }

    /**
     * Clears all registrations. Used during hot-reload.
     */
    public void clear() {
        profiles.clear();
        biomes.clear();
        noises.clear();
        terrains.clear();
        waters.clear();
    }
}
