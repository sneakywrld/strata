package com.protectcord.strata.core.structure;

import com.protectcord.strata.api.block.StrataBlockState;
import com.protectcord.strata.api.chunk.HeightmapType;
import com.protectcord.strata.api.chunk.ProtoChunkAccess;
import com.protectcord.strata.api.core.NamespacedKey;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * Connects nearby villages and structures via A* pathfinding on the heightmap.
 * Creates cobblestone/gravel paths that follow terrain contours, builds wooden
 * bridges over water, and places signs at intersections.
 */
public final class RoadNetworkBuilder {

    private static final StrataBlockState AIR = StrataBlockState.of(NamespacedKey.minecraft("air"));
    private static final StrataBlockState COBBLESTONE = StrataBlockState.of(NamespacedKey.minecraft("cobblestone"));
    private static final StrataBlockState GRAVEL = StrataBlockState.of(NamespacedKey.minecraft("gravel"));
    private static final StrataBlockState WATER = StrataBlockState.of(NamespacedKey.minecraft("water"));
    private static final StrataBlockState OAK_PLANKS = StrataBlockState.of(NamespacedKey.minecraft("oak_planks"));
    private static final StrataBlockState OAK_FENCE = StrataBlockState.of(NamespacedKey.minecraft("oak_fence"));
    private static final StrataBlockState OAK_SIGN = StrataBlockState.of(NamespacedKey.minecraft("oak_sign"));

    private static final int MAX_ROAD_DISTANCE = 256;
    private static final int ROAD_HALF_WIDTH = 1;
    private static final int MAX_PATHFINDING_ITERATIONS = 10000;
    private static final double WATER_TRAVERSAL_COST = 4.0;
    private static final double SLOPE_COST_MULTIPLIER = 2.0;
    private static final int[][] CARDINAL_DIRS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    public record StructureNode(int x, int z, String name) {}

    private record PathNode(int x, int z, double g, double f, PathNode parent) {}

    public void buildNetwork(ProtoChunkAccess chunk, List<StructureNode> structures) {
        if (structures.size() < 2) return;

        List<int[]> connections = computeConnections(structures);
        Map<Long, Integer> roadUsage = new HashMap<>();
        Set<Long> intersectionPositions = new HashSet<>();

        for (int[] connection : connections) {
            StructureNode from = structures.get(connection[0]);
            StructureNode to = structures.get(connection[1]);

            double dist = distance(from.x(), from.z(), to.x(), to.z());
            if (dist > MAX_ROAD_DISTANCE) continue;

            List<int[]> path = findPath(chunk, from.x(), from.z(), to.x(), to.z());
            if (path.isEmpty()) continue;

            for (int[] pos : path) {
                long key = packPos(pos[0], pos[1]);
                roadUsage.merge(key, 1, Integer::sum);
                if (roadUsage.get(key) > 1) {
                    intersectionPositions.add(key);
                }
            }

            layRoadSurface(chunk, path);
        }

        for (long pos : intersectionPositions) {
            placeIntersectionMarker(chunk, unpackX(pos), unpackZ(pos));
        }
    }

    private List<int[]> computeConnections(List<StructureNode> structures) {
        List<int[]> connections = new ArrayList<>();
        boolean[] connected = new boolean[structures.size()];
        connected[0] = true;

        for (int step = 1; step < structures.size(); step++) {
            double bestDist = Double.MAX_VALUE;
            int bestFrom = -1;
            int bestTo = -1;

            for (int i = 0; i < structures.size(); i++) {
                if (!connected[i]) continue;
                for (int j = 0; j < structures.size(); j++) {
                    if (connected[j]) continue;
                    double dist = distance(
                            structures.get(i).x(), structures.get(i).z(),
                            structures.get(j).x(), structures.get(j).z());
                    if (dist < bestDist) {
                        bestDist = dist;
                        bestFrom = i;
                        bestTo = j;
                    }
                }
            }

            if (bestTo >= 0) {
                connected[bestTo] = true;
                connections.add(new int[]{bestFrom, bestTo});
            }
        }

        return connections;
    }

    private List<int[]> findPath(ProtoChunkAccess chunk, int startX, int startZ, int endX, int endZ) {
        PriorityQueue<PathNode> open = new PriorityQueue<>(Comparator.comparingDouble(PathNode::f));
        Set<Long> closed = new HashSet<>();

        double h = distance(startX, startZ, endX, endZ);
        open.add(new PathNode(startX, startZ, 0, h, null));

        int iterations = 0;
        while (!open.isEmpty() && iterations++ < MAX_PATHFINDING_ITERATIONS) {
            PathNode current = open.poll();
            long key = packPos(current.x(), current.z());

            if (closed.contains(key)) continue;
            closed.add(key);

            if (Math.abs(current.x() - endX) <= 1 && Math.abs(current.z() - endZ) <= 1) {
                return reconstructPath(current);
            }

            for (int[] dir : CARDINAL_DIRS) {
                int nx = current.x() + dir[0];
                int nz = current.z() + dir[1];
                long nkey = packPos(nx, nz);
                if (closed.contains(nkey)) continue;

                int currentY = chunk.getHeight(HeightmapType.WORLD_SURFACE, current.x(), current.z()) - 1;
                int neighborY = chunk.getHeight(HeightmapType.WORLD_SURFACE, nx, nz) - 1;

                double moveCost = 1.0;
                moveCost += Math.abs(neighborY - currentY) * SLOPE_COST_MULTIPLIER;

                StrataBlockState neighborBlock = chunk.getBlock(nx, neighborY, nz);
                if (isWaterBlock(neighborBlock)) {
                    moveCost += WATER_TRAVERSAL_COST;
                }

                double ng = current.g() + moveCost;
                double nh = distance(nx, nz, endX, endZ);
                open.add(new PathNode(nx, nz, ng, ng + nh, current));
            }
        }

        return buildDirectPath(startX, startZ, endX, endZ);
    }

