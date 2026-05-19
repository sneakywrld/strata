package com.protectcord.strata.paper;

import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.api.core.StrataProvider;
import com.protectcord.strata.api.world.WorldProfile;
import com.protectcord.strata.config.loader.ProfileLoader;
import com.protectcord.strata.config.model.ProfileConfig;
import com.protectcord.strata.config.registry.ConfigRegistry;
import com.protectcord.strata.config.reload.FileWatcher;
import com.protectcord.strata.config.reload.ReloadCoordinator;
import com.protectcord.strata.core.engine.StrataEngine;
import com.protectcord.strata.core.world.SimpleWorldProfile;
import com.protectcord.strata.paper.api.StrataAPIImpl;
import com.protectcord.strata.paper.command.StrataCommand;
import com.protectcord.strata.paper.guide.GuideRegistry;
import com.protectcord.strata.paper.listener.WorldLoadListener;
import com.protectcord.strata.paper.metrics.MetricsCharts;
import com.protectcord.strata.paper.world.PaperWorldManager;
import org.bstats.bukkit.Metrics;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Strata Paper Plugin — PaperMC entry point.
 * Initializes the generation engine, loads profiles, registers commands and listeners.
 */
public final class StrataPlugin extends JavaPlugin {

    private static final int BSTATS_PLUGIN_ID = 31423;
    private static final String DEFAULT_PROFILE = "elysium";

    private ConfigRegistry configRegistry;
    private ReloadCoordinator reloadCoordinator;
    private PaperWorldManager worldManager;
    private StrataAPIImpl strataAPI;
    private FileWatcher fileWatcher;
    private GuideRegistry guideRegistry;
    private final Map<String, StrataEngine> generatorEngines = new ConcurrentHashMap<>();

    @Override
    public void onEnable() {
        long start = System.currentTimeMillis();

        // Ensure config directories exist
        getDataFolder().mkdirs();
        Path dataDir = getDataFolder().toPath();
        Path profilesDir = dataDir.resolve("profiles");
        profilesDir.toFile().mkdirs();

        // First-run detection and setup
        Path strataToml = dataDir.resolve("strata.toml");
        if (!Files.isRegularFile(strataToml)) {
            handleFirstRun(dataDir, strataToml, profilesDir);
        }

        // Initialize config system
        configRegistry = new ConfigRegistry();
        ProfileLoader profileLoader = new ProfileLoader(profilesDir);
        reloadCoordinator = new ReloadCoordinator(profileLoader, configRegistry);

        // Load all profiles
        int profileCount = reloadCoordinator.reloadAll();
        getLogger().info("Loaded " + profileCount + " world generation profile(s)");

        // Initialize world manager and API
        worldManager = new PaperWorldManager(this, configRegistry);
        strataAPI = new StrataAPIImpl(this, configRegistry);
        strataAPI.setWorldManager(worldManager);
        StrataProvider.set(strataAPI);

        // Load in-game guide
        guideRegistry = new GuideRegistry(getLogger());
        guideRegistry.loadFromResources();

        // Register commands
        var strataCmd = getCommand("strata");
        if (strataCmd != null) {
            StrataCommand executor = new StrataCommand(this, configRegistry, reloadCoordinator,
                    guideRegistry, worldManager);
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
        MetricsCharts.register(metrics, this);

        long elapsed = System.currentTimeMillis() - start;
        getLogger().info("Strata v" + getDescription().getVersion() + " enabled in " + elapsed + "ms");
    }

    /**
     * Called by Bukkit when a world is configured with generator: Strata or Strata:profile-name
     * in bukkit.yml. This is the hook that makes Strata the world generator.
     */
    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        String profileId = (id == null || id.isEmpty()) ? DEFAULT_PROFILE : id;
        NamespacedKey profileKey = profileId.contains(":")
                ? NamespacedKey.parse(profileId)
                : NamespacedKey.strata(profileId);

        // Config may not be loaded yet during early world creation (before onEnable).
        // Try to load it on demand.
        if (configRegistry == null) {
            getLogger().info("Early generator request for '" + worldName + "' — initializing config...");
            getDataFolder().mkdirs();
            Path profilesDir = getDataFolder().toPath().resolve("profiles");
            configRegistry = new ConfigRegistry();
            ProfileLoader loader = new ProfileLoader(profilesDir);
            reloadCoordinator = new ReloadCoordinator(loader, configRegistry);
            reloadCoordinator.reloadAll();
        }

        Optional<ProfileConfig> profileConfig = configRegistry.getProfile(profileKey);
        if (profileConfig.isEmpty()) {
            getLogger().warning("Profile '" + profileKey + "' not found for world '" + worldName
                    + "' — falling back to vanilla generation");
            return null;
        }

        WorldProfile profile = new SimpleWorldProfile(profileConfig.get());
        long seed = getServer().getWorlds().isEmpty() ? 0L : getServer().getWorlds().get(0).getSeed();
        StrataEngine engine = new StrataEngine(profile, seed);
        engine.initialize();
        generatorEngines.put(worldName, engine);

        getLogger().info("Strata generator assigned to world '" + worldName
                + "' using profile '" + profileKey + "'");

        return new StrataChunkGenerator(engine);
    }

