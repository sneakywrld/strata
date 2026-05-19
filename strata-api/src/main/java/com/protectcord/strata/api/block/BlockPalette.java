package com.protectcord.strata.api.block;

import com.protectcord.strata.api.core.Keyed;
import com.protectcord.strata.api.core.NamespacedKey;

import java.util.List;

/**
 * A weighted palette of block states used for surface layers, ore veins,
 * and other block placement systems where multiple block types can appear with
 * varying probabilities.
 *
 * <p>Each {@link Entry} has a weight that determines its relative selection probability.
 * For example, a palette with entries {@code [grass:3.0, dirt:1.0]} would place grass
 * blocks 75% of the time and dirt blocks 25% of the time.</p>
 *
 * <p>Block palettes are referenced by {@link com.protectcord.strata.api.biome.Biome#surfacePalette()}
 * and {@link com.protectcord.strata.api.biome.Biome#underwaterPalette()}.</p>
 *
 * @since 1.0.0
 * @see StrataBlockState
 * @see com.protectcord.strata.api.biome.Biome
 */
public interface BlockPalette extends Keyed {

    /**
     * A single entry in the palette, pairing a block state with a selection weight.
     *
     * @param block  the block state for this entry
     * @param weight the relative selection weight (higher = more frequent)
     * @since 1.0.0
     */
    record Entry(StrataBlockState block, double weight) {}

    /**
     * Returns all entries in this palette.
     *
     * @return an unmodifiable list of weighted entries, never {@code null} or empty
     */
    List<Entry> entries();

    /**
     * Samples a random block state from this palette using weighted selection.
     *
     * <p>The {@code randomValue} should be uniformly distributed in the range [0.0, 1.0).
     * The method selects an entry based on cumulative weight distribution.</p>
     *
     * @param randomValue a random value in [0.0, 1.0)
     * @return the selected block state, never {@code null}
     */
    StrataBlockState sample(double randomValue);

    /**
     * Returns the primary (highest-weight) block state in this palette.
     *
     * <p>Useful for preview rendering and default block selection when randomization
     * is not desired.</p>
     *
     * @return the block state with the highest weight, never {@code null}
     */
    StrataBlockState primary();
}
