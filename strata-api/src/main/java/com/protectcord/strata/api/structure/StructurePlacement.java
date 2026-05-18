package com.protectcord.strata.api.structure;

/**
 * Controls how structures are distributed across the world.
 *
 * @param spacing      minimum distance between structures in chunks
 * @param separation   guaranteed minimum separation in chunks
 * @param salt         random salt for placement randomization
 * @param spreadType   how placement positions are distributed
 */
public record StructurePlacement(
        int spacing,
        int separation,
        int salt,
        SpreadType spreadType
) {

    public enum SpreadType {
        /** Linear grid with random offset. */
        LINEAR,
        /** Triangular grid for more even distribution. */
        TRIANGULAR
    }

    public static StructurePlacement of(int spacing, int separation, int salt) {
        return new StructurePlacement(spacing, separation, salt, SpreadType.LINEAR);
    }
}
