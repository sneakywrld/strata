package com.protectcord.strata.paper;

import com.protectcord.strata.api.core.StrataProvider;
import com.protectcord.strata.config.loader.ProfileLoader;
import com.protectcord.strata.config.registry.ConfigRegistry;
import com.protectcord.strata.config.reload.FileWatcher;
import com.protectcord.strata.config.reload.ReloadCoordinator;
import com.protectcord.strata.paper.api.StrataAPIImpl;
import com.protectcord.strata.paper.command.StrataCommand;
import com.protectcord.strata.paper.listener.WorldLoadListener;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Strata Paper Plugin — PaperMC entry point.
 * Initializes the generation engine, loads profiles, registers commands and listeners.
 */
public final class StrataPlugin extends JavaPlugin {

    private static final int BSTATS_PLUGIN_ID = 31423;

    private ConfigRegistry configRegistry;
    private ReloadCoordinator reloadCoordinator;
    private StrataAPIImpl strataAPI;
    private FileWatcher fileWatcher;

    @Override
    public void onEnable() {
        long start = System.currentTimeMillis();

        // Ensure config directories exist
        saveDefaultConfig();
        Path profilesDir = getDataFolder().toPath().resolve("profiles");
        profilesDir.toFile().mkdirs();

        // Initialize config system
        configRegistry = new ConfigRegistry();
        ProfileLoader profileLoader = new ProfileLoader(profilesDir);
        reloadCoordinator = new ReloadCoordinator(profileLoader, configRegistry);

        // Load all profiles
        int profileCount = reloadCoordinator.reloadAll();
        getLogger().info("Loaded " + profileCount + " world generation profile(s)");

        // Initialize API
        strataAPI = new StrataAPIImpl(this, configRegistry);
        StrataProvider.set(strataAPI);

        // Register commands
        var strataCmd = getCommand("strata");
        if (strataCmd != null) {
            StrataCommand executor = new StrataCommand(this, configRegistry, reloadCoordinator);
            strataCmd.setExecutor(executor);
            strataCmd.setTabCompleter(executor);
        }

        // Register listeners
        getServer().getPluginManager().registerEvents(new WorldLoadListener(this), this);

        // Start file watcher for hot-reload
        try {
            fileWatcher = new FileWatcher(profilesDir, path -> {
                getLogger().info("Config change detected, scheduling reload...");
                getServer().getScheduler().runTask(this, () -> reloadCoordinator.reloadAll());
            });
            fileWatcher.start();
        } catch (IOException e) {
            getLogger().warning("Could not start file watcher for hot-reload: " + e.getMessage());
        }

        // bStats metrics
        Metrics metrics = new Metrics(this, BSTATS_PLUGIN_ID);
        metrics.addCustomChart(new SimplePie("profiles_loaded",
                () -> String.valueOf(configRegistry.profileKeys().size())));
        metrics.addCustomChart(new SimplePie("biomes_registered",
                () -> String.valueOf(configRegistry.biomeKeys().size())));

        long elapsed = System.currentTimeMillis() - start;
        getLogger().info("Strata v" + getDescription().getVersion() + " enabled in " + elapsed + "ms");
    }

    @Override
    public void onDisable() {
        if (fileWatcher != null) {
            try {
                fileWatcher.close();
            } catch (IOException e) {
                getLogger().warning("Error closing file watcher: " + e.getMessage());
            }
        }

        getLogger().info("Strata disabled");
    }

    public ConfigRegistry configRegistry() { return configRegistry; }
    public StrataAPIImpl strataAPI() { return strataAPI; }
}
