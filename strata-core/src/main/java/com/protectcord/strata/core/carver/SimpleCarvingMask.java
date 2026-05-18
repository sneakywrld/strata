package com.protectcord.strata.core.carver;

import com.protectcord.strata.api.carver.CarvingMask;

import java.util.BitSet;

/**
 * BitSet-backed implementation of CarvingMask.
 */
public final class SimpleCarvingMask implements CarvingMask {

    private final int minY;
    private final int height;
    private final BitSet bits;
    private int count;

    public SimpleCarvingMask(int minY, int maxY) {
        this.minY = minY;
        this.height = maxY - minY;
        this.bits = new BitSet(16 * 16 * height);
    }

    @Override
    public void set(int x, int y, int z) {
        if (y < minY || y >= minY + height) return;
        int idx = (x & 15) + ((z & 15) << 4) + ((y - minY) << 8);
        if (!bits.get(idx)) {
            bits.set(idx);
            count++;
        }
    }

    @Override
    public boolean get(int x, int y, int z) {
        if (y < minY || y >= minY + height) return false;
        int idx = (x & 15) + ((z & 15) << 4) + ((y - minY) << 8);
        return bits.get(idx);
    }

    @Override
    public int carvedCount() {
        return count;
    }
}
