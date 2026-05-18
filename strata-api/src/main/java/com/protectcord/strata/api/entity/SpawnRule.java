package com.protectcord.strata.api.entity;

import com.protectcord.strata.api.core.Keyed;
import com.protectcord.strata.api.core.NamespacedKey;

/**
 * Defines a mob spawn rule for a biome.
 */
public interface SpawnRule extends Keyed {

    /**
     * The entity type (e.g., minecraft:zombie, minecraft:cow).
     */
    NamespacedKey entityType();

    /**
     * The spawn category.
     */
    SpawnCategory category();

    /**
     * The relative weight of this spawn entry within its category.
     */
    int weight();

    /**
     * Minimum group size.
     */
    int minGroupSize();

    /**
     * Maximum group size.
     */
    int maxGroupSize();

    /**
     * Minimum Y level for spawning.
     */
    int minY();

    /**
     * Maximum Y level for spawning.
     */
    int maxY();

    /**
     * Whether this spawn requires a specific light level range.
     */
    int maxLightLevel();

    /**
     * Optional MythicMobs mob ID to use instead of vanilla.
     * Returns null if this is a vanilla spawn.
     */
    String mythicMobId();
}
