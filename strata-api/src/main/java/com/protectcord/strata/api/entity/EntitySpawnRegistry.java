package com.protectcord.strata.api.entity;

import com.protectcord.strata.api.core.NamespacedKey;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Registry for biome-scoped entity spawn rules.
 *
 * <p>Maps biome keys to lists of {@link SpawnRule}s that define which entities can spawn
 * in each biome and under what conditions.</p>
 *
 * @since 1.0.0
 * @see SpawnRule
 * @see SpawnCategory
 */
public interface EntitySpawnRegistry {

    /**
     * Registers a spawn rule for a specific biome.
     *
     * @param biome the biome key to associate the rule with
     * @param rule  the spawn rule to register
     * @throws NullPointerException if {@code biome} or {@code rule} is {@code null}
     */
    void registerRule(NamespacedKey biome, SpawnRule rule);

    /**
     * Returns all spawn rules registered for the given biome.
     *
     * @param biome the biome key to look up
     * @return an unmodifiable list of spawn rules for the biome, or an empty list if none
     */
    List<SpawnRule> getRulesForBiome(NamespacedKey biome);

    /**
     * Returns an unmodifiable view of all registered spawn rules grouped by biome.
     *
     * @return a map of biome keys to their spawn rule lists, never {@code null}
     */
    Map<NamespacedKey, List<SpawnRule>> getAllRules();
}
