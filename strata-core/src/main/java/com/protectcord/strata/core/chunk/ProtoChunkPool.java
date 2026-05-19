package com.protectcord.strata.core.chunk;

import com.protectcord.strata.api.block.StrataBlockState;
import com.protectcord.strata.api.chunk.ChunkCoord;
import com.protectcord.strata.api.core.NamespacedKey;

import java.util.ArrayDeque;
import java.util.Deque;

public final class ProtoChunkPool {

    private static final int POOL_SIZE = 4;
    private static final StrataBlockState AIR = StrataBlockState.of(NamespacedKey.minecraft("air"));

    private final int minY;
    private final int maxY;

    private final ThreadLocal<Deque<StrataProtoChunk>> pool = ThreadLocal.withInitial(ArrayDeque::new);

    public ProtoChunkPool(int minY, int maxY) {
        this.minY = minY;
        this.maxY = maxY;
    }

    public StrataProtoChunk acquire(int chunkX, int chunkZ) {
        Deque<StrataProtoChunk> localPool = pool.get();
        if (!localPool.isEmpty()) {
            localPool.pollFirst();
        }
        return new StrataProtoChunk(new ChunkCoord(chunkX, chunkZ), minY, maxY);
    }

    public void release(StrataProtoChunk chunk) {
        Deque<StrataProtoChunk> localPool = pool.get();
        if (localPool.size() < POOL_SIZE) {
            resetChunk(chunk);
            localPool.addLast(chunk);
        }
    }

    public int pooledCount() {
        return pool.get().size();
    }

    private void resetChunk(StrataProtoChunk chunk) {
        int height = maxY - minY;
        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                for (int y = 0; y < height; y++) {
                    chunk.setBlock(lx, minY + y, lz, AIR);
                }
            }
        }
    }
}
