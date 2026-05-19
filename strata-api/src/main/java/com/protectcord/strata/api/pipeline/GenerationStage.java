package com.protectcord.strata.api.pipeline;

/**
 * The 14 ordered stages of the Strata generation pipeline.
 *
 * <p>Each chunk passes through every enabled stage in sequence, from {@link #INITIALIZATION}
 * through {@link #FINALIZATION}. Stages can be individually enabled/disabled and monitored
 * via the {@link PipelineAccessor}.</p>
 *
 * <p>Third-party plugins can hook into specific stages by subscribing to
 * {@link com.protectcord.strata.api.event.ChunkGeneratingEvent} (fired before each stage)
 * and {@link com.protectcord.strata.api.event.ChunkGeneratedEvent} (fired after all stages complete).</p>
 *
 * @since 1.0.0
 * @see GenerationContext#currentStage()
 * @see PipelineAccessor
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
     * Returns the zero-based execution order index of this stage.
     *
     * @return the order index (0 for {@link #INITIALIZATION} through 13 for {@link #FINALIZATION})
     */
    public int order() {
        return order;
    }

    /**
     * Returns the total number of pipeline stages (currently 14).
     *
     * @return the stage count
     */
    public static int count() {
        return 14;
    }
}
