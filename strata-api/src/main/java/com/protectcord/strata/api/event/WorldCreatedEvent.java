package com.protectcord.strata.api.event;

import com.protectcord.strata.api.core.NamespacedKey;

/**
 * Fired when a new Strata-managed world is created via the {@link com.protectcord.strata.api.world.WorldManager}.
 *
 * <p>This is a read-only notification event. Plugins can use it to perform world-specific
 * initialization, register additional content, or log world creation.</p>
 *
 * @since 1.0.0
 * @see com.protectcord.strata.api.world.WorldManager#createWorld(String, NamespacedKey)
 * @see EventBus
 */
public class WorldCreatedEvent implements StrataEvent {

    private final String worldName;
    private final NamespacedKey profileKey;
    private final long seed;

    /**
     * Constructs a new world-created event.
     *
     * @param worldName  the name of the newly created world
     * @param profileKey the key of the profile used for generation
     * @param seed       the world seed
     */
    public WorldCreatedEvent(String worldName, NamespacedKey profileKey, long seed) {
        this.worldName = worldName;
        this.profileKey = profileKey;
        this.seed = seed;
    }

    /**
     * Returns the name of the newly created world.
     *
     * @return the world name, never {@code null}
     */
    public String worldName() { return worldName; }

    /**
     * Returns the key of the profile used to generate this world.
     *
     * @return the profile key, never {@code null}
     */
    public NamespacedKey profileKey() { return profileKey; }

    /**
     * Returns the world seed.
     *
     * @return the seed value
     */
    public long seed() { return seed; }
}
