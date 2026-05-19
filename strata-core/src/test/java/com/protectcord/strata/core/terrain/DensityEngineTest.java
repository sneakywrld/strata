package com.protectcord.strata.core.terrain;

import com.protectcord.strata.api.biome.ClimateParameters;
import com.protectcord.strata.api.noise.NoiseFunction;
import com.protectcord.strata.api.pipeline.GenerationContext;
import com.protectcord.strata.api.terrain.TerrainSettings;
import com.protectcord.strata.api.world.WorldProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link DensityEngine} terrain density evaluation.
 */
@DisplayName("DensityEngine Tests")
@MockitoSettings(strictness = Strictness.LENIENT)
class DensityEngineTest {

    @Mock private NoiseFunction densityNoise;
    @Mock private SplineEvaluator splineEvaluator;
    @Mock private GenerationContext ctx;
    @Mock private WorldProfile profile;

    private DensityEngine engine;

    @BeforeEach
    void setUp() {
        engine = new DensityEngine(densityNoise, splineEvaluator);

        TerrainSettings terrain = TerrainSettings.defaultOverworld();
        lenient().when(ctx.profile()).thenReturn(profile);
        lenient().when(profile.terrainSettings()).thenReturn(terrain);
        lenient().when(ctx.seed()).thenReturn(12345L);

        // By default no climate data is in context
        lenient().when(ctx.has("continental_values")).thenReturn(false);
        lenient().when(ctx.has("climate_grid")).thenReturn(false);
    }

    // ================================================================ basic density evaluation

    @Nested
    @DisplayName("Basic density evaluation")
    class BasicDensityTests {

        @Test
        @DisplayName("Below base height produces positive density (solid)")
        void belowBaseHeight_positiveDensity() {
            // Spline returns baseHeight = 60, seaLevel = 63
            // At y=0 (far below), heightBias = (60 + 63 - 0) * 0.02 + 0.0 = 2.46
            // With zero noise, density = 0 + 2.46 = positive -> solid
            when(splineEvaluator.evaluate(0.0, 0.0, 0.0)).thenReturn(60.0);
            when(densityNoise.sample(anyDouble(), anyDouble(), anyDouble())).thenReturn(0.0);

            double density = engine.evaluate(0, 0, 0, ctx);
            assertTrue(density > 0, "Below base height should produce positive density, was: " + density);
        }

        @Test
        @DisplayName("Above base height produces negative density (air)")
        void aboveBaseHeight_negativeDensity() {
            // Spline returns baseHeight = 60, seaLevel = 63
            // At y=200 (far above), heightBias = (60 + 63 - 200) * 0.02 + 0.0 = -1.54
            // With zero noise, density = 0 + (-1.54) = negative -> air
            when(splineEvaluator.evaluate(0.0, 0.0, 0.0)).thenReturn(60.0);
            when(densityNoise.sample(anyDouble(), anyDouble(), anyDouble())).thenReturn(0.0);

            double density = engine.evaluate(0, 200, 0, ctx);
            assertTrue(density < 0, "Above base height should produce negative density, was: " + density);
        }

        @Test
        @DisplayName("Density at exact surface height is near zero")
        void atSurfaceHeight_nearZero() {
            double baseHeight = 60.0;
            int seaLevel = 63;
            // y = baseHeight + seaLevel -> heightBias = 0 + baseHeightOffset(0)
            int surfaceY = (int) baseHeight + seaLevel;

            when(splineEvaluator.evaluate(0.0, 0.0, 0.0)).thenReturn(baseHeight);
            when(densityNoise.sample(anyDouble(), anyDouble(), anyDouble())).thenReturn(0.0);

            double density = engine.evaluate(0, surfaceY, 0, ctx);
            assertEquals(0.0, density, 0.1,
                    "Density at approximate surface should be near zero");
        }
    }

    // ================================================================ stone/air decisions

    @Nested
    @DisplayName("Stone/air threshold decisions")
    class StoneAirTests {

