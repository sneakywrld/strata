package com.protectcord.strata.config.model;

import java.util.List;
import java.util.Map;

/**
 * Parsed surface rule configuration from TOML.
 * Rules are defined as [[rules]] array-of-tables entries.
 */
public record SurfaceConfig(
        List<RuleEntry> rules
) {

    /**
     * A single surface rule entry.
     *
     * @param condition the condition type and its parameters
     * @param block     the block to place when the condition matches
     * @param priority  rule evaluation priority (lower values evaluated first)
     */
    public record RuleEntry(
            Condition condition,
            String block,
            int priority
    ) {}

    /**
     * A condition controlling when a surface rule applies.
     *
     * @param type   the condition type (e.g., BIOME, DEPTH, NOISE_THRESHOLD, STEEP)
     * @param params additional parameters for the condition
     */
    public record Condition(
            String type,
            Map<String, Object> params
    ) {}
}
