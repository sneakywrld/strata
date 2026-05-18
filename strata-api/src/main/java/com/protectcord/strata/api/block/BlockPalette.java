package com.protectcord.strata.api.block;

import com.protectcord.strata.api.core.Keyed;
import com.protectcord.strata.api.core.NamespacedKey;

import java.util.List;

/**
 * A weighted palette of block states used for surface layers, ore veins,
 * and other block placement systems.
 *
 * <p>Each entry has a weight that determines its relative selection probability.</p>
 */
public interface BlockPalette extends Keyed {

    /**
     * A single entry in the palette.
     */
    record Entry(StrataBlockState block, double weight) {}

    /**
     * Returns all entries in this palette.
     */
    List<Entry> entries();

    /**
     * Samples a random block state from this palette using the given random value (0-1).
     */
    StrataBlockState sample(double randomValue);

    /**
     * Returns the primary (highest-weight) block state.
     */
    StrataBlockState primary();
}
