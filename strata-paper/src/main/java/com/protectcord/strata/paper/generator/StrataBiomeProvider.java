package com.protectcord.strata.paper.generator;

import com.protectcord.strata.api.biome.Biome;
import com.protectcord.strata.api.biome.ClimateParameters;
import com.protectcord.strata.api.noise.NoiseFunction;
import com.protectcord.strata.core.biome.BiomeLookupTable;
import com.protectcord.strata.core.biome.ClimateMapper;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;

import java.util.List;

public final class StrataBiomeProvider extends BiomeProvider {

    private final BiomeLookupTable lookupTable;
    private final NoiseFunction tempNoise;
    private final NoiseFunction humidNoise;
    private final NoiseFunction erosionNoise;
    private final NoiseFunction weirdNoise;
    private final List<org.bukkit.block.Biome> biomeList;

    public StrataBiomeProvider(BiomeLookupTable lookupTable,
                               NoiseFunction tempNoise, NoiseFunction humidNoise,
                               NoiseFunction erosionNoise, NoiseFunction weirdNoise) {
        this.lookupTable = lookupTable;
        this.tempNoise = tempNoise;
        this.humidNoise = humidNoise;
        this.erosionNoise = erosionNoise;
        this.weirdNoise = weirdNoise;
        this.biomeList = List.of(org.bukkit.block.Biome.values());
    }

    @Override
    public org.bukkit.block.Biome getBiome(WorldInfo worldInfo, int x, int y, int z) {
        if (lookupTable == null) {
            return org.bukkit.block.Biome.PLAINS;
        }

        ClimateParameters climate = ClimateMapper.sampleClimate(
                x, z, tempNoise, humidNoise, erosionNoise, weirdNoise);
        Biome strataBiome = lookupTable.lookup(climate);
        return mapToBukkit(strataBiome);
    }

    @Override
    public List<org.bukkit.block.Biome> getBiomes(WorldInfo worldInfo) {
        return biomeList;
    }

    private org.bukkit.block.Biome mapToBukkit(Biome strataBiome) {
        if (strataBiome == null) {
            return org.bukkit.block.Biome.PLAINS;
        }
        String key = strataBiome.key().key().toUpperCase();
        try {
            return org.bukkit.block.Biome.valueOf(key);
        } catch (IllegalArgumentException e) {
            return org.bukkit.block.Biome.PLAINS;
        }
    }
}
