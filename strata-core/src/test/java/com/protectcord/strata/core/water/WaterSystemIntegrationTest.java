package com.protectcord.strata.core.water;

import com.protectcord.strata.api.biome.Biome;
import com.protectcord.strata.api.biome.BiomeEffects;
import com.protectcord.strata.api.block.StrataBlockState;
import com.protectcord.strata.api.chunk.ChunkCoord;
import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.api.noise.NoiseFunction;
import com.protectcord.strata.api.water.RiverSettings;
import com.protectcord.strata.core.chunk.StrataProtoChunk;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for the water system: river networks, waterfall detection,
 * and lake filling.
 */
@DisplayName("Water System Integration Tests")
@MockitoSettings(strictness = Strictness.LENIENT)
class WaterSystemIntegrationTest {

    private static final StrataBlockState WATER = StrataBlockState.of(NamespacedKey.minecraft("water"));
    private static final StrataBlockState STONE = StrataBlockState.of(NamespacedKey.minecraft("stone"));

    // ================================================================ River Network Builder

    @Nested
    @DisplayName("RiverNetworkBuilder")
    class RiverNetworkBuilderTests {

        @Test
        @DisplayName("Computes river segments for a region with slope")
        void computesRiverSegments() {
            // Create a noise function that produces a slope: higher in the north, lower in the south.
            // This ensures water flows from high to low (negative z direction has lower height).
            NoiseFunction slopedNoise = mock(NoiseFunction.class);
            when(slopedNoise.sample(anyDouble(), anyDouble())).thenAnswer(invocation -> {
                double z = invocation.getArgument(1, Double.class);
                // Height decreases as z increases -> flow from low z to high z
                return 1.0 - z * 0.5;
            });

            RiverSettings settings = new RiverSettings(
                    true, 6, 4, 0.8, 0.3,
                    NamespacedKey.strata("river_noise"),
                    true, 0.3
            );

            RiverNetworkBuilder builder = new RiverNetworkBuilder(slopedNoise, settings, 42L);
            List<RiverNetworkBuilder.RiverSegment> segments = builder.computeRegion(0, 0);

            // With a uniform slope, water should accumulate into river paths
            assertFalse(segments.isEmpty(),
                    "Sloped terrain should produce at least some river segments");
        }

        @Test
        @DisplayName("Flat terrain produces fewer or no river segments")
        void flatTerrainFewerRivers() {
            NoiseFunction flatNoise = mock(NoiseFunction.class);
            when(flatNoise.sample(anyDouble(), anyDouble())).thenReturn(0.5);

            RiverSettings settings = RiverSettings.defaults();

            RiverNetworkBuilder builder = new RiverNetworkBuilder(flatNoise, settings, 42L);
            List<RiverNetworkBuilder.RiverSegment> segments = builder.computeRegion(0, 0);

            // Flat terrain has no flow direction -> flow accumulation stays at 1 everywhere.
            // With threshold >= 5, no cell should reach the threshold.
            assertTrue(segments.isEmpty(),
                    "Perfectly flat terrain should produce no river segments");
        }

        @Test
        @DisplayName("River segments have positive width and depth")
        void segmentsHavePositiveDimensions() {
            NoiseFunction slopedNoise = mock(NoiseFunction.class);
            when(slopedNoise.sample(anyDouble(), anyDouble())).thenAnswer(invocation -> {
                double x = invocation.getArgument(0, Double.class);
                double z = invocation.getArgument(1, Double.class);
                // Bowl shape -> flow toward center
                return x * x + z * z;
            });

            RiverSettings settings = new RiverSettings(
                    true, 8, 5, 0.7, 0.4,
                    NamespacedKey.strata("river_noise"),
                    true, 0.3
            );

            RiverNetworkBuilder builder = new RiverNetworkBuilder(slopedNoise, settings, 42L);
            List<RiverNetworkBuilder.RiverSegment> segments = builder.computeRegion(0, 0);

            for (RiverNetworkBuilder.RiverSegment seg : segments) {
                assertTrue(seg.width() > 0, "River width should be positive, was: " + seg.width());
                assertTrue(seg.depth() > 0, "River depth should be positive, was: " + seg.depth());
            }
        }

