package com.protectcord.strata.paper.world;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class WorldConfigStore {

    public record WorldMapping(String profileName, long seed) {}

    private final Path storePath;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, WorldMapping> mappings = new ConcurrentHashMap<>();

    public WorldConfigStore(Path storePath) {
        this.storePath = storePath;
    }

    public void load() {
        if (!Files.exists(storePath)) {
            return;
        }
        try {
            Map<String, WorldMapping> loaded = mapper.readValue(
                    storePath.toFile(),
                    new TypeReference<>() {}
            );
            mappings.putAll(loaded);
        } catch (IOException e) {
            // Corrupted file — start fresh
            mappings.clear();
        }
    }

    public void save() {
        try {
            Files.createDirectories(storePath.getParent());
            mapper.writerWithDefaultPrettyPrinter().writeValue(storePath.toFile(), mappings);
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
