package com.protectcord.strata.config.model;

import java.util.List;

/**
 * Parsed structure placement configuration from TOML.
 *
 * @param type            structure generation strategy
 * @param spacing         average distance between structures in chunks
 * @param separation      minimum distance between structures in chunks
 * @param salt            seed salt for randomized placement
 * @param biomes          list of biome IDs where this structure may generate
 * @param adjustToTerrain whether to snap the structure to terrain height
 * @param maxPieces       maximum jigsaw pieces (jigsaw type only)
 * @param schematicPath   path to schematic file (schematic type only)
 */
public record StructureConfig(
        String type,
        int spacing,
        int separation,
        int salt,
        List<String> biomes,
        boolean adjustToTerrain,
        int maxPieces,
        String schematicPath
) {

    /**
     * Known structure generation strategies.
     */
    public enum StructureType {
        JIGSAW,
        SCHEMATIC,
        PROCEDURAL
    }
}
