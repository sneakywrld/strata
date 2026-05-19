package com.protectcord.strata.api.world;

import com.protectcord.strata.api.core.NamespacedKey;

/**
 * Represents a world managed by Strata's generation pipeline.
 *
 * <p>A {@code StrataWorld} wraps a Bukkit world with Strata-specific metadata: the
 * {@linkplain #profileKey() profile key} that drives generation, the resolved
 * {@linkplain #profile() profile instance}, and the {@linkplain #seed() world seed}.
 * Instances are obtained from the {@link WorldManager}.</p>
 *
 * @since 1.0.0
 * @see WorldManager
 * @see WorldProfile
 */
public interface StrataWorld {

    /**
     * Returns the world's unique name, matching the Bukkit world name.
     *
     * @return the world name, never {@code null}
     */
    String name();

    /**
     * Returns the namespaced key of the {@link WorldProfile} used to generate this world.
     *
     * @return the profile key, never {@code null}
     */
    NamespacedKey profileKey();

    /**
     * Returns the active {@link WorldProfile} instance driving generation for this world.
     *
     * @return the resolved profile, never {@code null}
     */
    WorldProfile profile();

    /**
     * Returns the world seed used for deterministic generation.
     *
     * @return the world seed
     */
    long seed();

    /**
     * Returns whether this world is currently loaded by the Bukkit server.
     *
     * @return {@code true} if the world is loaded and active
     */
    boolean isLoaded();
}
