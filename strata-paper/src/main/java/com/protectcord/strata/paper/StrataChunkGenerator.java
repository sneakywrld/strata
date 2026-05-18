package com.protectcord.strata.paper;

import com.protectcord.strata.api.block.StrataBlockState;
import com.protectcord.strata.api.chunk.ChunkCoord;
import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.core.chunk.StrataProtoChunk;
import com.protectcord.strata.core.engine.StrataEngine;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

import java.util.Random;

/**
 * Bukkit ChunkGenerator implementation that delegates to the Strata engine.
 */
public final class StrataChunkGenerator extends ChunkGenerator {

    private final StrataEngine engine;

    public StrataChunkGenerator(StrataEngine engine) {
        this.engine = engine;
    }

    @Override
    public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome) {
        ChunkData data = createChunkData(world);

        StrataProtoChunk proto = engine.generateChunk(new ChunkCoord(chunkX, chunkZ));

        // Copy block data from proto chunk to Bukkit ChunkData
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = chunkX * 16 + x;
                int worldZ = chunkZ * 16 + z;

                for (int y = proto.minY(); y < proto.maxY(); y++) {
                    StrataBlockState state = proto.getBlock(worldX, y, worldZ);
                    Material material = mapToMaterial(state);
                    if (material != Material.AIR) {
                        data.setBlock(x, y, z, material);
                    }
                }
            }
        }

        return data;
    }

    @Override
    public boolean isParallelCapable() {
        return true; // Strata's generation is thread-safe
    }

    @Override
    public boolean shouldGenerateNoise() {
        return false; // We handle all terrain shaping
    }

    @Override
    public boolean shouldGenerateSurface() {
        return false; // We handle surface building
    }

    @Override
    public boolean shouldGenerateCaves() {
        return false; // We handle carving
    }

    @Override
    public boolean shouldGenerateDecorations() {
        return false; // We handle feature decoration
    }

    @Override
    public boolean shouldGenerateMobs() {
        return false; // We handle entity spawning
    }

    @Override
    public boolean shouldGenerateStructures() {
        return false; // We handle structures
    }

    private Material mapToMaterial(StrataBlockState state) {
        // Map Strata block state to Bukkit Material
        String key = state.blockId().key().toUpperCase();
        try {
            return Material.valueOf(key);
        } catch (IllegalArgumentException e) {
            // Fallback for states with properties or unmapped blocks
            return switch (state.blockId().toString()) {
                case "minecraft:grass_block" -> Material.GRASS_BLOCK;
                case "minecraft:water" -> Material.WATER;
                case "minecraft:lava" -> Material.LAVA;
                case "minecraft:stone" -> Material.STONE;
                case "minecraft:dirt" -> Material.DIRT;
                case "minecraft:sand" -> Material.SAND;
                case "minecraft:gravel" -> Material.GRAVEL;
                default -> Material.AIR;
            };
        }
    }

    public StrataEngine engine() {
        return engine;
    }
}
