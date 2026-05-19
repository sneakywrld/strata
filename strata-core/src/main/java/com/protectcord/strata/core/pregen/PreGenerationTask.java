package com.protectcord.strata.core.pregen;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public final class PreGenerationTask {

    public record PreGenProgress(int generated, int total, double percentComplete) {}

    private static final int SAVE_BATCH_SIZE = 256;

    private final int centerX;
    private final int centerZ;
    private final int radius;
    private final Consumer<int[]> chunkGenerator;
    private final Runnable saveCallback;

    private final AtomicInteger generated = new AtomicInteger(0);
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean paused = new AtomicBoolean(false);
    private final AtomicBoolean cancelled = new AtomicBoolean(false);

    private final int total;

    public PreGenerationTask(int centerX, int centerZ, int radius,
                             Consumer<int[]> chunkGenerator, Runnable saveCallback) {
        this.centerX = centerX;
        this.centerZ = centerZ;
        this.radius = radius;
        this.chunkGenerator = chunkGenerator;
        this.saveCallback = saveCallback;

        int diameter = 2 * radius + 1;
        this.total = diameter * diameter;
    }

    public void start() {
        if (!running.compareAndSet(false, true)) return;
        cancelled.set(false);
        paused.set(false);

        int x = 0;
        int z = 0;
        int dx = 0;
        int dz = -1;
        int sinceLastSave = 0;

        for (int i = 0; i < total; i++) {
            if (cancelled.get()) break;

            while (paused.get() && !cancelled.get()) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    cancelled.set(true);
                    break;
                }
            }
            if (cancelled.get()) break;

            int chunkX = centerX + x;
            int chunkZ = centerZ + z;
            chunkGenerator.accept(new int[]{chunkX, chunkZ});
            generated.incrementAndGet();
            sinceLastSave++;

            if (sinceLastSave >= SAVE_BATCH_SIZE) {
                saveCallback.run();
                sinceLastSave = 0;
            }

            if (x == z || (x < 0 && x == -z) || (x > 0 && x == 1 - z)) {
                int temp = dx;
                dx = -dz;
                dz = temp;
            }
            x += dx;
            z += dz;
        }

        if (sinceLastSave > 0) {
            saveCallback.run();
        }

        running.set(false);
    }

    public void pause() {
        paused.set(true);
    }

    public void resume() {
        paused.set(false);
    }

    public void cancel() {
        cancelled.set(true);
        paused.set(false);
    }

    public PreGenProgress getProgress() {
        int gen = generated.get();
        double pct = total > 0 ? (gen * 100.0) / total : 0.0;
        return new PreGenProgress(gen, total, pct);
    }

    public boolean isRunning() { return running.get(); }

    public boolean isPaused() { return paused.get(); }

    public boolean isCancelled() { return cancelled.get(); }

    public int centerX() { return centerX; }

    public int centerZ() { return centerZ; }

    public int radius() { return radius; }
}
