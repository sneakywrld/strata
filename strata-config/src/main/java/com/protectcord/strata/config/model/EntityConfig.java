package com.protectcord.strata.config.model;

import java.util.List;
import java.util.Map;

/**
 * Parsed entity spawn configuration from TOML.
 */
public record EntityConfig(
        List<SpawnEntry> entries
) {

    /**
     * A single mob spawn entry.
     *
     * @param mobType      the entity type identifier
     * @param category     spawn category (e.g., MONSTER, CREATURE, AMBIENT, WATER_CREATURE)
     * @param weight       spawn weight relative to other entries in the same category
     * @param minGroupSize minimum entities per spawn group
     * @param maxGroupSize maximum entities per spawn group
     * @param conditions   additional spawn conditions (e.g., light level, block below)
     */
    public record SpawnEntry(
            String mobType,
            String category,
            int weight,
            int minGroupSize,
            int maxGroupSize,
            Map<String, Object> conditions
    ) {}
}