        @Test
        @DisplayName("Higher branching factor produces more river segments")
        void higherBranchingMoreSegments() {
            NoiseFunction valleyNoise = mock(NoiseFunction.class);
            when(valleyNoise.sample(anyDouble(), anyDouble())).thenAnswer(invocation -> {
                double x = invocation.getArgument(0, Double.class);
                double z = invocation.getArgument(1, Double.class);
                return Math.abs(x) + z * 0.5;
            });

            RiverSettings lowBranching = new RiverSettings(
                    true, 6, 4, 0.1, 0.5,
                    NamespacedKey.strata("river_noise"),
                    true, 0.3
            );
            RiverSettings highBranching = new RiverSettings(
                    true, 6, 4, 0.9, 0.5,
                    NamespacedKey.strata("river_noise"),
                    true, 0.3
            );

            RiverNetworkBuilder lowBuilder = new RiverNetworkBuilder(valleyNoise, lowBranching, 42L);
            RiverNetworkBuilder highBuilder = new RiverNetworkBuilder(valleyNoise, highBranching, 42L);

            List<RiverNetworkBuilder.RiverSegment> lowSegs = lowBuilder.computeRegion(0, 0);
            List<RiverNetworkBuilder.RiverSegment> highSegs = highBuilder.computeRegion(0, 0);

            // Higher branching = lower flow threshold = more segments
            assertTrue(highSegs.size() >= lowSegs.size(),
                    "Higher branching factor should produce >= segments: low="
                            + lowSegs.size() + " high=" + highSegs.size());
        }

        @Test
        @DisplayName("Different seeds produce different river networks")
        void differentSeedsDifferentNetworks() {
            NoiseFunction noise = mock(NoiseFunction.class);
            when(noise.sample(anyDouble(), anyDouble())).thenAnswer(invocation -> {
                double x = invocation.getArgument(0, Double.class);
                double z = invocation.getArgument(1, Double.class);
                return x * 0.3 + z * 0.7;
            });

            RiverSettings settings = new RiverSettings(
                    true, 6, 4, 0.6, 0.5,
                    NamespacedKey.strata("river_noise"),
                    true, 0.3
            );

            RiverNetworkBuilder builder1 = new RiverNetworkBuilder(noise, settings, 42L);
            RiverNetworkBuilder builder2 = new RiverNetworkBuilder(noise, settings, 99L);

            List<RiverNetworkBuilder.RiverSegment> segs1 = builder1.computeRegion(0, 0);
            List<RiverNetworkBuilder.RiverSegment> segs2 = builder2.computeRegion(0, 0);

            // The noise is the same but the meander offsets use different seeds,
            // so at least some segment positions should differ
            if (!segs1.isEmpty() && !segs2.isEmpty()) {
                boolean anyDifferent = false;
                int compareCount = Math.min(segs1.size(), segs2.size());
                for (int i = 0; i < compareCount; i++) {
                    if (segs1.get(i).x() != segs2.get(i).x()
                            || segs1.get(i).z() != segs2.get(i).z()) {
                        anyDifferent = true;
                        break;
                    }
                }
                assertTrue(anyDifferent,
                        "Different seeds should produce different meander offsets");
            }
        }
    }

    // ================================================================ RiverNetwork spatial index

    @Nested
    @DisplayName("RiverNetwork spatial index")
    class RiverNetworkTests {

        @Test
        @DisplayName("Added segment is retrievable by chunk coordinate")
        void addedSegmentRetrievable() {
            RiverNetwork network = new RiverNetwork();
            RiverNetwork.RiverSegment seg = new RiverNetwork.RiverSegment(
                    8.0, 8.0, 12.0, 12.0, 3.0, 2.0, 0.0
            );
            network.addSegment(seg);

            // Segment is centered in chunk (0,0)
            List<RiverNetwork.RiverSegment> found = network.getSegmentsInChunk(0, 0);
            assertFalse(found.isEmpty(), "Should find segment in its chunk");
            assertEquals(seg, found.get(0));
        }

