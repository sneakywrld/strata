package com.protectcord.strata.paper.listener;

import com.protectcord.strata.paper.StrataPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

import java.util.concurrent.atomic.AtomicLong;

public final class ChunkLoadListener implements Listener {

    private final StrataPlugin plugin;
    private final AtomicLong chunksGenerated = new AtomicLong();
    private final AtomicLong totalGenerationTimeNanos = new AtomicLong();

    public ChunkLoadListener(StrataPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!event.isNewChunk()) {
            return;
        }
        chunksGenerated.incrementAndGet();
    }

    public long chunksGenerated() {
        return chunksGenerated.get();
    }

    public double averageGenerationTimeMs() {
        long count = chunksGenerated.get();
        if (count == 0) return 0;
        return (totalGenerationTimeNanos.get() / 1_000_000.0) / count;
    }

    public void recordGenerationTime(long nanos) {
        totalGenerationTimeNanos.addAndGet(nanos);
    }
}
