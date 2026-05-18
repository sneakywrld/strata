package com.protectcord.strata.api.biome;

import com.protectcord.strata.api.block.BlockPalette;
import com.protectcord.strata.api.core.NamespacedKey;

import java.util.List;
import java.util.Optional;

/**
 * Default immutable implementation of {@link Biome}, constructed via {@link BiomeBuilder}.
 */
record BiomeImpl(
        NamespacedKey key,
        ClimateParameters climate,
        NamespacedKey vanillaMapping,
        BiomeDimension dimension,
        BiomeCategory category,
        BlockPalette surfacePaletteValue,
        BlockPalette underwaterPaletteValue,
        List<NamespacedKey> features,
        List<NamespacedKey> carvers,
        List<NamespacedKey> structures,
        List<NamespacedKey> spawnRules,
        BiomeEffects effects,
        double baseHeight,
        double heightVariation
) implements Biome {

    @Override
    public Optional<BlockPalette> surfacePalette() {
        return Optional.ofNullable(surfacePaletteValue);
    }

    @Override
    public Optional<BlockPalette> underwaterPalette() {
        return Optional.ofNullable(underwaterPaletteValue);
    }
}
