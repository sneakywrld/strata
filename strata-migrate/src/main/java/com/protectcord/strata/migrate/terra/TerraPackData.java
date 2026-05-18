package com.protectcord.strata.migrate.terra;

import java.util.List;
import java.util.Map;

/**
 * Parsed data from a Terra configuration pack.
 */
public record TerraPackData(
        String packId,
        String author,
        List<Map<String, Object>> biomes,
        List<Map<String, Object>> noises,
        List<Map<String, Object>> palettes,
        List<Map<String, Object>> flora,
        List<Map<String, Object>> structures,
        List<Map<String, Object>> carvers
) {}
