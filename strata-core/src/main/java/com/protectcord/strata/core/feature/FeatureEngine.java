package com.protectcord.strata.core.feature;

import com.protectcord.strata.api.chunk.HeightmapType;
import com.protectcord.strata.api.feature.Feature;
import com.protectcord.strata.api.feature.FeaturePlacement;
import com.protectcord.strata.api.feature.PlacementModifier;
import com.protectcord.strata.api.pipeline.GenerationContext;
import com.protectcord.strata.core.chunk.StrataProtoChunk;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class FeatureEngine {

    private FeatureEngine() {}

    public static void decorateChunk(StrataProtoChunk chunk, String biomeKey,
                                     List<FeaturePlacement> features,
                                     GenerationContext ctx) {
        long positionSeed = ctx.seed() ^ ((long) chunk.coord().x() * 341873128712L
                + (long) chunk.coord().z() * 132897987541L);

        int baseX = chunk.coord().blockX();
        int baseZ = chunk.coord().blockZ();

        for (int i = 0; i < features.size(); i++) {
            FeaturePlacement placement = features.get(i);
            Random random = new Random(positionSeed + i);

            List<int[]> positions = resolvePositions(chunk, placement, baseX, baseZ, random);

            Feature feature = resolveFeature(placement, ctx);
            if (feature == null) continue;

            for (int[] pos : positions) {
                feature.place(chunk, random, pos[0], pos[1], pos[2]);
            }
        }
    }

    private static List<int[]> resolvePositions(StrataProtoChunk chunk,
                                                 FeaturePlacement placement,
                                                 int baseX, int baseZ,
                                                 Random random) {
        List<int[]> positions = new ArrayList<>();
        positions.add(new int[]{baseX, 0, baseZ});

        for (PlacementModifier modifier : placement.modifiers()) {
            positions = applyModifier(modifier, positions, chunk, placement, random);
        }
        return positions;
    }

    private static List<int[]> applyModifier(PlacementModifier modifier,
                                              List<int[]> positions,
                                              StrataProtoChunk chunk,
                                              FeaturePlacement placement,
                                              Random random) {
        return switch (modifier) {
            case COUNT -> {
                int count = ((Number) placement.parameters().getOrDefault("count", 1)).intValue();
                List<int[]> expanded = new ArrayList<>(count);
                for (int c = 0; c < count; c++) {
                    for (int[] pos : positions) {
                        expanded.add(new int[]{pos[0], pos[1], pos[2]});
                    }
                }
                yield expanded;
            }
            case IN_SQUARE -> {
                List<int[]> spread = new ArrayList<>(positions.size());
                int chunkBaseX = chunk.coord().blockX();
                int chunkBaseZ = chunk.coord().blockZ();
                for (int[] pos : positions) {
                    int x = chunkBaseX + random.nextInt(16);
                    int z = chunkBaseZ + random.nextInt(16);
                    spread.add(new int[]{x, pos[1], z});
                }
                yield spread;
            }
            case HEIGHTMAP -> {
                List<int[]> snapped = new ArrayList<>(positions.size());
                for (int[] pos : positions) {
                    int surfaceY = chunk.getHeight(HeightmapType.WORLD_SURFACE, pos[0], pos[2]);
                    if (surfaceY > chunk.minY()) {
                        snapped.add(new int[]{pos[0], surfaceY, pos[2]});
                    }
                }
                yield snapped;
            }
            case UNIFORM_Y -> {
                int minY = ((Number) placement.parameters().getOrDefault("min_y", chunk.minY())).intValue();
                int maxY = ((Number) placement.parameters().getOrDefault("max_y", chunk.maxY())).intValue();
                List<int[]> distributed = new ArrayList<>(positions.size());
                for (int[] pos : positions) {
                    int y = minY + random.nextInt(Math.max(1, maxY - minY));
                    distributed.add(new int[]{pos[0], y, pos[2]});
                }
                yield distributed;
            }
            case TRIANGLE_Y -> {
                int minY = ((Number) placement.parameters().getOrDefault("min_y", chunk.minY())).intValue();
                int maxY = ((Number) placement.parameters().getOrDefault("max_y", chunk.maxY())).intValue();
                int range = Math.max(1, maxY - minY);
                List<int[]> distributed = new ArrayList<>(positions.size());
                for (int[] pos : positions) {
                    int y = minY + (random.nextInt(range) + random.nextInt(range)) / 2;
                    distributed.add(new int[]{pos[0], y, pos[2]});
                }
                yield distributed;
            }
            case FIXED_Y -> {
                int fixedY = ((Number) placement.parameters().getOrDefault("y", 64)).intValue();
                List<int[]> fixed = new ArrayList<>(positions.size());
                for (int[] pos : positions) {
                    fixed.add(new int[]{pos[0], fixedY, pos[2]});
                }
                yield fixed;
            }
            case RARITY_FILTER -> {
                int rarity = ((Number) placement.parameters().getOrDefault("rarity", 1)).intValue();
                if (rarity <= 1 || random.nextInt(rarity) == 0) {
                    yield positions;
                }
                yield List.of();
            }
            case NOISE_THRESHOLD -> {
                double threshold = ((Number) placement.parameters()
                        .getOrDefault("noise_threshold", 0.0)).doubleValue();
                List<int[]> filtered = new ArrayList<>();
                for (int[] pos : positions) {
                    double noise = pseudoNoise(pos[0], pos[2]);
                    if (noise >= threshold) {
                        filtered.add(pos);
                    }
                }
                yield filtered;
            }
            case RANDOM_OFFSET -> {
                int rangeX = ((Number) placement.parameters().getOrDefault("offset_xz", 8)).intValue();
                int rangeY = ((Number) placement.parameters().getOrDefault("offset_y", 4)).intValue();
                List<int[]> offset = new ArrayList<>(positions.size());
                for (int[] pos : positions) {
                    offset.add(new int[]{
                            pos[0] + random.nextInt(2 * rangeX + 1) - rangeX,
                            pos[1] + random.nextInt(2 * rangeY + 1) - rangeY,
                            pos[2] + random.nextInt(2 * rangeX + 1) - rangeX
                    });
                }
                yield offset;
            }
            case BIOME_FILTER, BLOCK_PREDICATE, WATER_DEPTH_FILTER -> positions;
        };
    }

    private static double pseudoNoise(int x, int z) {
        long n = (long) x * 3129871L ^ (long) z * 116129781L;
        n = n * n * 42317861L + n * 11L;
        return ((double) (n >> 16 & 0xFFL)) / 255.0;
    }

    private static Feature resolveFeature(FeaturePlacement placement, GenerationContext ctx) {
        Object featureRef = placement.parameters().get("feature");
        if (featureRef instanceof Feature f) {
            return f;
        }
        return null;
    }
}
