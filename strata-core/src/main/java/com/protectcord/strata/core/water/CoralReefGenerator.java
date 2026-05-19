package com.protectcord.strata.core.water;

import com.protectcord.strata.api.block.StrataBlockState;
import com.protectcord.strata.api.chunk.HeightmapType;
import com.protectcord.strata.api.chunk.ProtoChunkAccess;
import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.api.pipeline.GenerationContext;
import com.protectcord.strata.noise.math.NoiseMath;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Generates coral reef clusters in warm ocean biomes using Poisson-disk sampling
 * for natural-looking placement. Creates branching coral structures from coral blocks,
 * fans, and tubes, and places sea pickles for ambient light.
 */
public final class CoralReefGenerator {

    private static final StrataBlockState WATER = StrataBlockState.of(NamespacedKey.minecraft("water"));

    private static final String[] CORAL_TYPES = {"brain", "bubble", "fire", "horn", "tube"};

    private static final Set<String> WARM_OCEAN_BIOMES = Set.of(
            "minecraft:warm_ocean",
            "minecraft:lukewarm_ocean",
            "minecraft:deep_lukewarm_ocean"
    );

    private static final double MIN_DISK_RADIUS = 4.0;
    private static final double MAX_DISK_RADIUS = 8.0;
    private static final int MAX_PLACEMENT_ATTEMPTS = 30;
    private static final int MIN_WATER_DEPTH = 4;
    private static final int MAX_CORAL_HEIGHT = 6;
    private static final long CORAL_SALT = 0xC0A1BEEF0000L;

    public void generate(ProtoChunkAccess chunk, int seaLevel, GenerationContext ctx) {
        int baseX = chunk.coord().blockX();
        int baseZ = chunk.coord().blockZ();

        String biomeKey = getBiomeKey(chunk, baseX + 8, seaLevel - 1, baseZ + 8);
        if (!WARM_OCEAN_BIOMES.contains(biomeKey)) return;

        long seed = ctx.seed();
        long chunkSeed = NoiseMath.hash(seed ^ CORAL_SALT, chunk.coord().x(), chunk.coord().z());
        Random random = new Random(chunkSeed);

        List<int[]> reefCenters = poissonDiskSample(random, 16, 16, MIN_DISK_RADIUS, MAX_DISK_RADIUS);

        for (int[] center : reefCenters) {
            int worldX = baseX + center[0];
            int worldZ = baseZ + center[1];

            int floorY = chunk.getHeight(HeightmapType.OCEAN_FLOOR, worldX, worldZ) - 1;
            int waterDepth = seaLevel - floorY;
            if (waterDepth < MIN_WATER_DEPTH) continue;

            placeCoralCluster(chunk, random, worldX, floorY, worldZ, seaLevel);
        }
    }

    private void placeCoralCluster(ProtoChunkAccess chunk, Random random,
                                   int cx, int floorY, int cz, int seaLevel) {
        int clusterRadius = 1 + random.nextInt(3);

        for (int dx = -clusterRadius; dx <= clusterRadius; dx++) {
            for (int dz = -clusterRadius; dz <= clusterRadius; dz++) {
                if (dx * dx + dz * dz > clusterRadius * clusterRadius) continue;
                if (random.nextDouble() < 0.3) continue;

                int px = cx + dx;
                int pz = cz + dz;
                int localFloorY = chunk.getHeight(HeightmapType.OCEAN_FLOOR, px, pz) - 1;
                if (localFloorY < chunk.minY() || localFloorY >= seaLevel - 2) continue;

                String coralType = CORAL_TYPES[random.nextInt(CORAL_TYPES.length)];
                double structureRoll = random.nextDouble();

                if (structureRoll < 0.4) {
                    placeCoralColumn(chunk, random, px, localFloorY, pz, seaLevel, coralType);
                } else if (structureRoll < 0.7) {
                    placeCoralFan(chunk, px, localFloorY + 1, pz, coralType);
                } else {
                    placeCoralBlock(chunk, px, localFloorY + 1, pz, coralType);
                }

                if (random.nextDouble() < 0.15) {
                    placeSeaPickle(chunk, random, px, localFloorY + 1, pz, seaLevel);
                }
            }
        }
    }

    private void placeCoralColumn(ProtoChunkAccess chunk, Random random,
                                  int x, int floorY, int z, int seaLevel, String coralType) {
        int height = 1 + random.nextInt(Math.min(MAX_CORAL_HEIGHT, seaLevel - floorY - 2));
        StrataBlockState coralBlock = StrataBlockState.of(
                NamespacedKey.minecraft(coralType + "_coral_block"));

        for (int dy = 1; dy <= height; dy++) {
            int y = floorY + dy;
            if (y >= seaLevel) break;
            chunk.setBlock(x, y, z, coralBlock);
        }

        int topY = floorY + height;
        if (topY < seaLevel - 1) {
            placeBranchingFans(chunk, random, x, topY, z, coralType);
        }
    }

    private void placeBranchingFans(ProtoChunkAccess chunk, Random random,
                                    int x, int topY, int z, String coralType) {
        StrataBlockState fan = StrataBlockState.of(
                NamespacedKey.minecraft(coralType + "_coral_fan"));

        int[][] horizontalOffsets = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (int[] offset : horizontalOffsets) {
            if (random.nextDouble() < 0.5) {
                int fx = x + offset[0];
                int fz = z + offset[1];
                StrataBlockState existing = chunk.getBlock(fx, topY, fz);
                if (existing.equals(WATER)) {
                    chunk.setBlock(fx, topY, fz, fan);
                }
            }
        }
    }

    private void placeCoralFan(ProtoChunkAccess chunk, int x, int y, int z, String coralType) {
        if (y >= chunk.maxY()) return;
        StrataBlockState fan = StrataBlockState.of(
                NamespacedKey.minecraft(coralType + "_coral_fan"));
        if (chunk.getBlock(x, y, z).equals(WATER)) {
            chunk.setBlock(x, y, z, fan);
        }
    }

    private void placeCoralBlock(ProtoChunkAccess chunk, int x, int y, int z, String coralType) {
        if (y >= chunk.maxY()) return;
        StrataBlockState block = StrataBlockState.of(
                NamespacedKey.minecraft(coralType + "_coral_block"));
        if (chunk.getBlock(x, y, z).equals(WATER)) {
            chunk.setBlock(x, y, z, block);
        }
    }

    private void placeSeaPickle(ProtoChunkAccess chunk, Random random,
                                int x, int y, int z, int seaLevel) {
        if (y >= seaLevel || y >= chunk.maxY()) return;
        int count = 1 + random.nextInt(4);
        StrataBlockState pickle = new StrataBlockState(
                NamespacedKey.minecraft("sea_pickle"),
                java.util.Map.of("pickles", String.valueOf(count), "waterlogged", "true"));
        if (chunk.getBlock(x, y, z).equals(WATER)) {
            chunk.setBlock(x, y, z, pickle);
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
