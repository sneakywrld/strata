package com.protectcord.strata.config.loader;

import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.config.model.ProfileConfig;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Discovers and loads world generation profiles from the plugin's profiles directory.
 * Each profile is a subdirectory containing a {@code profile.toml} root file.
 *
 * <p>Directory layout:
 * <pre>
 * plugins/Strata/profiles/
 *   default-overworld/
 *     profile.toml
 *     biomes/
 *     noise/
 *     terrain/
 *     ...
 *   netherveil/
 *     profile.toml
 *     ...
 * </pre>
 */
public final class ProfileLoader {

    private static final String PROFILE_FILE = "profile.toml";
    private static final Logger LOGGER = Logger.getLogger("Strata");

    private final Path profilesDir;

    public ProfileLoader(Path profilesDir) {
        this.profilesDir = profilesDir;
    }

    /**
     * Discovers all profile directories and loads their profile.toml files.
     *
     * @return a map of profile key -> ProfileConfig
     */
    public Map<NamespacedKey, ProfileConfig> loadAll() throws IOException {
        Map<NamespacedKey, ProfileConfig> profiles = new LinkedHashMap<>();

        if (!Files.isDirectory(profilesDir)) {
            LOGGER.warning("Profiles directory not found: " + profilesDir);
            return profiles;
        }

        try (DirectoryStream<Path> dirs = Files.newDirectoryStream(profilesDir, Files::isDirectory)) {
            for (Path dir : dirs) {
                Path profileFile = dir.resolve(PROFILE_FILE);
                if (!Files.isRegularFile(profileFile)) {
                    LOGGER.warning("Profile directory missing profile.toml: " + dir.getFileName());
                    continue;
                }

                String dirName = dir.getFileName().toString();
                NamespacedKey key = NamespacedKey.strata(dirName);

                try {
                    ProfileConfig config = loadProfile(key, dir, profileFile);
                    profiles.put(key, config);
                    LOGGER.info("Loaded profile: " + key);
                } catch (Exception e) {
                    LOGGER.severe("Failed to load profile " + dirName + ": " + e.getMessage());
                }
            }
        }

        return profiles;
    }

    /**
     * Loads a single profile from its directory.
     */
    public ProfileConfig loadProfile(NamespacedKey key, Path dir, Path profileFile) {
        TomlReader toml = TomlReader.fromFile(profileFile);

        return new ProfileConfig(
                key,
                toml.getString("name"),
                toml.getOptionalString("description").orElse(""),
                toml.getOptionalString("author").orElse("Unknown"),
                toml.getOptionalString("extends").orElse(null),
                dir,
                toml
        );
    }
}
