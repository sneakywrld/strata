package com.protectcord.strata.core.world;

import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.api.terrain.TerrainSettings;
import com.protectcord.strata.api.water.WaterSystemSettings;
import com.protectcord.strata.api.world.WorldProfile;
import com.protectcord.strata.config.model.ProfileConfig;

import java.util.List;
import java.util.Optional;

/**
 * Default implementation of {@link WorldProfile} backed by a {@link ProfileConfig}.
 */
public final class SimpleWorldProfile implements WorldProfile {

    private final ProfileConfig config;
    private final TerrainSettings terrainSettings;
    private final WaterSystemSettings waterSettings;
    private final List<NamespacedKey> biomes;
    private final Long seedOverride;

    public SimpleWorldProfile(ProfileConfig config) {
        this(config, TerrainSettings.defaultOverworld(), WaterSystemSettings.defaults(),
                List.of(), null);
    }

    public SimpleWorldProfile(ProfileConfig config, TerrainSettings terrainSettings,
                              WaterSystemSettings waterSettings, List<NamespacedKey> biomes,
                              Long seedOverride) {
        this.config = config;
        this.terrainSettings = terrainSettings;
        this.waterSettings = waterSettings;
        this.biomes = List.copyOf(biomes);
        this.seedOverride = seedOverride;
    }

    @Override
    public NamespacedKey key() {
        return config.key();
    }

    @Override
    public String displayName() {
        return config.displayName();
    }

    @Override
    public String description() {
        return config.description() != null ? config.description() : "";
    }

    @Override
    public String author() {
        return config.author() != null ? config.author() : "";
    }

    @Override
    public Optional<NamespacedKey> parent() {
        String ext = config.extendsFrom();
        if (ext == null || ext.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(NamespacedKey.parse(ext));
    }

    @Override
    public TerrainSettings terrainSettings() {
        return terrainSettings;
    }

    @Override
    public WaterSystemSettings waterSettings() {
        return waterSettings;
    }

    @Override
    public List<NamespacedKey> biomes() {
        return biomes;
    }

    @Override
    public Optional<Long> seedOverride() {
        return Optional.ofNullable(seedOverride);
    }

    public ProfileConfig config() {
        return config;
    }
}
