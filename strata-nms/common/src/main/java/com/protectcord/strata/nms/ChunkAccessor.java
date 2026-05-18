package com.protectcord.strata.nms;

import com.protectcord.strata.core.chunk.StrataProtoChunk;

/**
 * Writes a completed StrataProtoChunk into a native NMS chunk.
 */
public interface ChunkAccessor {

    /**
     * Copies all block data from the Strata proto chunk into the native chunk.
     */
    void writeToNative(StrataProtoChunk strataChunk, Object nativeChunk);

    /**
     * Reads a native chunk into a Strata proto chunk format.
     */
    StrataProtoChunk readFromNative(Object nativeChunk);
}
