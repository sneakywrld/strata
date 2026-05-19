package com.protectcord.strata.core.structure;

import com.protectcord.strata.api.block.StrataBlockState;
import com.protectcord.strata.api.chunk.ProtoChunkAccess;
import com.protectcord.strata.api.structure.StructureDefinition;
import com.protectcord.strata.api.structure.StructureType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generates structures at runtime using callback-based procedural generation.
 * API users register generation callbacks per structure key, which are invoked
 * when a procedural structure placement is resolved.
 */
public final class ProceduralStructureEngine {

    @FunctionalInterface
    public interface BlockPlacer {
        void place(int x, int y, int z, StrataBlockState block);
    }

    @FunctionalInterface
    public interface StructureGenerator {
        void generate(BlockPlacer placer, int originX, int originY, int originZ, Random random);
    }

    private final List<RegisteredGenerator> generators = new ArrayList<>();

    public record RegisteredGenerator(String structureKey, StructureGenerator generator) {}

    public void registerGenerator(String structureKey, StructureGenerator generator) {
        generators.add(new RegisteredGenerator(structureKey, generator));
    }

    public void generate(StructureDefinition def, ProtoChunkAccess chunk,
                         int originX, int originY, int originZ, Random random) {
        if (def.type() != StructureType.PROCEDURAL) return;

        String defKey = def.key().toString();
        StructureGenerator generator = findGenerator(defKey);

        if (generator == null) return;

        int baseX = chunk.coord().blockX();
        int baseZ = chunk.coord().blockZ();

        BlockPlacer placer = (x, y, z, block) -> {
            if (x < baseX || x >= baseX + 16) return;
            if (z < baseZ || z >= baseZ + 16) return;
            if (y < chunk.minY() || y >= chunk.maxY()) return;
            chunk.setBlock(x, y, z, block);
        };

        generator.generate(placer, originX, originY, originZ, random);
    }

    private StructureGenerator findGenerator(String structureKey) {
        for (RegisteredGenerator reg : generators) {
            if (reg.structureKey().equals(structureKey)) {
                return reg.generator();
            }
        }
        return null;
    }
}
