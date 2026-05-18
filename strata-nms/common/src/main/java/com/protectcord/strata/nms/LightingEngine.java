package com.protectcord.strata.nms;

/**
 * Version-specific lighting engine for recalculating light levels after generation.
 */
public interface LightingEngine {

    /**
     * Recalculates lighting for a native chunk.
     */
    void recalculate(Object nativeChunk);
}
