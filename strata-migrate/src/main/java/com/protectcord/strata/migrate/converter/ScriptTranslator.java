package com.protectcord.strata.migrate.converter;

import com.protectcord.strata.migrate.report.MigrationReport;
import com.protectcord.strata.migrate.terra.ScriptAnalysis;

import java.util.Map;

/**
 * Best-effort TerraScript to Strata conversion. Extracts block placements
 * from script analysis results and generates schematic-like TOML definitions.
 * Does NOT attempt full script execution.
 */
public final class ScriptTranslator {

    public String translate(ScriptAnalysis analysis, MigrationReport report) {
        StringBuilder toml = new StringBuilder();
        String strataId = analysis.scriptId().toLowerCase().replace("-", "_");

        toml.append("# Converted from TerraScript: ").append(analysis.scriptId()).append("\n");
        toml.append("# This is a best-effort structural extraction; manual review required.\n\n");

        toml.append("[schematic.\"strata:").append(strataId).append("\"]\n");
        toml.append("type = \"block_list\"\n");

        // Block placements
        if (!analysis.blockPlacements().isEmpty()) {
            toml.append("\n# Block placements extracted from script\n");
            int index = 0;
            for (Map<String, Object> placement : analysis.blockPlacements()) {
                toml.append("\n[[schematic.\"strata:").append(strataId).append("\".blocks]]\n");

                String x = placement.getOrDefault("x", "0").toString();
                String y = placement.getOrDefault("y", "0").toString();
                String z = placement.getOrDefault("z", "0").toString();
                String block = placement.getOrDefault("block", "minecraft:stone").toString();

                if (!block.contains(":")) {
                    block = "minecraft:" + block.toLowerCase();
                }

                // Only output numeric offsets; dynamic expressions get flagged
                if (isNumeric(x) && isNumeric(y) && isNumeric(z)) {
                    toml.append("offset = [").append(x).append(", ").append(y).append(", ").append(z).append("]\n");
                    toml.append("block = \"").append(block).append("\"\n");
                } else {
                    toml.append("# Dynamic offset: [").append(x).append(", ").append(y).append(", ").append(z).append("]\n");
                    toml.append("# block = \"").append(block).append("\"\n");
                    report.addApproximated("script." + analysis.scriptId() + ".block[" + index + "]",
                            "Dynamic coordinate expression cannot be statically resolved");
                }
                index++;
            }
        }

        // Structure references
        if (!analysis.structureReferences().isEmpty()) {
            toml.append("\n# Structure references found in script\n");
            toml.append("[schematic.\"strata:").append(strataId).append("\".structure-refs]\n");
            toml.append("refs = [\n");
            for (String ref : analysis.structureReferences()) {
                toml.append("    \"").append(ref).append("\",\n");
            }
            toml.append("]\n");
        }

        // Flag unsupported constructs
        for (String construct : analysis.unsupportedConstructs()) {
            report.addUnsupported("script." + analysis.scriptId() + "." + extractConstructName(construct),
                    construct);
        }

        if (analysis.blockPlacements().isEmpty() && analysis.structureReferences().isEmpty()) {
            report.addWarning("Script '" + analysis.scriptId() +
                    "' produced no extractable block placements or structure references");
        } else {
            report.addApproximated("script." + analysis.scriptId(),
                    "TerraScript structural extraction is best-effort; " +
                            analysis.blockPlacements().size() + " block placements, " +
                            analysis.structureReferences().size() + " structure refs extracted");
        }

        return toml.toString();
    }

    private boolean isNumeric(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String extractConstructName(String construct) {
        int paren = construct.indexOf('(');
        if (paren > 0) {
            return construct.substring(0, paren).replace(" ", "-");
        }
        int space = construct.indexOf(' ');
        if (space > 0) {
            return construct.substring(0, space).replace(" ", "-");
        }
        return construct;
    }
}
