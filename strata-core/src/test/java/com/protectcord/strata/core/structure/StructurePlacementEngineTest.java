package com.protectcord.strata.core.structure;

import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.api.structure.StructureDefinition;
import com.protectcord.strata.api.structure.StructurePlacement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link StructurePlacementEngine} grid-based structure placement.
 */
@DisplayName("StructurePlacementEngine Tests")
@MockitoSettings(strictness = Strictness.LENIENT)
class StructurePlacementEngineTest {

    private StructurePlacementEngine engine;

    @BeforeEach
    void setUp() {
        engine = new StructurePlacementEngine();
    }

    // ------------------------------------------------------------------ helpers

    /**
     * Creates a mock StructureDefinition with the given placement parameters.
     */
    private StructureDefinition mockStructure(String name, int spacing, int separation, int salt) {
        return mockStructure(name, spacing, separation, salt, StructurePlacement.SpreadType.LINEAR);
    }

    private StructureDefinition mockStructure(String name, int spacing, int separation, int salt,
                                               StructurePlacement.SpreadType spreadType) {
        StructureDefinition def = mock(StructureDefinition.class, name);
        when(def.key()).thenReturn(NamespacedKey.strata(name));
        when(def.placement()).thenReturn(
                new StructurePlacement(spacing, separation, salt, spreadType));
        when(def.validBiomes()).thenReturn(List.of());
        return def;
    }

    /**
     * Searches all chunks in a grid to find where a given structure places.
     */
    private List<StructurePlacementEngine.StructureStart> findAllStarts(
            StructureDefinition def, long seed, int searchRadius) {
        List<StructurePlacementEngine.StructureStart> allStarts = new ArrayList<>();
        for (int cx = -searchRadius; cx <= searchRadius; cx++) {
            for (int cz = -searchRadius; cz <= searchRadius; cz++) {
                List<StructurePlacementEngine.StructureStart> starts =
                        engine.getStarts(cx, cz, seed, List.of(def));
                allStarts.addAll(starts);
            }
        }
        return allStarts;
    }

    // ================================================================ grid spacing verification

    @Nested
    @DisplayName("Grid spacing verification")
    class GridSpacingTests {

        @Test
        @DisplayName("Structure places exactly once per grid cell")
        void exactlyOncePerGridCell() {
            StructureDefinition structure = mockStructure("village", 32, 8, 10387312);
            long seed = 42L;

            // Search a large area to find multiple placements
            List<StructurePlacementEngine.StructureStart> starts =
                    findAllStarts(structure, seed, 64);

            // Should find at least some structures in a 128x128 chunk area
            // Grid cell is 32 chunks, so 128/32 = 4 cells per axis = ~16 cells
            assertFalse(starts.isEmpty(), "Should find at least one structure in search area");

            // Verify no two starts share the same grid cell
            Set<Long> gridCells = new HashSet<>();
            for (var start : starts) {
                int gridX = Math.floorDiv(start.chunkX(), 32);
                int gridZ = Math.floorDiv(start.chunkZ(), 32);
                long cellKey = ((long) gridX << 32) | (gridZ & 0xFFFFFFFFL);
                assertTrue(gridCells.add(cellKey),
                        "Two structures should not occupy the same grid cell: grid("
                                + gridX + ", " + gridZ + ")");
            }
        }

        @Test
        @DisplayName("Structure starts are within their grid cell bounds")
        void startsWithinGridCell() {
            int spacing = 16;
            StructureDefinition structure = mockStructure("temple", spacing, 4, 14357620);
            long seed = 123456L;

            List<StructurePlacementEngine.StructureStart> starts =
                    findAllStarts(structure, seed, 48);

            for (var start : starts) {
                int gridX = Math.floorDiv(start.chunkX(), spacing);
                int gridZ = Math.floorDiv(start.chunkZ(), spacing);

                int cellMinX = gridX * spacing;
                int cellMinZ = gridZ * spacing;

                assertTrue(start.chunkX() >= cellMinX && start.chunkX() < cellMinX + spacing,
                        "Structure at chunk (" + start.chunkX() + ", " + start.chunkZ()
                                + ") should be within grid cell [" + cellMinX + ", "
                                + (cellMinX + spacing) + ")");
                assertTrue(start.chunkZ() >= cellMinZ && start.chunkZ() < cellMinZ + spacing,
                        "Structure at chunk (" + start.chunkX() + ", " + start.chunkZ()
                                + ") should be within grid cell [" + cellMinZ + ", "
                                + (cellMinZ + spacing) + ")");
            }
        }

        @Test
        @DisplayName("Separation distance is respected between starts")
        void separationDistanceRespected() {
            int spacing = 20;
            int separation = 8;
            StructureDefinition structure = mockStructure("mansion", spacing, separation, 10387319);
            long seed = 99999L;

            List<StructurePlacementEngine.StructureStart> starts =
                    findAllStarts(structure, seed, 60);

            // Offsets must be >= separation from cell origin, so check that offsets
            // within the cell are at least separation
            for (var start : starts) {
                int gridX = Math.floorDiv(start.chunkX(), spacing);
                int gridZ = Math.floorDiv(start.chunkZ(), spacing);
                int offsetX = start.chunkX() - gridX * spacing;
                int offsetZ = start.chunkZ() - gridZ * spacing;

                assertTrue(offsetX >= separation,
                        "X offset " + offsetX + " should be >= separation " + separation);
                assertTrue(offsetZ >= separation,
                        "Z offset " + offsetZ + " should be >= separation " + separation);
            }
        }
    }

