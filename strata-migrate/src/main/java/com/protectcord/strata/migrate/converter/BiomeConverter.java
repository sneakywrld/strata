package com.protectcord.strata.migrate.converter;

import com.protectcord.strata.migrate.report.MigrationReport;

import java.util.*;

/**
 * Converts Terra biome YAML definitions to Strata biome TOML.
 */
public final class BiomeConverter {

    /**
     * Converts a Terra biome config map to Strata TOML content.
     */
    public String convert(Map<String, Object> terraBiome, MigrationReport report) {
        StringBuilder toml = new StringBuilder();
        String id = getString(terraBiome, "id", "unknown");

        toml.append("# Converted from Terra biome: ").append(id).append("\n\n");
        toml.append("[biome]\n");
        toml.append("id = \"strata:").append(id.toLowerCase().replace("-", "_")).append("\"\n");

        // Vanilla mapping
        String vanilla = getString(terraBiome, "vanilla", "minecraft:plains");
        toml.append("vanilla-mapping = \"").append(vanilla).append("\"\n");

        // Climate parameters (Terra uses different parameter names)
        toml.append("\n[biome.climate]\n");
        toml.append("temperature = ").append(getDouble(terraBiome, "temperature", 0.5)).append("\n");
        toml.append("humidity = ").append(getDouble(terraBiome, "humidity", 0.5)).append("\n");

        // Terrain
        Object terrainObj = terraBiome.get("terrain");
        if (terrainObj instanceof Map<?, ?> terrain) {
            toml.append("\n[biome.terrain]\n");
            toml.append("base-height = ").append(getDouble(terrain, "base", 0.1)).append("\n");
            toml.append("height-variation = ").append(getDouble(terrain, "variation", 0.2)).append("\n");
        }

        // Palette references
        Object paletteObj = terraBiome.get("palette");
        if (paletteObj instanceof List<?> palettes) {
            toml.append("\n# Surface palette (converted from Terra palette references)\n");
            toml.append("[biome.surface]\n");
            for (Object p : palettes) {
                if (p instanceof Map<?, ?> paletteEntry) {
                    toml.append("# palette-ref: ").append(paletteEntry).append("\n");
                }
            }
            report.addConverted("biome." + id + ".palette");
        }

        // Flora references
        Object floraObj = terraBiome.get("flora");
        if (floraObj instanceof Map<?, ?> flora) {
            toml.append("\n# Features (converted from Terra flora references)\n");
            toml.append("[biome.features]\n");
            for (var entry : ((Map<?, ?>) flora).entrySet()) {
                toml.append("# flora-ref: ").append(entry.getKey()).append(" = ").append(entry.getValue()).append("\n");
            }
            report.addConverted("biome." + id + ".flora");
        }

        report.addConverted("biome." + id);
        return toml.toString();
    }

    private static String getString(Map<?, ?> map, String key, String defaultValue) {
        Object val = map.get(key);
        return val != null ? val.toString() : defaultValue;
    }

    private static double getDouble(Map<?, ?> map, String key, double defaultValue) {
        Object val = map.get(key);
        if (val instanceof Number n) return n.doubleValue();
        return defaultValue;
    }
}
