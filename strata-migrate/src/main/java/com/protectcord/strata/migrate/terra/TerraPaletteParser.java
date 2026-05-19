package com.protectcord.strata.migrate.terra;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Parses Terra palette YAML files. Extracts block layers with material,
 * depth range, and probability. Returns an ordered list of PaletteLayer records.
 */
public final class TerraPaletteParser {

    private final Yaml yaml = new Yaml();

    @SuppressWarnings("unchecked")
    public TerraPaletteParseResult parse(Path paletteFile) throws IOException {
        List<String> warnings = new ArrayList<>();
        List<PaletteLayer> layers = new ArrayList<>();

        Map<String, Object> raw;
        try (InputStream is = Files.newInputStream(paletteFile)) {
            Object loaded = yaml.load(is);
            if (!(loaded instanceof Map)) {
                throw new IOException("Palette file does not contain a valid YAML mapping: " + paletteFile);
            }
            raw = (Map<String, Object>) loaded;
        }

        String id = getString(raw, "id", deriveIdFromPath(paletteFile));

        Object layersObj = raw.get("layers");
        if (layersObj instanceof List<?> layerList) {
            int currentDepth = 0;
            for (Object layerObj : layerList) {
                if (layerObj instanceof Map<?, ?> layerMap) {
                    int depth = getInt(layerMap, "layers", 1);
                    Object materials = layerMap.get("materials");

                    if (materials instanceof List<?> matList) {
                        for (Object mat : matList) {
                            if (mat instanceof Map<?, ?> matMap) {
                                for (var entry : matMap.entrySet()) {
                                    String block = entry.getKey().toString();
                                    double probability = toDouble(entry.getValue(), 1.0);
                                    layers.add(new PaletteLayer(block, currentDepth, currentDepth + depth, probability));
                                }
                            } else if (mat instanceof String s) {
                                layers.add(new PaletteLayer(s, currentDepth, currentDepth + depth, 1.0));
                            }
                        }
                    } else if (materials instanceof Map<?, ?> matMap) {
                        for (var entry : matMap.entrySet()) {
                            String block = entry.getKey().toString();
                            double probability = toDouble(entry.getValue(), 1.0);
                            layers.add(new PaletteLayer(block, currentDepth, currentDepth + depth, probability));
                        }
                    } else if (materials instanceof String s) {
                        layers.add(new PaletteLayer(s, currentDepth, currentDepth + depth, 1.0));
                    }

                    currentDepth += depth;
                } else {
                    warnings.add("Unexpected layer format in palette " + id);
                }
            }
        } else {
            warnings.add("No 'layers' key found in palette " + id);
        }

        // Handle simplex/noise-based palette variants
        if (raw.containsKey("simplex")) {
            warnings.add("Palette '" + id + "' uses simplex noise blending; approximate conversion only");
        }

        return new TerraPaletteParseResult(id, layers, warnings);
    }

    public record TerraPaletteParseResult(
            String id,
            List<PaletteLayer> layers,
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

    private static double toDouble(Object val, double defaultValue) {
        if (val instanceof Number n) return n.doubleValue();
        try {
            return Double.parseDouble(val.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
