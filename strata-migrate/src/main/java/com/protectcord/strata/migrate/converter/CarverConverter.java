package com.protectcord.strata.migrate.converter;

import com.protectcord.strata.migrate.report.MigrationReport;
import com.protectcord.strata.migrate.terra.CarverDefinition;

/**
 * Converts Terra carver definitions to Strata carver TOML.
 */
public final class CarverConverter {

    public String convert(CarverDefinition carver, MigrationReport report) {
        StringBuilder toml = new StringBuilder();
        String strataId = carver.id().toLowerCase().replace("-", "_");

        toml.append("# Converted from Terra carver: ").append(carver.id()).append("\n\n");
        toml.append("[carver.\"strata:").append(strataId).append("\"]\n");
        toml.append("type = \"").append(mapCarverType(carver.type())).append("\"\n");
        toml.append("probability = ").append(carver.probability()).append("\n");

        // Height range
        toml.append("\n[carver.\"strata:").append(strataId).append("\".height]\n");
        toml.append("min = ").append(carver.minHeight()).append("\n");
        toml.append("max = ").append(carver.maxHeight()).append("\n");

        // Dimensions
        toml.append("\n[carver.\"strata:").append(strataId).append("\".dimensions]\n");
        if ("RAVINE".equals(carver.type())) {
            toml.append("horizontal-radius = ").append(carver.width()).append("\n");
            toml.append("vertical-radius = ").append(carver.length()).append("\n");
        } else {
            toml.append("width = ").append(carver.width()).append("\n");
            toml.append("height = ").append(carver.length()).append("\n");
        }

        report.addApproximated("carver." + carver.id(),
                "Carver parameters were approximated; test in-game and adjust");
        return toml.toString();
    }

    private String mapCarverType(String terraType) {
        return switch (terraType.toUpperCase()) {
            case "CAVE" -> "cave";
            case "RAVINE", "CANYON" -> "ravine";
            default -> "cave";
        };
    }
}
