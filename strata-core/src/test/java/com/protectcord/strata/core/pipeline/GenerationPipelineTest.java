package com.protectcord.strata.core.pipeline;

import com.protectcord.strata.api.chunk.ChunkCoord;
import com.protectcord.strata.api.chunk.ProtoChunkAccess;
import com.protectcord.strata.api.pipeline.GenerationContext;
import com.protectcord.strata.api.pipeline.GenerationStage;
import com.protectcord.strata.api.terrain.TerrainSettings;
import com.protectcord.strata.api.world.WorldProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link GenerationPipeline} orchestration logic.
 */
@MockitoSettings(strictness = Strictness.LENIENT)
class GenerationPipelineTest {

    @Mock
    private ProtoChunkAccess mockChunk;

    @Mock
    private WorldProfile mockProfile;

    private GenerationPipeline pipeline;
    private GenerationContextImpl context;

    @BeforeEach
    void setUp() {
        pipeline = new GenerationPipeline();

        // Set up minimal stubs needed for GenerationContextImpl to function
        TerrainSettings terrain = TerrainSettings.defaultOverworld();
        lenient().when(mockProfile.terrainSettings()).thenReturn(terrain);
        lenient().when(mockChunk.coord()).thenReturn(new ChunkCoord(0, 0));

        context = new GenerationContextImpl(mockChunk, mockProfile, 12345L);
    }

    // ------------------------------------------------------------------ helpers

    /**
     * Creates a mock PipelineStage for the given GenerationStage.
     */
    private PipelineStage mockStage(GenerationStage stage) {
        PipelineStage ps = mock(PipelineStage.class, stage.name());
        when(ps.stage()).thenReturn(stage);
        return ps;
    }

    /**
     * Creates a mock PipelineStage that records its invocation order.
     */
    private PipelineStage mockStageWithOrderTracking(GenerationStage stage,
                                                      List<GenerationStage> executionOrder) {
        PipelineStage ps = mock(PipelineStage.class, stage.name());
        when(ps.stage()).thenReturn(stage);
        doAnswer(invocation -> {
            executionOrder.add(stage);
            return null;
        }).when(ps).generate(any(GenerationContext.class));
        return ps;
    }

    // ------------------------------------------------ register and stages()

    @Test
    void registerStage_appearsInStagesList() {
        PipelineStage init = mockStage(GenerationStage.INITIALIZATION);
        pipeline.registerStage(init);

        List<GenerationStage> stages = pipeline.stages();
        assertTrue(stages.contains(GenerationStage.INITIALIZATION),
                "Registered stage should appear in stages()");
    }

    @Test
    void registerMultipleStages_allAppearInStagesList() {
        pipeline.registerStage(mockStage(GenerationStage.INITIALIZATION));
        pipeline.registerStage(mockStage(GenerationStage.CLIMATE_SAMPLING));
        pipeline.registerStage(mockStage(GenerationStage.TERRAIN_SHAPING));

        List<GenerationStage> stages = pipeline.stages();
        assertEquals(3, stages.size());
        assertTrue(stages.contains(GenerationStage.INITIALIZATION));
        assertTrue(stages.contains(GenerationStage.CLIMATE_SAMPLING));
        assertTrue(stages.contains(GenerationStage.TERRAIN_SHAPING));
    }

    @Test
    void stages_returnsInEnumOrder() {
        // Register in reverse order
        pipeline.registerStage(mockStage(GenerationStage.FINALIZATION));
        pipeline.registerStage(mockStage(GenerationStage.INITIALIZATION));
        pipeline.registerStage(mockStage(GenerationStage.CARVING));

        List<GenerationStage> stages = pipeline.stages();
        assertEquals(3, stages.size());
        assertEquals(GenerationStage.INITIALIZATION, stages.get(0));
        assertEquals(GenerationStage.CARVING, stages.get(1));
        assertEquals(GenerationStage.FINALIZATION, stages.get(2));
    }

    // ------------------------------------------------ disable / enable

    @Test
    void disableStage_removesFromStagesList() {
        PipelineStage init = mockStage(GenerationStage.INITIALIZATION);
        pipeline.registerStage(init);

        pipeline.disableStage(GenerationStage.INITIALIZATION);

        assertFalse(pipeline.stages().contains(GenerationStage.INITIALIZATION),
                "Disabled stage should not appear in stages()");
    }

    @Test
    void isStageEnabled_returnsFalseForDisabledStage() {
        pipeline.registerStage(mockStage(GenerationStage.INITIALIZATION));
        pipeline.disableStage(GenerationStage.INITIALIZATION);

        assertFalse(pipeline.isStageEnabled(GenerationStage.INITIALIZATION));
    }

