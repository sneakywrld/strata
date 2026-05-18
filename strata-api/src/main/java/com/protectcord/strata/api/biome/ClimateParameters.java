package com.protectcord.strata.api.biome;

/**
 * 5-dimensional climate parameters used for biome selection via KD-tree nearest-neighbor lookup.
 *
 * <p>Each parameter typically ranges from {@code -1.0} to {@code 1.0}. During the
 * {@link com.protectcord.strata.api.pipeline.GenerationStage#CLIMATE_SAMPLING CLIMATE_SAMPLING} stage,
 * noise functions produce climate values at each position, and the biome with the closest
 * {@link #distanceSquared(ClimateParameters) Euclidean distance} is selected.</p>
 *
 * @param temperature     hot (positive) vs cold (negative)
 * @param humidity        wet (positive) vs dry (negative)
 * @param continentalness inland (positive) vs ocean (negative)
 * @param erosion         flat/eroded (positive) vs mountainous (negative)
 * @param weirdness       unusual terrain variants (positive) vs normal (negative)
 * @since 1.0.0
 * @see Biome#climate()
 */
public record ClimateParameters(
        double temperature,
        double humidity,
        double continentalness,
        double erosion,
        double weirdness
) {

    /**
     * Returns the squared Euclidean distance to another parameter set.
     *
     * <p>Used by the KD-tree for nearest-neighbor biome lookup. The squared distance
     * avoids a costly square-root operation and preserves ordering for comparisons.</p>
     *
     * @param other the other climate parameters to compute distance against
     * @return the sum of squared differences across all five dimensions
     */
    public double distanceSquared(ClimateParameters other) {
        double dt = this.temperature - other.temperature;
        double dh = this.humidity - other.humidity;
        double dc = this.continentalness - other.continentalness;
        double de = this.erosion - other.erosion;
        double dw = this.weirdness - other.weirdness;
        return dt * dt + dh * dh + dc * dc + de * de + dw * dw;
    }
}
