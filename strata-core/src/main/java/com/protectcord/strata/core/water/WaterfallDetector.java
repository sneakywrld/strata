package com.protectcord.strata.core.water;

import java.util.ArrayList;
import java.util.List;

/**
 * Detects steep gradient river segments where waterfalls should form.
 * A waterfall is identified when the height difference between adjacent
 * river segment endpoints exceeds 8 blocks over a short horizontal distance.
 */
public final class WaterfallDetector {

    public record WaterfallPoint(int x, int z, int topY, int bottomY, int width) {
        public int height() {
            return topY - bottomY;
        }
    }

    private static final int MIN_HEIGHT_DIFF = 8;
    private static final int MAX_HORIZONTAL_DIST = 16;

    public List<WaterfallPoint> detect(RiverNetwork network, int[] heightmap,
                                       int chunkX, int chunkZ) {
        List<WaterfallPoint> waterfalls = new ArrayList<>();
        List<RiverNetwork.RiverSegment> segments = network.getSegmentsInChunk(chunkX, chunkZ);

        int baseX = chunkX << 4;
        int baseZ = chunkZ << 4;

        for (RiverNetwork.RiverSegment segment : segments) {
            int x1Local = (int) segment.x1() - baseX;
            int z1Local = (int) segment.z1() - baseZ;
            int x2Local = (int) segment.x2() - baseX;
            int z2Local = (int) segment.z2() - baseZ;

            int y1 = sampleHeightmap(heightmap, x1Local, z1Local);
            int y2 = sampleHeightmap(heightmap, x2Local, z2Local);

            if (y1 < 0 || y2 < 0) continue;

            int heightDiff = Math.abs(y1 - y2);
            double horizontalDist = segment.length();

            if (heightDiff >= MIN_HEIGHT_DIFF && horizontalDist <= MAX_HORIZONTAL_DIST) {
                int topY, bottomY;
                int waterfallX, waterfallZ;

                if (y1 > y2) {
                    topY = y1;
                    bottomY = y2;
                    waterfallX = (int) segment.x1();
                    waterfallZ = (int) segment.z1();
                } else {
                    topY = y2;
                    bottomY = y1;
                    waterfallX = (int) segment.x2();
                    waterfallZ = (int) segment.z2();
                }

                int waterfallWidth = Math.max(1, (int) segment.width());
                waterfalls.add(new WaterfallPoint(waterfallX, waterfallZ, topY, bottomY, waterfallWidth));
            }
        }

        return waterfalls;
    }

    private static int sampleHeightmap(int[] heightmap, int localX, int localZ) {
        if (localX < 0 || localX >= 16 || localZ < 0 || localZ >= 16) return -1;
        return heightmap[localX + localZ * 16];
    }
}
