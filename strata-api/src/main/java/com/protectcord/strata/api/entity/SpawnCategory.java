package com.protectcord.strata.api.entity;

/**
 * Mob spawn categories matching Minecraft's internal entity classification system.
 *
 * <p>Each category has its own mob cap (maximum number of concurrent spawns) and
 * spawning frequency. The category determines how the server's spawn cycle processes
 * the entity.</p>
 *
 * @since 1.0.0
 * @see SpawnRule#category()
 */
public enum SpawnCategory {
    /** Hostile mobs (zombies, skeletons, creepers, etc.). Spawns in darkness. */
    MONSTER,
    /** Passive land animals (cows, pigs, sheep, etc.). Spawns on grass in light. */
    CREATURE,
    /** Ambient mobs (bats). Low mob cap, minimal impact. */
    AMBIENT,
    /** Large water creatures (dolphins, squid). Spawns in ocean biomes. */
    WATER_CREATURE,
    /** Small water ambient mobs (tropical fish, pufferfish). */
    WATER_AMBIENT,
    /** Underground water creatures (glow squid). Spawns in dark water below Y=30. */
    UNDERGROUND_WATER_CREATURE,
    /** Axolotls. Spawns in lush cave water. */
    AXOLOTLS,
    /** Miscellaneous entities that do not fit other categories. */
    MISC
}
