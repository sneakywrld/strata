package com.protectcord.strata.api.world;

import com.protectcord.strata.api.core.NamespacedKey;

import java.util.Collection;
import java.util.Optional;

/**
 * Manages Strata-controlled worlds: creation, loading, and profile assignment.
 */
public interface WorldManager {

    /**
     * Creates a new world with the given name and profile.
     *
     * @param worldName  the Bukkit world name
     * @param profileKey the profile to use for generation
     * @return the created StrataWorld
     */
    StrataWorld createWorld(String worldName, NamespacedKey profileKey);

    /**
     * Creates a new world with a specific seed.
     */
    StrataWorld createWorld(String worldName, NamespacedKey profileKey, long seed);

    /**
     * Gets a Strata-managed world by name.
     */
    Optional<StrataWorld> getWorld(String worldName);

    /**
     * Returns all Strata-managed worlds.
     */
    Collection<StrataWorld> worlds();

    /**
     * Checks if a world is managed by Strata.
     */
    boolean isStrataWorld(String worldName);

    /**
     * Gets a loaded profile by key.
     */
    Optional<WorldProfile> getProfile(NamespacedKey profileKey);

    /**
     * Returns all loaded profile keys.
     */
    Collection<NamespacedKey> profileKeys();
}