        @Test
        @DisplayName("Empty network returns no segments")
        void emptyNetworkNoSegments() {
            RiverNetwork network = new RiverNetwork();
            assertTrue(network.isEmpty());
            assertEquals(0, network.segmentCount());
            assertTrue(network.getSegmentsInChunk(0, 0).isEmpty());
        }

        @Test
        @DisplayName("Segment spanning multiple chunks appears in all")
        void segmentSpanningMultipleChunks() {
            RiverNetwork network = new RiverNetwork();
            // Segment from chunk (0,0) to chunk (1,0) with width=4
            RiverNetwork.RiverSegment seg = new RiverNetwork.RiverSegment(
                    8.0, 8.0, 24.0, 8.0, 4.0, 3.0, 0.0
            );
            network.addSegment(seg);

            assertFalse(network.getSegmentsInChunk(0, 0).isEmpty(),
                    "Should appear in origin chunk");
            assertFalse(network.getSegmentsInChunk(1, 0).isEmpty(),
                    "Should appear in adjacent chunk");
        }

        @Test
        @DisplayName("RiverSegment length calculation is correct")
        void segmentLengthCalculation() {
            RiverNetwork.RiverSegment seg = new RiverNetwork.RiverSegment(
                    0.0, 0.0, 3.0, 4.0, 2.0, 1.0, 0.0
            );
            assertEquals(5.0, seg.length(), 1e-10, "Length of 3-4-5 triangle should be 5");
        }

        @Test
        @DisplayName("RiverSegment midpoint calculation is correct")
        void segmentMidpointCalculation() {
            RiverNetwork.RiverSegment seg = new RiverNetwork.RiverSegment(
                    10.0, 20.0, 30.0, 40.0, 2.0, 1.0, 0.0
            );
            assertEquals(20.0, seg.midX(), 1e-10);
            assertEquals(30.0, seg.midZ(), 1e-10);
        }
    }

    // ================================================================ Waterfall Detection

    @Nested
    @DisplayName("WaterfallDetector")
    class WaterfallDetectorTests {

        @Test
        @DisplayName("Detects waterfall on steep gradient")
        void detectsSteepGradient() {
            WaterfallDetector detector = new WaterfallDetector();

            // Create a river segment within chunk (0,0) with steep gradient
            RiverNetwork network = new RiverNetwork();
            // From (4,4) to (12,4) -- short horizontal distance
            network.addSegment(new RiverNetwork.RiverSegment(
                    4.0, 4.0, 12.0, 4.0, 3.0, 2.0, 0.0
            ));

            // Heightmap: position (4,4) is at y=80, position (12,4) is at y=60
            // Height diff = 20 >= 8, horizontal dist = 8 <= 16
            int[] heightmap = new int[16 * 16];
            for (int i = 0; i < heightmap.length; i++) {
                heightmap[i] = 70; // default
            }
            heightmap[4 + 4 * 16] = 80;  // high end
            heightmap[12 + 4 * 16] = 60; // low end

            List<WaterfallDetector.WaterfallPoint> waterfalls =
                    detector.detect(network, heightmap, 0, 0);

            assertFalse(waterfalls.isEmpty(), "Should detect a waterfall on steep gradient");

            WaterfallDetector.WaterfallPoint wf = waterfalls.get(0);
            assertEquals(80, wf.topY(), "Top Y should be the higher elevation");
            assertEquals(60, wf.bottomY(), "Bottom Y should be the lower elevation");
            assertEquals(20, wf.height(), "Waterfall height should be the difference");
            assertTrue(wf.width() >= 1, "Waterfall should have positive width");
        }

