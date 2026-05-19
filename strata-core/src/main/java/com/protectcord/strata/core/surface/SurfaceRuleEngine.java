package com.protectcord.strata.core.surface;

import com.protectcord.strata.api.biome.Biome;
import com.protectcord.strata.api.block.StrataBlockState;
import com.protectcord.strata.api.chunk.HeightmapType;
import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.api.pipeline.GenerationContext;
import com.protectcord.strata.api.surface.SurfaceCondition;
import com.protectcord.strata.api.surface.SurfaceRule;
import com.protectcord.strata.core.chunk.StrataProtoChunk;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Evaluates an ordered list of surface rules. For each Y from surface down
 * to the depth limit, tests rule conditions in order and places the first
 * matching block.
 */
public final class SurfaceRuleEngine {

    private static final StrataBlockState STONE = StrataBlockState.of(NamespacedKey.minecraft("stone"));

    private static final int MAX_SURFACE_DEPTH = 8;

    private final List<SurfaceRule> rules;

    public SurfaceRuleEngine(List<SurfaceRule> rules) {
        this.rules = rules.stream()
                .sorted(Comparator.comparingInt(SurfaceRule::priority))
                .toList();
    }

    public void apply(StrataProtoChunk chunk, int x, int z, String biomeKey,
                       GenerationContext ctx) {
        int worldX = chunk.coord().blockX() + (x & 15);
        int worldZ = chunk.coord().blockZ() + (z & 15);

        int surfaceY = chunk.getHeight(HeightmapType.WORLD_SURFACE, worldX, worldZ) - 1;
        if (surfaceY < chunk.minY()) return;

        Biome biome = chunk.getBiome(worldX, surfaceY, worldZ);
        if (biome == null) return;

        int seaLevel = ctx.seaLevel();

        int[] heightmap = new int[16 * 16];
        for (int hx = 0; hx < 16; hx++) {
            for (int hz = 0; hz < 16; hz++) {
                heightmap[hx + hz * 16] = chunk.getHeight(HeightmapType.WORLD_SURFACE,
                        chunk.coord().blockX() + hx, chunk.coord().blockZ() + hz);
            }
        }
        double slope = SlopeSampler.getSlopeAt(x & 15, z & 15, heightmap, 16);

        for (int depth = 0; depth < MAX_SURFACE_DEPTH; depth++) {
            int y = surfaceY - depth;
            if (y < chunk.minY()) break;

            StrataBlockState current = chunk.getBlock(worldX, y, worldZ);
            if (!current.equals(STONE)) continue;

            boolean underwater = y < seaLevel;
            int waterDepth = underwater ? seaLevel - y : 0;

            SurfaceCondition.SurfaceContext surfCtx = new SurfaceCondition.SurfaceContext(
                    worldX, y, worldZ,
                    surfaceY,
                    depth,
                    slope,
                    biome,
                    underwater,
                    waterDepth,
                    ctx.seed()
            );

            for (SurfaceRule rule : rules) {
                Optional<StrataBlockState> result = rule.apply(surfCtx);
                if (result.isPresent()) {
                    chunk.setBlock(worldX, y, worldZ, result.get());
                    break;
                }
            }
        }
    }
}
