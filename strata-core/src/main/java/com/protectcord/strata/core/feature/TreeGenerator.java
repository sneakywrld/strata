package com.protectcord.strata.core.feature;

import com.protectcord.strata.api.block.StrataBlockState;
import com.protectcord.strata.api.chunk.BlockAccess;
import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.api.feature.Feature;
import com.protectcord.strata.api.feature.FeaturePlacement;
import com.protectcord.strata.api.feature.FeatureType;

import java.util.Random;
import java.util.Set;

public final class TreeGenerator implements Feature {

    private static final StrataBlockState AIR = StrataBlockState.of(NamespacedKey.minecraft("air"));
    private static final Set<String> VALID_GROUND = Set.of(
            "minecraft:grass_block", "minecraft:dirt", "minecraft:podzol",
            "minecraft:rooted_dirt", "minecraft:mycelium", "minecraft:mud"
    );

    public enum TreeType {
        OAK, BIRCH, SPRUCE, JUNGLE, DARK_OAK
    }

    private final NamespacedKey key;
    private final TreeType treeType;
    private final FeaturePlacement placement;

    public TreeGenerator(NamespacedKey key, TreeType treeType, FeaturePlacement placement) {
        this.key = key;
        this.treeType = treeType;
        this.placement = placement;
    }

    @Override
    public NamespacedKey key() { return key; }

    @Override
    public FeatureType type() { return FeatureType.TREE; }

    @Override
    public FeaturePlacement placement() { return placement; }

    @Override
    public boolean place(BlockAccess blocks, Random random, int x, int y, int z) {
        int groundY = y - 1;
        if (groundY < blocks.minY() || groundY >= blocks.maxY()) return false;
        if (!isValidGround(blocks.getBlock(x, groundY, z))) return false;

        return switch (treeType) {
            case OAK -> placeOak(blocks, random, x, y, z);
            case BIRCH -> placeBirch(blocks, random, x, y, z);
            case SPRUCE -> placeSpruce(blocks, random, x, y, z);
            case JUNGLE -> placeJungle(blocks, random, x, y, z);
            case DARK_OAK -> placeDarkOak(blocks, random, x, y, z);
        };
    }

    private boolean placeOak(BlockAccess blocks, Random random, int x, int y, int z) {
        int trunkHeight = 4 + random.nextInt(4);
        if (!hasAirAbove(blocks, x, y, z, trunkHeight + 3)) return false;

        StrataBlockState log = StrataBlockState.of(NamespacedKey.minecraft("oak_log"));
        StrataBlockState leaves = StrataBlockState.of(NamespacedKey.minecraft("oak_leaves"));

        for (int dy = 0; dy < trunkHeight; dy++) {
            blocks.setBlock(x, y + dy, z, log);
        }

        int leafBase = y + trunkHeight - 2;
        for (int dy = 0; dy < 4; dy++) {
            int radius = dy < 2 ? 2 : 1;
            placeLeafLayer(blocks, x, leafBase + dy, z, radius, leaves, random);
        }

        blocks.setBlock(x, y - 1, z, StrataBlockState.of(NamespacedKey.minecraft("dirt")));
        return true;
    }

    private boolean placeBirch(BlockAccess blocks, Random random, int x, int y, int z) {
        int trunkHeight = 5 + random.nextInt(3);
        if (!hasAirAbove(blocks, x, y, z, trunkHeight + 2)) return false;

        StrataBlockState log = StrataBlockState.of(NamespacedKey.minecraft("birch_log"));
        StrataBlockState leaves = StrataBlockState.of(NamespacedKey.minecraft("birch_leaves"));

        for (int dy = 0; dy < trunkHeight; dy++) {
            blocks.setBlock(x, y + dy, z, log);
        }

        int leafBase = y + trunkHeight - 2;
        for (int dy = 0; dy < 3; dy++) {
            int radius = dy < 2 ? 2 : 1;
            placeLeafLayer(blocks, x, leafBase + dy, z, radius, leaves, random);
        }

        blocks.setBlock(x, y - 1, z, StrataBlockState.of(NamespacedKey.minecraft("dirt")));
        return true;
    }

