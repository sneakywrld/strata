package com.protectcord.strata.api.chunk;

/**
 * Types of heightmaps that can be queried during and after generation.
 */
public enum HeightmapType {

    /** Highest solid block (used for surface placement). */
    WORLD_SURFACE,

    /** Highest motion-blocking block (includes leaves, fences). */
    MOTION_BLOCKING,

    /** Highest motion-blocking block, excluding leaves. */
    MOTION_BLOCKING_NO_LEAVES,

    /** Highest non-air block in the ocean floor (excludes water). */
    OCEAN_FLOOR
}
