package com.protectcord.strata.core.entity;

import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.api.entity.SpawnCategory;
import com.protectcord.strata.api.entity.SpawnRule;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class SpawnTableBuilder {

    private SpawnTableBuilder() {}

    @SuppressWarnings("unchecked")
    public static Map<SpawnCategory, List<SpawnRule>> buildTable(String biomeKey,
                                                                 Map<String, Object> spawnConfig) {
        Map<SpawnCategory, List<SpawnRule>> table = new EnumMap<>(SpawnCategory.class);
        for (SpawnCategory category : SpawnCategory.values()) {
            table.put(category, new ArrayList<>());
        }

        for (Map.Entry<String, Object> entry : spawnConfig.entrySet()) {
            String entityId = entry.getKey();
            if (!(entry.getValue() instanceof Map<?, ?> rawProps)) continue;
            Map<String, Object> props = (Map<String, Object>) rawProps;

            SpawnCategory category = parseCategory(
                    (String) props.getOrDefault("category", "CREATURE"));
            int weight = ((Number) props.getOrDefault("weight", 10)).intValue();
            int minGroup = ((Number) props.getOrDefault("min_group", 1)).intValue();
            int maxGroup = ((Number) props.getOrDefault("max_group", 4)).intValue();
            int minY = ((Number) props.getOrDefault("min_y", -64)).intValue();
            int maxY = ((Number) props.getOrDefault("max_y", 320)).intValue();
            int maxLight = ((Number) props.getOrDefault("max_light", 15)).intValue();
            String mythicMobId = (String) props.getOrDefault("mythic_mob_id", null);

            NamespacedKey ruleKey = NamespacedKey.strata(
                    biomeKey.replace(":", "_") + "_" + entityId.replace(":", "_"));
            NamespacedKey entityType = NamespacedKey.parse(entityId);

            SpawnRule rule = new SpawnRuleImpl(
                    ruleKey, entityType, category, weight,
                    minGroup, maxGroup, minY, maxY, maxLight, mythicMobId
            );
            table.get(category).add(rule);
        }

        return table;
    }

    private static SpawnCategory parseCategory(String name) {
        try {
            return SpawnCategory.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return SpawnCategory.CREATURE;
        }
    }

    private record SpawnRuleImpl(
            NamespacedKey key,
            NamespacedKey entityType,
            SpawnCategory category,
            int weight,
            int minGroupSize,
            int maxGroupSize,
            int minY,
            int maxY,
            int maxLightLevel,
            String mythicMobId
    ) implements SpawnRule {}
}
