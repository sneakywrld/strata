package com.protectcord.strata.api.structure;

import com.protectcord.strata.api.core.Keyed;
import com.protectcord.strata.api.core.NamespacedKey;

import java.util.List;

/**
 * Defines a structure that can be placed in the world during the
 * {@link com.protectcord.strata.api.pipeline.GenerationStage#STRUCTURE_GENERATION STRUCTURE_GENERATION} stage.
 *
 * <p>Structures range from simple schematic placements (temples, wells) to complex
 * jigsaw-assembled builds (villages, bastions) to fully procedural generation.
 * Each definition specifies its {@linkplain #type() type}, {@linkplain #placement() placement rules},
 * {@linkplain #validBiomes() biome restrictions}, and Y-range bounds.</p>
 *
 * <p>Structure definitions are registered in the structure registry
 * ({@link com.protectcord.strata.api.core.StrataAPI#structureRegistry()}) and referenced by key
 * in {@link com.protectcord.strata.api.biome.Biome#structures()}.</p>
 *
 * @since 1.0.0
 * @see StructureType
 * @see StructurePlacement
 */
public interface StructureDefinition extends Keyed {

    /**
     * Returns the structure generation type (jigsaw, schematic, procedural, or custom).
     *
     * @return the {@link StructureType}, never {@code null}
     */
    StructureType type();

    /**
     * Returns the placement distribution settings controlling spacing, separation,
     * and randomization of this structure across the world.
     *
     * @return the {@link StructurePlacement} settings, never {@code null}
     */
    StructurePlacement placement();

    /**
     * Returns the list of biome keys where this structure can generate.
     *
     * <p>An empty list means the structure can generate in any biome.</p>
     *
     * @return an unmodifiable list of valid biome keys, never {@code null}
     */
    List<NamespacedKey> validBiomes();

    /**
     * Returns the minimum Y level at which this structure can be placed.
     *
     * @return the minimum Y coordinate
     */
    int minY();

    /**
     * Returns the maximum Y level at which this structure can be placed.
     *
     * @return the maximum Y coordinate
     */
    int maxY();

    /**
     * Returns whether this structure adapts to the underlying terrain.
     *
     * <p>When {@code true}, the structure adjusts its vertical placement and path routing
     * to follow terrain slopes (e.g., village paths going up and down hills).</p>
     *
     * @return {@code true} if terrain adaptation is enabled
     */
    boolean terrainAdaptation();

    /**
     * Returns the size multiplier relative to vanilla defaults.
     *
     * <p>For example, a value of {@code 2.0} for a village produces a village twice the
     * normal size.</p>
     *
     * @return the size multiplier (1.0 = vanilla size)
     */
    double sizeMultiplier();
}
