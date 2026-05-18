package com.protectcord.strata.config.loader;

import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.config.model.ProfileConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ProfileLoaderTest {

    // ---------------------------------------------------------------
    // loadAll: single profile
    // ---------------------------------------------------------------

    @Test
    void loadsSingleProfileFromDirectory(@TempDir Path tempDir) throws IOException {
        Path profileDir = tempDir.resolve("my-profile");
        Files.createDirectories(profileDir);
        Files.writeString(profileDir.resolve("profile.toml"), """
                name = "My Profile"
                description = "A test profile"
                author = "Tester"
                """);

        ProfileLoader loader = new ProfileLoader(tempDir);
        Map<NamespacedKey, ProfileConfig> profiles = loader.loadAll();

        assertEquals(1, profiles.size());

        NamespacedKey expectedKey = NamespacedKey.strata("my-profile");
        assertTrue(profiles.containsKey(expectedKey));

        ProfileConfig config = profiles.get(expectedKey);
        assertEquals("My Profile", config.displayName());
        assertEquals("A test profile", config.description());
        assertEquals("Tester", config.author());
        assertNull(config.extendsFrom());
        assertEquals(profileDir, config.directory());
        assertNotNull(config.rawConfig());
    }

    // ---------------------------------------------------------------
    // loadAll: multiple profiles
    // ---------------------------------------------------------------

    @Test
    void loadsMultipleProfiles(@TempDir Path tempDir) throws IOException {
        createProfileDir(tempDir, "overworld", """
                name = "Overworld"
                author = "Alice"
                """);
        createProfileDir(tempDir, "nether", """
                name = "Nether"
                author = "Bob"
                """);
        createProfileDir(tempDir, "end", """
                name = "End"
                author = "Carol"
                """);

        ProfileLoader loader = new ProfileLoader(tempDir);
        Map<NamespacedKey, ProfileConfig> profiles = loader.loadAll();

        assertEquals(3, profiles.size());
        assertTrue(profiles.containsKey(NamespacedKey.strata("overworld")));
        assertTrue(profiles.containsKey(NamespacedKey.strata("nether")));
        assertTrue(profiles.containsKey(NamespacedKey.strata("end")));
    }

    // ---------------------------------------------------------------
    // loadAll: missing profile.toml is skipped
    // ---------------------------------------------------------------

    @Test
    void skipsDirectoryWithoutProfileToml(@TempDir Path tempDir) throws IOException {
        // Valid profile
        createProfileDir(tempDir, "valid", """
                name = "Valid"
                """);

        // Directory without profile.toml
        Path emptyDir = tempDir.resolve("incomplete");
        Files.createDirectories(emptyDir);

        ProfileLoader loader = new ProfileLoader(tempDir);
        Map<NamespacedKey, ProfileConfig> profiles = loader.loadAll();

        assertEquals(1, profiles.size());
        assertTrue(profiles.containsKey(NamespacedKey.strata("valid")));
        assertFalse(profiles.containsKey(NamespacedKey.strata("incomplete")));
    }

    // ---------------------------------------------------------------
    // loadAll: non-existent profiles directory
    // ---------------------------------------------------------------

    @Test
    void returnsEmptyMapForNonExistentDirectory(@TempDir Path tempDir) throws IOException {
        Path nonExistent = tempDir.resolve("does-not-exist");

        ProfileLoader loader = new ProfileLoader(nonExistent);
        Map<NamespacedKey, ProfileConfig> profiles = loader.loadAll();

        assertNotNull(profiles);
        assertTrue(profiles.isEmpty());
    }

    // ---------------------------------------------------------------
    // loadAll: empty profiles directory
    // ---------------------------------------------------------------

    @Test
    void returnsEmptyMapForEmptyDirectory(@TempDir Path tempDir) throws IOException {
        ProfileLoader loader = new ProfileLoader(tempDir);
        Map<NamespacedKey, ProfileConfig> profiles = loader.loadAll();

        assertNotNull(profiles);
        assertTrue(profiles.isEmpty());
    }

    // ---------------------------------------------------------------
    // loadAll: profile with extends
    // ---------------------------------------------------------------

    @Test
    void loadsProfileWithExtendsField(@TempDir Path tempDir) throws IOException {
        createProfileDir(tempDir, "child", """
                name = "Child Profile"
                extends = "parent"
                """);

        ProfileLoader loader = new ProfileLoader(tempDir);
        Map<NamespacedKey, ProfileConfig> profiles = loader.loadAll();

        ProfileConfig child = profiles.get(NamespacedKey.strata("child"));
        assertNotNull(child);
        assertEquals("parent", child.extendsFrom());
    }

    // ---------------------------------------------------------------
    // loadAll: default values for optional fields
    // ---------------------------------------------------------------

    @Test
    void defaultValuesForOptionalFields(@TempDir Path tempDir) throws IOException {
        createProfileDir(tempDir, "minimal", """
                name = "Minimal"
                """);

        ProfileLoader loader = new ProfileLoader(tempDir);
        Map<NamespacedKey, ProfileConfig> profiles = loader.loadAll();

        ProfileConfig config = profiles.get(NamespacedKey.strata("minimal"));
        assertNotNull(config);
        assertEquals("Minimal", config.displayName());
        assertEquals("", config.description()); // default
        assertEquals("Unknown", config.author()); // default
        assertNull(config.extendsFrom()); // default
    }

    // ---------------------------------------------------------------
    // loadProfile: direct single-profile load
    // ---------------------------------------------------------------

    @Test
    void loadProfileReadsSingleProfile(@TempDir Path tempDir) throws IOException {
        Path dir = tempDir.resolve("direct");
        Files.createDirectories(dir);
        Path tomlFile = dir.resolve("profile.toml");
        Files.writeString(tomlFile, """
                name = "Direct Load"
                description = "Loaded directly"
                author = "Someone"
                extends = "base"
                """);

        ProfileLoader loader = new ProfileLoader(tempDir);
        NamespacedKey key = NamespacedKey.strata("direct");
        ProfileConfig config = loader.loadProfile(key, dir, tomlFile);

        assertEquals(key, config.key());
        assertEquals("Direct Load", config.displayName());
        assertEquals("Loaded directly", config.description());
        assertEquals("Someone", config.author());
        assertEquals("base", config.extendsFrom());
        assertEquals(dir, config.directory());
    }

    // ---------------------------------------------------------------
    // loadAll: files in the profiles dir (not dirs) are ignored
    // ---------------------------------------------------------------

    @Test
    void regularFilesInProfilesDirAreIgnored(@TempDir Path tempDir) throws IOException {
        // A regular file should not be treated as a profile
        Files.writeString(tempDir.resolve("readme.txt"), "Not a profile");

        // A valid profile directory
        createProfileDir(tempDir, "real-profile", """
                name = "Real"
                """);

        ProfileLoader loader = new ProfileLoader(tempDir);
        Map<NamespacedKey, ProfileConfig> profiles = loader.loadAll();

        assertEquals(1, profiles.size());
        assertTrue(profiles.containsKey(NamespacedKey.strata("real-profile")));
    }

    // ---------------------------------------------------------------
    // loadAll: profile with extra TOML sections accessible via rawConfig
    // ---------------------------------------------------------------

    @Test
    void rawConfigRetainsFullTomlData(@TempDir Path tempDir) throws IOException {
        createProfileDir(tempDir, "full", """
                name = "Full Profile"

                [world]
                min-y = -64
                max-y = 320

                [biomes]
                default = "plains"
                """);

        ProfileLoader loader = new ProfileLoader(tempDir);
        Map<NamespacedKey, ProfileConfig> profiles = loader.loadAll();

        ProfileConfig config = profiles.get(NamespacedKey.strata("full"));
        assertNotNull(config);

        TomlReader raw = config.rawConfig();
        assertEquals(-64, raw.getInt("world.min-y", 0));
        assertEquals(320, raw.getInt("world.max-y", 0));
        assertEquals("plains", raw.getString("biomes.default"));
    }

    // ---------------------------------------------------------------
    // Helper
    // ---------------------------------------------------------------

    private void createProfileDir(Path root, String name, String tomlContent) throws IOException {
        Path dir = root.resolve(name);
        Files.createDirectories(dir);
        Files.writeString(dir.resolve("profile.toml"), tomlContent);
    }
}
