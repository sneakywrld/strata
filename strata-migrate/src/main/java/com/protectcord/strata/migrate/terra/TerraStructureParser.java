package com.protectcord.strata.migrate.terra;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Parses Terra structure YAML files. Extracts structure type, jigsaw pool references,
 * max pieces, spacing/separation, and biome filter.
 */
public final class TerraStructureParser {

    private final Yaml yaml = new Yaml();

    @SuppressWarnings("unchecked")
    public TerraStructureParseResult parse(Path structureFile) throws IOException {
        List<String> warnings = new ArrayList<>();

        Map<String, Object> raw;
        try (InputStream is = Files.newInputStream(structureFile)) {
            Object loaded = yaml.load(is);
            if (!(loaded instanceof Map)) {
                throw new IOException("Structure file does not contain a valid YAML mapping: " + structureFile);
            }
            raw = (Map<String, Object>) loaded;
        }

        String id = getString(raw, "id", deriveIdFromPath(structureFile));
        String type = getString(raw, "type", "FEATURE");

        // Jigsaw pools
        List<String> jigsawPools = new ArrayList<>();
        Object poolsObj = raw.get("loot") != null ? raw.get("loot") : raw.get("structures");
        if (poolsObj == null) poolsObj = raw.get("pools");
        if (poolsObj instanceof List<?> poolList) {
            for (Object p : poolList) {
                jigsawPools.add(p.toString());
            }
        } else if (poolsObj instanceof Map<?, ?> poolMap) {
            for (var entry : poolMap.entrySet()) {
                jigsawPools.add(entry.getKey().toString());
            }
        }

        // Spacing/separation (Terra uses these for structure placement grid)
        int spacing = getInt(raw, "spacing", 32);
        int separation = getInt(raw, "separation", 8);
        int maxPieces = getInt(raw, "max-pieces", 7);

        // Spawn settings
        Object spawnObj = raw.get("spawn");
        if (spawnObj instanceof Map<?, ?> spawnMap) {
            spacing = getInt(spawnMap, "spacing", spacing);
            separation = getInt(spawnMap, "separation", separation);
        }

        // Biome filter
        Map<String, Object> biomeFilter = new LinkedHashMap<>();
        Object biomeObj = raw.get("biomes");
        if (biomeObj instanceof List<?> biomeList) {
            biomeFilter.put("include", biomeList);
        } else if (biomeObj instanceof Map<?, ?> biomeMap) {
            if (biomeMap.containsKey("whitelist")) {
                biomeFilter.put("include", biomeMap.get("whitelist"));
            }
            if (biomeMap.containsKey("blacklist")) {
                biomeFilter.put("exclude", biomeMap.get("blacklist"));
            }
        }

        // Script-based structures
        if (raw.containsKey("script")) {
            warnings.add("Structure '" + id + "' uses TerraScript; structure conversion is approximate");
        }

        // NMS structures
        if ("NMS".equalsIgnoreCase(type)) {
            warnings.add("Structure '" + id + "' is NMS-backed; may not convert cleanly");
        }

        StructureDefinition definition = new StructureDefinition(
                id, type, jigsawPools, maxPieces, spacing, separation, biomeFilter
        );
        return new TerraStructureParseResult(definition, warnings);
    }

    public record TerraStructureParseResult(
            StructureDefinition definition,
            List<String> warnings
    ) {}

    private String deriveIdFromPath(Path file) {
        String name = file.getFileName().toString();
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }

    private static String getString(Map<?, ?> map, String key, String defaultValue) {
        Object val = map.get(key);
        return val != null ? val.toString() : defaultValue;
    }

    private static int getInt(Map<?, ?> map, String key, int defaultValue) {
        Object val = map.get(key);
        if (val instanceof Number n) return n.intValue();
        return defaultValue;
    }
}
