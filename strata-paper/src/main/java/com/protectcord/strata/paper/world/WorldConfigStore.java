package com.protectcord.strata.paper.world;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Persists world-to-profile mappings as a properties file.
 * Each entry uses {@code worldName.profile} and {@code worldName.seed} keys.
 */
public final class WorldConfigStore {

    public record WorldMapping(String profileName, long seed) {}

    private final Path storePath;
    private final Map<String, WorldMapping> mappings = new ConcurrentHashMap<>();

    public WorldConfigStore(Path storePath) {
        this.storePath = storePath;
    }

    public void load() {
        if (!Files.exists(storePath)) {
            return;
        }
        Properties props = new Properties();
        try (BufferedReader reader = Files.newBufferedReader(storePath)) {
            props.load(reader);
        } catch (IOException e) {
            mappings.clear();
            return;
        }

        for (String key : props.stringPropertyNames()) {
            if (key.endsWith(".profile")) {
                String worldName = key.substring(0, key.length() - ".profile".length());
                String profile = props.getProperty(key);
                long seed = Long.parseLong(props.getProperty(worldName + ".seed", "0"));
                mappings.put(worldName, new WorldMapping(profile, seed));
            }
        }
    }

    public void save() {
        try {
            Files.createDirectories(storePath.getParent());
            Properties props = new Properties();
            for (Map.Entry<String, WorldMapping> entry : mappings.entrySet()) {
                props.setProperty(entry.getKey() + ".profile", entry.getValue().profileName());
                props.setProperty(entry.getKey() + ".seed", String.valueOf(entry.getValue().seed()));
            }
            try (BufferedWriter writer = Files.newBufferedWriter(storePath)) {
                props.store(writer, "Strata world-profile mappings");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save world config store", e);
        }
    }

    public void setMapping(String worldName, String profileName, long seed) {
        mappings.put(worldName, new WorldMapping(profileName, seed));
    }

    public WorldMapping getMapping(String worldName) {
        return mappings.get(worldName);
    }

    public void removeMapping(String worldName) {
        mappings.remove(worldName);
    }

    public Map<String, WorldMapping> allMappings() {
        return Map.copyOf(mappings);
    }
}
