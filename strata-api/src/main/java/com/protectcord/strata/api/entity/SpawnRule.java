package com.protectcord.strata.api.entity;

import com.protectcord.strata.api.core.Keyed;
import com.protectcord.strata.api.core.NamespacedKey;

/**
 * Defines a mob spawn rule for a biome, controlling which entities spawn and under what conditions.
 *
 * <p>Spawn rules are evaluated during the
 * {@link com.protectcord.strata.api.pipeline.GenerationStage#ENTITY_SPAWNING ENTITY_SPAWNING} stage.
 * Each rule specifies an entity type, spawn category, weight (for weighted random selection),
 * group size range, Y-level bounds, and light level constraints.</p>
 *
 * <p>Spawn rules are referenced by key in {@link com.protectcord.strata.api.biome.Biome#spawnRules()}.
 * For MythicMobs integration, rules can specify a {@linkplain #mythicMobId() MythicMobs ID} to
 * spawn a custom mob instead of a vanilla entity.</p>
 *
 * @since 1.0.0
 * @see SpawnCategory
 * @see com.protectcord.strata.api.biome.Biome#spawnRules()
 */
public interface SpawnRule extends Keyed {

    /**
     * Returns the entity type to spawn (e.g., {@code minecraft:zombie}, {@code minecraft:cow}).
     *
     * @return the entity type key, never {@code null}
     */
    NamespacedKey entityType();

    /**
     * Returns the spawn category this rule belongs to (monster, creature, ambient, etc.).
     *
     * @return the {@link SpawnCategory}, never {@code null}
     */
    SpawnCategory category();

    /**
     * Returns the relative weight of this spawn entry within its {@link SpawnCategory}.
     *
     * <p>Higher weights make this entity more likely to be selected when a spawn attempt
     * occurs for this category. For example, a weight of 100 alongside another entry with
     * weight 50 means this entity is twice as likely to be chosen.</p>
     *
     * @return the spawn weight (positive integer)
     */
    int weight();

    /**
     * Returns the minimum number of entities to spawn in a single group.
     *
     * @return the minimum group size (at least 1)
     */
    int minGroupSize();

    /**
     * Returns the maximum number of entities to spawn in a single group.
     *
     * @return the maximum group size (greater than or equal to {@link #minGroupSize()})
     */
    int maxGroupSize();

    /**
     * Returns the minimum Y level at which this entity can spawn.
     *
     * @return the minimum Y coordinate
     */
    int minY();

    /**
     * Returns the maximum Y level at which this entity can spawn.
     *
     * @return the maximum Y coordinate
     */
    int maxY();

    /**
     * Returns the maximum light level at which this entity can spawn.
     *
     * <p>Hostile mobs typically require low light levels (e.g., 7 or below).
     * A value of 15 means the entity can spawn at any light level.</p>
     *
     * @return the maximum light level (0-15)
     */
    int maxLightLevel();

    /**
     * Returns the optional MythicMobs mob ID to use instead of the vanilla entity type.
     *
     * <p>When non-{@code null}, the Strata MythicMobs integration spawns the specified
     * custom mob instead of the vanilla entity referenced by {@link #entityType()}.</p>
     *
     * @return the MythicMobs mob ID, or {@code null} if this is a vanilla spawn
     */
    String mythicMobId();
}
