package com.protectcord.strata.core.water;

import com.protectcord.strata.api.block.StrataBlockState;
import com.protectcord.strata.api.chunk.HeightmapType;
import com.protectcord.strata.api.chunk.ProtoChunkAccess;
import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.api.pipeline.GenerationContext;
import com.protectcord.strata.noise.math.NoiseMath;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Generates kelp forests in cold and temperate ocean biomes. Uses Poisson-disk
 * sampling to space kelp columns 3-6 blocks apart, producing dense but natural
 * underwater forests with varying column heights.
 */
public final class KelpForestGenerator {

    private static final StrataBlockState WATER = StrataBlockState.of(NamespacedKey.minecraft("water"));
    private static final StrataBlockState KELP_PLANT = StrataBlockState.of(NamespacedKey.minecraft("kelp_plant"));
    private static final int MAX_KELP_AGE = 25;

    private static final Set<String> VALID_OCEAN_BIOMES = Set.of(
            "minecraft:ocean",
            "minecraft:deep_ocean",
            "minecraft:cold_ocean",
            "minecraft:deep_cold_ocean",
            "minecraft:lukewarm_ocean",
            "minecraft:deep_lukewarm_ocean"
    );

    private static final Set<String> VALID_FLOOR = Set.of(
            "minecraft:sand", "minecraft:gravel", "minecraft:clay",
            "minecraft:dirt", "minecraft:stone"
    );

    private static final double MIN_SPACING = 3.0;
    private static final double MAX_SPACING = 6.0;
    private static final int MIN_HEIGHT = 5;
    private static final int MAX_HEIGHT = 25;
    private static final int MIN_WATER_DEPTH = 6;
    private static final int MAX_PLACEMENT_ATTEMPTS = 30;
    private static final long KELP_SALT = 0xAE1BF0BE57L;

    public void generate(ProtoChunkAccess chunk, int seaLevel, GenerationContext ctx) {
        int baseX = chunk.coord().blockX();
        int baseZ = chunk.coord().blockZ();

        String biomeKey = getBiomeKey(chunk, baseX + 8, seaLevel - 1, baseZ + 8);
        if (!VALID_OCEAN_BIOMES.contains(biomeKey)) return;

        long seed = ctx.seed();
        long chunkSeed = NoiseMath.hash(seed ^ KELP_SALT, chunk.coord().x(), chunk.coord().z());
        Random random = new Random(chunkSeed);

        List<int[]> kelpPositions = poissonDiskSample(random, 16, 16, MIN_SPACING, MAX_SPACING);

        for (int[] pos : kelpPositions) {
            int worldX = baseX + pos[0];
            int worldZ = baseZ + pos[1];

            int floorY = chunk.getHeight(HeightmapType.OCEAN_FLOOR, worldX, worldZ) - 1;
            int waterDepth = seaLevel - floorY;
            if (waterDepth < MIN_WATER_DEPTH) continue;

            StrataBlockState floorBlock = chunk.getBlock(worldX, floorY, worldZ);
            if (!VALID_FLOOR.contains(floorBlock.blockId().toString())) continue;

            placeKelpColumn(chunk, random, worldX, floorY, worldZ, seaLevel);
        }
    }

    private void placeKelpColumn(ProtoChunkAccess chunk, Random random,
                                 int x, int floorY, int z, int seaLevel) {
        int maxPossibleHeight = seaLevel - floorY - 2;
        if (maxPossibleHeight < MIN_HEIGHT) return;

        int targetHeight = MIN_HEIGHT + random.nextInt(Math.min(MAX_HEIGHT, maxPossibleHeight) - MIN_HEIGHT + 1);
        int age = random.nextInt(MAX_KELP_AGE + 1);

        for (int dy = 1; dy <= targetHeight; dy++) {
            int y = floorY + dy;
            if (y >= seaLevel) break;

            StrataBlockState existing = chunk.getBlock(x, y, z);
            if (!existing.equals(WATER)) break;

            if (dy == targetHeight) {
                StrataBlockState kelpTop = new StrataBlockState(
                        NamespacedKey.minecraft("kelp"),
                        Map.of("age", String.valueOf(age)));
                chunk.setBlock(x, y, z, kelpTop);
            } else {
                chunk.setBlock(x, y, z, KELP_PLANT);
            }
        }
    }

    private List<int[]> poissonDiskSample(Random random, int width, int height,
                                          double minRadius, double maxRadius) {
        List<int[]> points = new ArrayList<>();
        List<int[]> active = new ArrayList<>();

        int startX = random.nextInt(width);
        int startZ = random.nextInt(height);
        points.add(new int[]{startX, startZ});
        active.add(new int[]{startX, startZ});

        while (!active.isEmpty()) {
            int idx = random.nextInt(active.size());
            int[] point = active.get(idx);
            boolean found = false;

            double radius = minRadius + random.nextDouble() * (maxRadius - minRadius);

            for (int attempt = 0; attempt < MAX_PLACEMENT_ATTEMPTS; attempt++) {
                double angle = random.nextDouble() * 2.0 * Math.PI;
                double dist = radius + random.nextDouble() * radius;
                int nx = point[0] + (int) (Math.cos(angle) * dist);
                int nz = point[1] + (int) (Math.sin(angle) * dist);

                if (nx < 0 || nx >= width || nz < 0 || nz >= height) continue;

                boolean tooClose = false;
                for (int[] existing : points) {
                    double dx = nx - existing[0];
                    double dz = nz - existing[1];
                    if (dx * dx + dz * dz < minRadius * minRadius) {
                        tooClose = true;
                        break;
                    }
                }

                if (!tooClose) {
                    int[] newPoint = new int[]{nx, nz};
                    points.add(newPoint);
                    active.add(newPoint);
                    found = true;
                    break;
                }
            }

            if (!found) {
                active.remove(idx);
            }
        }

        return points;
    }

    private static String getBiomeKey(ProtoChunkAccess chunk, int x, int y, int z) {
        var biome = chunk.getBiome(x, y, z);
        return biome != null ? biome.key().toString() : "";
    }
}
