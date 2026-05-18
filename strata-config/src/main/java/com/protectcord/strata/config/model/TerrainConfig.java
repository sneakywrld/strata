package com.protectcord.strata.config.model;

/**
 * Parsed terrain shaping configuration from TOML.
 */
public record TerrainConfig(
        int seaLevel,
        int minY,
        int maxY,
        String densityFunction,
        String continentalSpline,
        String erosionSpline,
        double baseHeightOffset,
        double heightScale
) {}
