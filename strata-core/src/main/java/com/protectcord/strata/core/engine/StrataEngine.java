package com.protectcord.strata.core.engine;

import com.protectcord.strata.api.biome.Biome;
import com.protectcord.strata.api.carver.Carver;
import com.protectcord.strata.api.chunk.ChunkCoord;
import com.protectcord.strata.api.feature.Feature;
import com.protectcord.strata.api.noise.NoiseFunction;
import com.protectcord.strata.api.noise.NoiseType;
import com.protectcord.strata.api.registry.Registry;
import com.protectcord.strata.api.structure.StructureDefinition;
import com.protectcord.strata.api.surface.SurfaceRule;
import com.protectcord.strata.api.water.WaterSystemSettings;
import com.protectcord.strata.api.world.WorldProfile;
import com.protectcord.strata.core.biome.BiomeLookupTable;
import com.protectcord.strata.core.chunk.StrataProtoChunk;
import com.protectcord.strata.core.pipeline.GenerationContextImpl;
import com.protectcord.strata.core.pipeline.GenerationPipeline;
import com.protectcord.strata.core.pipeline.stage.*;
import com.protectcord.strata.core.registry.SimpleRegistry;
import com.protectcord.strata.core.water.RiverNetworkBuilder;
import com.protectcord.strata.core.water.WaterSystemStage;
import com.protectcord.strata.noise.NoiseFactory;
import com.protectcord.strata.api.core.NamespacedKey;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * The main Strata generation engine. Owns the pipeline and registries,
 * and coordinates chunk generation for a single world.
 */
public final class StrataEngine {

    private static final Logger LOGGER = Logger.getLogger("Strata");

    private final WorldProfile profile;
    private final long seed;
    private final GenerationPipeline pipeline;

    private final Registry<Biome> biomeRegistry;
    private final Registry<NoiseFunction> noiseRegistry;
    private final Registry<SurfaceRule> surfaceRuleRegistry;
    private final Registry<Carver> carverRegistry;
    private final Registry<StructureDefinition> structureRegistry;
    private final Registry<Feature> featureRegistry;

    private BiomeLookupTable biomeLookup;

    public StrataEngine(WorldProfile profile, long seed) {
        this.profile = profile;
        this.seed = seed;
        this.pipeline = new GenerationPipeline();

        this.biomeRegistry = new SimpleRegistry<>("biome");
        this.noiseRegistry = new SimpleRegistry<>("noise");
        this.surfaceRuleRegistry = new SimpleRegistry<>("surface_rule");
        this.carverRegistry = new SimpleRegistry<>("carver");
        this.structureRegistry = new SimpleRegistry<>("structure");
        this.featureRegistry = new SimpleRegistry<>("feature");
    }

    /**
     * Initializes the engine: builds noise functions, lookup tables, and pipeline stages.
     * Call after all biomes and config have been registered.
     */
    public void initialize() {
        LOGGER.info("Initializing Strata engine for profile: " + profile.key());

        // Build biome KD-tree
        List<Biome> biomes = new ArrayList<>(biomeRegistry.entries());
        if (!biomes.isEmpty()) {
            biomeLookup = BiomeLookupTable.build(biomes);
        }

        // Create core noise functions
        NoiseFunction densityNoise = NoiseFactory.create(
                NamespacedKey.strata("density"), NoiseType.SIMPLEX, seed);
        NoiseFunction tempNoise = NoiseFactory.create(
                NamespacedKey.strata("temperature"), NoiseType.PERLIN, seed + 1);
        NoiseFunction humidityNoise = NoiseFactory.create(
                NamespacedKey.strata("humidity"), NoiseType.PERLIN, seed + 2);
        NoiseFunction contNoise = NoiseFactory.create(
                NamespacedKey.strata("continentalness"), NoiseType.SIMPLEX, seed + 3);
        NoiseFunction erosionNoise = NoiseFactory.create(
                NamespacedKey.strata("erosion"), NoiseType.SIMPLEX, seed + 4);
        NoiseFunction weirdNoise = NoiseFactory.create(
                NamespacedKey.strata("weirdness"), NoiseType.OPEN_SIMPLEX_2, seed + 5);
        NoiseFunction riverMicroNoise = NoiseFactory.create(
                NamespacedKey.strata("river_micro"), NoiseType.PERLIN, seed + 6);

        // Register pipeline stages
        pipeline.registerStage(new TerrainShapingStage(densityNoise));

        if (biomeLookup != null) {
            pipeline.registerStage(new BiomeAssignmentStage(
                    biomeLookup, tempNoise, humidityNoise, contNoise, erosionNoise, weirdNoise));
        }

        pipeline.registerStage(new SurfaceBuildingStage());

        List<Carver> carvers = new ArrayList<>(carverRegistry.entries());
        if (!carvers.isEmpty()) {
            pipeline.registerStage(new CarvingStage(carvers));
        }

        // Water system
        WaterSystemSettings waterSettings = profile.waterSettings();
        if (waterSettings.rivers().enabled() || waterSettings.oceans().enabled()) {
            RiverNetworkBuilder riverBuilder = new RiverNetworkBuilder(contNoise, waterSettings.rivers(), seed);
            pipeline.registerStage(new WaterSystemStage(waterSettings, riverBuilder, riverMicroNoise));
        }

        LOGGER.info("Strata engine initialized with " + pipeline.stages().size() + " pipeline stages");
    }

    /**
     * Generates a single chunk.
     */
    public StrataProtoChunk generateChunk(ChunkCoord coord) {
        StrataProtoChunk chunk = new StrataProtoChunk(
                coord,
                profile.terrainSettings().minY(),
                profile.terrainSettings().maxY()
        );

        GenerationContextImpl context = new GenerationContextImpl(chunk, profile, seed);
        pipeline.generate(context);

        return chunk;
    }

    public GenerationPipeline pipeline() { return pipeline; }
    public Registry<Biome> biomeRegistry() { return biomeRegistry; }
    public Registry<NoiseFunction> noiseRegistry() { return noiseRegistry; }
    public Registry<SurfaceRule> surfaceRuleRegistry() { return surfaceRuleRegistry; }
    public Registry<Carver> carverRegistry() { return carverRegistry; }
    public Registry<StructureDefinition> structureRegistry() { return structureRegistry; }
    public Registry<Feature> featureRegistry() { return featureRegistry; }
    public WorldProfile profile() { return profile; }
    public long seed() { return seed; }
}
