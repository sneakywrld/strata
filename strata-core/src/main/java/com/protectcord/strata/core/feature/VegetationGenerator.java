package com.protectcord.strata.core.feature;

import com.protectcord.strata.api.block.StrataBlockState;
import com.protectcord.strata.api.chunk.BlockAccess;
import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.api.feature.Feature;
import com.protectcord.strata.api.feature.FeaturePlacement;
import com.protectcord.strata.api.feature.FeatureType;

import java.util.List;
import java.util.Random;
import java.util.Set;

public final class VegetationGenerator implements Feature {

    private static final StrataBlockState AIR = StrataBlockState.of(NamespacedKey.minecraft("air"));

    private static final Set<String> GRASS_PLACEABLE = Set.of(
            "minecraft:grass_block", "minecraft:dirt", "minecraft:podzol",
            "minecraft:rooted_dirt", "minecraft:mycelium", "minecraft:moss_block"
    );
    private static final Set<String> SAND_BLOCKS = Set.of(
            "minecraft:sand", "minecraft:red_sand"
    );
    private static final String WATER = "minecraft:water";

    private static final List<StrataBlockState> FLOWERS = List.of(
            StrataBlockState.of(NamespacedKey.minecraft("poppy")),
            StrataBlockState.of(NamespacedKey.minecraft("dandelion")),
            StrataBlockState.of(NamespacedKey.minecraft("cornflower")),
            StrataBlockState.of(NamespacedKey.minecraft("azure_bluet")),
            StrataBlockState.of(NamespacedKey.minecraft("oxeye_daisy")),
            StrataBlockState.of(NamespacedKey.minecraft("allium")),
            StrataBlockState.of(NamespacedKey.minecraft("lily_of_the_valley")),
            StrataBlockState.of(NamespacedKey.minecraft("blue_orchid"))
    );

    public enum VegetationType {
        TALL_GRASS, FERN, FLOWER, DEAD_BUSH, CACTUS, SUGAR_CANE, BAMBOO, BERRY_BUSH
    }

    private final NamespacedKey key;
    private final VegetationType vegType;
    private final FeaturePlacement placement;

    public VegetationGenerator(NamespacedKey key, VegetationType vegType, FeaturePlacement placement) {
        this.key = key;
        this.vegType = vegType;
        this.placement = placement;
    }

    @Override
    public NamespacedKey key() { return key; }

    @Override
    public FeatureType type() { return FeatureType.VEGETATION; }

    @Override
    public FeaturePlacement placement() { return placement; }

    @Override
    public boolean place(BlockAccess blocks, Random random, int x, int y, int z) {
        return switch (vegType) {
            case TALL_GRASS -> placeSinglePlant(blocks, x, y, z,
                    StrataBlockState.of(NamespacedKey.minecraft("short_grass")), GRASS_PLACEABLE);
            case FERN -> placeSinglePlant(blocks, x, y, z,
                    StrataBlockState.of(NamespacedKey.minecraft("fern")), GRASS_PLACEABLE);
            case FLOWER -> placeSinglePlant(blocks, x, y, z,
                    FLOWERS.get(random.nextInt(FLOWERS.size())), GRASS_PLACEABLE);
            case DEAD_BUSH -> placeSinglePlant(blocks, x, y, z,
                    StrataBlockState.of(NamespacedKey.minecraft("dead_bush")), SAND_BLOCKS);
            case CACTUS -> placeCactus(blocks, random, x, y, z);
            case SUGAR_CANE -> placeSugarCane(blocks, random, x, y, z);
            case BAMBOO -> placeBamboo(blocks, random, x, y, z);
            case BERRY_BUSH -> placeSinglePlant(blocks, x, y, z,
                    StrataBlockState.of(NamespacedKey.minecraft("sweet_berry_bush")), GRASS_PLACEABLE);
        };
    }

