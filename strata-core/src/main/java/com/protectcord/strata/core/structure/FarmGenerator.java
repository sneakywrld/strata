package com.protectcord.strata.core.structure;

import com.protectcord.strata.api.block.StrataBlockState;
import com.protectcord.strata.api.chunk.BlockAccess;
import com.protectcord.strata.api.chunk.HeightmapType;
import com.protectcord.strata.api.chunk.ProtoChunkAccess;
import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.noise.math.NoiseMath;

import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Standalone farm placement for village and rural areas. Creates fenced crop fields
 * (16x16 to 24x24) with irrigated rows, biome-appropriate crops, and optional
 * animal pens.
 */
public final class FarmGenerator {

    private static final StrataBlockState AIR = StrataBlockState.of(NamespacedKey.minecraft("air"));
    private static final StrataBlockState FARMLAND = StrataBlockState.of(NamespacedKey.minecraft("farmland"));
    private static final StrataBlockState WATER = StrataBlockState.of(NamespacedKey.minecraft("water"));
    private static final StrataBlockState DIRT = StrataBlockState.of(NamespacedKey.minecraft("dirt"));
    private static final StrataBlockState GRASS_BLOCK = StrataBlockState.of(NamespacedKey.minecraft("grass_block"));
    private static final StrataBlockState OAK_FENCE = StrataBlockState.of(NamespacedKey.minecraft("oak_fence"));
    private static final StrataBlockState OAK_FENCE_GATE = StrataBlockState.of(NamespacedKey.minecraft("oak_fence_gate"));
    private static final StrataBlockState COMPOSTER = StrataBlockState.of(NamespacedKey.minecraft("composter"));
    private static final StrataBlockState HAY_BLOCK = StrataBlockState.of(NamespacedKey.minecraft("hay_block"));

    private static final int MIN_SIZE = 16;
    private static final int MAX_SIZE = 24;
    private static final int IRRIGATION_INTERVAL = 4;
    private static final int PEN_SIZE = 6;
    private static final long FARM_SALT = 0xFA2ACAFE00L;

    private static final Map<String, String[]> BIOME_CROPS = Map.of(
            "temperate", new String[]{"wheat", "carrots", "potatoes", "beetroots"},
            "desert", new String[]{"cactus", "dead_bush"},
            "cold", new String[]{"potatoes", "wheat"},
            "tropical", new String[]{"melon_stem", "pumpkin_stem", "sugar_cane"}
    );

    private static final Set<String> DESERT_BIOMES = Set.of(
            "minecraft:desert", "minecraft:badlands", "minecraft:eroded_badlands"
    );
    private static final Set<String> COLD_BIOMES = Set.of(
            "minecraft:snowy_plains", "minecraft:snowy_taiga", "minecraft:frozen_river"
    );
    private static final Set<String> TROPICAL_BIOMES = Set.of(
            "minecraft:jungle", "minecraft:bamboo_jungle", "minecraft:sparse_jungle"
    );

    public boolean generate(ProtoChunkAccess chunk, Random random, int originX, int originY, int originZ,
                            String biomeKey) {
        int farmWidth = MIN_SIZE + random.nextInt(MAX_SIZE - MIN_SIZE + 1);
        int farmDepth = MIN_SIZE + random.nextInt(MAX_SIZE - MIN_SIZE + 1);

        int chunkMinX = chunk.coord().blockX();
        int chunkMinZ = chunk.coord().blockZ();

        String climate = classifyClimate(biomeKey);
        String[] crops = BIOME_CROPS.getOrDefault(climate, BIOME_CROPS.get("temperate"));
        boolean isDesert = "desert".equals(climate);

        flattenArea(chunk, originX, originY, originZ, farmWidth, farmDepth, chunkMinX, chunkMinZ);
        buildFence(chunk, originX, originY, originZ, farmWidth, farmDepth, chunkMinX, chunkMinZ);
        buildCropRows(chunk, random, originX, originY, originZ, farmWidth, farmDepth,
                chunkMinX, chunkMinZ, crops, isDesert);

        if (random.nextDouble() < 0.6) {
            int penX = originX + farmWidth + 2;
            int penZ = originZ + (farmDepth - PEN_SIZE) / 2;
            buildAnimalPen(chunk, penX, originY, penZ, chunkMinX, chunkMinZ);
        }

        placeDecorations(chunk, random, originX, originY, originZ, farmWidth, farmDepth,
                chunkMinX, chunkMinZ);

        return true;
    }

    private void flattenArea(ProtoChunkAccess chunk, int ox, int oy, int oz,
                             int width, int depth, int chunkMinX, int chunkMinZ) {
        for (int dx = -1; dx <= width; dx++) {
            for (int dz = -1; dz <= depth; dz++) {
                int wx = ox + dx;
                int wz = oz + dz;
                if (!isInChunk(wx, wz, chunkMinX, chunkMinZ)) continue;

                int surfaceY = chunk.getHeight(HeightmapType.WORLD_SURFACE, wx, wz) - 1;
                if (surfaceY > oy) {
                    for (int y = surfaceY; y > oy; y--) {
                        chunk.setBlock(wx, y, wz, AIR);
                    }
                } else if (surfaceY < oy) {
                    for (int y = surfaceY + 1; y <= oy; y++) {
                        chunk.setBlock(wx, y, wz, DIRT);
                    }
                }
                chunk.setBlock(wx, oy, wz, DIRT);
            }
        }
    }

