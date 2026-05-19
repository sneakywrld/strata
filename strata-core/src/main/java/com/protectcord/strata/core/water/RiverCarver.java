package com.protectcord.strata.core.water;

import com.protectcord.strata.api.block.StrataBlockState;
import com.protectcord.strata.api.chunk.HeightmapType;
import com.protectcord.strata.api.chunk.ProtoChunkAccess;
import com.protectcord.strata.api.core.NamespacedKey;

import java.util.List;

/**
 * Carves river channels into terrain using a parabolic cross-section profile.
 * Places water source blocks in the channel, gravel on the riverbed, and
 * grass on the banks.
 */
public final class RiverCarver {

    private static final StrataBlockState WATER = StrataBlockState.of(NamespacedKey.minecraft("water"));
    private static final StrataBlockState AIR = StrataBlockState.of(NamespacedKey.minecraft("air"));
    private static final StrataBlockState GRAVEL = StrataBlockState.of(NamespacedKey.minecraft("gravel"));
    private static final StrataBlockState GRASS_BLOCK = StrataBlockState.of(NamespacedKey.minecraft("grass_block"));
    private static final StrataBlockState DIRT = StrataBlockState.of(NamespacedKey.minecraft("dirt"));

    public void carveRivers(ProtoChunkAccess chunk, List<RiverNetwork.RiverSegment> segments, int seaLevel) {
        if (segments.isEmpty()) return;

        int baseX = chunk.coord().blockX();
        int baseZ = chunk.coord().blockZ();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = baseX + x;
                int worldZ = baseZ + z;

                double minDist = Double.MAX_VALUE;
                RiverNetwork.RiverSegment nearest = null;

                for (RiverNetwork.RiverSegment seg : segments) {
                    double dist = distanceToSegment(worldX, worldZ, seg);
                    if (dist < minDist) {
                        minDist = dist;
                        nearest = seg;
                    }
                }

                if (nearest == null || minDist > nearest.width()) continue;

                int surfaceY = chunk.getHeight(HeightmapType.WORLD_SURFACE, worldX, worldZ) - 1;
                if (surfaceY < chunk.minY()) continue;

                double normalizedDist = minDist / nearest.width();
                double parabolicDepth = nearest.depth() * (1.0 - normalizedDist * normalizedDist);
                int carveDepth = (int) Math.ceil(parabolicDepth);

                int waterLevel = Math.min(surfaceY, seaLevel);
                int bottomY = surfaceY - carveDepth;

                for (int y = surfaceY; y >= bottomY && y >= chunk.minY(); y--) {
                    if (y == bottomY) {
                        chunk.setBlock(worldX, y, worldZ, GRAVEL);
                    } else if (y <= waterLevel) {
                        chunk.setBlock(worldX, y, worldZ, WATER);
                    } else {
                        chunk.setBlock(worldX, y, worldZ, AIR);
                    }
                }

                if (normalizedDist > 0.7 && normalizedDist <= 1.0) {
                    applyBankBlocks(chunk, worldX, surfaceY, worldZ);
                }
            }
        }
    }

    private void applyBankBlocks(ProtoChunkAccess chunk, int x, int surfaceY, int z) {
        chunk.setBlock(x, surfaceY, z, GRASS_BLOCK);
        if (surfaceY - 1 >= chunk.minY()) {
            chunk.setBlock(x, surfaceY - 1, z, DIRT);
        }
    }

    private static double distanceToSegment(double px, double pz,
                                            RiverNetwork.RiverSegment seg) {
        double dx = seg.x2() - seg.x1();
        double dz = seg.z2() - seg.z1();
        double lenSq = dx * dx + dz * dz;

        if (lenSq == 0) {
            double ex = px - seg.x1();
            double ez = pz - seg.z1();
            return Math.sqrt(ex * ex + ez * ez);
        }

        double t = ((px - seg.x1()) * dx + (pz - seg.z1()) * dz) / lenSq;
        t = Math.max(0, Math.min(1, t));

        double closestX = seg.x1() + t * dx;
        double closestZ = seg.z1() + t * dz;

        double ex = px - closestX;
        double ez = pz - closestZ;
        return Math.sqrt(ex * ex + ez * ez);
    }
}