    private static List<int[]> reconstructPath(PathNode node) {
        List<int[]> path = new ArrayList<>();
        PathNode current = node;
        while (current != null) {
            path.addFirst(new int[]{current.x(), current.z()});
            current = current.parent();
        }
        return path;
    }

    private static List<int[]> buildDirectPath(int x1, int z1, int x2, int z2) {
        List<int[]> path = new ArrayList<>();
        int dx = Math.abs(x2 - x1);
        int dz = Math.abs(z2 - z1);
        int sx = x1 < x2 ? 1 : -1;
        int sz = z1 < z2 ? 1 : -1;
        int err = dx - dz;
        int cx = x1, cz = z1;

        while (true) {
            path.add(new int[]{cx, cz});
            if (cx == x2 && cz == z2) break;
            int e2 = 2 * err;
            if (e2 > -dz) { err -= dz; cx += sx; }
            if (e2 < dx) { err += dx; cz += sz; }
        }
        return path;
    }

    private void layRoadSurface(ProtoChunkAccess chunk, List<int[]> path) {
        int chunkMinX = chunk.coord().blockX();
        int chunkMinZ = chunk.coord().blockZ();

        for (int[] pos : path) {
            for (int dx = -ROAD_HALF_WIDTH; dx <= ROAD_HALF_WIDTH; dx++) {
                for (int dz = -ROAD_HALF_WIDTH; dz <= ROAD_HALF_WIDTH; dz++) {
                    int rx = pos[0] + dx;
                    int rz = pos[1] + dz;

                    if (!isInChunk(rx, rz, chunkMinX, chunkMinZ)) continue;

                    int surfaceY = chunk.getHeight(HeightmapType.WORLD_SURFACE, rx, rz) - 1;
                    if (surfaceY < chunk.minY() || surfaceY >= chunk.maxY()) continue;

                    StrataBlockState surfaceBlock = chunk.getBlock(rx, surfaceY, rz);

                    if (isWaterBlock(surfaceBlock)) {
                        buildBridgeSegment(chunk, rx, surfaceY, rz);
                    } else {
                        StrataBlockState roadBlock = (dx == 0 && dz == 0) ? COBBLESTONE : GRAVEL;
                        chunk.setBlock(rx, surfaceY, rz, roadBlock);

                        for (int cy = surfaceY + 1; cy <= surfaceY + 3 && cy < chunk.maxY(); cy++) {
                            if (!chunk.getBlock(rx, cy, rz).equals(AIR)) {
                                chunk.setBlock(rx, cy, rz, AIR);
                            }
                        }
                    }
                }
            }
        }
    }

    private void buildBridgeSegment(ProtoChunkAccess chunk, int x, int waterY, int z) {
        int bridgeY = waterY + 1;
        if (bridgeY >= chunk.maxY()) return;

        chunk.setBlock(x, bridgeY, z, OAK_PLANKS);

        for (int cy = bridgeY + 1; cy <= bridgeY + 3 && cy < chunk.maxY(); cy++) {
            chunk.setBlock(x, cy, z, AIR);
        }
    }

    private void placeIntersectionMarker(ProtoChunkAccess chunk, int x, int z) {
        int chunkMinX = chunk.coord().blockX();
        int chunkMinZ = chunk.coord().blockZ();
        if (!isInChunk(x, z, chunkMinX, chunkMinZ)) return;

        int surfaceY = chunk.getHeight(HeightmapType.WORLD_SURFACE, x, z);
        if (surfaceY < chunk.minY() || surfaceY + 2 >= chunk.maxY()) return;

        chunk.setBlock(x, surfaceY, z, OAK_FENCE);
        chunk.setBlock(x, surfaceY + 1, z, OAK_SIGN);
    }

    private static boolean isWaterBlock(StrataBlockState state) {
        return state.blockId().toString().equals("minecraft:water");
    }

    private static double distance(int x1, int z1, int x2, int z2) {
        double dx = x2 - x1;
        double dz = z2 - z1;
        return Math.sqrt(dx * dx + dz * dz);
    }

    private static long packPos(int x, int z) {
        return ((long) x << 32) | (z & 0xFFFFFFFFL);
    }

    private static int unpackX(long packed) {
        return (int) (packed >> 32);
    }

    private static int unpackZ(long packed) {
        return (int) packed;
    }

    private static boolean isInChunk(int x, int z, int chunkMinX, int chunkMinZ) {
        int localX = x - chunkMinX;
        int localZ = z - chunkMinZ;
        return localX >= 0 && localX <= 15 && localZ >= 0 && localZ <= 15;
    }
}
