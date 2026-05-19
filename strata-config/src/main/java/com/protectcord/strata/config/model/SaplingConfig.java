package com.protectcord.strata.config.model;

import java.util.List;
import java.util.Map;

/**
 * Parsed sapling growth configuration from TOML.
 *
 * @param saplingType          the sapling block type
 * @param allowedBiomes        biomes where this sapling configuration applies (empty means all)
 * @param deniedBiomes         biomes where this sapling cannot grow
 * @param growthRateMultiplier multiplier applied to sapling growth speed
 * @param treeVariants         mapping of biome ID to tree variant name
 */
public record SaplingConfig(
        String saplingType,
        List<String> allowedBiomes,
        List<String> deniedBiomes,
        double growthRateMultiplier,
        Map<String, String> treeVariants
) {}
