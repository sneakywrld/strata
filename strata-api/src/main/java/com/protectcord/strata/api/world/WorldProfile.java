package com.protectcord.strata.api.world;

import com.protectcord.strata.api.core.Keyed;
import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.api.terrain.TerrainSettings;
import com.protectcord.strata.api.water.WaterSystemSettings;

import java.util.List;
import java.util.Optional;

/**
 * A world generation profile containing all configuration for a single world.
 * Profiles reference biomes, noise functions, surface rules, etc. by key.
 *
 * <p>Profiles can inherit from a parent profile using the {@code extends} field,
 * allowing users to create variants without duplicating config.</p>
 */
public interface WorldProfile extends Keyed {

    /**
     * Human-readable display name for this profile.
     */
    String displayName();

    /**
     * Optional description of the profile.
     */
    String description();

    /**
     * The author of this profile.
     */
    String author();

    /**
     * The parent profile this one inherits from, if any.
     */
    Optional<NamespacedKey> parent();

    /**
     * Terrain shaping settings.
     */
    TerrainSettings terrainSettings();

    /**
     * Water system settings (rivers, oceans, waterfalls, lakes, aquifers).
     */
    WaterSystemSettings waterSettings();

    /**
     * Ordered list of biome keys available in this profile.
     */
    List<NamespacedKey> biomes();

    /**
     * The world seed override, or empty to use the server seed.
     */
    Optional<Long> seedOverride();
}
