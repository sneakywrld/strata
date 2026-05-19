package com.protectcord.strata.core.structure;

import com.protectcord.strata.api.block.StrataBlockState;
import com.protectcord.strata.api.chunk.ProtoChunkAccess;
import com.protectcord.strata.api.core.NamespacedKey;

import java.util.Map;

/**
 * Places schematic block data into a chunk with rotation support. Handles the four
 * cardinal rotations (0, 90, 180, 270 degrees) and skips structure void blocks
 * to preserve existing terrain where the schematic has intentional gaps.
 */
public final class SchematicPlacer {

    private static final StrataBlockState STRUCTURE_VOID =
            StrataBlockState.of(NamespacedKey.minecraft("structure_void"));

    private static final Map<String, String> ROTATION_MAP_90 = Map.of(
            "north", "east", "east", "south", "south", "west", "west", "north"
    );
    private static final Map<String, String> ROTATION_MAP_180 = Map.of(
            "north", "south", "east", "west", "south", "north", "west", "east"
    );
    private static final Map<String, String> ROTATION_MAP_270 = Map.of(
            "north", "west", "east", "north", "south", "east", "west", "south"
    );

    private StrataBlockState[][][] schematicData;
    private int sizeX;
    private int sizeY;
    private int sizeZ;

    public void setSchematicData(StrataBlockState[][][] data) {
        this.schematicData = data;
        this.sizeX = data.length;
        this.sizeY = data[0].length;
        this.sizeZ = data[0][0].length;
    }

    public void place(ProtoChunkAccess chunk, int originX, int originY, int originZ, int rotation) {
        if (schematicData == null) return;

        int baseX = chunk.coord().blockX();
        int baseZ = chunk.coord().blockZ();

        for (int sx = 0; sx < sizeX; sx++) {
            for (int sy = 0; sy < sizeY; sy++) {
                for (int sz = 0; sz < sizeZ; sz++) {
                    StrataBlockState block = schematicData[sx][sy][sz];
                    if (block == null || block.equals(STRUCTURE_VOID)) continue;

                    int[] rotated = rotatePosition(sx, sz, sizeX, sizeZ, rotation);
                    int worldX = originX + rotated[0];
                    int worldY = originY + sy;
                    int worldZ = originZ + rotated[1];

                    if (worldX < baseX || worldX >= baseX + 16) continue;
                    if (worldZ < baseZ || worldZ >= baseZ + 16) continue;
                    if (worldY < chunk.minY() || worldY >= chunk.maxY()) continue;

                    StrataBlockState rotatedBlock = rotateBlockState(block, rotation);
                    chunk.setBlock(worldX, worldY, worldZ, rotatedBlock);
                }
            }
        }
    }

    private static int[] rotatePosition(int x, int z, int sizeX, int sizeZ, int rotation) {
        return switch (rotation) {
            case 90 -> new int[]{sizeZ - 1 - z, x};
            case 180 -> new int[]{sizeX - 1 - x, sizeZ - 1 - z};
            case 270 -> new int[]{z, sizeX - 1 - x};
            default -> new int[]{x, z};
        };
    }

    private static StrataBlockState rotateBlockState(StrataBlockState state, int rotation) {
        if (rotation == 0 || state.properties().isEmpty()) return state;

        String facing = state.properties().get("facing");
        if (facing == null) return state;

        Map<String, String> rotationMap = switch (rotation) {
            case 90 -> ROTATION_MAP_90;
            case 180 -> ROTATION_MAP_180;
            case 270 -> ROTATION_MAP_270;
            default -> null;
        };

        if (rotationMap == null) return state;

        String newFacing = rotationMap.getOrDefault(facing, facing);
        if (newFacing.equals(facing)) return state;

        var newProps = new java.util.HashMap<>(state.properties());
        newProps.put("facing", newFacing);
        return new StrataBlockState(state.blockId(), Map.copyOf(newProps));
    }
}
