package com.protectcord.strata.core.feature;

import com.protectcord.strata.api.block.StrataBlockState;
import com.protectcord.strata.api.chunk.BlockAccess;
import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.api.feature.Feature;
import com.protectcord.strata.api.feature.FeaturePlacement;
import com.protectcord.strata.api.feature.FeatureType;

import java.util.Random;
import java.util.Set;

public final class OreGenerator implements Feature {

    private static final StrataBlockState AIR = StrataBlockState.of(NamespacedKey.minecraft("air"));
    private static final Set<String> REPLACEABLE = Set.of(
            "minecraft:stone", "minecraft:deepslate", "minecraft:granite",
            "minecraft:diorite", "minecraft:andesite", "minecraft:tuff"
    );

    private final NamespacedKey key;
    private final StrataBlockState oreBlock;
    private final int size;
    private final float discardOnAirExposure;
    private final FeaturePlacement placement;

    public OreGenerator(NamespacedKey key, StrataBlockState oreBlock, int size,
                        float discardOnAirExposure, FeaturePlacement placement) {
        this.key = key;
        this.oreBlock = oreBlock;
        this.size = size;
        this.discardOnAirExposure = discardOnAirExposure;
        this.placement = placement;
    }

    @Override
    public NamespacedKey key() { return key; }

    @Override
    public FeatureType type() { return FeatureType.ORE; }

    @Override
    public FeaturePlacement placement() { return placement; }

    @Override
    public boolean place(BlockAccess blocks, Random random, int x, int y, int z) {
        if (y < blocks.minY() || y >= blocks.maxY()) return false;

        int placed = 0;
        int cx = x;
        int cy = y;
        int cz = z;

        for (int i = 0; i < size; i++) {
            if (cy < blocks.minY() || cy >= blocks.maxY()) {
                int dir = random.nextInt(6);
                cx += DIRS[dir][0];
                cy += DIRS[dir][1];
                cz += DIRS[dir][2];
                continue;
            }

            StrataBlockState existing = blocks.getBlock(cx, cy, cz);
            if (!isReplaceable(existing)) {
                int dir = random.nextInt(6);
                cx += DIRS[dir][0];
                cy += DIRS[dir][1];
                cz += DIRS[dir][2];
                continue;
            }

            if (discardOnAirExposure > 0.0f && isExposedToAir(blocks, cx, cy, cz)) {
                if (random.nextFloat() < discardOnAirExposure) {
                    int dir = random.nextInt(6);
                    cx += DIRS[dir][0];
                    cy += DIRS[dir][1];
                    cz += DIRS[dir][2];
                    continue;
                }
            }

            blocks.setBlock(cx, cy, cz, oreBlock);
            placed++;

            int dir = random.nextInt(6);
            cx += DIRS[dir][0];
            cy += DIRS[dir][1];
            cz += DIRS[dir][2];
        }

        return placed > 0;
    }

    private static final int[][] DIRS = {
            {1, 0, 0}, {-1, 0, 0},
            {0, 1, 0}, {0, -1, 0},
            {0, 0, 1}, {0, 0, -1}
    };

    private boolean isReplaceable(StrataBlockState state) {
        return REPLACEABLE.contains(state.blockId().toString());
    }

    private boolean isExposedToAir(BlockAccess blocks, int x, int y, int z) {
        for (int[] dir : DIRS) {
            int nx = x + dir[0];
            int ny = y + dir[1];
            int nz = z + dir[2];
            if (ny < blocks.minY() || ny >= blocks.maxY()) continue;
            if (blocks.getBlock(nx, ny, nz).equals(AIR)) return true;
        }
        return false;
    }
}
