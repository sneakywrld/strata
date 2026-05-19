package com.protectcord.strata.core.chunk;

import com.protectcord.strata.api.block.StrataBlockState;
import com.protectcord.strata.api.core.NamespacedKey;

import java.util.ArrayList;
import java.util.List;

public final class PaletteStorage {

    private static final int SECTION_SIZE = 16;
    private static final int TOTAL_BLOCKS = SECTION_SIZE * SECTION_SIZE * SECTION_SIZE;
    private static final StrataBlockState AIR = StrataBlockState.of(NamespacedKey.minecraft("air"));

    private final List<StrataBlockState> palette;
    private final int[] indices;

    public PaletteStorage() {
        this.palette = new ArrayList<>();
        this.palette.add(AIR);
        this.indices = new int[TOTAL_BLOCKS];
    }

    public StrataBlockState get(int x, int y, int z) {
        int idx = index(x, y, z);
        return palette.get(indices[idx]);
    }

    public void set(int x, int y, int z, StrataBlockState state) {
        int paletteIndex = indexOfOrAdd(state);
        indices[index(x, y, z)] = paletteIndex;
    }

    public int getPaletteSize() {
        return palette.size();
    }

    public boolean isEmpty() {
        if (palette.size() == 1 && palette.getFirst().equals(AIR)) return true;
        for (int idx : indices) {
            if (idx != 0) return false;
        }
        return true;
    }

    public List<StrataBlockState> palette() {
        return List.copyOf(palette);
    }

    public int[] rawIndices() {
        return indices.clone();
    }

    private int indexOfOrAdd(StrataBlockState state) {
        for (int i = 0; i < palette.size(); i++) {
            if (palette.get(i).equals(state)) return i;
        }
        palette.add(state);
        return palette.size() - 1;
    }

    private static int index(int x, int y, int z) {
        return (x & 15) | ((z & 15) << 4) | ((y & 15) << 8);
    }
}
