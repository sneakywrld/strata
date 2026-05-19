package com.protectcord.strata.migrate.terra;

import java.util.List;
import java.util.Map;

/**
 * Result of analyzing a TerraScript file.
 * Contains identified block placements, structure references,
 * and a list of unsupported constructs encountered.
 */
public record ScriptAnalysis(
        String scriptId,
        List<Map<String, Object>> blockPlacements,
        List<String> structureReferences,
        List<String> unsupportedConstructs
) {}
