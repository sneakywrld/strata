package com.protectcord.strata.api.structure;

/**
 * Built-in structure generation types.
 *
 * <p>Each constant describes a distinct approach to structure generation. Use {@link #CUSTOM}
 * for plugin-defined generation algorithms that do not fit the built-in categories.</p>
 *
 * @since 1.0.0
 * @see StructureDefinition#type()
 */
public enum StructureType {
    /** Jigsaw-assembled structure (villages, bastions). */
    JIGSAW,

    /** Schematic-placed structure (temples, wells). */
    SCHEMATIC,

    /** Procedurally generated structure. */
    PROCEDURAL,

    /** Custom structure type (for plugins). */
    CUSTOM
}