    private void buildFence(ProtoChunkAccess chunk, int ox, int oy, int oz,
                            int width, int depth, int chunkMinX, int chunkMinZ) {
        int fenceY = oy + 1;

        for (int dx = -1; dx <= width; dx++) {
            placeFenceAt(chunk, ox + dx, fenceY, oz - 1, chunkMinX, chunkMinZ);
            placeFenceAt(chunk, ox + dx, fenceY, oz + depth, chunkMinX, chunkMinZ);
        }
        for (int dz = 0; dz < depth; dz++) {
            placeFenceAt(chunk, ox - 1, fenceY, oz + dz, chunkMinX, chunkMinZ);
            placeFenceAt(chunk, ox + width, fenceY, oz + dz, chunkMinX, chunkMinZ);
        }

        int gateX = ox + width / 2;
        int gateZ = oz - 1;
        if (isInChunk(gateX, gateZ, chunkMinX, chunkMinZ)) {
            chunk.setBlock(gateX, fenceY, gateZ, OAK_FENCE_GATE);
        }
    }

    private void placeFenceAt(ProtoChunkAccess chunk, int x, int y, int z,
                              int chunkMinX, int chunkMinZ) {
        if (isInChunk(x, z, chunkMinX, chunkMinZ) && y < chunk.maxY()) {
            chunk.setBlock(x, y, z, OAK_FENCE);
        }
    }

    private void buildCropRows(ProtoChunkAccess chunk, Random random,
                               int ox, int oy, int oz, int width, int depth,
                               int chunkMinX, int chunkMinZ, String[] crops, boolean isDesert) {
        for (int dx = 0; dx < width; dx++) {
            for (int dz = 0; dz < depth; dz++) {
                int wx = ox + dx;
                int wz = oz + dz;
                if (!isInChunk(wx, wz, chunkMinX, chunkMinZ)) continue;

                if (dx % IRRIGATION_INTERVAL == 0 && !isDesert) {
                    chunk.setBlock(wx, oy, wz, WATER);
                } else {
                    if (isDesert && "cactus".equals(crops[0])) {
                        chunk.setBlock(wx, oy, wz, StrataBlockState.of(NamespacedKey.minecraft("sand")));
                        if (dz % 2 == 0 && dx % 2 == 0) {
                            int cactusHeight = 1 + random.nextInt(3);
                            for (int cy = 1; cy <= cactusHeight; cy++) {
                                chunk.setBlock(wx, oy + cy, wz,
                                        StrataBlockState.of(NamespacedKey.minecraft("cactus")));
                            }
                        }
                    } else {
                        chunk.setBlock(wx, oy, wz, FARMLAND);
                        String crop = crops[random.nextInt(crops.length)];
                        int maxAge = getCropMaxAge(crop);
                        int age = random.nextInt(maxAge + 1);
                        StrataBlockState cropState = new StrataBlockState(
                                NamespacedKey.minecraft(crop),
                                Map.of("age", String.valueOf(age)));
                        chunk.setBlock(wx, oy + 1, wz, cropState);
                    }
                }
            }
        }
    }

    private void buildAnimalPen(ProtoChunkAccess chunk, int ox, int oy, int oz,
                                int chunkMinX, int chunkMinZ) {
        for (int dx = 0; dx < PEN_SIZE; dx++) {
            for (int dz = 0; dz < PEN_SIZE; dz++) {
                int wx = ox + dx;
                int wz = oz + dz;
                if (!isInChunk(wx, wz, chunkMinX, chunkMinZ)) continue;

                chunk.setBlock(wx, oy, wz, GRASS_BLOCK);
                chunk.setBlock(wx, oy + 1, wz, AIR);

                boolean isBorder = dx == 0 || dx == PEN_SIZE - 1 || dz == 0 || dz == PEN_SIZE - 1;
                if (isBorder) {
                    if (dx == PEN_SIZE / 2 && dz == 0) {
                        chunk.setBlock(wx, oy + 1, wz, OAK_FENCE_GATE);
                    } else {
                        chunk.setBlock(wx, oy + 1, wz, OAK_FENCE);
                    }
                }
            }
        }
    }

    private void placeDecorations(ProtoChunkAccess chunk, Random random,
                                  int ox, int oy, int oz, int width, int depth,
                                  int chunkMinX, int chunkMinZ) {
        int compX = ox;
        int compZ = oz;
        if (isInChunk(compX, compZ, chunkMinX, chunkMinZ)) {
            chunk.setBlock(compX, oy + 1, compZ, COMPOSTER);
        }

        if (random.nextDouble() < 0.5) {
            int hayX = ox + width - 1;
            int hayZ = oz + depth - 1;
            if (isInChunk(hayX, hayZ, chunkMinX, chunkMinZ)) {
                int hayHeight = 1 + random.nextInt(3);
                for (int h = 0; h < hayHeight; h++) {
                    chunk.setBlock(hayX, oy + 1 + h, hayZ, HAY_BLOCK);
                }
            }
        }
    }

    private static String classifyClimate(String biomeKey) {
        if (DESERT_BIOMES.contains(biomeKey)) return "desert";
        if (COLD_BIOMES.contains(biomeKey)) return "cold";
        if (TROPICAL_BIOMES.contains(biomeKey)) return "tropical";
        return "temperate";
    }

    private static int getCropMaxAge(String crop) {
        return switch (crop) {
            case "wheat", "carrots", "potatoes" -> 7;
            case "beetroots" -> 3;
            case "melon_stem", "pumpkin_stem" -> 7;
            default -> 0;
        };
    }

    private static boolean isInChunk(int x, int z, int chunkMinX, int chunkMinZ) {
        int localX = x - chunkMinX;
        int localZ = z - chunkMinZ;
        return localX >= 0 && localX <= 15 && localZ >= 0 && localZ <= 15;
    }
}
