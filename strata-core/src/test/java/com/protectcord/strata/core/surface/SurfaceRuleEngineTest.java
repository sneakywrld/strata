package com.protectcord.strata.core.surface;

import com.protectcord.strata.api.biome.Biome;
import com.protectcord.strata.api.block.StrataBlockState;
import com.protectcord.strata.api.chunk.ChunkCoord;
import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.api.pipeline.GenerationContext;
import com.protectcord.strata.api.surface.SurfaceCondition;
import com.protectcord.strata.api.surface.SurfaceRule;
import com.protectcord.strata.core.chunk.StrataProtoChunk;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link SurfaceRuleEngine} condition chain evaluation and biome-specific surface rules.
 */
@DisplayName("SurfaceRuleEngine Tests")
@MockitoSettings(strictness = Strictness.LENIENT)
class SurfaceRuleEngineTest {

    private static final StrataBlockState STONE = StrataBlockState.of(NamespacedKey.minecraft("stone"));
    private static final StrataBlockState GRASS_BLOCK = StrataBlockState.of(NamespacedKey.minecraft("grass_block"));
    private static final StrataBlockState DIRT = StrataBlockState.of(NamespacedKey.minecraft("dirt"));
    private static final StrataBlockState SAND = StrataBlockState.of(NamespacedKey.minecraft("sand"));
    private static final StrataBlockState GRAVEL = StrataBlockState.of(NamespacedKey.minecraft("gravel"));

    @Mock private GenerationContext ctx;
    @Mock private Biome mockBiome;

    private StrataProtoChunk chunk;

    @BeforeEach
    void setUp() {
        chunk = new StrataProtoChunk(new ChunkCoord(0, 0), -64, 320);
        lenient().when(ctx.seaLevel()).thenReturn(63);
        lenient().when(ctx.seed()).thenReturn(42L);
    }

    // ------------------------------------------------------------------ helpers

    /**
     * Places stone in the chunk from minY up to the given surface Y,
     * sets the biome, and returns the chunk ready for testing.
     */
    private void prepareTerrain(int surfaceY) {
        int worldX = 0;
        int worldZ = 0;
        for (int y = chunk.minY(); y <= surfaceY; y++) {
            chunk.setBlock(worldX, y, worldZ, STONE);
        }
        // Set the biome at the surface
        chunk.setBiome(worldX, surfaceY, worldZ, mockBiome);
    }

    /**
     * Creates a mock SurfaceRule that always matches and places the given block.
     */
    private SurfaceRule alwaysMatchRule(int priority, StrataBlockState block) {
        SurfaceRule rule = mock(SurfaceRule.class, "rule_" + priority + "_" + block.blockId().value());
        when(rule.priority()).thenReturn(priority);
        when(rule.apply(any(SurfaceCondition.SurfaceContext.class)))
                .thenReturn(Optional.of(block));
        when(rule.key()).thenReturn(NamespacedKey.strata("rule_" + priority));
        return rule;
    }

    /**
     * Creates a mock SurfaceRule that never matches.
     */
    private SurfaceRule neverMatchRule(int priority) {
        SurfaceRule rule = mock(SurfaceRule.class, "never_match_" + priority);
        when(rule.priority()).thenReturn(priority);
        when(rule.apply(any(SurfaceCondition.SurfaceContext.class)))
                .thenReturn(Optional.empty());
        when(rule.key()).thenReturn(NamespacedKey.strata("never_" + priority));
        return rule;
    }

    /**
     * Creates a mock SurfaceRule that matches only at specific depths.
     */
    private SurfaceRule depthMatchRule(int priority, int matchDepth, StrataBlockState block) {
        SurfaceRule rule = mock(SurfaceRule.class, "depth_" + matchDepth + "_rule");
        when(rule.priority()).thenReturn(priority);
        when(rule.apply(any(SurfaceCondition.SurfaceContext.class))).thenAnswer(invocation -> {
            SurfaceCondition.SurfaceContext surfCtx = invocation.getArgument(0);
            if (surfCtx.depthBelowSurface() == matchDepth) {
                return Optional.of(block);
            }
            return Optional.empty();
        });
        when(rule.key()).thenReturn(NamespacedKey.strata("depth_rule_" + matchDepth));
        return rule;
    }

    /**
     * Creates a mock SurfaceRule that only matches when the position is underwater.
     */
    private SurfaceRule underwaterRule(int priority, StrataBlockState block) {
        SurfaceRule rule = mock(SurfaceRule.class, "underwater_rule");
        when(rule.priority()).thenReturn(priority);
        when(rule.apply(any(SurfaceCondition.SurfaceContext.class))).thenAnswer(invocation -> {
            SurfaceCondition.SurfaceContext surfCtx = invocation.getArgument(0);
            if (surfCtx.underwater()) {
                return Optional.of(block);
            }
            return Optional.empty();
        });
        when(rule.key()).thenReturn(NamespacedKey.strata("underwater_rule"));
        return rule;
    }

