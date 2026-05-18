package com.protectcord.strata.core.water;

import com.protectcord.strata.api.noise.NoiseFunction;
import com.protectcord.strata.api.water.RiverSettings;
import com.protectcord.strata.noise.math.NoiseMath;

import java.util.ArrayList;
import java.util.List;

/**
 * Macro-scale river network builder using drainage basin computation.
 *
 * <p>The algorithm works in three phases:
 * <ol>
 *   <li>Generate a heightmap at region scale (~512 block resolution)</li>
 *   <li>Compute flow direction for each cell (steepest downhill neighbor)</li>
 *   <li>Accumulate flow to find river paths (cells with high flow = rivers)</li>
 * </ol>
 *
 * <p>Rivers naturally flow downhill and merge into larger rivers that
 * terminate at ocean biomes or map edges.</p>
 */
public final class RiverNetworkBuilder {

    private static final int REGION_RESOLUTION = 32; // cells per region axis
    private static final int CELL_SIZE = 16; // blocks per cell

    private final NoiseFunction heightNoise;
    private final RiverSettings settings;
    private final long seed;

    public RiverNetworkBuilder(NoiseFunction heightNoise, RiverSettings settings, long seed) {
        this.heightNoise = heightNoise;
        this.settings = settings;
        this.seed = seed;
    }

    /**
     * Computes river paths for a region.
     *
     * @param regionX region X coordinate
     * @param regionZ region Z coordinate
     * @return list of river segments in this region
     */
    public List<RiverSegment> computeRegion(int regionX, int regionZ) {
        // Generate regional heightmap
        double[][] heights = new double[REGION_RESOLUTION][REGION_RESOLUTION];
        for (int x = 0; x < REGION_RESOLUTION; x++) {
            for (int z = 0; z < REGION_RESOLUTION; z++) {
                int worldX = regionX * REGION_RESOLUTION * CELL_SIZE + x * CELL_SIZE;
                int worldZ = regionZ * REGION_RESOLUTION * CELL_SIZE + z * CELL_SIZE;
                heights[x][z] = heightNoise.sample(worldX * 0.002, worldZ * 0.002);
            }
        }

        // Compute flow directions (steepest descent)
        int[][] flowDir = new int[REGION_RESOLUTION][REGION_RESOLUTION]; // encoded as dx*3+dz+4
        for (int x = 0; x < REGION_RESOLUTION; x++) {
            for (int z = 0; z < REGION_RESOLUTION; z++) {
                flowDir[x][z] = computeFlowDirection(heights, x, z);
            }
        }

        // Accumulate flow to find river cells
        int[][] flowAccum = new int[REGION_RESOLUTION][REGION_RESOLUTION];
        for (int x = 0; x < REGION_RESOLUTION; x++) {
            for (int z = 0; z < REGION_RESOLUTION; z++) {
                accumulateFlow(flowDir, flowAccum, x, z, new boolean[REGION_RESOLUTION][REGION_RESOLUTION]);
            }
        }

        // Extract river segments from high-flow cells
        int threshold = (int) (5 + (1.0 - settings.branchingFactor()) * 10);
        List<RiverSegment> segments = new ArrayList<>();

        for (int x = 0; x < REGION_RESOLUTION; x++) {
            for (int z = 0; z < REGION_RESOLUTION; z++) {
                if (flowAccum[x][z] >= threshold) {
                    int worldX = regionX * REGION_RESOLUTION * CELL_SIZE + x * CELL_SIZE + CELL_SIZE / 2;
                    int worldZ = regionZ * REGION_RESOLUTION * CELL_SIZE + z * CELL_SIZE + CELL_SIZE / 2;

                    // Apply meander offset using noise
                    double meander = settings.meanderStrength() * CELL_SIZE * 0.5;
                    long mHash = NoiseMath.hash(seed, x + regionX * 100, z + regionZ * 100);
                    double offsetX = NoiseMath.hashToDouble(mHash) * meander;
                    double offsetZ = NoiseMath.hashToDouble(mHash ^ 0x12345) * meander;

                    // Width scales with accumulated flow
                    int width = Math.min(settings.riverWidth(),
                            settings.riverWidth() / 2 + flowAccum[x][z] / 4);

                    segments.add(new RiverSegment(
                            worldX + (int) offsetX,
                            worldZ + (int) offsetZ,
                            width,
                            settings.riverDepth(),
                            heights[x][z]
                    ));
                }
            }
        }

        return segments;
    }

    private int computeFlowDirection(double[][] heights, int x, int z) {
        double minHeight = heights[x][z];
        int bestDir = 4; // center = no flow

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;
                int nx = x + dx, nz = z + dz;
                if (nx < 0 || nx >= REGION_RESOLUTION || nz < 0 || nz >= REGION_RESOLUTION) continue;

                if (heights[nx][nz] < minHeight) {
                    minHeight = heights[nx][nz];
                    bestDir = (dx + 1) * 3 + (dz + 1);
                }
            }
        }

        return bestDir;
    }

    private int accumulateFlow(int[][] flowDir, int[][] flowAccum, int x, int z, boolean[][] visited) {
        if (x < 0 || x >= REGION_RESOLUTION || z < 0 || z >= REGION_RESOLUTION) return 0;
        if (visited[x][z]) return flowAccum[x][z];
        visited[x][z] = true;

        int flow = 1;
        // Sum flow from all cells that flow into this one
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;
                int nx = x + dx, nz = z + dz;
                if (nx < 0 || nx >= REGION_RESOLUTION || nz < 0 || nz >= REGION_RESOLUTION) continue;

                int dir = flowDir[nx][nz];
                int targetDx = (dir / 3) - 1;
                int targetDz = (dir % 3) - 1;

                if (nx + targetDx == x && nz + targetDz == z) {
                    flow += accumulateFlow(flowDir, flowAccum, nx, nz, visited);
                }
            }
        }

        flowAccum[x][z] = flow;
        return flow;
    }

    /**
     * A segment of a river path.
     */
    public record RiverSegment(int x, int z, int width, int depth, double terrainHeight) {}
}