        @Test
        @DisplayName("Positive density means stone")
        void positiveDensity_stone() {
            when(splineEvaluator.evaluate(0.0, 0.0, 0.0)).thenReturn(100.0);
            when(densityNoise.sample(anyDouble(), anyDouble(), anyDouble())).thenReturn(0.5);

            double density = engine.evaluate(8, 10, 8, ctx);
            assertTrue(density > 0, "Deep underground with positive noise should be stone");
        }

        @Test
        @DisplayName("Negative density means air")
        void negativeDensity_air() {
            when(splineEvaluator.evaluate(0.0, 0.0, 0.0)).thenReturn(0.0);
            when(densityNoise.sample(anyDouble(), anyDouble(), anyDouble())).thenReturn(-1.0);

            double density = engine.evaluate(8, 200, 8, ctx);
            assertTrue(density < 0, "High above ground with negative noise should be air");
        }

        @Test
        @DisplayName("Noise can push air position into solid")
        void noisePushesAirToSolid() {
            // At a height just above the surface, positive noise could make it solid
            when(splineEvaluator.evaluate(0.0, 0.0, 0.0)).thenReturn(60.0);
            // y = 124 -> heightBias = (60 + 63 - 124) * 0.02 = -0.02
            // With strong positive noise, total density can be positive
            when(densityNoise.sample(anyDouble(), anyDouble(), anyDouble())).thenReturn(0.5);

            double density = engine.evaluate(0, 124, 0, ctx);
            assertTrue(density > 0,
                    "Strong positive noise should override slightly negative height bias");
        }

        @Test
        @DisplayName("Noise can push solid position into air")
        void noisePushesSolidToAir() {
            // At a height just below the surface, strong negative noise carves air
            when(splineEvaluator.evaluate(0.0, 0.0, 0.0)).thenReturn(60.0);
            // y = 122 -> heightBias = (60 + 63 - 122) * 0.02 = 0.02
            // With strong negative noise, total density can be negative
            when(densityNoise.sample(anyDouble(), anyDouble(), anyDouble())).thenReturn(-0.5);

            double density = engine.evaluate(0, 122, 0, ctx);
            assertTrue(density < 0,
                    "Strong negative noise should override slightly positive height bias");
        }
    }

    // ================================================================ climate grid integration

    @Nested
    @DisplayName("Climate grid integration")
    class ClimateGridTests {

        @Test
        @DisplayName("Climate grid parameters are passed to spline evaluator")
        void climateGridPassedToSpline() {
            ClimateParameters[] grid = new ClimateParameters[16];
            for (int i = 0; i < 16; i++) {
                grid[i] = new ClimateParameters(0.0, 0.0, 0.5, -0.3, 0.2);
            }

            when(ctx.has("climate_grid")).thenReturn(true);
            when(ctx.get("climate_grid", ClimateParameters[].class)).thenReturn(grid);
            when(splineEvaluator.evaluate(0.5, -0.3, 0.2)).thenReturn(70.0);
            when(densityNoise.sample(anyDouble(), anyDouble(), anyDouble())).thenReturn(0.0);

            engine.evaluate(0, 0, 0, ctx);

            verify(splineEvaluator).evaluate(0.5, -0.3, 0.2);
        }

        @Test
        @DisplayName("Continental values fallback when no climate grid")
        void continentalValuesFallback() {
            float[] continentalValues = new float[256];
            continentalValues[0] = 0.7f; // index 0 = local coords (0, 0)

            when(ctx.has("climate_grid")).thenReturn(false);
            when(ctx.has("continental_values")).thenReturn(true);
            when(ctx.get("continental_values", float[].class)).thenReturn(continentalValues);
            when(splineEvaluator.evaluate(anyDouble(), anyDouble(), anyDouble())).thenReturn(60.0);
            when(densityNoise.sample(anyDouble(), anyDouble(), anyDouble())).thenReturn(0.0);

            engine.evaluate(0, 0, 0, ctx);

            // Continentalness is taken from continental_values, erosion and weirdness are 0.
            // 0.7f cast to double has minor precision loss, so use a capturing approach.
            verify(splineEvaluator).evaluate(
                    AdditionalMatchers.eq(0.7, 0.001),
                    AdditionalMatchers.eq(0.0, 0.001),
                    AdditionalMatchers.eq(0.0, 0.001));
        }

