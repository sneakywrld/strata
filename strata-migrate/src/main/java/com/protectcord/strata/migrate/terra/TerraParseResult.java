package com.protectcord.strata.migrate.terra;

import java.util.List;
import java.util.Map;

/**
 * Generic result from parsing a Terra YAML config file.
 * Carries the extracted data map and any warnings generated during parsing.
 */
public record TerraParseResult(
        Map<String, Object> data,
        List<String> warnings
) {}
