package com.protectcord.strata.api.biome;

/**
 * The dimension a biome belongs to.
 *
 * <p>Each biome is assigned to exactly one dimension, which determines the set of
 * terrain settings, default effects, and generation pipeline behavior that apply.
 * Only biomes matching the world's dimension are considered during biome assignment.</p>
 *
 * @since 1.0.0
 * @see Biome#dimension()
 */
public enum BiomeDimension {
    /** The standard overworld dimension (Y range typically -64 to 320). */
    OVERWORLD,
    /** The nether dimension (Y range typically 0 to 128). */
    NETHER,
    /** The end dimension (Y range typically 0 to 256). */
    END
}
