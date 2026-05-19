package com.protectcord.strata.api.structure;

/**
 * Controls how structures are distributed across the world grid.
 *
 * <p>The placement algorithm divides the world into cells of {@code spacing} chunks.
 * Within each cell, a random position is chosen (using the {@code salt} for determinism),
 * subject to a minimum {@code separation} between neighboring structures.
 * The {@link SpreadType} controls whether the grid is linear or triangular.</p>
 *
 * @param spacing    minimum distance between structure placement attempts, in chunks
 * @param separation guaranteed minimum separation between placed structures, in chunks
 *                   (must be less than {@code spacing})
 * @param salt       random salt value for deterministic placement randomization
 * @param spreadType how placement grid positions are distributed
 * @since 1.0.0
 * @see StructureDefinition#placement()
 */
public record StructurePlacement(
        int spacing,
        int separation,
        int salt,
        SpreadType spreadType
) {

    /**
     * Grid distribution strategies for structure placement.
     *
     * @since 1.0.0
     */
    public enum SpreadType {
        /** Linear grid with random offset per cell. */
        LINEAR,
        /** Triangular grid for more even spatial distribution, avoiding clustering. */
        TRIANGULAR
    }

    /**
     * Creates a placement with the given spacing, separation, and salt, using
     * {@link SpreadType#LINEAR} distribution.
     *
     * @param spacing    minimum distance between attempts, in chunks
     * @param separation minimum separation between placed structures, in chunks
     * @param salt       random salt value
     * @return a new placement with linear spread type
     */
    public static StructurePlacement of(int spacing, int separation, int salt) {
        return new StructurePlacement(spacing, separation, salt, SpreadType.LINEAR);
    }
}