        @Test
        @DisplayName("No waterfall on gentle gradient")
        void noWaterfallOnGentleGradient() {
            WaterfallDetector detector = new WaterfallDetector();

            RiverNetwork network = new RiverNetwork();
            // Segment from (2,2) to (14,2) -- long horizontal distance
            network.addSegment(new RiverNetwork.RiverSegment(
                    2.0, 2.0, 14.0, 2.0, 3.0, 2.0, 0.0
            ));

            // Height diff = 3 blocks (< 8 minimum)
            int[] heightmap = new int[16 * 16];
            for (int i = 0; i < heightmap.length; i++) {
                heightmap[i] = 65;
            }
            heightmap[2 + 2 * 16] = 68;
            heightmap[14 + 2 * 16] = 65;

            List<WaterfallDetector.WaterfallPoint> waterfalls =
                    detector.detect(network, heightmap, 0, 0);

            assertTrue(waterfalls.isEmpty(),
                    "Gentle gradient (< 8 blocks) should not produce a waterfall");
        }

        @Test
        @DisplayName("No waterfall when horizontal distance exceeds limit")
        void noWaterfallWhenTooFarHorizontally() {
            WaterfallDetector detector = new WaterfallDetector();

            RiverNetwork network = new RiverNetwork();
            // Segment spanning more than 16 blocks horizontally
            // x1=0, z1=0, x2=20, z2=0 -> length ~20 > 16
            network.addSegment(new RiverNetwork.RiverSegment(
                    0.0, 0.0, 20.0, 0.0, 3.0, 2.0, 0.0
            ));

            int[] heightmap = new int[16 * 16];
            for (int i = 0; i < heightmap.length; i++) {
                heightmap[i] = 65;
            }
            heightmap[0] = 80;
            heightmap[15] = 60;

            List<WaterfallDetector.WaterfallPoint> waterfalls =
                    detector.detect(network, heightmap, 0, 0);

            assertTrue(waterfalls.isEmpty(),
                    "Segment too long horizontally should not produce waterfall");
        }

        @Test
        @DisplayName("Empty network produces no waterfalls")
        void emptyNetworkNoWaterfalls() {
            WaterfallDetector detector = new WaterfallDetector();
            RiverNetwork network = new RiverNetwork();
            int[] heightmap = new int[16 * 16];

            List<WaterfallDetector.WaterfallPoint> waterfalls =
                    detector.detect(network, heightmap, 0, 0);

            assertTrue(waterfalls.isEmpty());
        }

        @Test
        @DisplayName("Waterfall point height() returns correct value")
        void waterfallPointHeight() {
            WaterfallDetector.WaterfallPoint point =
                    new WaterfallDetector.WaterfallPoint(10, 20, 80, 60, 3);
            assertEquals(20, point.height());
        }

        @Test
        @DisplayName("Multiple waterfalls detected from multiple steep segments")
        void multipleWaterfalls() {
            WaterfallDetector detector = new WaterfallDetector();

            RiverNetwork network = new RiverNetwork();
            // Two steep river segments in the same chunk
            network.addSegment(new RiverNetwork.RiverSegment(
                    2.0, 2.0, 8.0, 2.0, 3.0, 2.0, 0.0
            ));
            network.addSegment(new RiverNetwork.RiverSegment(
                    2.0, 10.0, 8.0, 10.0, 3.0, 2.0, 0.0
            ));

            int[] heightmap = new int[16 * 16];
            for (int i = 0; i < heightmap.length; i++) {
                heightmap[i] = 65;
            }
            // First segment: steep
            heightmap[2 + 2 * 16] = 80;
            heightmap[8 + 2 * 16] = 60;
            // Second segment: steep
            heightmap[2 + 10 * 16] = 90;
            heightmap[8 + 10 * 16] = 70;

            List<WaterfallDetector.WaterfallPoint> waterfalls =
                    detector.detect(network, heightmap, 0, 0);

            assertEquals(2, waterfalls.size(),
                    "Should detect two waterfalls from two steep segments");
        }
    }

    // ================================================================ Lake Detection

    @Nested
    @DisplayName("LakeDetector")
    class LakeDetectorTests {

        @Test
        @DisplayName("Detects depression in heightmap")
        void detectsDepression() {
            LakeDetector detector = new LakeDetector();

            // Create a 16x16 heightmap with a depression at (8, 8)
            int[] heightmap = new int[16 * 16];
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    heightmap[x + z * 16] = 70; // surrounding terrain
                }
            }
            // Create a minimum at (8, 8)
            heightmap[8 + 8 * 16] = 60;

