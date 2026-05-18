package com.protectcord.strata.core.biome;

import com.protectcord.strata.api.biome.Biome;
import com.protectcord.strata.api.biome.ClimateParameters;
import com.protectcord.strata.api.core.NamespacedKey;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link BiomeLookupTable} KD-tree nearest-neighbor biome lookup.
 */
@MockitoSettings(strictness = Strictness.LENIENT)
class BiomeLookupTableTest {

    // ------------------------------------------------------------------ helpers

    /**
     * Creates a mock Biome with the given name and climate parameters.
     */
    private Biome mockBiome(String name, double temperature, double humidity,
                            double continentalness, double erosion, double weirdness) {
        Biome biome = mock(Biome.class, name);
        when(biome.climate()).thenReturn(
                new ClimateParameters(temperature, humidity, continentalness, erosion, weirdness));
        when(biome.key()).thenReturn(NamespacedKey.strata(name));
        return biome;
    }

    /**
     * Brute-force nearest-neighbor search for verification purposes.
     */
    private Biome bruteForceNearest(List<Biome> biomes, ClimateParameters target) {
        Biome best = null;
        double bestDist = Double.MAX_VALUE;
        for (Biome b : biomes) {
            double dist = target.distanceSquared(b.climate());
            if (dist < bestDist) {
                bestDist = dist;
                best = b;
            }
        }
        return best;
    }

    // ------------------------------------------------ empty list throws

    @Test
    void build_emptyList_throwsIllegalArgumentException() {
        List<Biome> empty = List.of();
        assertThrows(IllegalArgumentException.class, () -> BiomeLookupTable.build(empty));
    }

    // ------------------------------------------------ single biome

    @Test
    void lookup_singleBiome_alwaysReturnsThatBiome() {
        Biome plains = mockBiome("plains", 0.5, 0.3, 0.7, -0.2, 0.1);
        BiomeLookupTable table = BiomeLookupTable.build(List.of(plains));

        // Lookup with the exact same parameters
        assertSame(plains, table.lookup(new ClimateParameters(0.5, 0.3, 0.7, -0.2, 0.1)));

        // Lookup from the opposite corner of climate space
        assertSame(plains, table.lookup(new ClimateParameters(-1.0, -1.0, -1.0, -1.0, -1.0)));

        // Lookup from the origin
        assertSame(plains, table.lookup(new ClimateParameters(0.0, 0.0, 0.0, 0.0, 0.0)));

        // Lookup from extreme positive
        assertSame(plains, table.lookup(new ClimateParameters(1.0, 1.0, 1.0, 1.0, 1.0)));
    }

    // ------------------------------------------------ two biomes at opposite extremes

    @Test
    void lookup_twoBiomesOppositeExtremes_returnsNearest() {
        Biome hot = mockBiome("hot_desert", 1.0, -1.0, 1.0, -1.0, 0.0);
        Biome cold = mockBiome("frozen_tundra", -1.0, 1.0, -1.0, 1.0, 0.0);
        BiomeLookupTable table = BiomeLookupTable.build(List.of(hot, cold));

        // Query close to hot
        Biome result = table.lookup(new ClimateParameters(0.9, -0.8, 0.8, -0.7, 0.0));
        assertSame(hot, result, "Should return hot desert for warm/dry query");

        // Query close to cold
        result = table.lookup(new ClimateParameters(-0.8, 0.9, -0.7, 0.8, 0.0));
        assertSame(cold, result, "Should return frozen tundra for cold/wet query");

        // Query at exact midpoint -- either is acceptable but it should not crash
        result = table.lookup(new ClimateParameters(0.0, 0.0, 0.0, 0.0, 0.0));
        assertNotNull(result, "Midpoint query should still return a biome");
        assertTrue(result == hot || result == cold, "Midpoint should return one of the two biomes");
    }

    // ------------------------------------------------ exact match

