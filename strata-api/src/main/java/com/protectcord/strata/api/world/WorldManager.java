package com.protectcord.strata.api.world;

import com.protectcord.strata.api.core.NamespacedKey;

import java.util.Collection;
import java.util.Optional;

/**
 * Manages Strata-controlled worlds: creation, loading, querying, and profile assignment.
 *
 * <p>The world manager is the primary interface for creating new worlds with Strata generation,
 * looking up existing Strata-managed worlds, and accessing loaded
 * {@link WorldProfile WorldProfiles}. Access it via
 * {@link com.protectcord.strata.api.core.StrataAPI#worldManager()}.</p>
 *
 * <p>Example:</p>
 * <pre>{@code
 * WorldManager manager = api.worldManager();
 * StrataWorld world = manager.createWorld("my_world", NamespacedKey.strata("overworld_default"));
 * }</pre>
 *
 * @since 1.0.0
 * @see StrataWorld
 * @see WorldProfile
 */
public interface WorldManager {

    /**
     * Creates a new world with the given name and profile, using the server's default seed.
     *
     * <p>The world is registered as a Strata-managed world and uses the generation pipeline
     * defined by the specified profile.</p>
     *
     * @param worldName  the Bukkit world name (must be unique among all loaded worlds)
     * @param profileKey the key of the {@link WorldProfile} to use for generation
     * @return the created {@link StrataWorld}
     * @throws IllegalArgumentException if a world with the given name already exists
     *                                  or the profile key is not registered
     */
    StrataWorld createWorld(String worldName, NamespacedKey profileKey);

    /**
     * Creates a new world with the given name, profile, and explicit seed.
     *
     * @param worldName  the Bukkit world name (must be unique among all loaded worlds)
     * @param profileKey the key of the {@link WorldProfile} to use for generation
     * @param seed       the world seed for deterministic generation
     * @return the created {@link StrataWorld}
     * @throws IllegalArgumentException if a world with the given name already exists
     *                                  or the profile key is not registered
     */
    StrataWorld createWorld(String worldName, NamespacedKey profileKey, long seed);

    /**
     * Gets a Strata-managed world by its Bukkit world name.
     *
     * @param worldName the world name to look up
     * @return an {@link Optional} containing the {@link StrataWorld} if found, or empty
     */
    Optional<StrataWorld> getWorld(String worldName);

    /**
     * Returns all currently registered Strata-managed worlds.
     *
     * @return an unmodifiable collection of {@link StrataWorld} instances, never {@code null}
     */
    Collection<StrataWorld> worlds();

    /**
     * Checks if a world with the given name is managed by Strata.
     *
     * @param worldName the world name to check
     * @return {@code true} if the world is managed by Strata's generation pipeline
     */
    boolean isStrataWorld(String worldName);

    /**
     * Gets a loaded {@link WorldProfile} by its key.
     *
     * @param profileKey the profile's namespaced key
     * @return an {@link Optional} containing the profile if loaded, or empty
     */
    Optional<WorldProfile> getProfile(NamespacedKey profileKey);

    /**
     * Returns the keys of all currently loaded world profiles.
     *
     * @return an unmodifiable collection of profile keys, never {@code null}
     */
    Collection<NamespacedKey> profileKeys();
}