            List<LakeDetector.LakeRegion> lakes = detector.detect(heightmap, 16, 42L);

            assertFalse(lakes.isEmpty(), "Should detect a lake in the terrain depression");
        }

        @Test
        @DisplayName("Flat terrain produces no lakes")
        void flatTerrainNoLakes() {
            LakeDetector detector = new LakeDetector();

            int[] heightmap = new int[16 * 16];
            for (int i = 0; i < heightmap.length; i++) {
                heightmap[i] = 64;
            }

            List<LakeDetector.LakeRegion> lakes = detector.detect(heightmap, 16, 42L);
            assertTrue(lakes.isEmpty(), "Flat terrain should produce no lakes");
        }

        @Test
        @DisplayName("Shallow depression below min depth is rejected")
        void shallowDepressionRejected() {
            LakeDetector detector = new LakeDetector();

            int[] heightmap = new int[16 * 16];
            for (int i = 0; i < heightmap.length; i++) {
                heightmap[i] = 64;
            }
            // Depression of only 1 block (below MIN_DEPTH of 2)
            heightmap[8 + 8 * 16] = 63;

            List<LakeDetector.LakeRegion> lakes = detector.detect(heightmap, 16, 42L);
            assertTrue(lakes.isEmpty(),
                    "Depression shallower than min depth should not produce a lake");
        }

        @Test
        @DisplayName("Lake region has valid dimensions")
        void lakeRegionDimensions() {
            LakeDetector detector = new LakeDetector();

            int[] heightmap = new int[16 * 16];
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    heightmap[x + z * 16] = 80;
                }
            }
            heightmap[8 + 8 * 16] = 64;

            List<LakeDetector.LakeRegion> lakes = detector.detect(heightmap, 16, 42L);

            for (LakeDetector.LakeRegion lake : lakes) {
                assertTrue(lake.radius() >= 3, "Radius should be at least MIN_RADIUS=3");
                assertTrue(lake.radius() <= 7, "Radius should be at most MAX_RADIUS=7");
                assertTrue(lake.depth() >= 2, "Depth should be at least MIN_DEPTH=2");
                assertTrue(lake.depth() <= 6, "Depth should be at most MAX_DEPTH=6");
                assertTrue(lake.centerX() >= 0 && lake.centerX() < 16,
                        "Center X should be within chunk");
                assertTrue(lake.centerZ() >= 0 && lake.centerZ() < 16,
                        "Center Z should be within chunk");
            }
        }

        @Test
        @DisplayName("Different seeds produce different lake configurations")
        void differentSeedsDifferentLakes() {
            LakeDetector detector = new LakeDetector();

            int[] heightmap = new int[16 * 16];
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    heightmap[x + z * 16] = 80;
                }
            }
            heightmap[8 + 8 * 16] = 64;

            List<LakeDetector.LakeRegion> lakes1 = detector.detect(heightmap, 16, 42L);
            List<LakeDetector.LakeRegion> lakes2 = detector.detect(heightmap, 16, 99L);

            // Same heightmap but different seeds -> different size/depth due to hash-based sizing
            if (!lakes1.isEmpty() && !lakes2.isEmpty()) {
                boolean anyDifference = false;
                int count = Math.min(lakes1.size(), lakes2.size());
                for (int i = 0; i < count; i++) {
                    if (lakes1.get(i).radius() != lakes2.get(i).radius()
                            || lakes1.get(i).depth() != lakes2.get(i).depth()) {
                        anyDifference = true;
                        break;
                    }
                }
                assertTrue(anyDifference,
                        "Different seeds should produce different lake sizing");
            }
        }
    }

    // ================================================================ Lake Filling

    @Nested
    @DisplayName("LakeFiller")
    class LakeFillerTests {

        @Test
        @DisplayName("Fills lake region with water blocks")
        void fillsWithWater() {
            LakeFiller filler = new LakeFiller();
            StrataProtoChunk chunk = new StrataProtoChunk(new ChunkCoord(0, 0), -64, 320);

            // Fill stone base
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = 0; y < 80; y++) {
                        chunk.setBlock(x, y, z, STONE);
                    }
                }
            }

            LakeDetector.LakeRegion lake = new LakeDetector.LakeRegion(8, 8, 4, 70, 3);

            filler.fill(chunk, lake, 63);

            // Check that water exists in the lake area
            boolean foundWater = false;
            for (int dx = -4; dx <= 4; dx++) {
                for (int dz = -4; dz <= 4; dz++) {
                    int wx = 8 + dx;
                    int wz = 8 + dz;
                    if (wx < 0 || wx >= 16 || wz < 0 || wz >= 16) continue;

                    StrataBlockState block = chunk.getBlock(wx, 70, wz);
                    if (block.equals(WATER)) {
                        foundWater = true;
                        break;
                    }
                }
                if (foundWater) break;
            }
            assertTrue(foundWater, "Lake area should contain water blocks");
        }

        @Test
        @DisplayName("Lake water does not extend beyond radius")
        void waterDoesNotExceedRadius() {
            LakeFiller filler = new LakeFiller();
            StrataProtoChunk chunk = new StrataProtoChunk(new ChunkCoord(0, 0), -64, 320);

            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = 0; y < 80; y++) {
                        chunk.setBlock(x, y, z, STONE);
                    }
                }
            }

            // Small lake at (8, 8) with radius 3
            LakeDetector.LakeRegion lake = new LakeDetector.LakeRegion(8, 8, 3, 70, 3);
            filler.fill(chunk, lake, 63);

            // Check corners far outside radius -- should still be stone or air
            StrataBlockState corner = chunk.getBlock(0, 70, 0);
            assertNotEquals(WATER, corner,
                    "Corner blocks outside lake radius should not be water");

            corner = chunk.getBlock(15, 70, 15);
            assertNotEquals(WATER, corner,
                    "Far corner should not be water");
        }

        @Test
        @DisplayName("Lake has depth variation (deeper at center)")
        void lakeDepthVariation() {
            LakeFiller filler = new LakeFiller();
            StrataProtoChunk chunk = new StrataProtoChunk(new ChunkCoord(0, 0), -64, 320);

            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = 0; y < 80; y++) {
                        chunk.setBlock(x, y, z, STONE);
                    }
                }
            }

            LakeDetector.LakeRegion lake = new LakeDetector.LakeRegion(8, 8, 5, 70, 4);
            filler.fill(chunk, lake, 63);

            // Center should be deepest -- check that water reaches deeper at the center
            // than at the edge
            int centerWaterDepth = 0;
            for (int y = 70; y >= 60; y--) {
                if (chunk.getBlock(8, y, 8).equals(WATER)) {
                    centerWaterDepth++;
                } else {
                    break;
                }
            }

            // Edge water depth (near radius boundary)
            int edgeWaterDepth = 0;
            int edgeX = 8 + 4; // Near edge
            if (edgeX < 16) {
                for (int y = 70; y >= 60; y--) {
                    if (chunk.getBlock(edgeX, y, 8).equals(WATER)) {
                        edgeWaterDepth++;
                    } else {
                        break;
                    }
                }
            }

            assertTrue(centerWaterDepth >= edgeWaterDepth,
                    "Center should be at least as deep as edges: center="
                            + centerWaterDepth + " edge=" + edgeWaterDepth);
        }

        @Test
        @DisplayName("Warm biome lakes can have lily pads")
        void warmBiomeLilyPads() {
            LakeFiller filler = new LakeFiller();
            StrataProtoChunk chunk = new StrataProtoChunk(new ChunkCoord(0, 0), -64, 320);

            // Set up warm biome
            Biome warmBiome = mock(Biome.class);
            BiomeEffects warmEffects = new BiomeEffects(
                    0xC0D8FF, 0x3F76E4, 0x050533, 0x78A7FF,
                    java.util.OptionalInt.empty(), java.util.OptionalInt.empty(),
                    BiomeEffects.GrassColorModifier.NONE, java.util.OptionalInt.empty(),
                    0.8f, true  // temperature 0.8 >= 0.5 threshold
            );
            when(warmBiome.effects()).thenReturn(warmEffects);

            // Fill stone and set biome
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = 0; y < 80; y++) {
                        chunk.setBlock(x, y, z, STONE);
                    }
                    chunk.setBiome(x, 70, z, warmBiome);
                }
            }

            LakeDetector.LakeRegion lake = new LakeDetector.LakeRegion(8, 8, 5, 70, 3);
            filler.fill(chunk, lake, 63);

            // Check if any lily pads were placed (they appear at surfaceY + 1
            // at non-edge positions where (dx + dz) % 3 == 0)
            StrataBlockState lilyPad = StrataBlockState.of(NamespacedKey.minecraft("lily_pad"));
            boolean foundLilyPad = false;
            for (int x = 4; x <= 12; x++) {
                for (int z = 4; z <= 12; z++) {
                    if (chunk.getBlock(x, 71, z).equals(lilyPad)) {
                        foundLilyPad = true;
                        break;
                    }
                }
                if (foundLilyPad) break;
            }
            assertTrue(foundLilyPad,
                    "Warm biome lakes should have lily pads placed on surface");
        }
    }

    // ================================================================ End-to-end: river -> waterfall

    @Nested
    @DisplayName("End-to-end: river to waterfall pipeline")
    class EndToEndTests {

        @Test
        @DisplayName("River network feeds into waterfall detection")
        void riverToWaterfall() {
            // Build a river network and add a steep segment
            RiverNetwork network = new RiverNetwork();
            // Steep segment within chunk (0,0)
            network.addSegment(new RiverNetwork.RiverSegment(
                    3.0, 3.0, 10.0, 3.0, 4.0, 3.0, 0.0
            ));

            int[] heightmap = new int[16 * 16];
            for (int i = 0; i < heightmap.length; i++) {
                heightmap[i] = 65;
            }
            heightmap[3 + 3 * 16] = 85;  // high elevation at start
            heightmap[10 + 3 * 16] = 65; // low elevation at end

            WaterfallDetector detector = new WaterfallDetector();
            List<WaterfallDetector.WaterfallPoint> waterfalls =
                    detector.detect(network, heightmap, 0, 0);

            assertFalse(waterfalls.isEmpty(),
                    "Steep river segment should be detected as a waterfall");
            assertEquals(20, waterfalls.get(0).height());
        }

        @Test
        @DisplayName("Lake detection and filling pipeline")
        void lakeDetectionAndFilling() {
            // Create terrain with a depression
            int[] heightmap = new int[16 * 16];
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    heightmap[x + z * 16] = 70;
                }
            }
            heightmap[8 + 8 * 16] = 60;

            // Detect lakes
            LakeDetector detector = new LakeDetector();
            List<LakeDetector.LakeRegion> lakes = detector.detect(heightmap, 16, 42L);

            if (!lakes.isEmpty()) {
                // Fill the detected lake
                LakeFiller filler = new LakeFiller();
                StrataProtoChunk chunk = new StrataProtoChunk(new ChunkCoord(0, 0), -64, 320);
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        for (int y = 0; y < 80; y++) {
                            chunk.setBlock(x, y, z, STONE);
                        }
                    }
                }

                filler.fill(chunk, lakes.get(0), 63);

                // Verify water was actually placed
                boolean foundWater = false;
                LakeDetector.LakeRegion lake = lakes.get(0);
                for (int dx = -lake.radius(); dx <= lake.radius() && !foundWater; dx++) {
                    for (int dz = -lake.radius(); dz <= lake.radius() && !foundWater; dz++) {
                        int wx = lake.centerX() + dx;
                        int wz = lake.centerZ() + dz;
                        if (wx >= 0 && wx < 16 && wz >= 0 && wz < 16) {
                            if (chunk.getBlock(wx, lake.surfaceY(), wz).equals(WATER)) {
                                foundWater = true;
                            }
                        }
                    }
                }
                assertTrue(foundWater,
                        "Detected lake should produce water blocks when filled");
            }
        }
    }
}
