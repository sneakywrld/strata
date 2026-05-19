package com.protectcord.strata.paper.generator;

import com.protectcord.strata.api.biome.Biome;
import com.protectcord.strata.core.biome.BiomeLookupTable;
import com.protectcord.strata.core.biome.ClimateMapper;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;

import java.util.List;

public final class StrataBiomeProvider extends BiomeProvider {

    private final BiomeLookupTable lookupTable;
    private final ClimateMapper climateMapper;
    private final List<org.bukkit.block.Biome> biomeList;

    public StrataBiomeProvider(BiomeLookupTable lookupTable, ClimateMapper climateMapper) {
        this.lookupTable = lookupTable;
        this.climateMapper = climateMapper;
        this.biomeList = List.of(org.bukkit.block.Biome.values());
    }

    @Override
    public org.bukkit.block.Biome getBiome(WorldInfo worldInfo, int x, int y, int z) {
        if (climateMapper == null || lookupTable == null) {
            return org.bukkit.block.Biome.PLAINS;
        }

        var climate = climateMapper.sampleClimate(x, z);
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
