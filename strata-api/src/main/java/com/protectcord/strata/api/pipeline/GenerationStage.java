package com.protectcord.strata.api.pipeline;

/**
 * The 14 ordered stages of the Strata generation pipeline.
 * Each chunk passes through every stage in sequence.
 */
public enum GenerationStage {

    /** Set up chunk state, allocate buffers. */
    INITIALIZATION(0),

    /** Generate continental landmass shape. */
    CONTINENTAL_SHAPE(1),

    /** Sample temperature, humidity, continentalness, erosion, weirdness. */
    CLIMATE_SAMPLING(2),

    /** Assign biomes to 4x4x4 cells based on climate parameters. */
    BIOME_ASSIGNMENT(3),

    /** Shape 3D terrain using density functions and splines. */
    TERRAIN_SHAPING(4),

    /** Place underground aquifer water/lava pockets. */
    AQUIFER_PLACEMENT(5),

    /** Apply surface blocks (grass, sand, stone layers). */
    SURFACE_BUILDING(6),

    /** Carve caves, ravines, and tunnels. */
    CARVING(7),

    /** Generate rivers, waterfalls, lakes, ocean features. */
    WATER_SYSTEM(8),

    /** Place structures (villages, temples, etc.). */
    STRUCTURE_GENERATION(9),

    /** Place features (ores, trees, vegetation, etc.). */
    FEATURE_DECORATION(10),

    /** Compute entity spawn tables. */
    ENTITY_SPAWNING(11),

    /** Calculate block and sky lighting. */
    LIGHTING(12),

    /** Final cleanup and validation. */
    FINALIZATION(13);

    private final int order;

    GenerationStage(int order) {
        this.order = order;
    }

    /**
     * Returns the execution order index.
     */
    public int order() {
        return order;
    }

    /**
     * Returns the total number of stages.
     */
    public static int count() {
        return 14;
    }
}
