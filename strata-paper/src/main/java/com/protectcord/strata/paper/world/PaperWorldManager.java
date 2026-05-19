package com.protectcord.strata.paper.world;

import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.api.world.StrataWorld;
import com.protectcord.strata.api.world.WorldManager;
import com.protectcord.strata.api.world.WorldProfile;
import com.protectcord.strata.config.model.ProfileConfig;
import com.protectcord.strata.config.registry.ConfigRegistry;
import com.protectcord.strata.core.engine.StrataEngine;
import com.protectcord.strata.core.world.SimpleWorldProfile;
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
    private final Map<String, PaperStrataWorld> worlds = new ConcurrentHashMap<>();
    private final Map<String, StrataEngine> engines = new ConcurrentHashMap<>();

    public PaperWorldManager(JavaPlugin plugin, ConfigRegistry configRegistry) {
        this.plugin = plugin;
        this.configRegistry = configRegistry;
        this.configStore = new WorldConfigStore(plugin.getDataFolder().toPath().resolve("worlds.properties"));
        this.configStore.load();
    }

    @Override
    public StrataWorld createWorld(String worldName, NamespacedKey profileKey) {
        return createWorld(worldName, profileKey, new Random().nextLong());
    }

    @Override
    public StrataWorld createWorld(String worldName, NamespacedKey profileKey, long seed) {
        if (worlds.containsKey(worldName)) {
            throw new IllegalArgumentException("World already exists: " + worldName);
        }

        WorldProfile profile = resolveProfile(profileKey);

        StrataEngine engine = new StrataEngine(profile, seed);
        engine.initialize();
        engines.put(worldName, engine);

        WorldCreator creator = new WorldCreator(worldName);
        creator.seed(seed);
        creator.generator(new StrataChunkGenerator(engine));

        World world = Bukkit.createWorld(creator);
        if (world == null) {
            engines.remove(worldName);
            throw new IllegalStateException("Failed to create Bukkit world: " + worldName);
        }

        PaperStrataWorld strataWorld = new PaperStrataWorld(worldName, profileKey, profile, seed);
        worlds.put(worldName, strataWorld);

        configStore.setMapping(worldName, profileKey.toString(), seed);
        configStore.save();

        return strataWorld;
    }

    @Override
    public Optional<StrataWorld> getWorld(String worldName) {
        return Optional.ofNullable(worlds.get(worldName));
    }

    @Override
    public Collection<StrataWorld> worlds() {
        return Collections.unmodifiableCollection(worlds.values());
    }

    @Override
    public boolean isStrataWorld(String worldName) {
        return worlds.containsKey(worldName);
    }

    @Override
    public Optional<WorldProfile> getProfile(NamespacedKey profileKey) {
        return configRegistry.getProfile(profileKey)
                .map(SimpleWorldProfile::new);
    }

    @Override
    public Collection<NamespacedKey> profileKeys() {
        return configRegistry.profileKeys();
    }

    public Optional<StrataEngine> getEngine(String worldName) {
        return Optional.ofNullable(engines.get(worldName));
    }

    public WorldConfigStore configStore() {
        return configStore;
    }

    /**
     * Restores a world from the persisted config store on server startup.
     */
    public boolean restoreWorld(String worldName) {
        WorldConfigStore.WorldMapping mapping = configStore.getMapping(worldName);
        if (mapping == null) {
            return false;
        }

        NamespacedKey profileKey = NamespacedKey.parse(mapping.profileName());
        WorldProfile profile = resolveProfile(profileKey);

        StrataEngine engine = new StrataEngine(profile, mapping.seed());
        engine.initialize();
        engines.put(worldName, engine);

        WorldCreator creator = new WorldCreator(worldName);
        creator.seed(mapping.seed());
        creator.generator(new StrataChunkGenerator(engine));

        World world = Bukkit.createWorld(creator);
        if (world == null) {
            engines.remove(worldName);
            return false;
        }

        worlds.put(worldName, new PaperStrataWorld(worldName, profileKey, profile, mapping.seed()));
        return true;
    }

    private WorldProfile resolveProfile(NamespacedKey profileKey) {
        Optional<ProfileConfig> config = configRegistry.getProfile(profileKey);
        if (config.isEmpty()) {
            throw new IllegalArgumentException("Profile not registered: " + profileKey);
        }
        return new SimpleWorldProfile(config.get());
    }
}
