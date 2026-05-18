package com.protectcord.strata.api.world;

import com.protectcord.strata.api.core.NamespacedKey;

/**
 * Represents a world managed by Strata.
 */
public interface StrataWorld {

    /**
     * The world's unique name (same as the Bukkit world name).
     */
    String name();

    /**
     * The profile key used to generate this world.
     */
    NamespacedKey profileKey();

    /**
     * The active profile.
     */
    WorldProfile profile();

    /**
     * The world seed.
     */
    long seed();

    /**
     * Whether this world is currently loaded.
     */
    boolean isLoaded();
}
