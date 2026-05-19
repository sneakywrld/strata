package com.protectcord.strata.api.entity;

import com.protectcord.strata.api.core.NamespacedKey;

import java.util.Set;

/**
 * Defines a geographic spawn zone with custom mob density and difficulty settings.
 *
 * <p>Spawn zones allow fine-grained control over entity spawning within a circular region
 * of the world, filtered by biome. The {@link #difficultyMultiplier()} and
 * {@link #mobDensityModifier()} allow per-zone tuning of combat difficulty and mob population.</p>
 *
 * @param name                 a human-readable name for this zone
 * @param centerX              the center X block coordinate
 * @param centerZ              the center Z block coordinate
 * @param radius               the zone radius in blocks
 * @param biomeFilter          biomes this zone applies to; empty means all biomes within radius
 * @param difficultyMultiplier multiplier applied to mob difficulty (1.0 = default)
 * @param mobDensityModifier   multiplier applied to mob density cap (1.0 = default)
 * @since 1.0.0
 * @see EntitySpawnRegistry
 * @see SpawnRule
 */
public record SpawnZone(
        String name,
        int centerX,
        int centerZ,
        int radius,
        Set<NamespacedKey> biomeFilter,
        double difficultyMultiplier,
        double mobDensityModifier
) {

    public SpawnZone {
        biomeFilter = Set.copyOf(biomeFilter);
    }

    public boolean containsBlock(int blockX, int blockZ) {
        long dx = (long) blockX - centerX;
        long dz = (long) blockZ - centerZ;
        return dx * dx + dz * dz <= (long) radius * radius;
    }
}
