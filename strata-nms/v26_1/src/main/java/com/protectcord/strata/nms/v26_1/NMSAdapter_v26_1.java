package com.protectcord.strata.nms.v26_1;

import com.protectcord.strata.api.biome.Biome;
import com.protectcord.strata.api.block.StrataBlockState;
import com.protectcord.strata.core.chunk.StrataProtoChunk;
import com.protectcord.strata.nms.BlockStateMapper;
import com.protectcord.strata.nms.ChunkAccessor;
import com.protectcord.strata.nms.HeightmapWriter;
import com.protectcord.strata.nms.LightingEngine;
import com.protectcord.strata.nms.NMSAdapter;
import com.protectcord.strata.nms.NMSVersion;

public final class NMSAdapter_v26_1 implements NMSAdapter {

    private static final NMSVersion VERSION = NMSVersion.V26_1;

    @Override
    public NMSVersion version() {
        return VERSION;
    }

    @Override
    public Object toNativeBlockState(StrataBlockState state) {
        throw new UnsupportedOperationException("Not yet implemented for " + VERSION);
    }

    @Override
    public StrataBlockState fromNativeBlockState(Object nativeState) {
        throw new UnsupportedOperationException("Not yet implemented for " + VERSION);
    }

    @Override
    public void registerBiome(Biome biome) {
        // no-op
    }

    @Override
    public BlockStateMapper blockStateMapper() {
        return new BlockStateMapper() {
            @Override
            public void setBlock(Object chunkSection, int x, int y, int z, StrataBlockState state) {
                throw new UnsupportedOperationException("Not yet implemented for " + VERSION);
            }

            @Override
            public StrataBlockState getBlock(Object chunkSection, int x, int y, int z) {
                throw new UnsupportedOperationException("Not yet implemented for " + VERSION);
            }
        };
    }

    @Override
    public ChunkAccessor chunkAccessor() {
        return new ChunkAccessor() {
            @Override
            public void writeToNative(StrataProtoChunk strataChunk, Object nativeChunk) {
                throw new UnsupportedOperationException("Not yet implemented for " + VERSION);
            }

            @Override
            public StrataProtoChunk readFromNative(Object nativeChunk) {
                throw new UnsupportedOperationException("Not yet implemented for " + VERSION);
            }
        };
    }

    @Override
    public LightingEngine lightingEngine() {
        return nativeChunk -> {
            throw new UnsupportedOperationException("Not yet implemented for " + VERSION);
        };
    }

    @Override
    public HeightmapWriter heightmapWriter() {
        return (nativeChunk, worldSurface, motionBlocking) -> {
            throw new UnsupportedOperationException("Not yet implemented for " + VERSION);
        };
    }
}
