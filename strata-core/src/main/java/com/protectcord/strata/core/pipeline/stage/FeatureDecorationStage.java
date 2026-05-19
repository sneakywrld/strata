package com.protectcord.strata.core.pipeline.stage;

import com.protectcord.strata.api.biome.Biome;
import com.protectcord.strata.api.chunk.HeightmapType;
import com.protectcord.strata.api.chunk.ProtoChunkAccess;
import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.api.feature.Feature;
import com.protectcord.strata.api.feature.FeaturePlacement;
import com.protectcord.strata.api.feature.FeatureStep;
import com.protectcord.strata.api.feature.PlacementModifier;
import com.protectcord.strata.api.pipeline.GenerationContext;
import com.protectcord.strata.api.pipeline.GenerationStage;
import com.protectcord.strata.api.registry.Registry;
import com.protectcord.strata.core.pipeline.PipelineStage;
import com.protectcord.strata.noise.math.NoiseMath;

import java.util.*;

/**
 * Iterates feature steps in order (RAW, ORES, etc.). For each step,
 * gets features for the chunk's biomes, applies placement modifiers
 * (count, height range, rarity filter), and places features.
 */
public final class FeatureDecorationStage implements PipelineStage {

    private final Registry<Feature> featureRegistry;

    public FeatureDecorationStage(Registry<Feature> featureRegistry) {
        this.featureRegistry = featureRegistry;
    }

    @Override
    public GenerationStage stage() {
        return GenerationStage.FEATURE_DECORATION;
    }

    @Override
    public void generate(GenerationContext context) {
        ProtoChunkAccess chunk = context.chunk();
        int baseX = chunk.coord().blockX();
        int baseZ = chunk.coord().blockZ();
        long seed = context.seed();

        Biome centerBiome = chunk.getBiome(baseX + 8, context.seaLevel(), baseZ + 8);
        if (centerBiome == null) return;

        Set<NamespacedKey> biomeFeatureKeys = new LinkedHashSet<>(centerBiome.features());

        for (FeatureStep step : FeatureStep.values()) {
            for (NamespacedKey featureKey : biomeFeatureKeys) {
                Optional<Feature> optFeature = featureRegistry.get(featureKey);
                if (optFeature.isEmpty()) continue;

                Feature feature = optFeature.get();
                if (!matchesStep(feature, step)) continue;

                FeaturePlacement placement = feature.placement();
                Map<String, Object> params = placement.parameters();

                long featureSeed = NoiseMath.hash(seed, featureKey.hashCode(), step.ordinal());
                Random random = new Random(featureSeed ^ chunk.coord().toLong());

                if (hasModifier(placement, PlacementModifier.RARITY_FILTER)) {
                    int rarity = getInt(params, "rarity", 1);
                    if (rarity > 1 && random.nextInt(rarity) != 0) continue;
                }

                int count = 1;
                if (hasModifier(placement, PlacementModifier.COUNT)) {
                    count = getInt(params, "count", 1);
                }

                for (int i = 0; i < count; i++) {
                    int x, y, z;

                    if (hasModifier(placement, PlacementModifier.IN_SQUARE)) {
                        x = baseX + random.nextInt(16);
                        z = baseZ + random.nextInt(16);
                    } else {
                        x = baseX + 8;
                        z = baseZ + 8;
                    }

                    if (hasModifier(placement, PlacementModifier.HEIGHTMAP)) {
                        y = chunk.getHeight(HeightmapType.WORLD_SURFACE, x, z);
                    } else if (hasModifier(placement, PlacementModifier.UNIFORM_Y)) {
                        int minY = getInt(params, "min_y", chunk.minY());
                        int maxY = getInt(params, "max_y", chunk.maxY());
                        y = minY + random.nextInt(Math.max(1, maxY - minY));
                    } else if (hasModifier(placement, PlacementModifier.TRIANGLE_Y)) {
                        int minY = getInt(params, "min_y", chunk.minY());
                        int maxY = getInt(params, "max_y", chunk.maxY());
                        int range = maxY - minY;
                        y = minY + random.nextInt(Math.max(1, range / 2)) + random.nextInt(Math.max(1, range / 2));
                    } else if (hasModifier(placement, PlacementModifier.FIXED_Y)) {
                        y = getInt(params, "y", context.seaLevel());
                    } else {
                        y = chunk.getHeight(HeightmapType.WORLD_SURFACE, x, z);
                    }

                    feature.place(chunk, random, x, y, z);
                }
            }
        }
    }

    private static boolean matchesStep(Feature feature, FeatureStep step) {
        return switch (feature.type()) {
            case ORE -> step == FeatureStep.ORES;
            case TREE, BAMBOO, MUSHROOM, VEGETATION, NETHER_VEGETATION,
                 UNDERWATER_VEGETATION -> step == FeatureStep.VEGETAL;
            case CACTUS, SUGAR_CANE, CORAL, BOULDER, DISK,
                 END_FEATURE -> step == FeatureStep.SURFACE_DECORATION;
            case FLUID_SPRING -> step == FeatureStep.FLUID_SPRINGS;
            case SNOW_ICE -> step == FeatureStep.TOP_LAYER;
            case CAVE_DECORATION, DRIPSTONE, SCULK, GEODE -> step == FeatureStep.UNDERGROUND_DECORATION;
            default -> step == FeatureStep.RAW;
        };
    }

    private static boolean hasModifier(FeaturePlacement placement, PlacementModifier modifier) {
        return placement.modifiers().contains(modifier);
    }

    private static int getInt(Map<String, Object> params, String key, int defaultValue) {
        Object val = params.get(key);
        if (val instanceof Number n) return n.intValue();
        return defaultValue;
    }
}
