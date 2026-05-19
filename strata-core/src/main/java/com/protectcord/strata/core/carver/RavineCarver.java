package com.protectcord.strata.core.carver;

import com.protectcord.strata.api.block.StrataBlockState;
import com.protectcord.strata.api.carver.Carver;
import com.protectcord.strata.api.carver.CarverContext;
import com.protectcord.strata.api.carver.CarverType;
import com.protectcord.strata.api.carver.CarvingMask;
import com.protectcord.strata.api.chunk.HeightmapType;
import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.noise.math.NoiseMath;

/**
 * Carves surface-level canyons with a V-shaped cross section. Ravines start at the
 * terrain surface and cut downward 20-60 blocks, exposing ore layers in their walls.
 * Width ranges from 4-12 blocks at the top, narrowing to 1-3 at the bottom.
 */
public final class RavineCarver implements Carver {

    private static final NamespacedKey KEY = NamespacedKey.strata("ravine");

    private static final StrataBlockState AIR = StrataBlockState.of(NamespacedKey.minecraft("air"));
    private static final StrataBlockState BEDROCK = StrataBlockState.of(NamespacedKey.minecraft("bedrock"));

    private static final int MIN_DEPTH = 20;
    private static final int MAX_DEPTH = 60;
    private static final double MIN_TOP_WIDTH = 4.0;
    private static final double MAX_TOP_WIDTH = 12.0;
    private static final double MIN_BOTTOM_WIDTH = 1.0;
    private static final double MAX_BOTTOM_WIDTH = 3.0;
    private static final double PATH_FREQ = 0.005;
    private static final long RAVINE_SALT = 0xCAFEBABE12345678L;

    private final double probability;

    public RavineCarver(double probability) {
        this.probability = probability;
    }

    @Override
    public NamespacedKey key() {
        return KEY;
    }

    @Override
    public CarverType type() {
        return CarverType.RAVINE;
    }

    @Override
    public boolean carve(CarverContext context, CarvingMask mask) {
        int baseX = context.chunk().blockX();
        int baseZ = context.chunk().blockZ();
        long seed = context.seed();
        boolean carved = false;

        long chunkHash = NoiseMath.hash(seed, context.chunk().x(), context.chunk().z());
        double depthFactor = NoiseMath.hashToDouble(chunkHash) * 0.5 + 0.5;
        int ravineDepth = MIN_DEPTH + (int) (depthFactor * (MAX_DEPTH - MIN_DEPTH));

        double topWidthFactor = NoiseMath.hashToDouble(chunkHash ^ 0xABCDEFL) * 0.5 + 0.5;
        double topWidth = NoiseMath.lerp(MIN_TOP_WIDTH, MAX_TOP_WIDTH, topWidthFactor);
        double bottomWidth = NoiseMath.lerp(MIN_BOTTOM_WIDTH, MAX_BOTTOM_WIDTH, topWidthFactor * 0.5);

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = baseX + x;
                int worldZ = baseZ + z;

                double pathDist = computePathDistance(worldX, worldZ, seed);

                if (pathDist > topWidth * 0.5) continue;

                int surfaceY = context.blocks().maxY() - 1;
                if (context.blocks() instanceof com.protectcord.strata.api.chunk.ProtoChunkAccess proto) {
                    surfaceY = proto.getHeight(HeightmapType.WORLD_SURFACE, worldX, worldZ) - 1;
                }

                int bottomY = Math.max(surfaceY - ravineDepth, context.minY());

                for (int y = surfaceY; y >= bottomY; y--) {
                    double progress = (double) (surfaceY - y) / ravineDepth;
                    double widthAtY = NoiseMath.lerp(topWidth, bottomWidth, progress);
                    double halfWidth = widthAtY * 0.5;

                    if (pathDist <= halfWidth) {
                        StrataBlockState current = context.blocks().getBlock(worldX, y, worldZ);
                        if (current.equals(BEDROCK)) continue;

                        context.blocks().setBlock(worldX, y, worldZ, AIR);
                        mask.set(x, y, z);
                        carved = true;
                    }
                }
            }
        }

        return carved;
    }

    @Override
    public double probability() {
        return probability;
    }

    private static double computePathDistance(int worldX, int worldZ, long seed) {
        double sx = worldX * PATH_FREQ;
        double sz = worldZ * PATH_FREQ;

        int ix = NoiseMath.fastFloor(sx);
        int iz = NoiseMath.fastFloor(sz);
        double fx = NoiseMath.smootherstep(sx - ix);
        double fz = NoiseMath.smootherstep(sz - iz);

        double n00 = NoiseMath.hashToDouble(NoiseMath.hash(seed ^ RAVINE_SALT, ix, iz));
        double n10 = NoiseMath.hashToDouble(NoiseMath.hash(seed ^ RAVINE_SALT, ix + 1, iz));
        double n01 = NoiseMath.hashToDouble(NoiseMath.hash(seed ^ RAVINE_SALT, ix, iz + 1));
        double n11 = NoiseMath.hashToDouble(NoiseMath.hash(seed ^ RAVINE_SALT, ix + 1, iz + 1));

        double nx0 = NoiseMath.lerp(n00, n10, fx);
        double nx1 = NoiseMath.lerp(n01, n11, fx);
        double pathOffset = NoiseMath.lerp(nx0, nx1, fz);

        double perpX = NoiseMath.hashToDouble(NoiseMath.hash(seed, ix, iz)) * 0.7;
        double perpZ = NoiseMath.hashToDouble(NoiseMath.hash(seed ^ 0xFFL, ix, iz)) * 0.7;
        double perpLen = Math.sqrt(perpX * perpX + perpZ * perpZ);
        if (perpLen > 0) {
            perpX /= perpLen;
            perpZ /= perpLen;
        }

        double localX = (sx - ix) - 0.5 + pathOffset * 0.3;
        double localZ = (sz - iz) - 0.5 + pathOffset * 0.3;

        return Math.abs(localX * perpX + localZ * perpZ) / PATH_FREQ;
    }
}
