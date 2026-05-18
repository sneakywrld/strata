package com.protectcord.strata.core.pipeline.stage;

import com.protectcord.strata.api.carver.Carver;
import com.protectcord.strata.api.carver.CarverContext;
import com.protectcord.strata.api.carver.CarvingMask;
import com.protectcord.strata.api.pipeline.GenerationContext;
import com.protectcord.strata.api.pipeline.GenerationStage;
import com.protectcord.strata.core.carver.SimpleCarvingMask;
import com.protectcord.strata.core.pipeline.PipelineStage;

import java.util.List;
import java.util.Random;

/**
 * Runs all registered carvers to create caves, ravines, and tunnels.
 */
public final class CarvingStage implements PipelineStage {

    private final List<Carver> carvers;

    public CarvingStage(List<Carver> carvers) {
        this.carvers = carvers;
    }

    @Override
    public GenerationStage stage() {
        return GenerationStage.CARVING;
    }

    @Override
    public void generate(GenerationContext context) {
        CarverContext carverCtx = new CarverContext(
                context.chunk(),
                context.chunk().coord(),
                context.seed(),
                context.seaLevel(),
                context.chunk().minY(),
                context.chunk().maxY()
        );

        CarvingMask mask = new SimpleCarvingMask(
                context.chunk().minY(), context.chunk().maxY());

        Random random = new Random(carverCtx.chunkSeed());

        for (Carver carver : carvers) {
            if (random.nextDouble() < carver.probability()) {
                carver.carve(carverCtx, mask);
            }
        }

        // Store mask in context for later stages (e.g., water system)
        context.put("carving_mask", mask);
    }
}