        @Test
        @DisplayName("No climate data uses zero parameters")
        void noClimateData_usesZeros() {
            when(splineEvaluator.evaluate(0.0, 0.0, 0.0)).thenReturn(60.0);
            when(densityNoise.sample(anyDouble(), anyDouble(), anyDouble())).thenReturn(0.0);

            engine.evaluate(0, 0, 0, ctx);

            verify(splineEvaluator).evaluate(0.0, 0.0, 0.0);
        }
    }

    // ================================================================ noise sampling

    @Nested
    @DisplayName("Noise sampling")
    class NoiseSamplingTests {

        @Test
        @DisplayName("Density noise is sampled with scaled coordinates")
        void noiseIsSampledWithScaledCoords() {
            when(splineEvaluator.evaluate(anyDouble(), anyDouble(), anyDouble())).thenReturn(0.0);
            when(densityNoise.sample(anyDouble(), anyDouble(), anyDouble())).thenReturn(0.0);

            // With heightScale = 1.0 (default overworld):
            // x * 0.01 * 1.0, y * 0.015, z * 0.01 * 1.0
            engine.evaluate(100, 50, 200, ctx);

            verify(densityNoise).sample(
                    eq(100.0 * 0.01),
                    eq(50.0 * 0.015),
                    eq(200.0 * 0.01)
            );
        }

        @Test
        @DisplayName("Different positions produce different noise queries")
        void differentPositionsDifferentQueries() {
            when(splineEvaluator.evaluate(anyDouble(), anyDouble(), anyDouble())).thenReturn(0.0);
            when(densityNoise.sample(anyDouble(), anyDouble(), anyDouble())).thenReturn(0.0);

            engine.evaluate(10, 20, 30, ctx);
            engine.evaluate(40, 50, 60, ctx);

            verify(densityNoise).sample(eq(0.1), eq(0.3), eq(0.3));
            verify(densityNoise).sample(eq(0.4), eq(0.75), eq(0.6));
        }
    }

    // ================================================================ terrain settings

    @Nested
    @DisplayName("Terrain settings influence")
    class TerrainSettingsTests {

        @Test
        @DisplayName("Higher sea level shifts surface upward")
        void higherSeaLevel_shiftsUp() {
            // Use nether settings with seaLevel=32
            TerrainSettings nether = TerrainSettings.defaultNether();
            when(profile.terrainSettings()).thenReturn(nether);
            when(splineEvaluator.evaluate(0.0, 0.0, 0.0)).thenReturn(60.0);
            when(densityNoise.sample(anyDouble(), anyDouble(), anyDouble())).thenReturn(0.0);

            // With seaLevel=32: heightBias = (60 + 32 - y) * 0.02
            double densityLow = engine.evaluate(0, 10, 0, ctx);
            double densityHigh = engine.evaluate(0, 100, 0, ctx);

            assertTrue(densityLow > densityHigh,
                    "Lower y should have higher density than higher y");
        }

        @Test
        @DisplayName("Density values are deterministic for same inputs")
        void deterministicResults() {
            when(splineEvaluator.evaluate(0.0, 0.0, 0.0)).thenReturn(60.0);
            when(densityNoise.sample(anyDouble(), anyDouble(), anyDouble())).thenReturn(0.3);

            double d1 = engine.evaluate(10, 50, 20, ctx);
            double d2 = engine.evaluate(10, 50, 20, ctx);

            assertEquals(d1, d2, 0.0, "Same inputs should produce identical density");
        }
    }
}
