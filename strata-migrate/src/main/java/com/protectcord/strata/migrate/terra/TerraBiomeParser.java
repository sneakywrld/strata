package com.protectcord.strata.migrate.terra;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Parses Terra biome YAML files. Extracts biome ID, climate parameters,
 * palette references, flora references, and terrain settings.
 */
public final class TerraBiomeParser {

    private final Yaml yaml = new Yaml();

    @SuppressWarnings("unchecked")
    public TerraParseResult parse(Path biomeFile) throws IOException {
        List<String> warnings = new ArrayList<>();
        Map<String, Object> result = new LinkedHashMap<>();

        Map<String, Object> raw;
        try (InputStream is = Files.newInputStream(biomeFile)) {
            Object loaded = yaml.load(is);
            if (!(loaded instanceof Map)) {
                throw new IOException("Biome file does not contain a valid YAML mapping: " + biomeFile);
            }
            raw = (Map<String, Object>) loaded;
        }

        result.put("id", getString(raw, "id", deriveIdFromPath(biomeFile)));

        // Climate parameters
        Map<String, Object> climate = new LinkedHashMap<>();
        if (raw.containsKey("climate")) {
            Object climateObj = raw.get("climate");
            if (climateObj instanceof Map<?, ?> climateMap) {
                climate.put("temperature", getDouble(climateMap, "temperature", 0.5));
                climate.put("humidity", getDouble(climateMap, "humidity", 0.5));
            } else {
                warnings.add("Unexpected climate format in " + biomeFile.getFileName());
            }
        } else {
            climate.put("temperature", getDouble(raw, "temperature", 0.5));
            climate.put("humidity", getDouble(raw, "humidity", 0.5));
        }
        result.put("climate", climate);

        // Vanilla mapping
        if (raw.containsKey("vanilla")) {
            result.put("vanilla", getString(raw, "vanilla", "minecraft:plains"));
        }

        // Terrain settings
        if (raw.containsKey("terrain")) {
            Object terrainObj = raw.get("terrain");
            if (terrainObj instanceof Map<?, ?> terrainMap) {
                Map<String, Object> terrain = new LinkedHashMap<>();
                terrain.put("base", getDouble(terrainMap, "base", 0.1));
                terrain.put("variation", getDouble(terrainMap, "variation", 0.2));
                if (terrainMap.containsKey("noise-equation")) {
                    terrain.put("noise-equation", terrainMap.get("noise-equation").toString());
                    warnings.add("Biome '" + result.get("id") + "' uses noise-equation; manual review recommended");
                }
                result.put("terrain", terrain);
            }
        }

        // Palette references
        if (raw.containsKey("palette")) {
            Object paletteObj = raw.get("palette");
            List<Map<String, Object>> paletteRefs = new ArrayList<>();
            if (paletteObj instanceof List<?> paletteList) {
                for (Object entry : paletteList) {
                    if (entry instanceof Map<?, ?> entryMap) {
                        Map<String, Object> ref = new LinkedHashMap<>();
                        for (var kv : entryMap.entrySet()) {
                            ref.put(kv.getKey().toString(), kv.getValue());
                        }
                        paletteRefs.add(ref);
                    } else if (entry instanceof String s) {
                        paletteRefs.add(Map.of("ref", s));
                    }
                }
            } else if (paletteObj instanceof Map<?, ?> paletteMap) {
                for (var entry : paletteMap.entrySet()) {
                    paletteRefs.add(Map.of("range", entry.getKey().toString(), "ref", entry.getValue().toString()));
                }
            }
            result.put("palette", paletteRefs);
        }

        // Flora references
        if (raw.containsKey("flora")) {
            Object floraObj = raw.get("flora");
            if (floraObj instanceof Map<?, ?> floraMap) {
                Map<String, Object> floraRefs = new LinkedHashMap<>();
                for (var entry : floraMap.entrySet()) {
                    floraRefs.put(entry.getKey().toString(), entry.getValue());
                }
                result.put("flora", floraRefs);
            } else if (floraObj instanceof List<?> floraList) {
                result.put("flora", floraList);
            }
        }

        // Trees
        if (raw.containsKey("trees")) {
            result.put("trees", raw.get("trees"));
            warnings.add("Biome '" + result.get("id") + "' has tree definitions; tree conversion is approximate");
        }

        // Carvers
        if (raw.containsKey("carving")) {
            result.put("carving", raw.get("carving"));
        }

        // Structures
        if (raw.containsKey("structures")) {
            result.put("structures", raw.get("structures"));
        }

        // Slant settings
        if (raw.containsKey("slant")) {
            result.put("slant", raw.get("slant"));
            warnings.add("Biome '" + result.get("id") + "' uses slant configuration; approximate conversion only");
        }

        return new TerraParseResult(result, warnings);
    }

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
}