    /**
     * Handles first-run setup: extracts the default strata.toml with guided comments,
     * unpacks starter profiles from the bundled resource jar, configures bukkit.yml,
     * and logs a welcome message.
     */
    private void handleFirstRun(Path dataDir, Path strataToml, Path profilesDir) {
        getLogger().info("First run detected — setting up Strata...");

        // Extract the default strata.toml from plugin resources
        try (InputStream defaultConfig = getResource("strata.toml")) {
            if (defaultConfig != null) {
                Files.copy(defaultConfig, strataToml, StandardCopyOption.REPLACE_EXISTING);

                // Append the first-run flag so subsequent starts skip this block
                Files.writeString(strataToml,
                        Files.readString(strataToml) + "\n"
                                + "# -------------------------------------------------------\n"
                                + "# Internal\n"
                                + "# -------------------------------------------------------\n"
                                + "[internal]\n"
                                + "# first-run: Set to false after initial setup completes.\n"
                                + "# Do not change this manually.\n"
                                + "first-run = false\n");
            } else {
                getLogger().warning("Could not find default strata.toml in plugin resources");
            }
        } catch (IOException e) {
            getLogger().severe("Failed to write strata.toml: " + e.getMessage());
        }

        // Extract starter profiles into the profiles directory
        extractStarterProfiles(profilesDir);

        // Auto-configure bukkit.yml to use Strata for the default world
        configureBukkitYml();

        // Log welcome message with getting-started steps
        getLogger().info("========================================");
        getLogger().info("  Welcome to Strata!");
        getLogger().info("========================================");
        getLogger().info("");
        getLogger().info("  Your server is ready for custom world generation.");
        getLogger().info("  Default profiles have been installed to:");
        getLogger().info("    " + profilesDir.toAbsolutePath());
        getLogger().info("");
        getLogger().info("  The default world will use the 'elysium' profile.");
        getLogger().info("  Delete the 'world' folder and restart to generate");
        getLogger().info("  a fresh world with Strata terrain.");
        getLogger().info("");
        getLogger().info("  Commands:");
        getLogger().info("    /strata profiles  — list installed profiles");
        getLogger().info("    /strata create <world> <profile>  — create a new world");
        getLogger().info("    /strata guide <topic>  — in-game documentation");
        getLogger().info("    /strata migrate <terra-pack> <name>  — import Terra pack");
        getLogger().info("========================================");
    }

    /**
     * Configures bukkit.yml to use Strata as the default world generator.
     * Replaces existing generator entries or adds new ones.
     */
    private void configureBukkitYml() {
        Path bukkitYml = getServer().getWorldContainer().toPath().resolve("bukkit.yml");
        if (!Files.isRegularFile(bukkitYml)) {
            return;
        }

        try {
            String content = Files.readString(bukkitYml);

            if (content.contains("generator: Strata:")) {
                getLogger().info("bukkit.yml already configured for Strata, skipping");
                return;
            }

            // Replace any existing generator references (e.g., Terra) with Strata
            if (content.contains("generator:")) {
                content = content.replaceAll(
                        "(generator:\\s*)\\S+",
                        "$1Strata:" + DEFAULT_PROFILE);
                Files.writeString(bukkitYml, content);
                getLogger().info("Replaced existing generator entries in bukkit.yml with Strata:" + DEFAULT_PROFILE);
            } else {
                // No generator configured — append worlds section
                String worldConfig = "\nworlds:\n"
                        + "  world:\n"
                        + "    generator: Strata:" + DEFAULT_PROFILE + "\n";
                Files.writeString(bukkitYml, content + worldConfig);
                getLogger().info("Configured bukkit.yml to use Strata:" + DEFAULT_PROFILE
                        + " for the default world");
            }
        } catch (IOException e) {
            getLogger().warning("Could not auto-configure bukkit.yml: " + e.getMessage());
            getLogger().warning("To use Strata for world generation, add this to bukkit.yml:");
            getLogger().warning("  worlds:");
            getLogger().warning("    world:");
            getLogger().warning("      generator: Strata:" + DEFAULT_PROFILE);
        }
    }