    // ================================================================ condition chain evaluation

    @Nested
    @DisplayName("Condition chain evaluation")
    class ConditionChainTests {

        @Test
        @DisplayName("First matching rule is applied")
        void firstMatchingRuleApplied() {
            prepareTerrain(70);

            SurfaceRule grassRule = alwaysMatchRule(1, GRASS_BLOCK);
            SurfaceRule dirtRule = alwaysMatchRule(2, DIRT);

            SurfaceRuleEngine engine = new SurfaceRuleEngine(List.of(grassRule, dirtRule));
            engine.apply(chunk, 0, 0, "plains", ctx);

            // The surface stone block should now be grass (first matching rule)
            StrataBlockState placed = chunk.getBlock(0, 70, 0);
            assertEquals(GRASS_BLOCK, placed,
                    "First matching rule (priority 1) should place grass_block");
        }

        @Test
        @DisplayName("Lower priority number evaluates first")
        void lowerPriorityEvaluatedFirst() {
            prepareTerrain(70);

            // Insert in reverse priority order to verify sorting
            SurfaceRule dirtRule = alwaysMatchRule(10, DIRT);
            SurfaceRule grassRule = alwaysMatchRule(1, GRASS_BLOCK);

            SurfaceRuleEngine engine = new SurfaceRuleEngine(List.of(dirtRule, grassRule));
            engine.apply(chunk, 0, 0, "plains", ctx);

            StrataBlockState placed = chunk.getBlock(0, 70, 0);
            assertEquals(GRASS_BLOCK, placed,
                    "Lower priority number (1) should evaluate before higher (10)");
        }

        @Test
        @DisplayName("Non-matching rules are skipped")
        void nonMatchingRulesSkipped() {
            prepareTerrain(70);

            SurfaceRule neverRule = neverMatchRule(1);
            SurfaceRule grassRule = alwaysMatchRule(2, GRASS_BLOCK);

            SurfaceRuleEngine engine = new SurfaceRuleEngine(List.of(neverRule, grassRule));
            engine.apply(chunk, 0, 0, "plains", ctx);

            StrataBlockState placed = chunk.getBlock(0, 70, 0);
            assertEquals(GRASS_BLOCK, placed,
                    "Should skip non-matching rule and apply the next matching one");
        }

        @Test
        @DisplayName("No matching rules leaves stone unchanged")
        void noMatchingRulesLeavesStone() {
            prepareTerrain(70);

            SurfaceRule neverRule1 = neverMatchRule(1);
            SurfaceRule neverRule2 = neverMatchRule(2);

            SurfaceRuleEngine engine = new SurfaceRuleEngine(List.of(neverRule1, neverRule2));
            engine.apply(chunk, 0, 0, "plains", ctx);

            StrataBlockState placed = chunk.getBlock(0, 70, 0);
            assertEquals(STONE, placed, "Stone should remain when no rules match");
        }

        @Test
        @DisplayName("Empty rule list leaves terrain unchanged")
        void emptyRuleList() {
            prepareTerrain(70);

            SurfaceRuleEngine engine = new SurfaceRuleEngine(List.of());
            engine.apply(chunk, 0, 0, "plains", ctx);

            StrataBlockState placed = chunk.getBlock(0, 70, 0);
            assertEquals(STONE, placed, "Stone should remain with no rules");
        }
    }

    // ================================================================ depth-based surface rules

    @Nested
    @DisplayName("Depth-based surface rules")
    class DepthBasedTests {

        @Test
        @DisplayName("Grass at depth 0, dirt at depth 1-3 pattern")
        void grassAndDirtLayers() {
            prepareTerrain(70);

            SurfaceRule grassAtSurface = depthMatchRule(1, 0, GRASS_BLOCK);
            SurfaceRule dirtBelow = alwaysMatchRule(2, DIRT);

            SurfaceRuleEngine engine = new SurfaceRuleEngine(List.of(grassAtSurface, dirtBelow));
            engine.apply(chunk, 0, 0, "plains", ctx);

            // Depth 0 (y=70) should be grass
            assertEquals(GRASS_BLOCK, chunk.getBlock(0, 70, 0),
                    "Surface (depth 0) should be grass");

            // Depths 1+ should be dirt (falls through to always-match rule)
            assertEquals(DIRT, chunk.getBlock(0, 69, 0),
                    "Depth 1 should be dirt");
        }

