package com.protectcord.strata.config.loader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TomlReaderTest {

    private static final String FULL_TOML = """
            name = "TestProfile"
            version = "1.0.0"
            enabled = true
            count = 42
            ratio = 3.14
            big-number = 9999999999
            tags = ["alpha", "beta"]

            [world]
            min-y = -64
            max-y = 320
            sea-level = 63

            [world.nested]
            deep = "value"
            """;

    private TomlReader reader;

    @BeforeEach
    void setUp() {
        reader = TomlReader.fromString(FULL_TOML);
    }

    // ---------------------------------------------------------------
    // getString
    // ---------------------------------------------------------------
    @Nested
    class GetString {

        @Test
        void returnsValueForExistingStringKey() {
            assertEquals("TestProfile", reader.getString("name"));
        }

        @Test
        void returnsVersionString() {
            assertEquals("1.0.0", reader.getString("version"));
        }

        @Test
        void returnsNullForMissingKey() {
            assertNull(reader.getString("nonexistent"));
        }
    }

    // ---------------------------------------------------------------
    // getOptionalString
    // ---------------------------------------------------------------
    @Nested
    class GetOptionalString {

        @Test
        void returnsPresentOptionalForExistingKey() {
            Optional<String> opt = reader.getOptionalString("name");
            assertTrue(opt.isPresent());
            assertEquals("TestProfile", opt.get());
        }

        @Test
        void returnsEmptyOptionalForMissingKey() {
            Optional<String> opt = reader.getOptionalString("missing");
            assertTrue(opt.isEmpty());
        }
    }

    // ---------------------------------------------------------------
    // getInt
    // ---------------------------------------------------------------
    @Nested
    class GetInt {

        @Test
        void returnsIntValueForExistingKey() {
            assertEquals(42, reader.getInt("count", 0));
        }

        @Test
        void returnsDefaultForMissingKey() {
            assertEquals(99, reader.getInt("missing-key", 99));
        }

        @Test
        void returnsNestedIntValue() {
            assertEquals(-64, reader.getInt("world.min-y", 0));
            assertEquals(320, reader.getInt("world.max-y", 0));
            assertEquals(63, reader.getInt("world.sea-level", 0));
        }
    }

    // ---------------------------------------------------------------
    // getDouble
    // ---------------------------------------------------------------
    @Nested
    class GetDouble {

        @Test
        void returnsDoubleValueForExistingKey() {
            assertEquals(3.14, reader.getDouble("ratio", 0.0), 0.001);
        }

        @Test
        void returnsDefaultForMissingKey() {
            assertEquals(2.718, reader.getDouble("missing", 2.718), 0.001);
        }

        @Test
        void returnsIntValueAsDouble() {
            // TOML integer 42 can be read as double
            assertEquals(42.0, reader.getDouble("count", 0.0), 0.001);
        }
    }

    // ---------------------------------------------------------------
    // getBoolean
    // ---------------------------------------------------------------
    @Nested
    class GetBoolean {

        @Test
        void returnsTrueForEnabledKey() {
            assertTrue(reader.getBoolean("enabled", false));
        }

        @Test
        void returnsDefaultForMissingKey() {
            assertFalse(reader.getBoolean("missing-bool", false));
            assertTrue(reader.getBoolean("missing-bool", true));
        }
    }

    // ---------------------------------------------------------------
    // getLong
    // ---------------------------------------------------------------
    @Nested
    class GetLong {

        @Test
        void returnsLongValueForExistingKey() {
            assertEquals(9999999999L, reader.getLong("big-number", 0L));
        }

        @Test
        void returnsIntAsLong() {
            assertEquals(42L, reader.getLong("count", 0L));
        }

        @Test
        void returnsDefaultForMissingKey() {
            assertEquals(100L, reader.getLong("not-here", 100L));
        }
    }

    // ---------------------------------------------------------------
    // getList
    // ---------------------------------------------------------------
    @Nested
    class GetList {

        @Test
        void returnsListForExistingArrayKey() {
            List<String> tags = reader.getList("tags");
            assertEquals(2, tags.size());
            assertEquals("alpha", tags.get(0));
            assertEquals("beta", tags.get(1));
        }

        @Test
        void returnsEmptyListForMissingKey() {
            List<String> list = reader.getList("nonexistent-list");
            assertNotNull(list);
            assertTrue(list.isEmpty());
        }
    }

    // ---------------------------------------------------------------
    // getSubSection
    // ---------------------------------------------------------------
    @Nested
    class GetSubSection {

        @Test
        void returnsSubSectionForExistingTable() {
            Optional<TomlReader> world = reader.getSubSection("world");
            assertTrue(world.isPresent());
            assertEquals(-64, world.get().getInt("min-y", 0));
            assertEquals(320, world.get().getInt("max-y", 0));
        }

        @Test
        void subSectionContainsNestedTable() {
            Optional<TomlReader> world = reader.getSubSection("world");
            assertTrue(world.isPresent());

            Optional<TomlReader> nested = world.get().getSubSection("nested");
            assertTrue(nested.isPresent());
            assertEquals("value", nested.get().getString("deep"));
        }

        @Test
        void returnsEmptyForMissingSubSection() {
            Optional<TomlReader> opt = reader.getSubSection("does-not-exist");
            assertTrue(opt.isEmpty());
        }
    }

    // ---------------------------------------------------------------
    // contains
    // ---------------------------------------------------------------
    @Nested
    class Contains {

        @Test
        void returnsTrueForTopLevelKey() {
            assertTrue(reader.contains("name"));
            assertTrue(reader.contains("enabled"));
            assertTrue(reader.contains("count"));
        }

        @Test
        void returnsTrueForNestedPath() {
            assertTrue(reader.contains("world.min-y"));
            assertTrue(reader.contains("world.nested.deep"));
        }

        @Test
        void returnsFalseForMissingKey() {
            assertFalse(reader.contains("nope"));
            assertFalse(reader.contains("world.nonexistent"));
        }
    }

    // ---------------------------------------------------------------
    // toMap
    // ---------------------------------------------------------------
    @Nested
    class ToMap {

        @Test
        void returnsAllTopLevelEntries() {
            Map<String, Object> map = reader.toMap();
            assertTrue(map.containsKey("name"));
            assertTrue(map.containsKey("version"));
            assertTrue(map.containsKey("enabled"));
            assertTrue(map.containsKey("count"));
            assertTrue(map.containsKey("ratio"));
            assertTrue(map.containsKey("tags"));
            assertTrue(map.containsKey("world"));
        }

        @Test
        void topLevelValuesHaveCorrectTypes() {
            Map<String, Object> map = reader.toMap();
            assertInstanceOf(String.class, map.get("name"));
            assertInstanceOf(Number.class, map.get("count"));
            assertInstanceOf(List.class, map.get("tags"));
        }

        @Test
        void topLevelCountMatchesExpectedKeys() {
            Map<String, Object> map = reader.toMap();
            // name, version, enabled, count, ratio, big-number, tags, world
            assertEquals(8, map.size());
        }
    }

    // ---------------------------------------------------------------
    // raw
    // ---------------------------------------------------------------
    @Nested
    class Raw {

        @Test
        void returnsNonNullConfigObject() {
            assertNotNull(reader.raw());
        }
    }

    // ---------------------------------------------------------------
    // fromFile
    // ---------------------------------------------------------------
    @Nested
    class FromFile {

        @Test
        void readsTomlFromDisk(@TempDir Path tempDir) throws IOException {
            Path file = tempDir.resolve("test.toml");
            Files.writeString(file, """
                    greeting = "hello"
                    level = 5
                    """);

            TomlReader fromDisk = TomlReader.fromFile(file);
            assertEquals("hello", fromDisk.getString("greeting"));
            assertEquals(5, fromDisk.getInt("level", 0));
        }

        @Test
        void readsNestedTomlFromDisk(@TempDir Path tempDir) throws IOException {
            Path file = tempDir.resolve("nested.toml");
            Files.writeString(file, """
                    [section]
                    key = "val"
                    num = 10
                    """);

            TomlReader fromDisk = TomlReader.fromFile(file);
            assertEquals("val", fromDisk.getString("section.key"));
            assertEquals(10, fromDisk.getInt("section.num", 0));
        }
    }

    // ---------------------------------------------------------------
    // Empty TOML
    // ---------------------------------------------------------------
    @Nested
    class EmptyToml {

        @Test
        void emptyStringParsesWithoutError() {
            TomlReader empty = TomlReader.fromString("");
            assertNotNull(empty);
        }

        @Test
        void emptyTomlContainsNoKeys() {
            TomlReader empty = TomlReader.fromString("");
            assertTrue(empty.toMap().isEmpty());
        }

        @Test
        void emptyTomlReturnsDefaultsForAllGetters() {
            TomlReader empty = TomlReader.fromString("");
            assertNull(empty.getString("any"));
            assertTrue(empty.getOptionalString("any").isEmpty());
            assertEquals(7, empty.getInt("any", 7));
            assertEquals(1.5, empty.getDouble("any", 1.5), 0.001);
            assertFalse(empty.getBoolean("any", false));
            assertEquals(0L, empty.getLong("any", 0L));
            assertTrue(empty.getList("any").isEmpty());
            assertTrue(empty.getSubSection("any").isEmpty());
            assertFalse(empty.contains("any"));
        }
    }

    // ---------------------------------------------------------------
    // Comments and whitespace
    // ---------------------------------------------------------------
    @Nested
    class TomlEdgeCases {

        @Test
        void commentsAreIgnored() {
            TomlReader r = TomlReader.fromString("""
                    # This is a comment
                    key = "value"
                    # Another comment
                    """);
            assertEquals("value", r.getString("key"));
            assertEquals(1, r.toMap().size());
        }

        @Test
        void multipleTablesAreParsed() {
            TomlReader r = TomlReader.fromString("""
                    [a]
                    x = 1
                    [b]
                    y = 2
                    [c]
                    z = 3
                    """);
            assertEquals(1, r.getInt("a.x", 0));
            assertEquals(2, r.getInt("b.y", 0));
            assertEquals(3, r.getInt("c.z", 0));
        }
    }
}