    /**
     * Extracts bundled starter-pack profiles from the classpath into the profiles directory.
     * Only extracts a profile if its directory does not already exist.
     */
    private void extractStarterProfiles(Path profilesDir) {
        String[] defaultProfiles = {"elysium", "netherveil", "enderrift"};

        for (String profileName : defaultProfiles) {
            Path profileDir = profilesDir.resolve(profileName);
            if (Files.isDirectory(profileDir)) {
                getLogger().info("Profile '" + profileName + "' already exists, skipping extraction");
                continue;
            }

            String resourceBase = "profiles/" + profileName + "/profile.toml";
            try (InputStream profileToml = getClassLoader().getResourceAsStream(resourceBase)) {
                if (profileToml == null) {
                    getLogger().warning("Starter profile '" + profileName + "' not found on classpath");
                    continue;
                }

                Files.createDirectories(profileDir);
                Files.copy(profileToml, profileDir.resolve("profile.toml"),
                        StandardCopyOption.REPLACE_EXISTING);

                extractProfileSubdirectories(profileName, profileDir);

                getLogger().info("Extracted starter profile: " + profileName);
            } catch (IOException e) {
                getLogger().warning("Failed to extract starter profile '" + profileName + "': " + e.getMessage());
            }
        }
    }

    private void extractProfileSubdirectories(String profileName, Path profileDir) {
        String[][] subdirFiles = {
                {"terrain", "density.toml", "splines.toml", "continents.toml"},
                {"noise", "functions.toml"},
                {"water", "rivers.toml", "oceans.toml", "lakes.toml", "aquifers.toml"},
                {"carvers", "caves.toml", "ravines.toml"},
                {"surface", "rules.toml"},
                {"features", "ores.toml", "trees.toml", "vegetation.toml", "saplings.toml"},
                {"entities", "spawning.toml"},
                {"zones", "zones.toml"},
                {"structures", "structures.toml"},
        };

        for (String[] entry : subdirFiles) {
            String subdir = entry[0];
            Path subdirPath = profileDir.resolve(subdir);

            for (int i = 1; i < entry.length; i++) {
                String fileName = entry[i];
                String resourcePath = "profiles/" + profileName + "/" + subdir + "/" + fileName;

                try (InputStream resource = getClassLoader().getResourceAsStream(resourcePath)) {
                    if (resource != null) {
                        Files.createDirectories(subdirPath);
                        Files.copy(resource, subdirPath.resolve(fileName),
                                StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    getLogger().fine("Could not extract " + resourcePath + ": " + e.getMessage());
                }
            }
        }

        String[] biomeRegions = {
                "sanctuary", "verdant", "whispering", "stormbreak", "frostfang",
                "scorched", "shadowmire", "abyssal", "ocean"
        };

        for (String region : biomeRegions) {
            Path biomeDir = profileDir.resolve("biomes").resolve(region);
            extractBiomeRegionFiles(profileName, region, biomeDir);
        }
    }

    private void extractBiomeRegionFiles(String profileName, String region, Path biomeDir) {
        String[][] regionBiomes = getStarterBiomeManifest(profileName, region);

        for (String[] biomeEntry : regionBiomes) {
            String fileName = biomeEntry[0];
            String resourcePath = "profiles/" + profileName + "/biomes/" + region + "/" + fileName;

            try (InputStream resource = getClassLoader().getResourceAsStream(resourcePath)) {
                if (resource != null) {
                    Files.createDirectories(biomeDir);
                    Files.copy(resource, biomeDir.resolve(fileName),
                            StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                getLogger().fine("Could not extract biome " + fileName + ": " + e.getMessage());
            }
        }
    }

    private static String[][] getStarterBiomeManifest(String profileName, String region) {
        if (!"elysium".equals(profileName)) {
            return new String[0][];
        }

        return switch (region) {
            case "sanctuary" -> new String[][]{
                    {"sanctuary_meadow.toml"}, {"sanctuary_grove.toml"}, {"sanctuary_pond.toml"},
                    {"sanctuary_farmland.toml"}, {"sanctuary_orchard.toml"}, {"sanctuary_wildflower_field.toml"},
                    {"sanctuary_birch_park.toml"}, {"sanctuary_sunlit_clearing.toml"}, {"sanctuary_hillside.toml"},
                    {"sanctuary_stream.toml"}
            };
            case "verdant" -> new String[][]{
                    {"verdant_plains.toml"}, {"verdant_forest.toml"}, {"verdant_river_valley.toml"},
                    {"verdant_sunflower_fields.toml"}, {"verdant_rolling_hills.toml"}, {"verdant_oak_woodland.toml"},
                    {"verdant_birch_highlands.toml"}, {"verdant_flower_valley.toml"}, {"verdant_bamboo_thicket.toml"},
                    {"verdant_pumpkin_patch.toml"}
            };
            case "whispering" -> new String[][]{
                    {"whispering_dark_forest.toml"}, {"whispering_ancient_woods.toml"},
                    {"whispering_mossy_ravine.toml"}, {"whispering_fern_gully.toml"},
                    {"whispering_twilight_canopy.toml"}, {"whispering_mushroom_thicket.toml"},
                    {"whispering_overgrown_ruins.toml"}, {"whispering_misty_hollow.toml"},
                    {"whispering_spider_nest.toml"}, {"whispering_witch_wood.toml"},
                    {"whispering_tangled_briar.toml"}
            };
            case "stormbreak" -> new String[][]{
                    {"stormbreak_sea_cliffs.toml"}, {"stormbreak_tidal_caves.toml"},
                    {"stormbreak_coral_cove.toml"}, {"stormbreak_rocky_beach.toml"},
                    {"stormbreak_sandy_beach.toml"}, {"stormbreak_driftwood_shore.toml"},
                    {"stormbreak_mangrove_coast.toml"}, {"stormbreak_lighthouse_point.toml"},
                    {"stormbreak_harbor_bay.toml"}, {"stormbreak_sea_stack_field.toml"},
                    {"stormbreak_windswept_dunes.toml"}, {"stormbreak_cliff_forest.toml"}
            };
            case "frostfang" -> new String[][]{
                    {"frostfang_snowy_peak.toml"}, {"frostfang_ice_spires.toml"},
                    {"frostfang_frozen_waterfall.toml"}, {"frostfang_alpine_meadow.toml"},
                    {"frostfang_spruce_slope.toml"}, {"frostfang_glacial_valley.toml"},
                    {"frostfang_snowfield.toml"}, {"frostfang_ice_cave.toml"},
                    {"frostfang_frozen_lake.toml"}, {"frostfang_powder_snow_basin.toml"},
                    {"frostfang_rocky_ridge.toml"}
            };
            case "scorched" -> new String[][]{
                    {"scorched_red_mesa.toml"}, {"scorched_orange_canyon.toml"},
                    {"scorched_badlands_plateau.toml"}, {"scorched_dried_riverbed.toml"},
                    {"scorched_cactus_desert.toml"}, {"scorched_sandstone_arch.toml"},
                    {"scorched_ember_wastes.toml"}, {"scorched_gold_gulch.toml"}
            };
            case "shadowmire" -> new String[][]{
                    {"shadowmire_murky_swamp.toml"}, {"shadowmire_mangrove_tangle.toml"},
                    {"shadowmire_bubbling_mud.toml"}, {"shadowmire_dead_forest.toml"},
                    {"shadowmire_witch_bog.toml"}, {"shadowmire_lily_marsh.toml"}
            };
            case "abyssal" -> new String[][]{
                    {"abyssal_cheese_chamber.toml"}, {"abyssal_crystal_grotto.toml"},
                    {"abyssal_deep_dark.toml"}, {"abyssal_sculk_cavern.toml"},
                    {"abyssal_bone_crypt.toml"}, {"abyssal_lava_chamber.toml"},
                    {"abyssal_root_cellar.toml"}
            };
            case "ocean" -> new String[][]{
                    {"elysium_warm_ocean.toml"}, {"elysium_lukewarm_ocean.toml"},
                    {"elysium_temperate_ocean.toml"}, {"elysium_cold_ocean.toml"}
            };
            default -> new String[0][];
        };
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
    public PaperWorldManager worldManager() { return worldManager; }
    public StrataAPIImpl strataAPI() { return strataAPI; }
    public GuideRegistry guideRegistry() { return guideRegistry; }
}