    @Test
    void lookup_exactMatchOnParameters_returnsThatBiome() {
        Biome forest = mockBiome("forest", 0.4, 0.6, 0.3, -0.1, 0.2);
        Biome desert = mockBiome("desert", 0.9, -0.5, 0.8, 0.3, -0.1);
        Biome taiga = mockBiome("taiga", -0.3, 0.4, 0.5, 0.0, 0.1);
        BiomeLookupTable table = BiomeLookupTable.build(List.of(forest, desert, taiga));

        assertSame(forest, table.lookup(new ClimateParameters(0.4, 0.6, 0.3, -0.1, 0.2)));
        assertSame(desert, table.lookup(new ClimateParameters(0.9, -0.5, 0.8, 0.3, -0.1)));
        assertSame(taiga, table.lookup(new ClimateParameters(-0.3, 0.4, 0.5, 0.0, 0.1)));
    }

    // ------------------------------------------------ many biomes -- brute force verification

    @Test
    void lookup_manyBiomes_matchesBruteForceResult() {
        // Build 12 biomes spread across climate space
        List<Biome> biomes = new ArrayList<>();
        biomes.add(mockBiome("plains", 0.5, 0.4, 0.3, -0.1, 0.0));
        biomes.add(mockBiome("desert", 0.9, -0.7, 0.8, 0.4, -0.2));
        biomes.add(mockBiome("taiga", -0.5, 0.5, 0.4, 0.1, 0.1));
        biomes.add(mockBiome("tundra", -0.9, 0.2, -0.3, 0.7, -0.1));
        biomes.add(mockBiome("jungle", 0.8, 0.9, 0.2, -0.5, 0.3));
        biomes.add(mockBiome("ocean", 0.0, 0.0, -0.9, 0.0, 0.0));
        biomes.add(mockBiome("mountains", -0.2, -0.3, 0.7, 0.9, 0.5));
        biomes.add(mockBiome("swamp", 0.3, 0.8, -0.1, -0.4, -0.3));
        biomes.add(mockBiome("savanna", 0.7, -0.4, 0.5, 0.2, 0.4));
        biomes.add(mockBiome("mushroom_island", 0.1, 0.1, -0.8, -0.8, 0.9));
        biomes.add(mockBiome("ice_spikes", -0.8, -0.6, 0.1, 0.5, -0.7));
        biomes.add(mockBiome("badlands", 0.6, -0.9, 0.9, 0.6, 0.2));

        BiomeLookupTable table = BiomeLookupTable.build(biomes);

        // Test a grid of query points across the 5D space
        double[] sampleValues = {-1.0, -0.5, 0.0, 0.5, 1.0};
        for (double t : sampleValues) {
            for (double h : sampleValues) {
                for (double c : sampleValues) {
                    ClimateParameters query = new ClimateParameters(t, h, c, 0.0, 0.0);
                    Biome kdResult = table.lookup(query);
                    Biome bfResult = bruteForceNearest(biomes, query);
                    assertSame(bfResult, kdResult,
                            () -> "KD-tree mismatch at (" + t + ", " + h + ", " + c + ", 0, 0): "
                                    + "expected " + bfResult.key() + " but got " + kdResult.key());
                }
            }
        }
    }

    // ------------------------------------------------ all 5 dimensions varying