        @Test
        @DisplayName("Depth-specific rules apply at correct y levels")
        void depthSpecificRules() {
            prepareTerrain(70);

            SurfaceRule atDepth0 = depthMatchRule(1, 0, GRASS_BLOCK);
            SurfaceRule atDepth1 = depthMatchRule(1, 1, DIRT);
            SurfaceRule atDepth2 = depthMatchRule(1, 2, SAND);

            SurfaceRuleEngine engine = new SurfaceRuleEngine(
                    List.of(atDepth0, atDepth1, atDepth2));
            engine.apply(chunk, 0, 0, "plains", ctx);

            assertEquals(GRASS_BLOCK, chunk.getBlock(0, 70, 0), "Depth 0 -> grass");
            assertEquals(DIRT, chunk.getBlock(0, 69, 0), "Depth 1 -> dirt");
            assertEquals(SAND, chunk.getBlock(0, 68, 0), "Depth 2 -> sand");
        }
    }

    // ================================================================ biome-specific surface rules

    @Nested
    @DisplayName("Biome-specific surface rules")
    class BiomeSpecificTests {

        @Test
        @DisplayName("Biome context is available to rules")
        void biomeContextAvailable() {
            prepareTerrain(70);

            // Create a rule that checks the biome
            SurfaceRule biomeAwareRule = mock(SurfaceRule.class, "biome_aware");
            when(biomeAwareRule.priority()).thenReturn(1);
            when(biomeAwareRule.key()).thenReturn(NamespacedKey.strata("biome_aware"));
            when(biomeAwareRule.apply(any(SurfaceCondition.SurfaceContext.class)))
                    .thenAnswer(invocation -> {
                        SurfaceCondition.SurfaceContext surfCtx = invocation.getArgument(0);
                        assertNotNull(surfCtx.biome(),
                                "Biome should be available in surface context");
                        return Optional.of(SAND);
                    });

            SurfaceRuleEngine engine = new SurfaceRuleEngine(List.of(biomeAwareRule));
            engine.apply(chunk, 0, 0, "desert", ctx);

            assertEquals(SAND, chunk.getBlock(0, 70, 0));
        }

        @Test
        @DisplayName("Rule can choose block based on biome key")
        void ruleChoosesBasedOnBiome() {
            prepareTerrain(70);

            // Rule that places sand only in desert biomes
            when(mockBiome.key()).thenReturn(NamespacedKey.strata("desert"));
            SurfaceRule desertRule = mock(SurfaceRule.class, "desert_surface");
            when(desertRule.priority()).thenReturn(1);
            when(desertRule.key()).thenReturn(NamespacedKey.strata("desert_surface"));
            when(desertRule.apply(any(SurfaceCondition.SurfaceContext.class)))
                    .thenAnswer(invocation -> {
                        SurfaceCondition.SurfaceContext surfCtx = invocation.getArgument(0);
                        if (surfCtx.biome().key().equals(NamespacedKey.strata("desert"))) {
                            return Optional.of(SAND);
                        }
                        return Optional.empty();
                    });

            SurfaceRuleEngine engine = new SurfaceRuleEngine(List.of(desertRule));
            engine.apply(chunk, 0, 0, "desert", ctx);

            assertEquals(SAND, chunk.getBlock(0, 70, 0),
                    "Desert biome should get sand surface");
        }
    }

    // ================================================================ underwater handling

    @Nested
    @DisplayName("Underwater handling")
    class UnderwaterTests {

        @Test
        @DisplayName("Underwater flag is set for blocks below sea level")
        void underwaterFlagSet() {
            // Surface at y=50, sea level = 63 -> blocks are underwater
            prepareTerrain(50);

            SurfaceRule underwaterCheck = mock(SurfaceRule.class, "underwater_check");
            when(underwaterCheck.priority()).thenReturn(1);
            when(underwaterCheck.key()).thenReturn(NamespacedKey.strata("underwater_check"));
            when(underwaterCheck.apply(any(SurfaceCondition.SurfaceContext.class)))
                    .thenAnswer(invocation -> {
                        SurfaceCondition.SurfaceContext surfCtx = invocation.getArgument(0);
                        assertTrue(surfCtx.underwater(),
                                "Blocks below sea level should be marked underwater");
                        assertTrue(surfCtx.waterDepth() > 0,
                                "Water depth should be positive when underwater");
                        return Optional.of(GRAVEL);
                    });

            SurfaceRuleEngine engine = new SurfaceRuleEngine(List.of(underwaterCheck));
            engine.apply(chunk, 0, 0, "ocean", ctx);
        }

        @Test
        @DisplayName("Underwater rule only matches submerged positions")
        void underwaterRuleOnlyMatchesSubmerged() {
            // Surface at y=50, below sea level 63
            prepareTerrain(50);

            SurfaceRule uw = underwaterRule(1, GRAVEL);

            SurfaceRuleEngine engine = new SurfaceRuleEngine(List.of(uw));
            engine.apply(chunk, 0, 0, "ocean", ctx);

            assertEquals(GRAVEL, chunk.getBlock(0, 50, 0),
                    "Underwater rule should match at submerged surface");
        }

