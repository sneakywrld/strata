package com.protectcord.strata.migrate.converter;

import com.protectcord.strata.migrate.report.MigrationReport;

import java.util.Map;

/**
 * Converts Terra noise configuration to Strata noise function TOML.
 */
public final class NoiseConverter {

    public String convert(Map<String, Object> terraNoise, MigrationReport report) {
        StringBuilder toml = new StringBuilder();
        String id = getString(terraNoise, "id", "unknown");

        toml.append("# Converted from Terra noise: ").append(id).append("\n\n");
        toml.append("[noise.\"strata:").append(id.toLowerCase().replace("-", "_")).append("\"]\n");

        // Map Terra noise type to Strata
        String terraType = getString(terraNoise, "type", "SIMPLEX");
        String strataType = mapNoiseType(terraType);
        toml.append("type = \"").append(strataType).append("\"\n");

        // Frequency/octaves
        toml.append("frequency = ").append(getDouble(terraNoise, "frequency", 1.0)).append("\n");

        Object dimensions = terraNoise.get("dimensions");
        if (dimensions instanceof Number n) {
            toml.append("# Original Terra dimensions: ").append(n).append("\n");
        }

        // Fractal settings
        int octaves = getInt(terraNoise, "octaves", 1);
        if (octaves > 1) {
            toml.append("\n[noise.\"strata:").append(id.toLowerCase().replace("-", "_")).append("\".fractal]\n");
            toml.append("octaves = ").append(octaves).append("\n");
            toml.append("lacunarity = ").append(getDouble(terraNoise, "lacunarity", 2.0)).append("\n");
            toml.append("gain = ").append(getDouble(terraNoise, "gain", 0.5)).append("\n");
        }

        report.addConverted("noise." + id);
        return toml.toString();
    }

    private String mapNoiseType(String terraType) {
        return switch (terraType.toUpperCase()) {
            case "SIMPLEX", "FBM" -> "simplex";
            case "PERLIN" -> "perlin";
            case "OPENSIMPLEX2", "OPENSIMPLEX2S" -> "open_simplex_2";
            case "CELLULAR", "VORONOI" -> "cellular";
            case "VALUE" -> "value";
            case "RIDGED", "RIDGED_MULTI" -> "ridged_multi";
            case "WHITE" -> "white";
            default -> "simplex";
        };
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
