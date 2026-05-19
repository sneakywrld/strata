package com.protectcord.strata.nms;

import com.protectcord.strata.api.biome.Biome;

public interface BiomeInjector {

    void writeBiome(Object nativeChunk, int x, int y, int z, Biome biome);

    Biome readBiome(Object nativeChunk, int x, int y, int z);

    boolean supports3DBiomes();
}
