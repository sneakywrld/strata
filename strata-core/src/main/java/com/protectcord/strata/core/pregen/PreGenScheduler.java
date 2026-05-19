package com.protectcord.strata.core.pregen;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class PreGenScheduler {

    private static final Logger LOGGER = Logger.getLogger("Strata");

    private final AtomicReference<PreGenerationTask> activeTask = new AtomicReference<>();
    private volatile ForkJoinPool pool;

    public void submit(PreGenerationTask task, int threadCount) {
        if (!activeTask.compareAndSet(null, task)) {
            throw new IllegalStateException("A pre-generation task is already active");
        }

        pool = new ForkJoinPool(threadCount);

        pool.execute(() -> {
            try {
                LOGGER.info("Pre-generation started: center=(" + task.centerX()
                        + "," + task.centerZ() + ") radius=" + task.radius());

                task.start();

                PreGenerationTask.PreGenProgress progress = task.getProgress();
                LOGGER.info("Pre-generation complete: " + progress.generated()
                        + "/" + progress.total() + " chunks generated");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Pre-generation failed", e);
            } finally {
                activeTask.set(null);
            }
        });
    }

    public PreGenerationTask getActiveTask() {
        return activeTask.get();
    }

    public void shutdown() {
        PreGenerationTask task = activeTask.get();
        if (task != null) {
            task.cancel();
        }
        ForkJoinPool currentPool = pool;
        if (currentPool != null && !currentPool.isShutdown()) {
            currentPool.shutdown();
        }
    }
}