    // ================================================================ salt-based offset

    @Nested
    @DisplayName("Salt-based offset")
    class SaltTests {

        @Test
        @DisplayName("Different salts produce different placements")
        void differentSaltsDifferentPlacements() {
            StructureDefinition struct1 = mockStructure("village", 32, 8, 10387312);
            StructureDefinition struct2 = mockStructure("temple", 32, 8, 14357620);
            long seed = 42L;

            List<StructurePlacementEngine.StructureStart> starts1 =
                    findAllStarts(struct1, seed, 64);
            List<StructurePlacementEngine.StructureStart> starts2 =
                    findAllStarts(struct2, seed, 64);

            // Two structures with different salts should not have identical placement sets
            Set<Long> locations1 = new HashSet<>();
            for (var s : starts1) {
                locations1.add(((long) s.chunkX() << 32) | (s.chunkZ() & 0xFFFFFFFFL));
            }
            Set<Long> locations2 = new HashSet<>();
            for (var s : starts2) {
                locations2.add(((long) s.chunkX() << 32) | (s.chunkZ() & 0xFFFFFFFFL));
            }

            assertNotEquals(locations1, locations2,
                    "Different salt values should produce different placement locations");
        }

        @Test
        @DisplayName("Same salt and seed produce deterministic placement")
        void sameSaltDeterministic() {
            StructureDefinition structure = mockStructure("village", 32, 8, 10387312);
            long seed = 42L;

            List<StructurePlacementEngine.StructureStart> starts1 =
                    findAllStarts(structure, seed, 32);
            List<StructurePlacementEngine.StructureStart> starts2 =
                    findAllStarts(structure, seed, 32);

            assertEquals(starts1.size(), starts2.size(),
                    "Same parameters should produce same number of structures");

            for (int i = 0; i < starts1.size(); i++) {
                assertEquals(starts1.get(i).chunkX(), starts2.get(i).chunkX(),
                        "Structure X should be deterministic");
                assertEquals(starts1.get(i).chunkZ(), starts2.get(i).chunkZ(),
                        "Structure Z should be deterministic");
            }
        }

        @Test
        @DisplayName("Different world seeds produce different placements")
        void differentSeedsDifferentPlacements() {
            StructureDefinition structure = mockStructure("village", 32, 8, 10387312);

            List<StructurePlacementEngine.StructureStart> starts1 =
                    findAllStarts(structure, 42L, 64);
            List<StructurePlacementEngine.StructureStart> starts2 =
                    findAllStarts(structure, 99L, 64);

            // Different seeds should produce different placement sets
            Set<Long> locations1 = new HashSet<>();
            for (var s : starts1) {
                locations1.add(((long) s.chunkX() << 32) | (s.chunkZ() & 0xFFFFFFFFL));
            }
            Set<Long> locations2 = new HashSet<>();
            for (var s : starts2) {
                locations2.add(((long) s.chunkX() << 32) | (s.chunkZ() & 0xFFFFFFFFL));
            }

            assertNotEquals(locations1, locations2,
                    "Different world seeds should produce different placement locations");
        }
    }

    // ================================================================ spread type

    @Nested
    @DisplayName("Spread type")
    class SpreadTypeTests {

        @Test
        @DisplayName("LINEAR spread distributes within grid")
        void linearSpread() {
            StructureDefinition structure = mockStructure("monument", 32, 5, 10387313,
                    StructurePlacement.SpreadType.LINEAR);
            long seed = 42L;

            List<StructurePlacementEngine.StructureStart> starts =
                    findAllStarts(structure, seed, 64);

            assertFalse(starts.isEmpty(),
                    "LINEAR spread should produce placements");
        }

        @Test
        @DisplayName("TRIANGULAR spread produces different placements than LINEAR")
        void triangularDiffersFromLinear() {
            StructureDefinition linear = mockStructure("struct_linear", 32, 8, 12345,
                    StructurePlacement.SpreadType.LINEAR);
            StructureDefinition triangular = mockStructure("struct_triangular", 32, 8, 12345,
                    StructurePlacement.SpreadType.TRIANGULAR);
            long seed = 42L;

            List<StructurePlacementEngine.StructureStart> linearStarts =
                    findAllStarts(linear, seed, 64);
            List<StructurePlacementEngine.StructureStart> triangularStarts =
                    findAllStarts(triangular, seed, 64);

            // Both should produce structures
            assertFalse(linearStarts.isEmpty(), "LINEAR should produce starts");
            assertFalse(triangularStarts.isEmpty(), "TRIANGULAR should produce starts");

            // With same salt, the triangular offset differs from linear
            // due to the staggered row logic
            Set<Long> linearLocs = new HashSet<>();
            for (var s : linearStarts) {
                linearLocs.add(((long) s.chunkX() << 32) | (s.chunkZ() & 0xFFFFFFFFL));
            }
            Set<Long> triLocs = new HashSet<>();
            for (var s : triangularStarts) {
                triLocs.add(((long) s.chunkX() << 32) | (s.chunkZ() & 0xFFFFFFFFL));
            }

            assertNotEquals(linearLocs, triLocs,
                    "TRIANGULAR and LINEAR should produce different placements with same salt");
        }
    }

