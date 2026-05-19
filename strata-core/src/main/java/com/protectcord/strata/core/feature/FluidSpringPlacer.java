package com.protectcord.strata.core.feature;

import com.protectcord.strata.api.block.StrataBlockState;
import com.protectcord.strata.api.chunk.BlockAccess;
import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.api.feature.Feature;
import com.protectcord.strata.api.feature.FeaturePlacement;
import com.protectcord.strata.api.feature.FeatureType;

import java.util.Random;
import java.util.Set;

public final class FluidSpringPlacer implements Feature {

    private static final StrataBlockState AIR = StrataBlockState.of(NamespacedKey.minecraft("air"));
    private static final StrataBlockState WATER = StrataBlockState.of(NamespacedKey.minecraft("water"));
    private static final StrataBlockState LAVA = StrataBlockState.of(NamespacedKey.minecraft("lava"));

    private static final Set<String> STONE_TYPES = Set.of(
            "minecraft:stone", "minecraft:deepslate", "minecraft:granite",
            "minecraft:diorite", "minecraft:andesite", "minecraft:tuff",
            "minecraft:netherrack"
    );

    private static final int[][] NEIGHBOR_OFFSETS = {
            {1, 0, 0}, {-1, 0, 0},
            {0, 1, 0}, {0, -1, 0},
            {0, 0, 1}, {0, 0, -1}
    };

    private final NamespacedKey key;
    private final FeaturePlacement placement;
    private final float probability;

    public FluidSpringPlacer(NamespacedKey key, FeaturePlacement placement, float probability) {
        this.key = key;
        this.placement = placement;
        this.probability = probability;
    }

    @Override
    public NamespacedKey key() { return key; }

    @Override
    public FeatureType type() { return FeatureType.FLUID_SPRING; }

    @Override
    public FeaturePlacement placement() { return placement; }

    @Override
    public boolean place(BlockAccess blocks, Random random, int x, int y, int z) {
        if (y < blocks.minY() || y >= blocks.maxY()) return false;
        if (random.nextFloat() >= probability) return false;

        StrataBlockState current = blocks.getBlock(x, y, z);
        if (!isStone(current)) return false;

        int airNeighbors = 0;
        int stoneNeighbors = 0;
        for (int[] offset : NEIGHBOR_OFFSETS) {
            int nx = x + offset[0];
            int ny = y + offset[1];
            int nz = z + offset[2];
            if (ny < blocks.minY() || ny >= blocks.maxY()) {
                stoneNeighbors++;
                continue;
            }
            StrataBlockState neighbor = blocks.getBlock(nx, ny, nz);
            if (neighbor.equals(AIR)) {
                airNeighbors++;
            } else if (isStone(neighbor)) {
                stoneNeighbors++;
            }
        }

        if (airNeighbors != 1 || stoneNeighbors < 4) return false;

        StrataBlockState fluid = y >= 0 ? WATER : LAVA;
        blocks.setBlock(x, y, z, fluid);
        return true;
    }

    private boolean isStone(StrataBlockState state) {
        return STONE_TYPES.contains(state.blockId().toString());
    }
}
