package com.example.stratademo;

import com.protectcord.strata.api.biome.Biome;
import com.protectcord.strata.api.biome.BiomeBuilder;
import com.protectcord.strata.api.biome.BiomeCategory;
import com.protectcord.strata.api.biome.ClimateParameters;
import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.api.core.StrataAPI;
import com.protectcord.strata.api.core.StrataProvider;
import com.protectcord.strata.api.event.bus.EventPriority;
import com.protectcord.strata.api.event.bus.Subscribe;
import com.protectcord.strata.api.event.generation.BiomeAssignmentEvent;
import com.protectcord.strata.api.noise.NoiseFunction;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

/**
 * Example plugin demonstrating core Strata API patterns:
 *
 * <ul>
 *   <li>Registering a custom biome via {@link BiomeBuilder}</li>
 *   <li>Registering a custom noise function</li>
 *   <li>Listening to generation events via the {@link com.protectcord.strata.api.event.EventBus}</li>
 * </ul>
 *
 * <p>This is a minimal, working example. For production plugins, add error handling,
 * configuration files, and more robust lifecycle management as needed.</p>
 *
 * @author SneakyWrld
 */
public final class StrataDemoPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        Logger log = getLogger();

        // -----------------------------------------------------------------
        // 1. Obtain the Strata API
        // -----------------------------------------------------------------
        // Always check that Strata is present before accessing the API.
        // The plugin.yml "depend: [Strata]" guarantees load order, but
        // a defensive check is still good practice.
        if (!getServer().getPluginManager().isPluginEnabled("Strata")) {
            log.severe("Strata is not loaded! Disabling StrataDemoPlugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        StrataAPI api = StrataProvider.get();
        log.info("Connected to Strata API v" + api.version());

        // -----------------------------------------------------------------
        // 2. Register a custom biome
        // -----------------------------------------------------------------
        // The BiomeBuilder creates a biome definition programmatically.
        // This biome will participate in biome assignment alongside the
        // biomes defined in the active profile's TOML files.
        Biome enchantedGrove = new BiomeBuilder()
                .key(NamespacedKey.of("stratademo", "enchanted_grove"))
                .climate(new ClimateParameters(
                        0.6,   // temperature:      warm
                        0.7,   // humidity:          moist
                        0.4,   // continentalness:   inland
                        0.5,   // erosion:           gently rolling
                        0.3    // weirdness:         slightly unusual
                ))
                .vanillaMapping(NamespacedKey.minecraft("forest"))
                .category(BiomeCategory.FOREST)
                .baseHeight(6.0)
                .heightVariation(3.0)
                .build();

        api.biomeRegistry().register(enchantedGrove);
        log.info("Registered custom biome: stratademo:enchanted_grove");

        // -----------------------------------------------------------------
        // 3. Register a custom noise function
        // -----------------------------------------------------------------
        // Custom noise functions can be referenced by name in TOML configs
        // or used directly by other API consumers. This example creates a
        // simple sine-wave noise for demonstration purposes.
        NoiseFunction sineWaveNoise = new NoiseFunction() {
            private final NamespacedKey key =
                    NamespacedKey.of("stratademo", "sine_wave");

            @Override
            public NamespacedKey key() {
                return key;
            }

            @Override
            public double sample(double x, double z) {
                // A sine wave along the X axis, modulated by a cosine on Z.
                // Frequency ~0.01 gives a wavelength of about 628 blocks.
                return Math.sin(x * 0.01) * Math.cos(z * 0.01);
            }

            @Override
            public double sample(double x, double y, double z) {
                // For 3D sampling, project to 2D (ignoring Y).
                // A real implementation could incorporate Y for volumetric
                // effects like cave density or ore distribution.
                return sample(x, z);
            }

            @Override
            public double minValue() {
                return -1.0;
            }

            @Override
            public double maxValue() {
                return 1.0;
            }
        };

        api.noiseRegistry().register(sineWaveNoise);
        log.info("Registered custom noise function: stratademo:sine_wave");

        // -----------------------------------------------------------------
        // 4. Subscribe to generation events
        // -----------------------------------------------------------------
        // The event bus fires events during chunk generation. Event handlers
        // must be fast — they run on the generation thread and slow handlers
        // will degrade chunk generation performance.
        api.eventBus().register(new BiomeAssignmentListener(log));
        log.info("Registered BiomeAssignmentEvent listener");

        log.info("StrataDemoPlugin enabled successfully.");
    }

    @Override
    public void onDisable() {
        getLogger().info("StrataDemoPlugin disabled.");
    }

    // =====================================================================
    // Event Listener
    // =====================================================================

    /**
     * Listens for biome assignment events during chunk generation.
     *
     * <p>This is a read-only notification — you cannot change the biome
     * assignment from this event. It is useful for logging, analytics,
     * or triggering side effects when specific biomes are assigned.</p>
     */
    private static final class BiomeAssignmentListener {

        private final Logger log;
        private int chunksObserved = 0;

        BiomeAssignmentListener(Logger log) {
            this.log = log;
        }

        @Subscribe(priority = EventPriority.NORMAL)
        public void onBiomeAssignment(BiomeAssignmentEvent event) {
            chunksObserved++;

            // Log every 100th chunk to avoid flooding the console.
            // In a real plugin you would do something more useful here,
            // such as tracking biome distribution statistics.
            if (chunksObserved % 100 == 0) {
                log.info(String.format(
                        "Biome assignment observed for %d chunks (latest: world=%s, chunk=[%d, %d])",
                        chunksObserved,
                        event.worldName(),
                        event.chunkX(),
                        event.chunkZ()
                ));
            }
        }
    }
}
