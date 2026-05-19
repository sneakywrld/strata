package com.protectcord.strata.config.model;

import java.util.List;
import java.util.Map;

/**
 * Parsed spawn zone configuration from TOML.
 * Defines radial zones around world origin with distinct spawn behavior.
 *
 * @param name                 human-readable zone name
 * @param minRadius            inner radius boundary in blocks
 * @param maxRadius            outer radius boundary in blocks
 * @param biomes               biomes this zone applies to (empty means all)
 * @param difficultyMultiplier scaling factor for mob difficulty in this zone
 * @param mobDensityModifier   scaling factor for mob cap in this zone
 * @param vanillaSpawnTable    per-category spawn entries for vanilla mobs
 * @param mythicMobsTable      MythicMobs spawner table reference, null if not used
 */
public record SpawnZoneConfig(
        String name,
        int minRadius,
        int maxRadius,
        List<String> biomes,
        double difficultyMultiplier,
        double mobDensityModifier,
        Map<String, List<EntityConfig.SpawnEntry>> vanillaSpawnTable,
        String mythicMobsTable
) {}
