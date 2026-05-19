package com.protectcord.strata.core.water;

import com.protectcord.strata.api.chunk.ChunkCoord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Spatial index of river segments organized by chunk coordinate for efficient
 * per-chunk queries during water system generation.
 */
public final class RiverNetwork {

    public record RiverSegment(
            double x1, double z1,
            double x2, double z2,
            double width, double depth,
            double flowDirection
    ) {
        public double midX() {
            return (x1 + x2) * 0.5;
        }

        public double midZ() {
            return (z1 + z2) * 0.5;
        }

        public double length() {
            double dx = x2 - x1;
            double dz = z2 - z1;
            return Math.sqrt(dx * dx + dz * dz);
        }
    }

    private final Map<Long, List<RiverSegment>> segmentsByChunk = new HashMap<>();

    public void addSegment(RiverSegment segment) {
        int minChunkX = (int) Math.floor(Math.min(segment.x1, segment.x2) - segment.width) >> 4;
        int maxChunkX = (int) Math.floor(Math.max(segment.x1, segment.x2) + segment.width) >> 4;
        int minChunkZ = (int) Math.floor(Math.min(segment.z1, segment.z2) - segment.width) >> 4;
        int maxChunkZ = (int) Math.floor(Math.max(segment.z1, segment.z2) + segment.width) >> 4;

        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                long key = ChunkCoord.fromBlock(cx << 4, cz << 4).toLong();
                segmentsByChunk.computeIfAbsent(key, k -> new ArrayList<>()).add(segment);
            }
        }
    }

    public List<RiverSegment> getSegmentsInChunk(int chunkX, int chunkZ) {
        long key = new ChunkCoord(chunkX, chunkZ).toLong();
        return segmentsByChunk.getOrDefault(key, List.of());
    }

    public int segmentCount() {
        return segmentsByChunk.values().stream().mapToInt(List::size).sum();
    }

    public boolean isEmpty() {
        return segmentsByChunk.isEmpty();
    }
}
