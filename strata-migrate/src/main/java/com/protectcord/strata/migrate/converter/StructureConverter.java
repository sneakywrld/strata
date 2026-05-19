package com.protectcord.strata.migrate.converter;

import com.protectcord.strata.migrate.report.MigrationReport;
import com.protectcord.strata.migrate.terra.StructureDefinition;

import java.util.List;
import java.util.Map;

/**
 * Converts Terra structure definitions to Strata structure TOML.
 */
public final class StructureConverter {

    public String convert(StructureDefinition structure, MigrationReport report) {
        StringBuilder toml = new StringBuilder();
        String strataId = structure.id().toLowerCase().replace("-", "_");

        toml.append("# Converted from Terra structure: ").append(structure.id()).append("\n\n");
        toml.append("[structure.\"strata:").append(strataId).append("\"]\n");
        toml.append("type = \"").append(mapStructureType(structure.type())).append("\"\n");

        // Placement grid
        toml.append("\n[structure.\"strata:").append(strataId).append("\".placement]\n");
        toml.append("spacing = ").append(structure.spacing()).append("\n");
        toml.append("separation = ").append(structure.separation()).append("\n");

        // Jigsaw pool references
        if (!structure.jigsawPools().isEmpty()) {
            toml.append("\n[structure.\"strata:").append(strataId).append("\".jigsaw]\n");
            toml.append("max-pieces = ").append(structure.maxPieces()).append("\n");
            toml.append("pools = [\n");
            for (String pool : structure.jigsawPools()) {
                toml.append("    \"").append(pool).append("\",\n");
            }
            toml.append("]\n");
        }

        // Biome filter
        Map<String, Object> biomeFilter = structure.biomeFilter();
        if (!biomeFilter.isEmpty()) {
            toml.append("\n[structure.\"strata:").append(strataId).append("\".biome-filter]\n");

            Object include = biomeFilter.get("include");
            if (include instanceof List<?> includeList) {
                toml.append("include = [\n");
                for (Object b : includeList) {
                    toml.append("    \"").append(b).append("\",\n");
                }
                toml.append("]\n");
            }

            Object exclude = biomeFilter.get("exclude");
            if (exclude instanceof List<?> excludeList) {
                toml.append("exclude = [\n");
                for (Object b : excludeList) {
                    toml.append("    \"").append(b).append("\",\n");
                }
                toml.append("]\n");
            }
        }

        report.addApproximated("structure." + structure.id(),
                "Structure conversion is approximate; manual tuning recommended");
        return toml.toString();
    }

    private String mapStructureType(String terraType) {
        return switch (terraType.toUpperCase()) {
            case "FEATURE" -> "feature";
            case "JIGSAW" -> "jigsaw";
            case "NMS" -> "nms";
            default -> "feature";
        };
    }
}
