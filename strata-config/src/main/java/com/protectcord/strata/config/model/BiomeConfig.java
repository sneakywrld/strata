package com.protectcord.strata.config.model;

import java.util.List;
import java.util.Map;

/**
 * Parsed biome configuration from TOML.
 */
public record BiomeConfig(
        String id,
        String displayName,
        String vanillaMapping,
        String dimension,
        String category,
        double temperature,
        double humidity,
        double continentalness,
        double erosion,
        double weirdness,
        double baseHeight,
        double heightVariation,
        Map<String, Object> effects,
        List<String> features,
        List<String> carvers,
        List<String> structures,
        List<String> spawnRules,
        Map<String, Object> surface
) {}
