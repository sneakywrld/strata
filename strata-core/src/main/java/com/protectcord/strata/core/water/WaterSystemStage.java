package com.protectcord.strata.core.water;

import com.protectcord.strata.api.block.StrataBlockState;
import com.protectcord.strata.api.chunk.HeightmapType;
import com.protectcord.strata.api.chunk.ProtoChunkAccess;
import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.api.noise.NoiseFunction;
import com.protectcord.strata.api.pipeline.GenerationContext;
import com.protectcord.strata.api.pipeline.GenerationStage;
import com.protectcord.strata.api.water.RiverSettings;
import com.protectcord.strata.api.water.WaterSystemSettings;
import com.protectcord.strata.core.pipeline.PipelineStage;

import java.util.List;

/**
 * Water system pipeline stage — processes rivers, waterfalls, lakes, and ocean features.
 */
public final class WaterSystemStage implements PipelineStage {

    private static final StrataBlockState WATER = StrataBlockState.of(NamespacedKey.minecraft("water"));
    private static final StrataBlockState AIR = StrataBlockState.of(NamespacedKey.minecraft("air"));

    private final WaterSystemSettings settings;
    private final RiverNetworkBuilder riverBuilder;
    private final NoiseFunction riverMicroNoise;

    public WaterSystemStage(WaterSystemSettings settings, RiverNetworkBuilder riverBuilder,
                            NoiseFunction riverMicroNoise) {
        this.settings = settings;
        this.riverBuilder = riverBuilder;
        this.riverMicroNoise = riverMicroNoise;
    }

    @Override
    public GenerationStage stage() {
        return GenerationStage.WATER_SYSTEM;
    }

    @Override
    public void generate(GenerationContext context) {
        if (settings.rivers().enabled()) {
            carveRivers(context);
        }

        // Waterfalls, lakes, and ocean features handled here in the full implementation
    }

    private void carveRivers(GenerationContext context) {
        ProtoChunkAccess chunk = context.chunk();
        int chunkX = chunk.coord().x();
        int chunkZ = chunk.coord().z();
        int regionX = chunkX >> 5; // 32 chunks per region axis
        int regionZ = chunkZ >> 5;

        // Get river segments for this and adjacent regions
        for (int rx = regionX - 1; rx <= regionX + 1; rx++) {
            for (int rz = regionZ - 1; rz <= regionZ + 1; rz++) {
                List<RiverNetworkBuilder.RiverSegment> segments = riverBuilder.computeRegion(rx, rz);

                for (RiverNetworkBuilder.RiverSegment seg : segments) {
                    carveRiverSegment(context, seg);
                }
            }
        }
    }

    private void carveRiverSegment(GenerationContext context, RiverNetworkBuilder.RiverSegment segment) {
        ProtoChunkAccess chunk = context.chunk();
        int baseX = chunk.coord().blockX();
        int baseZ = chunk.coord().blockZ();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = baseX + x;
                int worldZ = baseZ + z;

                double dx = worldX - segment.x();
                double dz = worldZ - segment.z();

                // Add micro-noise for natural-looking banks
                double microOffset = riverMicroNoise.sample(worldX * 0.1, worldZ * 0.1) * 2.0;
                double dist = Math.sqrt(dx * dx + dz * dz) + microOffset;

                if (dist < segment.width()) {
                    int surfaceY = chunk.getHeight(HeightmapType.WORLD_SURFACE, worldX, worldZ) - 1;
                    int riverBottom = surfaceY - segment.depth();
                    int waterLevel = context.seaLevel();

                    // Carve the river channel
                    for (int y = surfaceY; y >= riverBottom; y--) {
                        if (y <= waterLevel - segment.depth()) {
                            chunk.setBlock(worldX, y, worldZ, WATER);
                        } else if (y < waterLevel) {
                            chunk.setBlock(worldX, y, worldZ, WATER);
                        } else {
                            chunk.setBlock(worldX, y, worldZ, AIR);
                        }
                    }

                    // Fill with water
                    for (int y = riverBottom; y <= waterLevel; y++) {
                        if (chunk.getBlock(worldX, y, worldZ).equals(AIR)) {
                            chunk.setBlock(worldX, y, worldZ, WATER);
                        }
                    }
                }
            }
        }
    }
}
