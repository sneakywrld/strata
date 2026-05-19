package com.protectcord.strata.paper;

import com.protectcord.strata.api.core.StrataProvider;
import com.protectcord.strata.config.loader.ProfileLoader;
import com.protectcord.strata.config.registry.ConfigRegistry;
import com.protectcord.strata.config.reload.FileWatcher;
import com.protectcord.strata.config.reload.ReloadCoordinator;
import com.protectcord.strata.paper.api.StrataAPIImpl;
import com.protectcord.strata.paper.command.StrataCommand;
import com.protectcord.strata.paper.guide.GuideRegistry;
import com.protectcord.strata.paper.listener.WorldLoadListener;
import com.protectcord.strata.paper.metrics.MetricsCharts;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

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
    private GuideRegistry guideRegistry;

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

        // Initialize API
        strataAPI = new StrataAPIImpl(this, configRegistry);
        StrataProvider.set(strataAPI);

        // Load in-game guide
        guideRegistry = new GuideRegistry(getLogger());
        guideRegistry.loadFromResources();

        // Register commands
        var strataCmd = getCommand("strata");
        if (strataCmd != null) {
            StrataCommand executor = new StrataCommand(this, configRegistry, reloadCoordinator, guideRegistry);
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
     * Handles first-run setup: extracts the default strata.toml with guided comments,
     * unpacks starter profiles from the bundled resource jar, and logs a welcome message.
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

        // Log welcome message with getting-started steps
        getLogger().info("========================================");
        getLogger().info("  Welcome to Strata!");
        getLogger().info("========================================");
        getLogger().info("");
        getLogger().info("  Your server is ready for custom world generation.");
        getLogger().info("  Default profiles have been installed to:");
        getLogger().info("    " + profilesDir.toAbsolutePath());
        getLogger().info("");
        getLogger().info("  Getting started:");
        getLogger().info("    1. Run /strata profiles to see installed profiles");
        getLogger().info("    2. Run /strata create <world> elysium to create your first world");
        getLogger().info("    3. Run /strata guide setup for an in-game walkthrough");
        getLogger().info("    4. Edit strata.toml to change global settings");
        getLogger().info("    5. Edit profiles in plugins/Strata/profiles/ to customize generation");
        getLogger().info("");
        getLogger().info("  Migrating from Terra?");
        getLogger().info("    Run /strata migrate <terra-pack-dir> <profile-name>");
        getLogger().info("    or use the standalone CLI: java -jar strata-migrate.jar");
        getLogger().info("");
        getLogger().info("  Documentation: /strata guide <topic>");
        getLogger().info("========================================");
    }

    /**
     * Extracts bundled starter-pack profiles from the classpath into the profiles directory.
     * Only extracts a profile if its directory does not already exist.
     */
    private void extractStarterProfiles(Path profilesDir) {
        // The strata-starter module bundles profiles as classpath resources under "profiles/"
        String[] defaultProfiles = {"elysium", "netherveil"};

        for (String profileName : defaultProfiles) {
            Path profileDir = profilesDir.resolve(profileName);
            if (Files.isDirectory(profileDir)) {
                getLogger().info("Profile '" + profileName + "' already exists, skipping extraction");
                continue;
            }

            // The profile.toml is the minimum required file — try to extract it
            String resourceBase = "profiles/" + profileName + "/profile.toml";
            try (InputStream profileToml = getClassLoader().getResourceAsStream(resourceBase)) {
                if (profileToml == null) {
                    getLogger().warning("Starter profile '" + profileName + "' not found on classpath");
                    continue;
                }

                Files.createDirectories(profileDir);
                Files.copy(profileToml, profileDir.resolve("profile.toml"),
                        StandardCopyOption.REPLACE_EXISTING);

                // Extract known subdirectories for this profile
                extractProfileSubdirectories(profileName, profileDir);

                getLogger().info("Extracted starter profile: " + profileName);
            } catch (IOException e) {
                getLogger().warning("Failed to extract starter profile '" + profileName + "': " + e.getMessage());
            }
        }
    }

    /**
     * Extracts subdirectory TOML files for a starter profile from classpath resources.
     */
    private void extractProfileSubdirectories(String profileName, Path profileDir) {
        // Known subdirectories in the starter pack structure
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

        // Extract biome files — these live in nested subdirectories
        String[] biomeRegions = {
                "sanctuary", "verdant", "whispering", "stormbreak", "frostfang",
                "scorched", "shadowmire", "abyssal", "ocean"
        };

        for (String region : biomeRegions) {
            Path biomeDir = profileDir.resolve("biomes").resolve(region);
            extractBiomeRegionFiles(profileName, region, biomeDir);
        }
    }

    /**
     * Attempts to extract biome TOML files for a specific region from classpath.
     */
    private void extractBiomeRegionFiles(String profileName, String region, Path biomeDir) {
        // Build a list of known biome files per region from the starter pack.
        // Since classpath enumeration is not reliable, we maintain a manifest.
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

    /**
     * Returns the list of biome TOML files for a given profile and region.
     * This acts as a build-time manifest for classpath resource extraction.
     */
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
    public StrataAPIImpl strataAPI() { return strataAPI; }
    public GuideRegistry guideRegistry() { return guideRegistry; }
}
