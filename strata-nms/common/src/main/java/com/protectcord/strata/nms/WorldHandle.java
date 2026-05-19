package com.protectcord.strata.nms;

public interface WorldHandle {

    Object getNativeWorld(String worldName);

    void forceLoadChunk(String worldName, int chunkX, int chunkZ);

    void unloadChunk(String worldName, int chunkX, int chunkZ, boolean save);

    int getMinBuildHeight();

    int getMaxBuildHeight();
}