    private boolean placeSinglePlant(BlockAccess blocks, int x, int y, int z,
                                     StrataBlockState plant, Set<String> validGround) {
        if (y < blocks.minY() || y >= blocks.maxY()) return false;
        if (!blocks.getBlock(x, y, z).equals(AIR)) return false;
        int groundY = y - 1;
        if (groundY < blocks.minY()) return false;
        if (!validGround.contains(blocks.getBlock(x, groundY, z).blockId().toString())) return false;
        blocks.setBlock(x, y, z, plant);
        return true;
    }

    private boolean placeCactus(BlockAccess blocks, Random random, int x, int y, int z) {
        if (y < blocks.minY() || y >= blocks.maxY()) return false;
        int groundY = y - 1;
        if (groundY < blocks.minY()) return false;
        if (!SAND_BLOCKS.contains(blocks.getBlock(x, groundY, z).blockId().toString())) return false;

        if (!isAdjacentClear(blocks, x, y, z)) return false;

        int height = 2 + random.nextInt(2);
        StrataBlockState cactus = StrataBlockState.of(NamespacedKey.minecraft("cactus"));
        int placed = 0;
        for (int dy = 0; dy < height; dy++) {
            int py = y + dy;
            if (py >= blocks.maxY()) break;
            if (!blocks.getBlock(x, py, z).equals(AIR)) break;
            if (dy > 0 && !isAdjacentClear(blocks, x, py, z)) break;
            blocks.setBlock(x, py, z, cactus);
            placed++;
        }
        return placed > 0;
    }

    private boolean placeSugarCane(BlockAccess blocks, Random random, int x, int y, int z) {
        if (y < blocks.minY() || y >= blocks.maxY()) return false;
        int groundY = y - 1;
        if (groundY < blocks.minY()) return false;

        String groundId = blocks.getBlock(x, groundY, z).blockId().toString();
        if (!GRASS_PLACEABLE.contains(groundId) && !SAND_BLOCKS.contains(groundId)) return false;
        if (!hasAdjacentWater(blocks, x, groundY, z)) return false;

        int height = 2 + random.nextInt(3);
        StrataBlockState sugarCane = StrataBlockState.of(NamespacedKey.minecraft("sugar_cane"));
        int placed = 0;
        for (int dy = 0; dy < height; dy++) {
            int py = y + dy;
            if (py >= blocks.maxY()) break;
            if (!blocks.getBlock(x, py, z).equals(AIR)) break;
            blocks.setBlock(x, py, z, sugarCane);
            placed++;
        }
        return placed > 0;
    }

    private boolean placeBamboo(BlockAccess blocks, Random random, int x, int y, int z) {
        if (y < blocks.minY() || y >= blocks.maxY()) return false;
        int groundY = y - 1;
        if (groundY < blocks.minY()) return false;
        if (!GRASS_PLACEABLE.contains(blocks.getBlock(x, groundY, z).blockId().toString())) return false;

        int height = 4 + random.nextInt(8);
        StrataBlockState bamboo = StrataBlockState.of(NamespacedKey.minecraft("bamboo"));
        int placed = 0;
        for (int dy = 0; dy < height; dy++) {
            int py = y + dy;
            if (py >= blocks.maxY()) break;
            if (!blocks.getBlock(x, py, z).equals(AIR)) break;
            blocks.setBlock(x, py, z, bamboo);
            placed++;
        }
        return placed > 0;
    }

    private boolean isAdjacentClear(BlockAccess blocks, int x, int y, int z) {
        return blocks.getBlock(x + 1, y, z).equals(AIR)
                && blocks.getBlock(x - 1, y, z).equals(AIR)
                && blocks.getBlock(x, y, z + 1).equals(AIR)
                && blocks.getBlock(x, y, z - 1).equals(AIR);
    }

    private boolean hasAdjacentWater(BlockAccess blocks, int x, int y, int z) {
        return blocks.getBlock(x + 1, y, z).blockId().toString().equals(WATER)
                || blocks.getBlock(x - 1, y, z).blockId().toString().equals(WATER)
                || blocks.getBlock(x, y, z + 1).blockId().toString().equals(WATER)
                || blocks.getBlock(x, y, z - 1).blockId().toString().equals(WATER);
    }
}
