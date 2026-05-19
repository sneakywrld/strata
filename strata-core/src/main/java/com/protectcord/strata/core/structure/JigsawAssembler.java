package com.protectcord.strata.core.structure;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Random;

/**
 * Assembles jigsaw-type structures by recursively placing connected pieces up to
 * a configurable depth and piece count limit. Pieces are connected at jigsaw
 * connection points with compatible orientations.
 */
public final class JigsawAssembler {

    public record PlacedPiece(int x, int y, int z, int rotation, String pieceKey) {
        public boolean overlaps(PlacedPiece other, int pieceSize) {
            return Math.abs(x - other.x()) < pieceSize
                    && Math.abs(y - other.y()) < pieceSize
                    && Math.abs(z - other.z()) < pieceSize;
        }
    }

    private record PendingConnection(int x, int y, int z, int depth, int facing) {}

    private static final int PIECE_SIZE = 8;
    private static final int[] ROTATIONS = {0, 90, 180, 270};
    private static final int[][] FACING_OFFSETS = {
            { 0, 0, -1}, // north
            { 1, 0,  0}, // east
            { 0, 0,  1}, // south
            {-1, 0,  0}, // west
            { 0, 1,  0}, // up
            { 0,-1,  0}, // down
    };

    public List<PlacedPiece> assemble(StructurePlacementEngine.StructureStart start,
                                      int maxDepth, int maxPieces, Random random) {
        List<PlacedPiece> placed = new ArrayList<>();
        Deque<PendingConnection> pending = new ArrayDeque<>();

        int startX = start.chunkX() << 4;
        int startZ = start.chunkZ() << 4;
        int startY = computeStartY(start, random);

        String rootKey = start.definition().key().toString() + "/center";
        int rootRotation = ROTATIONS[random.nextInt(ROTATIONS.length)];
        placed.add(new PlacedPiece(startX, startY, startZ, rootRotation, rootKey));

        for (int facing = 0; facing < 4; facing++) {
            int nx = startX + FACING_OFFSETS[facing][0] * PIECE_SIZE;
            int ny = startY + FACING_OFFSETS[facing][1] * PIECE_SIZE;
            int nz = startZ + FACING_OFFSETS[facing][2] * PIECE_SIZE;
            pending.add(new PendingConnection(nx, ny, nz, 1, facing));
        }

        while (!pending.isEmpty() && placed.size() < maxPieces) {
            PendingConnection conn = pending.poll();

            if (conn.depth() > maxDepth) continue;

            String pieceKey = selectPiece(start, conn.depth(), random);
            int rotation = ROTATIONS[random.nextInt(ROTATIONS.length)];
            PlacedPiece candidate = new PlacedPiece(conn.x(), conn.y(), conn.z(), rotation, pieceKey);

            if (hasOverlap(placed, candidate)) continue;

            placed.add(candidate);

            if (conn.depth() < maxDepth) {
                for (int facing = 0; facing < 4; facing++) {
                    int oppositeFacing = (conn.facing() + 2) % 4;
                    if (facing == oppositeFacing) continue;

                    int[] offset = rotateOffset(FACING_OFFSETS[facing], rotation);
                    int nx = conn.x() + offset[0] * PIECE_SIZE;
                    int ny = conn.y() + offset[1] * PIECE_SIZE;
                    int nz = conn.z() + offset[2] * PIECE_SIZE;

                    if (random.nextDouble() < 0.7) {
                        pending.add(new PendingConnection(nx, ny, nz, conn.depth() + 1, facing));
                    }
                }
            }
        }

        return placed;
    }

    private static int computeStartY(StructurePlacementEngine.StructureStart start, Random random) {
        int minY = start.definition().minY();
        int maxY = start.definition().maxY();
        int range = maxY - minY;
        return range > 0 ? minY + random.nextInt(range) : minY;
    }

    private static String selectPiece(StructurePlacementEngine.StructureStart start,
                                      int depth, Random random) {
        String base = start.definition().key().toString();
        int variant = random.nextInt(4) + 1;
        return base + "/piece_d" + depth + "_v" + variant;
    }

    private static boolean hasOverlap(List<PlacedPiece> placed, PlacedPiece candidate) {
        for (PlacedPiece existing : placed) {
            if (candidate.overlaps(existing, PIECE_SIZE)) return true;
        }
        return false;
    }

    private static int[] rotateOffset(int[] offset, int rotation) {
        return switch (rotation) {
            case 90 -> new int[]{-offset[2], offset[1], offset[0]};
            case 180 -> new int[]{-offset[0], offset[1], -offset[2]};
            case 270 -> new int[]{offset[2], offset[1], -offset[0]};
            default -> offset.clone();
        };
    }
}
