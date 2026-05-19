package com.protectcord.strata.migrate.terra;

/**
 * Parsed representation of a Terra carver definition.
 */
public record CarverDefinition(
        String id,
        String type,
        int minHeight,
        int maxHeight,
        double width,
        double length,
        double probability
) {}
