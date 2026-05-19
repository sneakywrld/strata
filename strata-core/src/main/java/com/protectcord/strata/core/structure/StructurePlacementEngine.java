package com.protectcord.strata.core.structure;

import com.protectcord.strata.api.structure.StructureDefinition;
import com.protectcord.strata.api.structure.StructurePlacement;
import com.protectcord.strata.noise.math.NoiseMath;

import java.util.ArrayList;
import java.util.List;

/**
 * Determines which structures start in a given chunk by evaluating placement rules
 * against a grid-based distribution algorithm. Uses salted hashing for deterministic
 * but unique placement per structure type.
 */
public final class StructurePlacementEngine {

    public record StructureStart(int chunkX, int chunkZ, StructureDefinition definition) {}

    public List<StructureStart> getStarts(int chunkX, int chunkZ, long worldSeed,
                                          List<StructureDefinition> structures) {
        List<StructureStart> starts = new ArrayList<>();

        for (StructureDefinition def : structures) {
            StructurePlacement placement = def.placement();

            int gridX = Math.floorDiv(chunkX, placement.spacing());
            int gridZ = Math.floorDiv(chunkZ, placement.spacing());

            long positionSeed = computePositionSeed(worldSeed, gridX, gridZ, placement.salt());

            int offsetX = computeOffset(positionSeed, placement.spacing(), placement.separation());
            int offsetZ = computeOffset(positionSeed ^ 0x9E3779B97F4A7C15L,
                    placement.spacing(), placement.separation());

            if (placement.spreadType() == StructurePlacement.SpreadType.TRIANGULAR) {
                if ((gridZ & 1) != 0) {
                    offsetX = (offsetX + placement.spacing() / 2) % placement.spacing();
                }
            }

            int startChunkX = gridX * placement.spacing() + offsetX;
            int startChunkZ = gridZ * placement.spacing() + offsetZ;

            if (startChunkX == chunkX && startChunkZ == chunkZ) {
                if (passesBiomeCheck(def)) {
                    starts.add(new StructureStart(chunkX, chunkZ, def));
                }
            }
        }

        return starts;
    }

    private static long computePositionSeed(long worldSeed, int gridX, int gridZ, int salt) {
        long h = worldSeed;
        h ^= (long) salt * 0x6C62272E07BB0142L;
        h ^= (long) gridX * 0x94D049BB133111EBL;
        h ^= (long) gridZ * 0x517CC1B727220A95L;
        h ^= h >>> 32;
        h *= 0xBF58476D1CE4E5B9L;
        h ^= h >>> 27;
        return h;
    }

    private static int computeOffset(long seed, int spacing, int separation) {
        int range = spacing - separation;
        if (range <= 0) return 0;
        long hash = NoiseMath.hash(seed, spacing, separation);
        return separation + (int) ((hash & Long.MAX_VALUE) % range);
    }

    private static boolean passesBiomeCheck(StructureDefinition def) {
        return true;
    }
}
