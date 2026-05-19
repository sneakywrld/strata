package com.protectcord.strata.migrate.terra;

import java.util.List;
import java.util.Map;

/**
 * Parsed representation of a Terra structure definition.
 */
public record StructureDefinition(
        String id,
        String type,
        List<String> jigsawPools,
        int maxPieces,
        int spacing,
        int separation,
        Map<String, Object> biomeFilter
) {}
