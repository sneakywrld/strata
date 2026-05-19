package com.protectcord.strata.core.structure;

import com.protectcord.strata.api.block.StrataBlockState;
import com.protectcord.strata.api.chunk.BlockAccess;
import com.protectcord.strata.api.chunk.HeightmapType;
import com.protectcord.strata.api.chunk.ProtoChunkAccess;
import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.api.pipeline.GenerationContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Enhanced village generation using JigsawAssembler. Produces villages 2-3x
 * larger than vanilla with biome-appropriate building styles, terrain flattening
 * under footprints, and interconnected road networks.
 */
public final class VillageGenerator {

    public enum BiomeStyle {
        PLAINS("oak", "cobblestone", "grass_block"),
        DESERT("sandstone", "smooth_sandstone", "sand"),
        TAIGA("spruce", "cobblestone", "podzol"),
        SAVANNA("acacia", "cobblestone", "grass_block"),
        SNOWY("spruce", "packed_ice", "snow_block");

        private final String woodPrefix;
        private final String pathBlock;
        private final String groundBlock;

        BiomeStyle(String woodPrefix, String pathBlock, String groundBlock) {
            this.woodPrefix = woodPrefix;
            this.pathBlock = pathBlock;
            this.groundBlock = groundBlock;
        }

        public String woodPrefix() { return woodPrefix; }
        public String pathBlock() { return pathBlock; }
        public String groundBlock() { return groundBlock; }
    }

    private static final StrataBlockState AIR = StrataBlockState.of(NamespacedKey.minecraft("air"));
    private static final StrataBlockState DIRT = StrataBlockState.of(NamespacedKey.minecraft("dirt"));
    private static final StrataBlockState TORCH = StrataBlockState.of(NamespacedKey.minecraft("torch"));

    private static final int ROAD_WIDTH = 3;
    private static final int MIN_PIECES = 20;
    private static final int MAX_PIECES = 40;
    private static final int MAX_DEPTH = 7;

    private static final Map<String, BiomeStyle> BIOME_STYLES = new HashMap<>();

    static {
        BIOME_STYLES.put("minecraft:plains", BiomeStyle.PLAINS);
        BIOME_STYLES.put("minecraft:sunflower_plains", BiomeStyle.PLAINS);
        BIOME_STYLES.put("minecraft:meadow", BiomeStyle.PLAINS);
        BIOME_STYLES.put("minecraft:desert", BiomeStyle.DESERT);
        BIOME_STYLES.put("minecraft:taiga", BiomeStyle.TAIGA);
        BIOME_STYLES.put("minecraft:old_growth_spruce_taiga", BiomeStyle.TAIGA);
        BIOME_STYLES.put("minecraft:savanna", BiomeStyle.SAVANNA);
        BIOME_STYLES.put("minecraft:savanna_plateau", BiomeStyle.SAVANNA);
        BIOME_STYLES.put("minecraft:snowy_plains", BiomeStyle.SNOWY);
        BIOME_STYLES.put("minecraft:snowy_taiga", BiomeStyle.SNOWY);
    }

    private final JigsawAssembler assembler = new JigsawAssembler();

    public List<JigsawAssembler.PlacedPiece> generate(
            StructurePlacementEngine.StructureStart start,
            ProtoChunkAccess chunk, GenerationContext ctx) {

        long seed = ctx.seed();
        Random random = new Random(seed ^ ((long) start.chunkX() << 32 | start.chunkZ()));

        BiomeStyle style = resolveStyle(chunk, start, ctx.seaLevel());
        int pieceCount = MIN_PIECES + random.nextInt(MAX_PIECES - MIN_PIECES + 1);

        List<JigsawAssembler.PlacedPiece> pieces = assembler.assemble(start, MAX_DEPTH, pieceCount, random);

        flattenTerrain(chunk, pieces, style);
        buildRoads(chunk, pieces, style);

        return pieces;
    }

    private BiomeStyle resolveStyle(ProtoChunkAccess chunk,
                                    StructurePlacementEngine.StructureStart start,
                                    int seaLevel) {
        int cx = start.chunkX() << 4;
        int cz = start.chunkZ() << 4;
        var biome = chunk.getBiome(cx + 8, seaLevel, cz + 8);
        if (biome == null) return BiomeStyle.PLAINS;

        String biomeKey = biome.key().toString();
        return BIOME_STYLES.getOrDefault(biomeKey, BiomeStyle.PLAINS);
    }

