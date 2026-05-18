package com.protectcord.strata.api.structure;

/**
 * Built-in structure types.
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
