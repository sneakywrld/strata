package com.protectcord.strata.core.pipeline.stage;

import com.protectcord.strata.api.block.StrataBlockState;
import com.protectcord.strata.api.chunk.ProtoChunkAccess;
import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.api.pipeline.GenerationContext;
import com.protectcord.strata.api.pipeline.GenerationStage;
import com.protectcord.strata.core.pipeline.PipelineStage;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Calculates block and sky lighting. Sky light propagates from Y=max down,
 * starting at 15 and decrementing by 1 per opaque block. Block light finds
 * light sources and uses BFS flood fill with attenuation.
 */
public final class LightingStage implements PipelineStage {

    private static final StrataBlockState AIR = StrataBlockState.of(NamespacedKey.minecraft("air"));
    private static final int MAX_LIGHT = 15;

    private static final StrataBlockState GLOWSTONE = StrataBlockState.of(NamespacedKey.minecraft("glowstone"));
    private static final StrataBlockState TORCH = StrataBlockState.of(NamespacedKey.minecraft("torch"));
    private static final StrataBlockState LAVA = StrataBlockState.of(NamespacedKey.minecraft("lava"));
    private static final StrataBlockState SEA_LANTERN = StrataBlockState.of(NamespacedKey.minecraft("sea_lantern"));
    private static final StrataBlockState JACK_O_LANTERN = StrataBlockState.of(NamespacedKey.minecraft("jack_o_lantern"));
    private static final StrataBlockState SHROOMLIGHT = StrataBlockState.of(NamespacedKey.minecraft("shroomlight"));

    @Override
    public GenerationStage stage() {
        return GenerationStage.LIGHTING;
    }

    @Override
    public void generate(GenerationContext context) {
        ProtoChunkAccess chunk = context.chunk();
        int baseX = chunk.coord().blockX();
        int baseZ = chunk.coord().blockZ();
        int minY = chunk.minY();
        int maxY = chunk.maxY();
        int height = maxY - minY;

        byte[] skyLight = new byte[16 * 16 * height];
        byte[] blockLight = new byte[16 * 16 * height];

        computeSkyLight(chunk, baseX, baseZ, minY, maxY, skyLight);
        computeBlockLight(chunk, baseX, baseZ, minY, maxY, blockLight);

        context.put("sky_light", skyLight);
        context.put("block_light", blockLight);
    }

    private void computeSkyLight(ProtoChunkAccess chunk, int baseX, int baseZ,
                                  int minY, int maxY, byte[] skyLight) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = baseX + x;
                int worldZ = baseZ + z;
                int light = MAX_LIGHT;

                for (int y = maxY - 1; y >= minY; y--) {
                    int idx = x + (z << 4) + ((y - minY) << 8);
                    StrataBlockState block = chunk.getBlock(worldX, y, worldZ);

                    if (isOpaque(block)) {
                        light = Math.max(0, light - 1);
                        skyLight[idx] = 0;
                    } else {
                        skyLight[idx] = (byte) light;
                    }

                    if (light <= 0) break;
                }
            }
        }
    }

    private void computeBlockLight(ProtoChunkAccess chunk, int baseX, int baseZ,
                                    int minY, int maxY, byte[] blockLight) {
        Queue<int[]> sources = new ArrayDeque<>();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = baseX + x;
                int worldZ = baseZ + z;

                for (int y = minY; y < maxY; y++) {
                    StrataBlockState block = chunk.getBlock(worldX, y, worldZ);
                    int emission = getLightEmission(block);
                    if (emission > 0) {
                        int idx = x + (z << 4) + ((y - minY) << 8);
                        blockLight[idx] = (byte) emission;
                        sources.add(new int[]{x, y - minY, z, emission});
                    }
                }
            }
        }

        int[][] offsets = {{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};
        int height = maxY - minY;

        while (!sources.isEmpty()) {
            int[] src = sources.poll();
            int sx = src[0], sy = src[1], sz = src[2], light = src[3];

            if (light <= 1) continue;

            for (int[] off : offsets) {
                int nx = sx + off[0];
                int ny = sy + off[1];
                int nz = sz + off[2];

                if (nx < 0 || nx >= 16 || nz < 0 || nz >= 16 || ny < 0 || ny >= height) continue;

                int nIdx = nx + (nz << 4) + (ny << 8);
                int newLight = light - 1;

                if (blockLight[nIdx] >= newLight) continue;

                StrataBlockState neighbor = chunk.getBlock(baseX + nx, minY + ny, baseZ + nz);
                if (isOpaque(neighbor)) continue;

                blockLight[nIdx] = (byte) newLight;
                sources.add(new int[]{nx, ny, nz, newLight});
            }
        }
    }

    private static boolean isOpaque(StrataBlockState block) {
        return !block.equals(AIR)
                && !block.blockId().key().equals("water")
                && !block.blockId().key().equals("glass")
                && !block.blockId().key().equals("ice");
    }

    private static int getLightEmission(StrataBlockState block) {
        if (block.equals(GLOWSTONE) || block.equals(SEA_LANTERN)
                || block.equals(JACK_O_LANTERN) || block.equals(SHROOMLIGHT)) return 15;
        if (block.equals(LAVA)) return 15;
        if (block.equals(TORCH)) return 14;
        return 0;
    }
}