    private void flattenTerrain(ProtoChunkAccess chunk,
                                List<JigsawAssembler.PlacedPiece> pieces,
                                BiomeStyle style) {
        StrataBlockState ground = StrataBlockState.of(NamespacedKey.minecraft(style.groundBlock()));
        int chunkMinX = chunk.coord().blockX();
        int chunkMinZ = chunk.coord().blockZ();
        int chunkMaxX = chunkMinX + 15;
        int chunkMaxZ = chunkMinZ + 15;

        for (JigsawAssembler.PlacedPiece piece : pieces) {
            int pieceMinX = piece.x() - 4;
            int pieceMaxX = piece.x() + 4;
            int pieceMinZ = piece.z() - 4;
            int pieceMaxZ = piece.z() + 4;
            int targetY = piece.y();

            int startX = Math.max(pieceMinX, chunkMinX);
            int endX = Math.min(pieceMaxX, chunkMaxX);
            int startZ = Math.max(pieceMinZ, chunkMinZ);
            int endZ = Math.min(pieceMaxZ, chunkMaxZ);

            for (int x = startX; x <= endX; x++) {
                for (int z = startZ; z <= endZ; z++) {
                    int surfaceY = chunk.getHeight(HeightmapType.WORLD_SURFACE, x, z) - 1;

                    if (surfaceY > targetY) {
                        for (int y = surfaceY; y > targetY; y--) {
                            chunk.setBlock(x, y, z, AIR);
                        }
                        chunk.setBlock(x, targetY, z, ground);
                    } else if (surfaceY < targetY) {
                        for (int y = surfaceY + 1; y <= targetY; y++) {
                            chunk.setBlock(x, y, z, y == targetY ? ground : DIRT);
                        }
                    }
                }
            }
        }
    }

    private void buildRoads(ProtoChunkAccess chunk,
                            List<JigsawAssembler.PlacedPiece> pieces,
                            BiomeStyle style) {
        if (pieces.size() < 2) return;

        StrataBlockState pathBlock = StrataBlockState.of(NamespacedKey.minecraft(style.pathBlock()));
        int chunkMinX = chunk.coord().blockX();
        int chunkMinZ = chunk.coord().blockZ();

        for (int i = 0; i < pieces.size() - 1; i++) {
            JigsawAssembler.PlacedPiece from = pieces.get(i);
            JigsawAssembler.PlacedPiece to = pieces.get(i + 1);

            List<int[]> roadPath = computeRoadPath(from.x(), from.z(), to.x(), to.z());

            for (int[] pos : roadPath) {
                for (int dx = -(ROAD_WIDTH / 2); dx <= ROAD_WIDTH / 2; dx++) {
                    for (int dz = -(ROAD_WIDTH / 2); dz <= ROAD_WIDTH / 2; dz++) {
                        int rx = pos[0] + dx;
                        int rz = pos[1] + dz;

                        int localX = rx - chunkMinX;
                        int localZ = rz - chunkMinZ;
                        if (localX < 0 || localX > 15 || localZ < 0 || localZ > 15) continue;

                        int surfaceY = chunk.getHeight(HeightmapType.WORLD_SURFACE, rx, rz) - 1;
                        if (surfaceY < chunk.minY() || surfaceY >= chunk.maxY()) continue;

                        chunk.setBlock(rx, surfaceY, rz, pathBlock);

                        for (int cy = surfaceY + 1; cy <= surfaceY + 3 && cy < chunk.maxY(); cy++) {
                            StrataBlockState above = chunk.getBlock(rx, cy, rz);
                            if (!above.equals(AIR)) {
                                chunk.setBlock(rx, cy, rz, AIR);
                            }
                        }
                    }
                }
            }
        }
    }

    private List<int[]> computeRoadPath(int x1, int z1, int x2, int z2) {
        List<int[]> path = new ArrayList<>();
        int dx = Math.abs(x2 - x1);
        int dz = Math.abs(z2 - z1);
        int sx = x1 < x2 ? 1 : -1;
        int sz = z1 < z2 ? 1 : -1;
        int err = dx - dz;

        int cx = x1;
        int cz = z1;

        while (true) {
            path.add(new int[]{cx, cz});
            if (cx == x2 && cz == z2) break;

            int e2 = 2 * err;
            if (e2 > -dz) {
                err -= dz;
                cx += sx;
            }
            if (e2 < dx) {
                err += dx;
                cz += sz;
            }
        }

        return path;
    }
}
