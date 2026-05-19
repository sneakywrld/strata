package com.protectcord.strata.core.pipeline.stage;

import com.protectcord.strata.api.biome.Biome;
import com.protectcord.strata.api.chunk.ProtoChunkAccess;
import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.api.pipeline.GenerationContext;
import com.protectcord.strata.api.pipeline.GenerationStage;
import com.protectcord.strata.api.registry.Registry;
import com.protectcord.strata.api.structure.StructureDefinition;
import com.protectcord.strata.api.structure.StructurePlacement;
import com.protectcord.strata.core.pipeline.PipelineStage;
import com.protectcord.strata.noise.math.NoiseMath;

import java.util.*;

/**
 * Uses grid-spacing placement: for each structure type, checks if this chunk
 * is a structure start (hash(seed+salt+chunkX+chunkZ) % spacing == 0).
 * Checks biome filter. Records structure starts in context.
 */
public final class StructureGenerationStage implements PipelineStage {

    private final Registry<StructureDefinition> structureRegistry;

    public StructureGenerationStage(Registry<StructureDefinition> structureRegistry) {
        this.structureRegistry = structureRegistry;
    }

    @Override
    public GenerationStage stage() {
        return GenerationStage.STRUCTURE_GENERATION;
    }

    @Override
    public void generate(GenerationContext context) {
        ProtoChunkAccess chunk = context.chunk();
        int chunkX = chunk.coord().x();
        int chunkZ = chunk.coord().z();
        long seed = context.seed();

        Biome centerBiome = chunk.getBiome(
                chunk.coord().blockX() + 8,
                context.seaLevel(),
                chunk.coord().blockZ() + 8
        );

        List<NamespacedKey> structureStarts = new ArrayList<>();

        for (StructureDefinition structure : structureRegistry.entries()) {
            StructurePlacement placement = structure.placement();
            int spacing = placement.spacing();
            int separation = placement.separation();
            int salt = placement.salt();

            int gridX = Math.floorDiv(chunkX, spacing);
            int gridZ = Math.floorDiv(chunkZ, spacing);

            long positionHash = NoiseMath.hash(seed + salt, gridX, gridZ);
            int offsetX = (int) ((positionHash & 0x7FFFFFFFL) % (spacing - separation));
            positionHash = NoiseMath.hash(positionHash, gridX, gridZ);
            int offsetZ = (int) ((positionHash & 0x7FFFFFFFL) % (spacing - separation));

            if (placement.spreadType() == StructurePlacement.SpreadType.TRIANGULAR) {
                offsetX = (offsetX + (int) ((positionHash >>> 32 & 0x7FFFFFFFL) % (spacing - separation))) / 2;
                offsetZ = (offsetZ + (int) ((positionHash >>> 16 & 0x7FFFFFFFL) % (spacing - separation))) / 2;
            }

            int startChunkX = gridX * spacing + offsetX + separation;
            int startChunkZ = gridZ * spacing + offsetZ + separation;

            if (startChunkX != chunkX || startChunkZ != chunkZ) continue;

            if (!isValidBiome(structure, centerBiome)) continue;

            structureStarts.add(structure.key());
        }

        context.put("structure_starts", structureStarts);
    }

    private static boolean isValidBiome(StructureDefinition structure, Biome biome) {
        List<NamespacedKey> validBiomes = structure.validBiomes();
        if (validBiomes.isEmpty()) return true;
        if (biome == null) return false;
        return validBiomes.contains(biome.key());
    }
}
