package com.protectcord.strata.migrate.terra;

import java.util.List;
import java.util.Map;

/**
 * Parsed representation of a Terra flora/tree definition.
 */
public record FloraDefinition(
        String id,
        String type,
        List<String> blockTypes,
        double density,
        Map<String, Object> distribution
) {}
