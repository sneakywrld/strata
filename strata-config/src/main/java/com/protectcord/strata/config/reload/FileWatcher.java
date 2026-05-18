package com.protectcord.strata.config.reload;

import java.io.IOException;
import java.nio.file.*;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Watches the profiles directory for file changes using NIO WatchService.
 * When a TOML file is modified, triggers a reload callback.
 */
public final class FileWatcher implements AutoCloseable {

    private static final Logger LOGGER = Logger.getLogger("Strata");

    private final WatchService watchService;
    private final Thread watchThread;
    private volatile boolean running = true;

    public FileWatcher(Path directory, Consumer<Path> onChange) throws IOException {
        this.watchService = FileSystems.getDefault().newWatchService();
        directory.register(watchService,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_CREATE);

        this.watchThread = new Thread(() -> {
            while (running) {
                try {
                    WatchKey key = watchService.poll(1, java.util.concurrent.TimeUnit.SECONDS);
                    if (key == null) continue;

                    for (WatchEvent<?> event : key.pollEvents()) {
                        if (event.kind() == StandardWatchEventKinds.OVERFLOW) continue;

                        @SuppressWarnings("unchecked")
                        WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
                        Path changed = directory.resolve(pathEvent.context());

                        if (changed.toString().endsWith(".toml")) {
                            LOGGER.info("Config change detected: " + changed.getFileName());
                            onChange.accept(changed);
                        }
                    }

                    key.reset();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "Strata-FileWatcher");

        this.watchThread.setDaemon(true);
    }

    /**
     * Starts watching for file changes.
     */
    public void start() {
        watchThread.start();
    }

    @Override
    public void close() throws IOException {
        running = false;
        watchThread.interrupt();
        watchService.close();
    }
}
