package com.protectcord.strata.paper.world;

import com.protectcord.strata.api.world.WorldManager;
import com.protectcord.strata.api.world.WorldProfile;
import com.protectcord.strata.config.registry.ConfigRegistry;
import com.protectcord.strata.core.engine.StrataEngine;
import com.protectcord.strata.paper.StrataChunkGenerator;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class PaperWorldManager implements WorldManager {

    private final JavaPlugin plugin;
    private final ConfigRegistry configRegistry;
    private final WorldConfigStore configStore;
    private final Map<String, StrataEngine> engines = new ConcurrentHashMap<>();

    public PaperWorldManager(JavaPlugin plugin, ConfigRegistry configRegistry) {
        this.plugin = plugin;
        this.configRegistry = configRegistry;
        this.configStore = new WorldConfigStore(plugin.getDataFolder().toPath().resolve("worlds.json"));
        this.configStore.load();
    }

    @Override
    public boolean createWorld(String name, String profileName, long seed) {
        WorldProfile profile = configRegistry.getProfile(profileName);
        if (profile == null) {
            plugin.getLogger().warning("Profile not found: " + profileName);
            return false;
        }

        StrataEngine engine = new StrataEngine(profile, seed);
        engines.put(name, engine);

        WorldCreator creator = new WorldCreator(name);
        creator.seed(seed);
        creator.generator(new StrataChunkGenerator(engine));
        creator.environment(mapEnvironment(profile.environment()));

        World world = Bukkit.createWorld(creator);
        if (world == null) {
            engines.remove(name);
            return false;
        }

        configStore.setMapping(name, profileName, seed);
        configStore.save();
        return true;
    }

    @Override
    public boolean loadWorld(String name) {
        WorldConfigStore.WorldMapping mapping = configStore.getMapping(name);
        if (mapping == null) {
            return false;
        }

        WorldProfile profile = configRegistry.getProfile(mapping.profileName());
        if (profile == null) {
            return false;
        }

        StrataEngine engine = new StrataEngine(profile, mapping.seed());
        engines.put(name, engine);

        WorldCreator creator = new WorldCreator(name);
        creator.seed(mapping.seed());
        creator.generator(new StrataChunkGenerator(engine));
        creator.environment(mapEnvironment(profile.environment()));

        return Bukkit.createWorld(creator) != null;
    }

    @Override
    public boolean unloadWorld(String name) {
        World world = Bukkit.getWorld(name);
        if (world == null) {
            return false;
        }
        boolean success = Bukkit.unloadWorld(world, true);
        if (success) {
            engines.remove(name);
        }
        return success;
    }

    @Override
    public Set<String> managedWorlds() {
        return Collections.unmodifiableSet(engines.keySet());
    }

    @Override
    public Optional<StrataEngine> getEngine(String worldName) {
        return Optional.ofNullable(engines.get(worldName));
    }

    public WorldConfigStore configStore() {
        return configStore;
    }

    private World.Environment mapEnvironment(String environment) {
        return switch (environment.toUpperCase()) {
            case "NETHER" -> World.Environment.NETHER;
            case "END" -> World.Environment.THE_END;
            default -> World.Environment.NORMAL;
        };
    }
}
