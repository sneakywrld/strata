package com.protectcord.strata.api.biome;

/**
 * High-level biome categories used for classification, filtering, and default behavior selection.
 *
 * <p>Categories are grouped by dimension (Overworld, Nether, End) plus a {@link #CUSTOM} fallback
 * for plugin-defined biomes that do not fit any standard category. The category influences
 * default surface rules, mob spawning tables, and structure eligibility when no explicit
 * overrides are provided.</p>
 *
 * @since 1.0.0
 * @see Biome#category()
 */
public enum BiomeCategory {

    // --- Overworld categories ---

    /** Flat grassland biomes (plains, sunflower plains). */
    PLAINS,
    /** Temperate forest biomes (forest, birch forest, dark forest). */
    FOREST,
    /** Cold coniferous forest biomes (taiga, old growth taiga). */
    TAIGA,
    /** Hot, arid biomes with sand surfaces. */
    DESERT,
    /** Warm, semi-arid grassland biomes. */
    SAVANNA,
    /** Dense tropical biomes with tall vegetation. */
    JUNGLE,
    /** Wet lowland biomes with shallow water and lily pads. */
    SWAMP,
    /** High-elevation biomes with steep terrain (peaks, stony shores). */
    MOUNTAIN,
    /** Moderate-elevation biomes with rolling terrain. */
    HILLS,
    /** Shoreline biomes between land and ocean. */
    BEACH,
    /** Ocean biomes of varying depth and temperature. */
    OCEAN,
    /** Narrow waterway biomes connecting bodies of water. */
    RIVER,
    /** Inland freshwater body biomes. */
    LAKE,
    /** Underground biomes (lush caves, dripstone caves). */
    CAVE,
    /** Rare mycelium-covered island biomes. */
    MUSHROOM,
    /** Frozen biomes (ice spikes, frozen ocean, snowy plains). */
    ICY,
    /** Badlands/mesa biomes with terracotta layers. */
    MESA,
    /** Volcanic biomes with basalt and lava features. */
    VOLCANIC,
    /** Open flower-covered highland biomes. */
    MEADOW,
    /** Cherry blossom grove biomes. */
    CHERRY,
    /** Mangrove swamp biomes with root-based terrain. */
    MANGROVE,
    /** Deep underground sculk-infested biomes. */
    DEEP_DARK,

    // --- Nether categories ---

    /** Standard nether waste biomes (netherrack, lava). */
    NETHER_WASTES,
    /** Soul sand valley biomes with soul fire. */
    SOUL_SAND,
    /** Crimson forest biomes with crimson fungi. */
    CRIMSON,
    /** Warped forest biomes with warped fungi. */
    WARPED,
    /** Basalt deltas biomes with columns and lava. */
    BASALT,
    /** Nether cave biomes for enclosed underground areas. */
    NETHER_CAVE,
    /** Zones around nether fortress generation. */
    NETHER_FORTRESS_ZONE,

    // --- End categories ---

    /** End highlands biomes (outer islands with chorus plants). */
    END_HIGHLANDS,
    /** End midlands biomes (transitional terrain around highlands). */
    END_MIDLANDS,
    /** End barrens biomes (edges of outer islands). */
    END_BARRENS,
    /** Small end island biomes scattered in the void. */
    END_ISLANDS,
    /** The void area of the End with no terrain. */
    END_VOID,

    // --- Special ---

    /** Custom category for plugin-defined biomes that do not fit standard categories. */
    CUSTOM
}
