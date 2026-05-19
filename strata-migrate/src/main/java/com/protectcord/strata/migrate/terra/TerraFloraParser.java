package com.protectcord.strata.migrate.terra;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Parses Terra flora/tree YAML files. Extracts flora type, block types,
 * placement density, and distribution settings.
 */
public final class TerraFloraParser {

    private final Yaml yaml = new Yaml();

    @SuppressWarnings("unchecked")
    public TerraFloraParseResult parse(Path floraFile) throws IOException {
        List<String> warnings = new ArrayList<>();

        Map<String, Object> raw;
        try (InputStream is = Files.newInputStream(floraFile)) {
            Object loaded = yaml.load(is);
            if (!(loaded instanceof Map)) {
                throw new IOException("Flora file does not contain a valid YAML mapping: " + floraFile);
            }
            raw = (Map<String, Object>) loaded;
        }

        String id = getString(raw, "id", deriveIdFromPath(floraFile));

        // Type: FLORA, TREE, LARGE_PLANT, etc.
        String type = getString(raw, "type", "FLORA");

        // Block types used by this flora
        List<String> blockTypes = new ArrayList<>();
        Object blocksObj = raw.get("blocks");
        if (blocksObj instanceof List<?> blockList) {
            for (Object b : blockList) {
                if (b instanceof Map<?, ?> blockMap) {
                    for (var entry : blockMap.entrySet()) {
                        blockTypes.add(entry.getKey().toString());
                    }
                } else {
                    blockTypes.add(b.toString());
                }
            }
        } else if (blocksObj instanceof Map<?, ?> blockMap) {
            for (var entry : blockMap.entrySet()) {
                blockTypes.add(entry.getKey().toString());
            }
        }

        // Spawnable blocks (what the flora can be placed on)
        if (raw.containsKey("spawnable")) {
            Object spawnObj = raw.get("spawnable");
            if (spawnObj instanceof List<?> spawnList) {
                List<String> spawnable = new ArrayList<>();
                for (Object s : spawnList) spawnable.add(s.toString());
                // Stored for converter reference, not a direct FloraDefinition field
            }
        }

        // Density / placement chance
        double density = getDouble(raw, "density", 0.0);
        if (density == 0.0) {
            density = getDouble(raw, "chance", 0.1);
        }

        // Distribution settings
        Map<String, Object> distribution = new LinkedHashMap<>();
        Object distObj = raw.get("distribution");
        if (distObj instanceof Map<?, ?> distMap) {
            for (var entry : distMap.entrySet()) {
                distribution.put(entry.getKey().toString(), entry.getValue());
            }
        }

        // Height range
        if (raw.containsKey("height")) {
            Object heightObj = raw.get("height");
            if (heightObj instanceof Map<?, ?> heightMap) {
                distribution.put("min-height", getInt(heightMap, "min", 0));
                distribution.put("max-height", getInt(heightMap, "max", 255));
            }
        }

        // Ceiling flora
        if (raw.containsKey("ceiling") && Boolean.TRUE.equals(raw.get("ceiling"))) {
            distribution.put("ceiling", true);
            warnings.add("Flora '" + id + "' is ceiling-attached; verify Strata support");
        }

        // TerraScript-based flora
        if (raw.containsKey("script")) {
            warnings.add("Flora '" + id + "' uses TerraScript; script translation is best-effort");
        }

        FloraDefinition definition = new FloraDefinition(id, type, blockTypes, density, distribution);
        return new TerraFloraParseResult(definition, warnings);
    }

    public record TerraFloraParseResult(
            FloraDefinition definition,
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

    private static double getDouble(Map<?, ?> map, String key, double defaultValue) {
        Object val = map.get(key);
        if (val instanceof Number n) return n.doubleValue();
        return defaultValue;
    }

    private static int getInt(Map<?, ?> map, String key, int defaultValue) {
        Object val = map.get(key);
        if (val instanceof Number n) return n.intValue();
        return defaultValue;
    }
}