        @Test
        @DisplayName("Above-water positions do not trigger underwater rules")
        void aboveWaterNotUnderwater() {
            // Surface at y=70, above sea level 63
            prepareTerrain(70);

            SurfaceRule uw = underwaterRule(1, GRAVEL);
            SurfaceRule fallback = alwaysMatchRule(2, GRASS_BLOCK);

            SurfaceRuleEngine engine = new SurfaceRuleEngine(List.of(uw, fallback));
            engine.apply(chunk, 0, 0, "plains", ctx);

            // Surface should be grass, not gravel, because it's not underwater
            assertEquals(GRASS_BLOCK, chunk.getBlock(0, 70, 0),
                    "Above-water surface should not trigger underwater rule");
        }
    }

    // ================================================================ surface context data

    @Nested
    @DisplayName("Surface context data")
    class SurfaceContextTests {

        @Test
        @DisplayName("Surface context contains correct coordinates")
        void contextContainsCorrectCoords() {
            prepareTerrain(70);

            SurfaceRule coordCheck = mock(SurfaceRule.class, "coord_check");
            when(coordCheck.priority()).thenReturn(1);
            when(coordCheck.key()).thenReturn(NamespacedKey.strata("coord_check"));
            when(coordCheck.apply(any(SurfaceCondition.SurfaceContext.class)))
                    .thenAnswer(invocation -> {
                        SurfaceCondition.SurfaceContext surfCtx = invocation.getArgument(0);
                        assertEquals(0, surfCtx.x(), "World X should match");
                        assertEquals(0, surfCtx.z(), "World Z should match");
                        assertEquals(70, surfCtx.surfaceY(), "Surface Y should be terrain height");
                        return Optional.of(GRASS_BLOCK);
                    });

            SurfaceRuleEngine engine = new SurfaceRuleEngine(List.of(coordCheck));
            engine.apply(chunk, 0, 0, "plains", ctx);
        }

        @Test
        @DisplayName("Surface context contains world seed")
        void contextContainsSeed() {
            prepareTerrain(70);

            SurfaceRule seedCheck = mock(SurfaceRule.class, "seed_check");
            when(seedCheck.priority()).thenReturn(1);
            when(seedCheck.key()).thenReturn(NamespacedKey.strata("seed_check"));
            when(seedCheck.apply(any(SurfaceCondition.SurfaceContext.class)))
                    .thenAnswer(invocation -> {
                        SurfaceCondition.SurfaceContext surfCtx = invocation.getArgument(0);
                        assertEquals(42L, surfCtx.seed(), "Seed should match context seed");
                        return Optional.of(GRASS_BLOCK);
                    });

            SurfaceRuleEngine engine = new SurfaceRuleEngine(List.of(seedCheck));
            engine.apply(chunk, 0, 0, "plains", ctx);
        }
    }

    // ================================================================ edge cases

    @Nested
    @DisplayName("Edge cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Surface at minimum Y does not throw")
        void surfaceAtMinY() {
            // Only place stone at minY
            chunk.setBlock(0, chunk.minY(), 0, STONE);
            chunk.setBiome(0, chunk.minY(), 0, mockBiome);

            SurfaceRule rule = alwaysMatchRule(1, GRASS_BLOCK);
            SurfaceRuleEngine engine = new SurfaceRuleEngine(List.of(rule));

            assertDoesNotThrow(() -> engine.apply(chunk, 0, 0, "plains", ctx));
        }

        @Test
        @DisplayName("Non-stone blocks are skipped by surface rules")
        void nonStoneBlocksSkipped() {
            // Put dirt instead of stone at the surface
            chunk.setBlock(0, 70, 0, DIRT);
            chunk.setBiome(0, 70, 0, mockBiome);

            // Put stone below
            for (int y = chunk.minY(); y < 70; y++) {
                chunk.setBlock(0, y, 0, STONE);
            }

            SurfaceRule rule = alwaysMatchRule(1, SAND);
            SurfaceRuleEngine engine = new SurfaceRuleEngine(List.of(rule));
            engine.apply(chunk, 0, 0, "plains", ctx);

            // Dirt at y=70 should remain unchanged (not stone, so skipped)
            assertEquals(DIRT, chunk.getBlock(0, 70, 0),
                    "Non-stone blocks should not be replaced by surface rules");

            // Stone at y=69 (depth 1) should be replaced
            assertEquals(SAND, chunk.getBlock(0, 69, 0),
                    "Stone blocks below surface should still be processed");
        }
    }
}
