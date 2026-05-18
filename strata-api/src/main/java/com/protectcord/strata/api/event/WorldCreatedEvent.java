package com.protectcord.strata.api.event;

import com.protectcord.strata.api.core.NamespacedKey;

/**
 * Fired when a new Strata world is created.
 */
public class WorldCreatedEvent implements StrataEvent {

    private final String worldName;
    private final NamespacedKey profileKey;
    private final long seed;

    public WorldCreatedEvent(String worldName, NamespacedKey profileKey, long seed) {
        this.worldName = worldName;
        this.profileKey = profileKey;
        this.seed = seed;
    }

    public String worldName() { return worldName; }
    public NamespacedKey profileKey() { return profileKey; }
    public long seed() { return seed; }
}
