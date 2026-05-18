package com.protectcord.strata.config.model;

import java.util.Map;

/**
 * Parsed water system configuration from TOML.
 */
public record WaterConfig(
        Map<String, Object> rivers,
        Map<String, Object> oceans,
        Map<String, Object> waterfalls,
        Map<String, Object> lakes,
        Map<String, Object> aquifers
) {}
