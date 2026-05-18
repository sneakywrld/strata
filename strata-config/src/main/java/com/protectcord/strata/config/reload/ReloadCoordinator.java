package com.protectcord.strata.config.reload;

import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.config.loader.ProfileLoader;
import com.protectcord.strata.config.model.ProfileConfig;
import com.protectcord.strata.config.registry.ConfigRegistry;
import com.protectcord.strata.config.validation.ConfigValidator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Coordinates configuration reloads: validates new config, swaps it in,
 * and notifies listeners. Ensures that invalid configs don't replace valid ones.
 */
public final class ReloadCoordinator {

    private static final Logger LOGGER = Logger.getLogger("Strata");

    private final ProfileLoader loader;
    private final ConfigRegistry registry;
    private final ConfigValidator validator;
    private Consumer<NamespacedKey> onProfileReloaded;

    public ReloadCoordinator(ProfileLoader loader, ConfigRegistry registry) {
        this.loader = loader;
        this.registry = registry;
        this.validator = new ConfigValidator();
    }

    /**
     * Sets a callback invoked when a profile is successfully reloaded.
     */
    public void onProfileReloaded(Consumer<NamespacedKey> callback) {
        this.onProfileReloaded = callback;
    }

    /**
     * Reloads all profiles from disk.
     *
     * @return the number of profiles successfully reloaded
     */
    public int reloadAll() {
        try {
            Map<NamespacedKey, ProfileConfig> loaded = loader.loadAll();
            int count = 0;

            for (var entry : loaded.entrySet()) {
                ConfigValidator.ValidationResult result = validator.validateProfile(entry.getValue());

                if (result.isValid()) {
                    registry.registerProfile(entry.getKey(), entry.getValue());
                    count++;
                    LOGGER.info("Reloaded profile: " + entry.getKey());

                    if (onProfileReloaded != null) {
                        onProfileReloaded.accept(entry.getKey());
                    }
                } else {
                    LOGGER.severe("Profile " + entry.getKey() + " has validation errors:");
                    result.errors().forEach(e -> LOGGER.severe("  - " + e));
                }

                result.warnings().forEach(w -> LOGGER.warning("  - " + w));
            }

            return count;
        } catch (IOException e) {
            LOGGER.severe("Failed to reload profiles: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Reloads a specific profile by key.
     */
    public boolean reloadProfile(NamespacedKey key) {
        ProfileConfig existing = registry.getProfile(key).orElse(null);
        if (existing == null) {
            LOGGER.warning("Cannot reload unknown profile: " + key);
            return false;
        }

        Path profileFile = existing.directory().resolve("profile.toml");
        ProfileConfig reloaded = loader.loadProfile(key, existing.directory(), profileFile);

        ConfigValidator.ValidationResult result = validator.validateProfile(reloaded);
        if (!result.isValid()) {
            LOGGER.severe("Reloaded profile " + key + " is invalid, keeping old config:");
            result.errors().forEach(e -> LOGGER.severe("  - " + e));
            return false;
        }

        registry.registerProfile(key, reloaded);
        LOGGER.info("Successfully reloaded profile: " + key);

        if (onProfileReloaded != null) {
            onProfileReloaded.accept(key);
        }

        return true;
    }
}
