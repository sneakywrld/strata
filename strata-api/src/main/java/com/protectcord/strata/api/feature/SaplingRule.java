package com.protectcord.strata.api.feature;

import com.protectcord.strata.api.core.NamespacedKey;

import java.util.Map;
import java.util.Set;

/**
 * Defines rules for sapling growth behavior within Strata-managed worlds.
 *
 * <p>A sapling rule specifies which biomes allow or deny growth for a given sapling type,
 * a multiplier that adjusts growth rate, and optional tree variant overrides per biome.</p>
 *
 * @param saplingType          the sapling type identifier (e.g., {@code "oak"}, {@code "birch"})
 * @param allowedBiomes        biomes where this sapling can grow; empty means all biomes allowed
 * @param deniedBiomes         biomes where this sapling is explicitly denied growth
 * @param growthRateMultiplier multiplier applied to the base growth rate (1.0 = default speed)
 * @param treeVariantOverride  per-biome tree variant overrides; maps biome key to variant name
 * @since 1.0.0
 * @see SaplingRegistry
 */
public record SaplingRule(
        String saplingType,
        Set<NamespacedKey> allowedBiomes,
        Set<NamespacedKey> deniedBiomes,
        double growthRateMultiplier,
        Map<NamespacedKey, String> treeVariantOverride
) {

    public SaplingRule {
        allowedBiomes = Set.copyOf(allowedBiomes);
        deniedBiomes = Set.copyOf(deniedBiomes);
        treeVariantOverride = Map.copyOf(treeVariantOverride);
    }
}
