package com.protectcord.strata.migrate.converter;

import com.protectcord.strata.migrate.report.MigrationReport;

import java.util.List;
import java.util.Map;

/**
 * Converts Terra palette definitions to Strata surface rules TOML.
 */
public final class PaletteConverter {

    public String convert(Map<String, Object> terraPalette, MigrationReport report) {
        StringBuilder toml = new StringBuilder();
        String id = getString(terraPalette, "id", "unknown");

        toml.append("# Converted from Terra palette: ").append(id).append("\n\n");
        toml.append("[surface-palette.\"strata:").append(id.toLowerCase().replace("-", "_")).append("\"]\n");

        // Terra palettes define layers from top to bottom
        Object layers = terraPalette.get("layers");
        if (layers instanceof List<?> layerList) {
            int depth = 0;
            for (Object layer : layerList) {
                if (layer instanceof Map<?, ?> layerMap) {
                    Object materials = layerMap.get("materials");
                    Object layerDepth = layerMap.get("layers");
                    int d = layerDepth instanceof Number n ? n.intValue() : 1;

                    toml.append("\n[[surface-palette.\"strata:")
                            .append(id.toLowerCase().replace("-", "_")).append("\".layers]]\n");
                    toml.append("depth = ").append(d).append("\n");

                    if (materials instanceof List<?> mats) {
                        for (Object mat : mats) {
                            toml.append("# material: ").append(mat).append("\n");
                        }
                    } else if (materials instanceof Map<?, ?> matMap) {
                        for (var entry : matMap.entrySet()) {
                            toml.append("block = \"").append(entry.getKey()).append("\"")
                                    .append("  # weight: ").append(entry.getValue()).append("\n");
                        }
                    }

                    depth += d;
                }
            }
        }

        report.addConverted("palette." + id);
        return toml.toString();
    }

    private static String getString(Map<?, ?> map, String key, String defaultValue) {
        Object val = map.get(key);
        return val != null ? val.toString() : defaultValue;
    }
}