    @Test
    void isStageEnabled_returnsTrueForEnabledStage() {
        pipeline.registerStage(mockStage(GenerationStage.INITIALIZATION));

        assertTrue(pipeline.isStageEnabled(GenerationStage.INITIALIZATION));
    }

    @Test
    void isStageEnabled_returnsFalseForUnregisteredStage() {
        // Never registered -- should not be considered enabled
        assertFalse(pipeline.isStageEnabled(GenerationStage.LIGHTING));
    }

    // ------------------------------------------------ generate() execution order

    @Test
    void generate_callsStagesInEnumOrder() {
        List<GenerationStage> executionOrder = new ArrayList<>();

        pipeline.registerStage(
                mockStageWithOrderTracking(GenerationStage.FINALIZATION, executionOrder));
        pipeline.registerStage(
                mockStageWithOrderTracking(GenerationStage.INITIALIZATION, executionOrder));
        pipeline.registerStage(
                mockStageWithOrderTracking(GenerationStage.SURFACE_BUILDING, executionOrder));
        pipeline.registerStage(
                mockStageWithOrderTracking(GenerationStage.BIOME_ASSIGNMENT, executionOrder));

        pipeline.generate(context);

        assertEquals(4, executionOrder.size());
        assertEquals(GenerationStage.INITIALIZATION, executionOrder.get(0));
        assertEquals(GenerationStage.BIOME_ASSIGNMENT, executionOrder.get(1));
        assertEquals(GenerationStage.SURFACE_BUILDING, executionOrder.get(2));
        assertEquals(GenerationStage.FINALIZATION, executionOrder.get(3));
    }

    // ------------------------------------------------ generate() skips disabled stages

    @Test
    void generate_skipsDisabledStages() {
        List<GenerationStage> executionOrder = new ArrayList<>();

        pipeline.registerStage(
                mockStageWithOrderTracking(GenerationStage.INITIALIZATION, executionOrder));
        pipeline.registerStage(
                mockStageWithOrderTracking(GenerationStage.CLIMATE_SAMPLING, executionOrder));
        pipeline.registerStage(
                mockStageWithOrderTracking(GenerationStage.TERRAIN_SHAPING, executionOrder));

        pipeline.disableStage(GenerationStage.CLIMATE_SAMPLING);
        pipeline.generate(context);

        assertEquals(2, executionOrder.size());
        assertEquals(GenerationStage.INITIALIZATION, executionOrder.get(0));
        assertEquals(GenerationStage.TERRAIN_SHAPING, executionOrder.get(1));
    }

    @Test
    void generate_disableAllStages_noneExecuted() {
        List<GenerationStage> executionOrder = new ArrayList<>();

        pipeline.registerStage(
                mockStageWithOrderTracking(GenerationStage.INITIALIZATION, executionOrder));
        pipeline.registerStage(
                mockStageWithOrderTracking(GenerationStage.LIGHTING, executionOrder));

        pipeline.disableStage(GenerationStage.INITIALIZATION);
        pipeline.disableStage(GenerationStage.LIGHTING);

        pipeline.generate(context);

        assertTrue(executionOrder.isEmpty(), "No stages should execute when all are disabled");
    }

    // ------------------------------------------------ exception isolation

    @Test
    void generate_exceptionInStage_doesNotPreventSubsequentStages() {
        List<GenerationStage> executionOrder = new ArrayList<>();

        // First stage: runs normally
        pipeline.registerStage(
                mockStageWithOrderTracking(GenerationStage.INITIALIZATION, executionOrder));

        // Second stage: throws
        PipelineStage failingStage = mockStage(GenerationStage.CLIMATE_SAMPLING);
        doThrow(new RuntimeException("simulated failure"))
                .when(failingStage).generate(any(GenerationContext.class));
        pipeline.registerStage(failingStage);

        // Third stage: should still run
        pipeline.registerStage(
                mockStageWithOrderTracking(GenerationStage.TERRAIN_SHAPING, executionOrder));

        pipeline.generate(context);

        assertEquals(2, executionOrder.size());
        assertEquals(GenerationStage.INITIALIZATION, executionOrder.get(0));
        assertEquals(GenerationStage.TERRAIN_SHAPING, executionOrder.get(1));
    }

