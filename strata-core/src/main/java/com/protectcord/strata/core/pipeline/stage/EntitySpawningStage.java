package com.protectcord.strata.core.pipeline.stage;

import com.protectcord.strata.api.biome.Biome;
import com.protectcord.strata.api.chunk.ProtoChunkAccess;
import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.api.entity.EntitySpawnRegistry;
import com.protectcord.strata.api.entity.SpawnRule;
import com.protectcord.strata.api.pipeline.GenerationContext;
import com.protectcord.strata.api.pipeline.GenerationStage;
import com.protectcord.strata.core.pipeline.PipelineStage;

import java.util.*;

/**
 * Reads spawn tables from biome config. For each 4x4 biome section in chunk,
 * adds spawn entries to chunk metadata map stored in context.
 */
public final class EntitySpawningStage implements PipelineStage {

    private final EntitySpawnRegistry spawnRegistry;

    public EntitySpawningStage(EntitySpawnRegistry spawnRegistry) {
        this.spawnRegistry = spawnRegistry;
    }

    @Override
    public GenerationStage stage() {
        return GenerationStage.ENTITY_SPAWNING;
    }

    @Override
    public void generate(GenerationContext context) {
        ProtoChunkAccess chunk = context.chunk();
        int baseX = chunk.coord().blockX();
        int baseZ = chunk.coord().blockZ();

        Map<NamespacedKey, List<SpawnRule>> chunkSpawnEntries = new LinkedHashMap<>();

        for (int bx = 0; bx < 4; bx++) {
            for (int bz = 0; bz < 4; bz++) {
                int worldX = baseX + bx * 4 + 2;
                int worldZ = baseZ + bz * 4 + 2;

                Biome biome = chunk.getBiome(worldX, context.seaLevel(), worldZ);
                if (biome == null) continue;

                List<NamespacedKey> spawnRuleKeys = biome.spawnRules();
                if (spawnRuleKeys.isEmpty()) continue;

                List<SpawnRule> biomeRules = spawnRegistry.getRulesForBiome(biome.key());
                if (biomeRules.isEmpty()) continue;

                chunkSpawnEntries.computeIfAbsent(biome.key(), k -> new ArrayList<>());
                List<SpawnRule> existing = chunkSpawnEntries.get(biome.key());
                for (SpawnRule rule : biomeRules) {
                    if (!existing.contains(rule)) {
                        existing.add(rule);
                    }
                }
            }
        }

        context.put("spawn_entries", chunkSpawnEntries);
    }
}
