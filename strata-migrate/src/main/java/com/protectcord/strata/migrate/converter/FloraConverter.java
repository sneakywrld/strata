package com.protectcord.strata.migrate.converter;

import com.protectcord.strata.migrate.report.MigrationReport;
import com.protectcord.strata.migrate.terra.FloraDefinition;

import java.util.Map;

/**
 * Converts Terra flora definitions to Strata feature TOML.
 */
public final class FloraConverter {

    public String convert(FloraDefinition flora, MigrationReport report) {
        StringBuilder toml = new StringBuilder();
        String strataId = flora.id().toLowerCase().replace("-", "_");

        toml.append("# Converted from Terra flora: ").append(flora.id()).append("\n\n");
        toml.append("[feature.\"strata:").append(strataId).append("\"]\n");
        toml.append("type = \"").append(mapFloraType(flora.type())).append("\"\n");

        // Block types
        if (!flora.blockTypes().isEmpty()) {
            toml.append("\n[feature.\"strata:").append(strataId).append("\".blocks]\n");
            for (int i = 0; i < flora.blockTypes().size(); i++) {
                String block = flora.blockTypes().get(i);
                if (!block.contains(":")) {
                    block = "minecraft:" + block.toLowerCase();
                }
                toml.append("block_").append(i).append(" = \"").append(block).append("\"\n");
            }
        }

        // Placement configuration
        toml.append("\n[feature.\"strata:").append(strataId).append("\".placement]\n");
        toml.append("step = \"").append(mapFeatureStep(flora.type())).append("\"\n");
        toml.append("density = ").append(flora.density()).append("\n");

        // Distribution settings
        Map<String, Object> dist = flora.distribution();
        if (!dist.isEmpty()) {
            toml.append("\n[feature.\"strata:").append(strataId).append("\".placement.distribution]\n");

            if (dist.containsKey("min-height")) {
                toml.append("min-height = ").append(dist.get("min-height")).append("\n");
            }
            if (dist.containsKey("max-height")) {
                toml.append("max-height = ").append(dist.get("max-height")).append("\n");
            }
            if (dist.containsKey("ceiling")) {
                toml.append("ceiling = ").append(dist.get("ceiling")).append("\n");
            }

            Object type = dist.get("type");
            if (type != null) {
                toml.append("type = \"").append(type).append("\"\n");
            }
        }

        report.addConverted("flora." + flora.id());
        return toml.toString();
    }

    private String mapFloraType(String terraType) {
        return switch (terraType.toUpperCase()) {
            case "FLORA" -> "simple_block";
            case "TREE", "LARGE_TREE" -> "tree";
            case "LARGE_PLANT", "TALL_PLANT" -> "tall_plant";
            case "CACTUS" -> "cactus";
            case "SHROOM", "MUSHROOM" -> "mushroom";
            default -> "simple_block";
        };
    }

    private String mapFeatureStep(String terraType) {
        return switch (terraType.toUpperCase()) {
            case "TREE", "LARGE_TREE" -> "vegetal_decoration";
            case "CACTUS" -> "vegetal_decoration";
            case "SHROOM", "MUSHROOM" -> "vegetal_decoration";
            case "LARGE_PLANT", "TALL_PLANT" -> "vegetal_decoration";
            default -> "vegetal_decoration";
        };
    }
}
