package com.protectcord.strata.core.pipeline;

import com.protectcord.strata.api.biome.Biome;
import com.protectcord.strata.api.carver.Carver;
import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.api.entity.EntitySpawnRegistry;
import com.protectcord.strata.api.feature.Feature;
import com.protectcord.strata.api.noise.NoiseFunction;
import com.protectcord.strata.api.noise.NoiseType;
import com.protectcord.strata.api.registry.Registry;
import com.protectcord.strata.api.structure.StructureDefinition;
import com.protectcord.strata.api.water.WaterSystemSettings;
import com.protectcord.strata.api.world.WorldProfile;
import com.protectcord.strata.core.biome.BiomeLookupTable;
import com.protectcord.strata.core.pipeline.stage.*;
import com.protectcord.strata.core.water.RiverNetworkBuilder;
import com.protectcord.strata.core.water.WaterSystemStage;
import com.protectcord.strata.noise.NoiseFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a {@link GenerationPipeline} from a {@link WorldProfile}.
 * Creates and orders all 14 pipeline stages.
 */
public final class PipelineBuilder {

    private PipelineBuilder() {}

    /**
     * Creates a fully configured {@link GenerationPipeline} with all 14 stages
     * ordered and registered.
     *
     * @param profile            the world profile to configure from
     * @param seed               the world seed
     * @param biomeRegistry      the biome registry
     * @param noiseRegistry      the noise function registry
     * @param carverRegistry     the carver registry
     * @param structureRegistry  the structure registry
     * @param featureRegistry    the feature registry
     * @param spawnRegistry      the entity spawn registry
     * @return a configured pipeline ready for chunk generation
     */
    public static GenerationPipeline fromProfile(WorldProfile profile,
                                                  long seed,
                                                  Registry<Biome> biomeRegistry,
                                                  Registry<NoiseFunction> noiseRegistry,
                                                  Registry<Carver> carverRegistry,
                                                  Registry<StructureDefinition> structureRegistry,
                                                  Registry<Feature> featureRegistry,
                                                  EntitySpawnRegistry spawnRegistry) {
        GenerationPipeline pipeline = new GenerationPipeline();

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
        NoiseFunction aquiferNoise = NoiseFactory.create(
                NamespacedKey.strata("aquifer"), NoiseType.SIMPLEX, seed + 7);

        // Stage 0: Initialization
        pipeline.registerStage(new InitializationStage());

        // Stage 1: Continental Shape
        pipeline.registerStage(new ContinentalShapeStage(contNoise));

        // Stage 2: Climate Sampling
        pipeline.registerStage(new ClimateSamplingStage(
                tempNoise, humidityNoise, contNoise, erosionNoise, weirdNoise));

        // Stage 3: Biome Assignment
        List<Biome> biomes = new ArrayList<>(biomeRegistry.entries());
        if (!biomes.isEmpty()) {
            BiomeLookupTable biomeLookup = BiomeLookupTable.build(biomes);
            pipeline.registerStage(new BiomeAssignmentStage(
                    biomeLookup, tempNoise, humidityNoise, contNoise, erosionNoise, weirdNoise));
        }

        // Stage 4: Terrain Shaping
        pipeline.registerStage(new TerrainShapingStage(densityNoise));

        // Stage 5: Aquifer Placement
        if (profile.waterSettings().aquifers().enabled()) {
            pipeline.registerStage(new AquiferPlacementStage(aquiferNoise));
        }

        // Stage 6: Surface Building
        pipeline.registerStage(new SurfaceBuildingStage());

        // Stage 7: Carving
        List<Carver> carvers = new ArrayList<>(carverRegistry.entries());
        if (!carvers.isEmpty()) {
            pipeline.registerStage(new CarvingStage(carvers));
        }

        // Stage 8: Water System
        WaterSystemSettings waterSettings = profile.waterSettings();
        if (waterSettings.rivers().enabled() || waterSettings.oceans().enabled()) {
            RiverNetworkBuilder riverBuilder = new RiverNetworkBuilder(
                    contNoise, waterSettings.rivers(), seed);
            pipeline.registerStage(new WaterSystemStage(waterSettings, riverBuilder, riverMicroNoise));
        }

        // Stage 9: Structure Generation
        pipeline.registerStage(new StructureGenerationStage(structureRegistry));

        // Stage 10: Feature Decoration
        pipeline.registerStage(new FeatureDecorationStage(featureRegistry));

        // Stage 11: Entity Spawning
        if (spawnRegistry != null) {
            pipeline.registerStage(new EntitySpawningStage(spawnRegistry));
        }

        // Stage 12: Lighting
        pipeline.registerStage(new LightingStage());

        // Stage 13: Finalization
        pipeline.registerStage(new FinalizationStage());

        return pipeline;
    }
}
