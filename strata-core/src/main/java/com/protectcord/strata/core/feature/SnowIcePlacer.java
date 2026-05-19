package com.protectcord.strata.core.feature;

import com.protectcord.strata.api.block.StrataBlockState;
import com.protectcord.strata.api.chunk.BlockAccess;
import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.api.feature.Feature;
import com.protectcord.strata.api.feature.FeaturePlacement;
import com.protectcord.strata.api.feature.FeatureType;

import java.util.Random;
import java.util.Set;

public final class SnowIcePlacer implements Feature {

    private static final StrataBlockState AIR = StrataBlockState.of(NamespacedKey.minecraft("air"));
    private static final StrataBlockState SNOW_LAYER = StrataBlockState.of(NamespacedKey.minecraft("snow"));
    private static final StrataBlockState ICE = StrataBlockState.of(NamespacedKey.minecraft("ice"));
    private static final String WATER_ID = "minecraft:water";

    private static final Set<String> NON_SNOWABLE = Set.of(
            "minecraft:ice", "minecraft:packed_ice", "minecraft:blue_ice",
            "minecraft:snow", "minecraft:snow_block", "minecraft:air",
            "minecraft:cave_air", "minecraft:water", "minecraft:lava"
    );

    private final NamespacedKey key;
    private final double temperature;
    private final FeaturePlacement placement;

    public SnowIcePlacer(NamespacedKey key, double temperature, FeaturePlacement placement) {
        this.key = key;
        this.temperature = temperature;
        this.placement = placement;
    }

    @Override
    public NamespacedKey key() { return key; }

    @Override
    public FeatureType type() { return FeatureType.SNOW_ICE; }

    @Override
    public FeaturePlacement placement() { return placement; }

    @Override
    public boolean place(BlockAccess blocks, Random random, int x, int y, int z) {
        if (y < blocks.minY() || y >= blocks.maxY()) return false;

        boolean placed = false;

        if (temperature < 0.15) {
            StrataBlockState surface = blocks.getBlock(x, y, z);
            if (!NON_SNOWABLE.contains(surface.blockId().toString())) {
                int aboveY = y + 1;
                if (aboveY < blocks.maxY() && blocks.getBlock(x, aboveY, z).equals(AIR)) {
                    blocks.setBlock(x, aboveY, z, SNOW_LAYER);
                    placed = true;
                }
            }
        }

        if (temperature < 0.0) {
            StrataBlockState block = blocks.getBlock(x, y, z);
            if (block.blockId().toString().equals(WATER_ID)) {
                int aboveY = y + 1;
                boolean exposed = aboveY >= blocks.maxY()
                        || blocks.getBlock(x, aboveY, z).equals(AIR)
                        || blocks.getBlock(x, aboveY, z).equals(SNOW_LAYER);
                if (exposed) {
                    blocks.setBlock(x, y, z, ICE);
                    placed = true;
                }
            }
        }

        return placed;
    }
}
