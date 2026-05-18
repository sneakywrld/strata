package com.protectcord.strata.config.model;

import java.util.List;
import java.util.Map;

/**
 * Parsed noise function configuration from TOML.
 */
public record NoiseConfig(
        String id,
        String type,
        long seed,
        int octaves,
        double frequency,
        double amplitude,
        double lacunarity,
        double persistence,
        String domainWarpSource,
        double domainWarpStrength,
        List<Map<String, Object>> operations
) {}