    @Test
    void generate_multipleExceptions_allSubsequentStagesStillRun() {
        List<GenerationStage> executionOrder = new ArrayList<>();

        // Stage 1: throws
        PipelineStage fail1 = mockStage(GenerationStage.INITIALIZATION);
        doThrow(new RuntimeException("fail1"))
                .when(fail1).generate(any(GenerationContext.class));
        pipeline.registerStage(fail1);

        // Stage 2: throws
        PipelineStage fail2 = mockStage(GenerationStage.CLIMATE_SAMPLING);
        doThrow(new RuntimeException("fail2"))
                .when(fail2).generate(any(GenerationContext.class));
        pipeline.registerStage(fail2);

        // Stage 3: runs normally
        pipeline.registerStage(
                mockStageWithOrderTracking(GenerationStage.TERRAIN_SHAPING, executionOrder));

        // Stage 4: runs normally
        pipeline.registerStage(
                mockStageWithOrderTracking(GenerationStage.FINALIZATION, executionOrder));

        pipeline.generate(context);

        assertEquals(2, executionOrder.size());
        assertEquals(GenerationStage.TERRAIN_SHAPING, executionOrder.get(0));
        assertEquals(GenerationStage.FINALIZATION, executionOrder.get(1));
    }

    // ------------------------------------------------ context stage tracking

    @Test
    void generate_setsCurrentStageOnContext() {
        List<GenerationStage> observedStages = new ArrayList<>();

        PipelineStage stage = mock(PipelineStage.class);
        when(stage.stage()).thenReturn(GenerationStage.SURFACE_BUILDING);
        doAnswer(invocation -> {
            GenerationContext ctx = invocation.getArgument(0);
            observedStages.add(ctx.currentStage());
            return null;
        }).when(stage).generate(any(GenerationContext.class));

        pipeline.registerStage(stage);
        pipeline.generate(context);

        assertEquals(1, observedStages.size());
        assertEquals(GenerationStage.SURFACE_BUILDING, observedStages.get(0));
    }

    // ------------------------------------------------ timing metrics

    @Test
    void stageAverageMs_afterGeneration_returnsNonNegativeValue() {
        PipelineStage stage = mockStage(GenerationStage.INITIALIZATION);
        doAnswer(invocation -> {
            // Simulate some work
            long sum = 0;
            for (int i = 0; i < 10_000; i++) sum += i;
            return null;
        }).when(stage).generate(any(GenerationContext.class));

        pipeline.registerStage(stage);
        pipeline.generate(context);

        double avg = pipeline.stageAverageMs(GenerationStage.INITIALIZATION);
        assertTrue(avg >= 0.0, "Average ms should be non-negative, was: " + avg);
    }

    @Test
    void stageAverageMs_unregisteredStage_returnsZero() {
        assertEquals(0.0, pipeline.stageAverageMs(GenerationStage.LIGHTING));
    }

    @Test
    void totalAverageMs_afterGeneration_returnsNonNegativeValue() {
        pipeline.registerStage(mockStage(GenerationStage.INITIALIZATION));
        pipeline.registerStage(mockStage(GenerationStage.FINALIZATION));

        pipeline.generate(context);

        double total = pipeline.totalAverageMs();
        assertTrue(total >= 0.0, "Total average ms should be non-negative, was: " + total);
    }

    @Test
    void totalAverageMs_equalsOrExceedsSumOfIndividualStages() {
        pipeline.registerStage(mockStage(GenerationStage.INITIALIZATION));
        pipeline.registerStage(mockStage(GenerationStage.CLIMATE_SAMPLING));
        pipeline.registerStage(mockStage(GenerationStage.FINALIZATION));

        pipeline.generate(context);

        double initAvg = pipeline.stageAverageMs(GenerationStage.INITIALIZATION);
        double climateAvg = pipeline.stageAverageMs(GenerationStage.CLIMATE_SAMPLING);
        double finalAvg = pipeline.stageAverageMs(GenerationStage.FINALIZATION);
        double total = pipeline.totalAverageMs();

        // total is the sum of individual averages per the implementation
        double expectedSum = initAvg + climateAvg + finalAvg;
        assertEquals(expectedSum, total, 0.001,
                "Total average should equal the sum of individual stage averages");
    }

    // ------------------------------------------------ no stages registered

    @Test
    void generate_noStagesRegistered_doesNotThrow() {
        assertDoesNotThrow(() -> pipeline.generate(context),
                "Generating with no registered stages should not throw");
    }

    @Test
    void stages_noStagesRegistered_returnsEmptyList() {
        assertTrue(pipeline.stages().isEmpty(), "stages() should be empty when none registered");
    }

    // ------------------------------------------------ all 14 stages

    @Test
    void generate_allStagesRegistered_executesAll14InOrder() {
        List<GenerationStage> executionOrder = new ArrayList<>();

        for (GenerationStage gs : GenerationStage.values()) {
            pipeline.registerStage(mockStageWithOrderTracking(gs, executionOrder));
        }

        pipeline.generate(context);

        assertEquals(GenerationStage.values().length, executionOrder.size(),
                "Should execute all 14 stages");

        for (int i = 0; i < executionOrder.size(); i++) {
            assertEquals(GenerationStage.values()[i], executionOrder.get(i),
                    "Stage at index " + i + " should match enum order");
        }
    }
}