    private boolean placeSpruce(BlockAccess blocks, Random random, int x, int y, int z) {
        int trunkHeight = 7 + random.nextInt(6);
        if (!hasAirAbove(blocks, x, y, z, trunkHeight + 1)) return false;

        StrataBlockState log = StrataBlockState.of(NamespacedKey.minecraft("spruce_log"));
        StrataBlockState leaves = StrataBlockState.of(NamespacedKey.minecraft("spruce_leaves"));

        for (int dy = 0; dy < trunkHeight; dy++) {
            blocks.setBlock(x, y + dy, z, log);
        }

        int leafStart = y + 2;
        int leafEnd = y + trunkHeight;
        for (int ly = leafEnd; ly >= leafStart; ly--) {
            int distFromTop = leafEnd - ly;
            int radius = Math.min(1 + distFromTop / 2, 3);
            if (distFromTop % 2 == 0) {
                placeLeafLayer(blocks, x, ly, z, radius, leaves, random);
            } else {
                placeLeafLayer(blocks, x, ly, z, Math.max(1, radius - 1), leaves, random);
            }
        }
        blocks.setBlock(x, y + trunkHeight, z, leaves);

        blocks.setBlock(x, y - 1, z, StrataBlockState.of(NamespacedKey.minecraft("dirt")));
        return true;
    }

    private boolean placeJungle(BlockAccess blocks, Random random, int x, int y, int z) {
        int trunkHeight = 10 + random.nextInt(10);
        if (!hasAirAbove(blocks, x, y, z, trunkHeight + 3)) return false;

        StrataBlockState log = StrataBlockState.of(NamespacedKey.minecraft("jungle_log"));
        StrataBlockState leaves = StrataBlockState.of(NamespacedKey.minecraft("jungle_leaves"));

        for (int dy = 0; dy < trunkHeight; dy++) {
            blocks.setBlock(x, y + dy, z, log);
            blocks.setBlock(x + 1, y + dy, z, log);
            blocks.setBlock(x, y + dy, z + 1, log);
            blocks.setBlock(x + 1, y + dy, z + 1, log);
        }

        int leafBase = y + trunkHeight - 2;
        for (int dy = 0; dy < 4; dy++) {
            int radius = dy < 2 ? 3 : 2;
            placeLeafLayer(blocks, x, leafBase + dy, z, radius, leaves, random);
            placeLeafLayer(blocks, x + 1, leafBase + dy, z + 1, radius, leaves, random);
        }

        for (int dx = 0; dx <= 1; dx++) {
            for (int dz = 0; dz <= 1; dz++) {
                blocks.setBlock(x + dx, y - 1, z + dz,
                        StrataBlockState.of(NamespacedKey.minecraft("dirt")));
            }
        }
        return true;
    }

    private boolean placeDarkOak(BlockAccess blocks, Random random, int x, int y, int z) {
        int trunkHeight = 6 + random.nextInt(3);
        if (!hasAirAbove(blocks, x, y, z, trunkHeight + 3)) return false;

        StrataBlockState log = StrataBlockState.of(NamespacedKey.minecraft("dark_oak_log"));
        StrataBlockState leaves = StrataBlockState.of(NamespacedKey.minecraft("dark_oak_leaves"));

        for (int dy = 0; dy < trunkHeight; dy++) {
            blocks.setBlock(x, y + dy, z, log);
            blocks.setBlock(x + 1, y + dy, z, log);
            blocks.setBlock(x, y + dy, z + 1, log);
            blocks.setBlock(x + 1, y + dy, z + 1, log);
        }

        int leafBase = y + trunkHeight - 2;
        for (int dy = 0; dy < 4; dy++) {
            int radius = dy < 3 ? 3 : 1;
            for (int cx = 0; cx <= 1; cx++) {
                for (int cz = 0; cz <= 1; cz++) {
                    placeLeafLayer(blocks, x + cx, leafBase + dy, z + cz, radius, leaves, random);
                }
            }
        }

        for (int dx = 0; dx <= 1; dx++) {
            for (int dz = 0; dz <= 1; dz++) {
                blocks.setBlock(x + dx, y - 1, z + dz,
                        StrataBlockState.of(NamespacedKey.minecraft("dirt")));
            }
        }
        return true;
    }

    private void placeLeafLayer(BlockAccess blocks, int cx, int y, int cz,
                                int radius, StrataBlockState leaves, Random random) {
        if (y < blocks.minY() || y >= blocks.maxY()) return;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (Math.abs(dx) == radius && Math.abs(dz) == radius && random.nextBoolean()) continue;
                int px = cx + dx;
                int pz = cz + dz;
                if (blocks.getBlock(px, y, pz).equals(AIR)) {
                    blocks.setBlock(px, y, pz, leaves);
                }
            }
        }
    }

    private boolean hasAirAbove(BlockAccess blocks, int x, int y, int z, int height) {
        for (int dy = 0; dy < height; dy++) {
            int checkY = y + dy;
            if (checkY >= blocks.maxY()) return false;
            if (!blocks.getBlock(x, checkY, z).equals(AIR)) return false;
        }
        return true;
    }

    private boolean isValidGround(StrataBlockState state) {
        return VALID_GROUND.contains(state.blockId().toString());
    }
}
