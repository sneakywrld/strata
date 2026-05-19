package com.protectcord.strata.api.structure;

import com.protectcord.strata.api.block.StrataBlockState;

/**
 * Read-only representation of a loaded schematic (structure template).
 *
 * <p>Provides access to the schematic's dimensions, block data, and metadata.
 * Implementations are typically loaded from NBT files or serialized structure data.</p>
 *
 * @since 1.0.0
 * @see StructureDefinition
 */
public interface SchematicData {

    /**
     * Returns the width of the schematic along the X axis.
     *
     * @return the X-axis size in blocks
     */
    int width();

    /**
     * Returns the height of the schematic along the Y axis.
     *
     * @return the Y-axis size in blocks
     */
    int height();

    /**
     * Returns the length of the schematic along the Z axis.
     *
     * @return the Z-axis size in blocks
     */
    int length();

    /**
     * Returns the block state at the given local coordinates within the schematic.
     *
     * @param x the local X coordinate (0 to {@link #width()} - 1)
     * @param y the local Y coordinate (0 to {@link #height()} - 1)
     * @param z the local Z coordinate (0 to {@link #length()} - 1)
     * @return the block state at the given position, never {@code null}
     * @throws IndexOutOfBoundsException if coordinates are outside the schematic bounds
     */
    StrataBlockState getBlock(int x, int y, int z);

    /**
     * Returns the author of this schematic, or {@code null} if not specified.
     *
     * @return the author name, or {@code null}
     */
    String author();

    /**
     * Returns whether the given local coordinates are within the schematic bounds.
     *
     * @param x the local X coordinate
     * @param y the local Y coordinate
     * @param z the local Z coordinate
     * @return {@code true} if the coordinates are within bounds
     */
    boolean contains(int x, int y, int z);
}