    @Test
    void lookup_allFiveDimensionsVarying_correctNearestNeighbor() {
        List<Biome> biomes = new ArrayList<>();
        biomes.add(mockBiome("biome_a", 0.1, 0.2, 0.3, 0.4, 0.5));
        biomes.add(mockBiome("biome_b", -0.1, -0.2, -0.3, -0.4, -0.5));
        biomes.add(mockBiome("biome_c", 0.8, -0.7, 0.6, -0.5, 0.4));
        biomes.add(mockBiome("biome_d", -0.6, 0.7, -0.8, 0.9, -0.3));
        biomes.add(mockBiome("biome_e", 0.0, 0.0, 0.0, 0.0, 0.0));

        BiomeLookupTable table = BiomeLookupTable.build(biomes);

        // Queries designed to be uniquely nearest to specific biomes across all 5 dimensions
        ClimateParameters[] queries = {
                new ClimateParameters(0.12, 0.18, 0.28, 0.38, 0.48),  // near biome_a
                new ClimateParameters(-0.12, -0.18, -0.28, -0.38, -0.48), // near biome_b
                new ClimateParameters(0.75, -0.65, 0.55, -0.45, 0.35),  // near biome_c
                new ClimateParameters(-0.55, 0.65, -0.75, 0.85, -0.25), // near biome_d
                new ClimateParameters(0.02, -0.01, 0.03, -0.02, 0.01),  // near biome_e (origin)
        };

        for (ClimateParameters query : queries) {
            Biome kdResult = table.lookup(query);
            Biome bfResult = bruteForceNearest(biomes, query);
            assertSame(bfResult, kdResult,
                    () -> "KD-tree mismatch for query " + query + ": expected "
                            + bfResult.key() + " but got " + kdResult.key());
        }
    }

    // ------------------------------------------------ stress: large number of biomes

    @Test
    void lookup_largeBiomeSet_matchesBruteForce() {
        // Generate 50 biomes with pseudo-random but deterministic climate values
        List<Biome> biomes = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            double t = Math.sin(i * 1.3) * 0.9;
            double h = Math.cos(i * 2.1) * 0.9;
            double c = Math.sin(i * 0.7 + 1.0) * 0.9;
            double e = Math.cos(i * 1.1 + 2.0) * 0.9;
            double w = Math.sin(i * 1.9 + 3.0) * 0.9;
            biomes.add(mockBiome("biome_" + i, t, h, c, e, w));
        }

        BiomeLookupTable table = BiomeLookupTable.build(biomes);

        // Test 20 scattered query points
        for (int i = 0; i < 20; i++) {
            final int idx = i;
            double t = Math.cos(idx * 3.7) * 0.8;
            double h = Math.sin(idx * 2.3) * 0.8;
            double c = Math.cos(idx * 1.9 + 0.5) * 0.8;
            double e = Math.sin(idx * 1.1 + 1.5) * 0.8;
            double w = Math.cos(idx * 0.9 + 2.5) * 0.8;
            ClimateParameters query = new ClimateParameters(t, h, c, e, w);

            Biome kdResult = table.lookup(query);
            Biome bfResult = bruteForceNearest(biomes, query);
            assertSame(bfResult, kdResult,
                    () -> "Large set KD-tree mismatch for query #" + idx + ": expected "
                            + bfResult.key() + " but got " + kdResult.key());
        }
    }

    // ------------------------------------------------ boundary: all biomes at same point

    @Test
    void lookup_allBiomesAtSameClimate_returnsOneOfThem() {
        ClimateParameters shared = new ClimateParameters(0.5, 0.5, 0.5, 0.5, 0.5);
        Biome a = mockBiome("dup_a", 0.5, 0.5, 0.5, 0.5, 0.5);
        Biome b = mockBiome("dup_b", 0.5, 0.5, 0.5, 0.5, 0.5);
        Biome c = mockBiome("dup_c", 0.5, 0.5, 0.5, 0.5, 0.5);

        BiomeLookupTable table = BiomeLookupTable.build(List.of(a, b, c));

        Biome result = table.lookup(shared);
        assertNotNull(result);
        assertTrue(result == a || result == b || result == c,
                "Should return one of the co-located biomes");
    }

    // ------------------------------------------------ boundary: extreme parameter values

    @Test
    void lookup_extremeParameterValues_doesNotFail() {
        Biome biome = mockBiome("extreme_biome", 0.0, 0.0, 0.0, 0.0, 0.0);
        BiomeLookupTable table = BiomeLookupTable.build(List.of(biome));

        // Very large values outside normal [-1, 1] range should still work
        assertSame(biome, table.lookup(new ClimateParameters(100.0, -100.0, 50.0, -50.0, 999.0)));
        assertSame(biome, table.lookup(new ClimateParameters(1000.0, -1000.0, 500.0, -500.0, 0.0)));
    }
}