    // ================================================================ multiple structure types

    @Nested
    @DisplayName("Multiple structure types")
    class MultipleStructureTests {

        @Test
        @DisplayName("Multiple structures can start in the same chunk")
        void multipleStructuresInSameChunk() {
            // Use extremely small spacing to increase chance of overlap
            StructureDefinition struct1 = mockStructure("small_a", 1, 0, 111);
            StructureDefinition struct2 = mockStructure("small_b", 1, 0, 222);
            long seed = 42L;

            // With spacing=1 and separation=0, every chunk is a valid start
            List<StructurePlacementEngine.StructureStart> starts =
                    engine.getStarts(0, 0, seed, List.of(struct1, struct2));

            assertEquals(2, starts.size(),
                    "Both structures should start when spacing=1, separation=0");
        }

        @Test
        @DisplayName("Empty structure list returns no starts")
        void emptyStructureList() {
            List<StructurePlacementEngine.StructureStart> starts =
                    engine.getStarts(0, 0, 42L, List.of());
            assertTrue(starts.isEmpty(), "No structures should produce no starts");
        }
    }

    // ================================================================ StructureStart record

    @Nested
    @DisplayName("StructureStart record")
    class StructureStartTests {

        @Test
        @DisplayName("StructureStart contains correct chunk coordinates and definition")
        void structureStartContainsCorrectData() {
            // Use spacing=1, separation=0 to guarantee a start at (0,0)
            StructureDefinition def = mockStructure("test", 1, 0, 0);
            List<StructurePlacementEngine.StructureStart> starts =
                    engine.getStarts(0, 0, 42L, List.of(def));

            assertFalse(starts.isEmpty(), "Should have a start at (0,0) with spacing 1");
            StructurePlacementEngine.StructureStart start = starts.get(0);
            assertEquals(0, start.chunkX());
            assertEquals(0, start.chunkZ());
            assertSame(def, start.definition());
        }
    }

    // ================================================================ edge cases

    @Nested
    @DisplayName("Edge cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Negative chunk coordinates work correctly")
        void negativeChunkCoordinates() {
            StructureDefinition structure = mockStructure("village", 32, 8, 10387312);
            long seed = 42L;

            // Should not throw for negative coordinates
            assertDoesNotThrow(() ->
                    engine.getStarts(-100, -200, seed, List.of(structure)));
        }

        @Test
        @DisplayName("Separation equal to spacing minus one produces valid results")
        void separationNearSpacing() {
            // spacing=10, separation=9 => range = 1, very constrained placement
            StructureDefinition structure = mockStructure("tight", 10, 9, 54321);
            long seed = 42L;

            List<StructurePlacementEngine.StructureStart> starts =
                    findAllStarts(structure, seed, 20);

            for (var start : starts) {
                int gridX = Math.floorDiv(start.chunkX(), 10);
                int gridZ = Math.floorDiv(start.chunkZ(), 10);
                int offsetX = start.chunkX() - gridX * 10;
                int offsetZ = start.chunkZ() - gridZ * 10;
                assertEquals(9, offsetX,
                        "With range=1, offset must equal separation=9");
                assertEquals(9, offsetZ,
                        "With range=1, offset must equal separation=9");
            }
        }

        @Test
        @DisplayName("Separation equal to spacing produces offset zero")
        void separationEqualSpacing() {
            // spacing=10, separation=10 => range = 0, computeOffset returns 0
            StructureDefinition structure = mockStructure("fixed", 10, 10, 12345);
            long seed = 42L;

            List<StructurePlacementEngine.StructureStart> starts =
                    findAllStarts(structure, seed, 20);

            for (var start : starts) {
                int gridX = Math.floorDiv(start.chunkX(), 10);
                int gridZ = Math.floorDiv(start.chunkZ(), 10);
                int offsetX = start.chunkX() - gridX * 10;
                int offsetZ = start.chunkZ() - gridZ * 10;
                assertEquals(0, offsetX, "Offset should be 0 when range <= 0");
                assertEquals(0, offsetZ, "Offset should be 0 when range <= 0");
            }
        }

        @Test
        @DisplayName("Large world seed does not cause overflow")
        void largeWorldSeed() {
            StructureDefinition structure = mockStructure("village", 32, 8, 10387312);
            assertDoesNotThrow(() ->
                    engine.getStarts(0, 0, Long.MAX_VALUE, List.of(structure)));
            assertDoesNotThrow(() ->
                    engine.getStarts(0, 0, Long.MIN_VALUE, List.of(structure)));
        }
    }
}
