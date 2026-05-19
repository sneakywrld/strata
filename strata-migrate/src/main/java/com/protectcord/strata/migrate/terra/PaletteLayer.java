package com.protectcord.strata.migrate.terra;

/**
 * A single layer within a Terra palette definition.
 */
public record PaletteLayer(
        String block,
        int minDepth,
        int maxDepth,
        double probability
) {}
