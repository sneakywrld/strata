package com.protectcord.strata.api.world;

import com.protectcord.strata.api.core.Keyed;
import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.api.terrain.TerrainSettings;
import com.protectcord.strata.api.water.WaterSystemSettings;

import java.util.List;
import java.util.Optional;

/**
 * A world generation profile containing all configuration for a single Strata-managed world.
 *
 * <p>Profiles are the top-level configuration unit in Strata. Each profile aggregates
 * references to biomes, terrain settings, water systems, and an optional seed override.
 * Profiles are loaded from TOML configuration files and registered in the
 * {@link WorldManager}.</p>
 *
 * <p>Profiles support single inheritance via the {@link #parent()} field, allowing users
 * to create variants (e.g., a "hardcore" overworld) without duplicating the entire
 * configuration. Child profiles inherit all settings from the parent and selectively
 * override specific values.</p>
 *
 * @since 1.0.0
 * @see WorldManager
 * @see TerrainSettings
 * @see WaterSystemSettings
 */
public interface WorldProfile extends Keyed {

    /**
     * Returns the human-readable display name for this profile, shown in commands and UIs.
     *
     * @return the display name, never {@code null}
     */
    String displayName();

    /**
     * Returns an optional description of the profile explaining its purpose or theme.
     *
     * @return the description, or an empty string if not set; never {@code null}
     */
    String description();

    /**
     * Returns the author of this profile.
     *
     * @return the author name, never {@code null}
     */
    String author();

    /**
     * Returns the key of the parent profile this one inherits from, if any.
     *
     * <p>When present, any settings not explicitly defined in this profile are inherited
     * from the parent. Inheritance is resolved at load time.</p>
     *
     * @return an {@link Optional} containing the parent profile key, or empty if this is a root profile
     */
    Optional<NamespacedKey> parent();

    /**
     * Returns the terrain shaping settings for this profile (sea level, Y range,
     * density functions, splines, and height transformations).
     *
     * @return the {@link TerrainSettings}, never {@code null}
     */
    TerrainSettings terrainSettings();

    /**
     * Returns the water system settings for this profile, including configuration for
     * rivers, oceans, waterfalls, lakes, and aquifers.
     *
     * @return the {@link WaterSystemSettings}, never {@code null}
     */
    WaterSystemSettings waterSettings();

    /**
     * Returns the ordered list of biome keys available in this profile.
     *
     * <p>Only biomes listed here participate in biome assignment for worlds using this profile.
     * Each key must reference a registered {@link com.protectcord.strata.api.biome.Biome}.</p>
     *
     * @return an unmodifiable list of biome keys, never {@code null}
     */
    List<NamespacedKey> biomes();

    /**
     * Returns the world seed override for this profile, or empty to use the server seed.
     *
     * <p>When present, worlds created with this profile use the specified seed regardless
     * of the server's default seed.</p>
     *
     * @return an {@link Optional} containing the seed override, or empty to use the server seed
     */
    Optional<Long> seedOverride();
}
