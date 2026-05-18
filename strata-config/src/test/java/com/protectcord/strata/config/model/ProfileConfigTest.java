package com.protectcord.strata.config.model;

import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.config.loader.TomlReader;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ProfileConfigTest {

    private static final String SAMPLE_TOML = """
            name = "Test Profile"
            description = "A test profile"
            author = "Tester"
            """;

    private TomlReader sampleReader() {
        return TomlReader.fromString(SAMPLE_TOML);
    }

    // ---------------------------------------------------------------
    // Construction and accessor methods
    // ---------------------------------------------------------------

    @Test
    void recordFieldsAreAccessible() {
        NamespacedKey key = NamespacedKey.strata("test");
        Path dir = Path.of("/tmp/profiles/test");
        TomlReader raw = sampleReader();

        ProfileConfig config = new ProfileConfig(
                key,
                "Test Profile",
                "A test profile",
                "Tester",
                "parent-profile",
                dir,
                raw
        );

        assertEquals(key, config.key());
        assertEquals("Test Profile", config.displayName());
        assertEquals("A test profile", config.description());
        assertEquals("Tester", config.author());
        assertEquals("parent-profile", config.extendsFrom());
        assertEquals(dir, config.directory());
        assertSame(raw, config.rawConfig());
    }

    @Test
    void namespacedKeyCorrectValues() {
        NamespacedKey key = NamespacedKey.strata("overworld");
        assertEquals("strata", key.namespace());
        assertEquals("overworld", key.key());
        assertEquals("strata:overworld", key.toString());
    }

    // ---------------------------------------------------------------
    // Null extendsFrom (root profile)
    // ---------------------------------------------------------------

    @Test
    void rootProfileHasNullExtendsFrom() {
        NamespacedKey key = NamespacedKey.strata("root");
        ProfileConfig config = new ProfileConfig(
                key, "Root", "Base profile", "Author",
                null, Path.of("/tmp/root"), sampleReader()
        );

        assertNull(config.extendsFrom());
    }

    @Test
    void childProfileHasNonNullExtendsFrom() {
        NamespacedKey key = NamespacedKey.strata("child");
        ProfileConfig config = new ProfileConfig(
                key, "Child", "Child profile", "Author",
                "root", Path.of("/tmp/child"), sampleReader()
        );

        assertNotNull(config.extendsFrom());
        assertEquals("root", config.extendsFrom());
    }

    // ---------------------------------------------------------------
    // Record equality semantics
    // ---------------------------------------------------------------

    @Test
    void recordsWithSameFieldsAreEqualExceptRawConfig() {
        // Records use structural equality for all fields.
        // Because TomlReader does not override equals, two ProfileConfig
        // instances with different TomlReader instances will NOT be equal
        // even if the underlying TOML is the same.
        NamespacedKey key = NamespacedKey.strata("eq-test");
        Path dir = Path.of("/tmp/eq");
        TomlReader reader1 = sampleReader();
        TomlReader reader2 = sampleReader();

        ProfileConfig a = new ProfileConfig(key, "Name", "Desc", "Auth", null, dir, reader1);
        ProfileConfig b = new ProfileConfig(key, "Name", "Desc", "Auth", null, dir, reader2);

        // Different TomlReader instances => not equal (reference equality on rawConfig)
        assertNotEquals(a, b);
    }

    @Test
    void sameInstanceIsEqual() {
        NamespacedKey key = NamespacedKey.strata("same");
        TomlReader raw = sampleReader();
        Path dir = Path.of("/tmp/same");

        ProfileConfig config = new ProfileConfig(key, "Same", "Desc", "Auth", null, dir, raw);
        assertEquals(config, config);
    }

    @Test
    void recordsWithSameReaderInstanceAreEqual() {
        NamespacedKey key = NamespacedKey.strata("shared");
        TomlReader shared = sampleReader();
        Path dir = Path.of("/tmp/shared");

        ProfileConfig a = new ProfileConfig(key, "Name", "Desc", "Auth", null, dir, shared);
        ProfileConfig b = new ProfileConfig(key, "Name", "Desc", "Auth", null, dir, shared);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    // ---------------------------------------------------------------
    // Null-safe fields
    // ---------------------------------------------------------------

    @Test
    void nullDescriptionIsPermitted() {
        ProfileConfig config = new ProfileConfig(
                NamespacedKey.strata("null-desc"),
                "Name", null, "Author",
                null, Path.of("/tmp"), sampleReader()
        );

        assertNull(config.description());
    }

    @Test
    void nullAuthorIsPermitted() {
        ProfileConfig config = new ProfileConfig(
                NamespacedKey.strata("null-author"),
                "Name", "Desc", null,
                null, Path.of("/tmp"), sampleReader()
        );

        assertNull(config.author());
    }

    // ---------------------------------------------------------------
    // rawConfig gives access to underlying TOML data
    // ---------------------------------------------------------------

    @Test
    void rawConfigProvidesAccessToTomlValues() {
        TomlReader raw = TomlReader.fromString("""
                name = "FromRaw"
                count = 7
                """);

        ProfileConfig config = new ProfileConfig(
                NamespacedKey.strata("raw-access"),
                "FromRaw", "", "Auth",
                null, Path.of("/tmp"), raw
        );

        assertEquals("FromRaw", config.rawConfig().getString("name"));
        assertEquals(7, config.rawConfig().getInt("count", 0));
    }

    // ---------------------------------------------------------------
    // toString contains all fields (record auto-generated)
    // ---------------------------------------------------------------

    @Test
    void toStringContainsFieldNames() {
        ProfileConfig config = new ProfileConfig(
                NamespacedKey.strata("tostring-test"),
                "Display", "Desc", "Author",
                null, Path.of("/tmp/ts"), sampleReader()
        );

        String str = config.toString();
        assertTrue(str.contains("Display"));
        assertTrue(str.contains("Desc"));
        assertTrue(str.contains("Author"));
    }
}
