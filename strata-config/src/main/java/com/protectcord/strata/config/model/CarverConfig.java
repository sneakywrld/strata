package com.protectcord.strata.config.model;

/**
 * Parsed carver configuration from TOML.
 *
 * @param type            carver algorithm type
 * @param minY            minimum generation height
 * @param maxY            maximum generation height
 * @param probability     chance of carver placement per chunk (0.0-1.0)
 * @param width           horizontal radius multiplier
 * @param height          vertical radius multiplier
 * @param floorLevel      the y-level at which the carver floor sits
 * @param aquifersEnabled whether to generate aquifers within carved regions
 * @param noiseFunction   reference to a noise config ID for shaping
 */
public record CarverConfig(
        String type,
        int minY,
        int maxY,
        double probability,
        double width,
        double height,
        double floorLevel,
        boolean aquifersEnabled,
        String noiseFunction
) {

    /**
     * Known carver algorithm types.
     */
    public enum CarverType {
        CHEESE,
        SPAGHETTI,
        NOODLE,
        RAVINE,
        CUSTOM
    }
}
