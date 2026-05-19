package com.protectcord.strata.migrate.terra;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Parses Terra carver YAML files. Extracts carver type, height range,
 * width, length, and probability.
 */
public final class TerraCarverParser {

    private final Yaml yaml = new Yaml();

    @SuppressWarnings("unchecked")
    public TerraCarverParseResult parse(Path carverFile) throws IOException {
        List<String> warnings = new ArrayList<>();

        Map<String, Object> raw;
        try (InputStream is = Files.newInputStream(carverFile)) {
            Object loaded = yaml.load(is);
            if (!(loaded instanceof Map)) {
                throw new IOException("Carver file does not contain a valid YAML mapping: " + carverFile);
            }
            raw = (Map<String, Object>) loaded;
        }

        String id = getString(raw, "id", deriveIdFromPath(carverFile));
        String type = getString(raw, "type", "CAVE");

        // Height range
        int minHeight = 0;
        int maxHeight = 128;
        Object heightObj = raw.get("height");
        if (heightObj instanceof Map<?, ?> heightMap) {
            minHeight = getInt(heightMap, "min", 0);
            maxHeight = getInt(heightMap, "max", 128);
        } else {
            minHeight = getInt(raw, "min-height", 0);
            maxHeight = getInt(raw, "max-height", 128);
        }

        // Dimensions
        double width = getDouble(raw, "width", 1.0);
        double length = getDouble(raw, "length", 1.0);

        // Some Terra carvers use radius/horizontal-radius/vertical-radius
        if (raw.containsKey("radius")) {
            width = getDouble(raw, "radius", width);
            length = width;
        }
        if (raw.containsKey("horizontal-radius")) {
            width = getDouble(raw, "horizontal-radius", width);
        }
        if (raw.containsKey("vertical-radius")) {
            length = getDouble(raw, "vertical-radius", length);
        }

        double probability = getDouble(raw, "probability", 0.1);
        if (raw.containsKey("chance")) {
            probability = getDouble(raw, "chance", probability);
        }

        // Recalculate blocks (specific blocks carved through)
        if (raw.containsKey("replace-blocks")) {
            warnings.add("Carver '" + id + "' uses custom replace-blocks list; verify Strata carver config");
        }

        // Noise-based carvers
        if (raw.containsKey("noise")) {
            warnings.add("Carver '" + id + "' uses custom noise settings; approximate conversion");
        }

        // Aquifer carvers
        if ("RAVINE".equalsIgnoreCase(type) || "CANYON".equalsIgnoreCase(type)) {
            type = "RAVINE";
        } else {
            type = "CAVE";
        }

        CarverDefinition definition = new CarverDefinition(
                id, type, minHeight, maxHeight, width, length, probability
        );
        return new TerraCarverParseResult(definition, warnings);
    }

    public record TerraCarverParseResult(
            CarverDefinition definition,
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
