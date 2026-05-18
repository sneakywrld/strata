package com.protectcord.strata.config.export;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.config.loader.TomlReader;
import com.protectcord.strata.config.model.ProfileConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConfigExporterTest {

    private ConfigExporter exporter;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        exporter = new ConfigExporter();
    }

    private ProfileConfig profileWithToml(String toml) {
        return new ProfileConfig(
                NamespacedKey.strata("export-test"),
                "Export Test",
                "desc",
                "author",
                null,
                Path.of("/tmp/export"),
                TomlReader.fromString(toml)
        );
    }

    // ---------------------------------------------------------------
    // toJson: basic roundtrip
    // ---------------------------------------------------------------
    @Nested
    class ToJson {

        @Test
        void producesValidJson() throws JsonProcessingException {
            ProfileConfig profile = profileWithToml("""
                    name = "TestProfile"
                    version = "1.0"
                    """);

            String json = exporter.toJson(profile);
            assertNotNull(json);
            assertFalse(json.isBlank());

            // Verify it's valid JSON by parsing it
            assertDoesNotThrow(() -> mapper.readTree(json));
        }

        @Test
        void jsonContainsStringValues() throws JsonProcessingException {
            ProfileConfig profile = profileWithToml("""
                    name = "Hello"
                    greeting = "World"
                    """);

            String json = exporter.toJson(profile);
            @SuppressWarnings("unchecked")
            Map<String, Object> parsed = mapper.readValue(json, Map.class);

            assertEquals("Hello", parsed.get("name"));
            assertEquals("World", parsed.get("greeting"));
        }

        @Test
        void jsonContainsNumericValues() throws JsonProcessingException {
            ProfileConfig profile = profileWithToml("""
                    count = 42
                    ratio = 3.14
                    """);

            String json = exporter.toJson(profile);
            @SuppressWarnings("unchecked")
            Map<String, Object> parsed = mapper.readValue(json, Map.class);

            assertEquals(42, ((Number) parsed.get("count")).intValue());
            assertEquals(3.14, ((Number) parsed.get("ratio")).doubleValue(), 0.001);
        }

        @Test
        void jsonContainsBooleanValues() throws JsonProcessingException {
            ProfileConfig profile = profileWithToml("""
                    active = true
                    debug = false
                    """);

            String json = exporter.toJson(profile);
            @SuppressWarnings("unchecked")
            Map<String, Object> parsed = mapper.readValue(json, Map.class);

            assertEquals(true, parsed.get("active"));
            assertEquals(false, parsed.get("debug"));
        }

        @Test
        void jsonContainsArrayValues() throws JsonProcessingException {
            ProfileConfig profile = profileWithToml("""
                    tags = ["one", "two", "three"]
                    """);

            String json = exporter.toJson(profile);
            @SuppressWarnings("unchecked")
            Map<String, Object> parsed = mapper.readValue(json, Map.class);

            assertInstanceOf(java.util.List.class, parsed.get("tags"));
            @SuppressWarnings("unchecked")
            java.util.List<String> tags = (java.util.List<String>) parsed.get("tags");
            assertEquals(3, tags.size());
            assertEquals("one", tags.get(0));
        }

        @Test
        void jsonIsIndented() throws JsonProcessingException {
            ProfileConfig profile = profileWithToml("""
                    key = "value"
                    """);

            String json = exporter.toJson(profile);
            // Indented output contains newlines and leading spaces
            assertTrue(json.contains("\n"));
        }

        @Test
        void emptyTomlProducesEmptyJsonObject() throws JsonProcessingException {
            ProfileConfig profile = profileWithToml("");

            String json = exporter.toJson(profile);
            @SuppressWarnings("unchecked")
            Map<String, Object> parsed = mapper.readValue(json, Map.class);

            assertTrue(parsed.isEmpty());
        }
    }

    // ---------------------------------------------------------------
    // toJson: nested tables
    // ---------------------------------------------------------------
    @Nested
    class NestedTables {

        @Test
        void nestedTablesAreSerializedAsNestedJson() throws JsonProcessingException {
            ProfileConfig profile = profileWithToml("""
                    name = "nested"

                    [world]
                    min-y = -64
                    max-y = 320
                    """);

            String json = exporter.toJson(profile);
            @SuppressWarnings("unchecked")
            Map<String, Object> parsed = mapper.readValue(json, Map.class);

            assertEquals("nested", parsed.get("name"));
            assertInstanceOf(Map.class, parsed.get("world"));

            @SuppressWarnings("unchecked")
            Map<String, Object> world = (Map<String, Object>) parsed.get("world");
            assertEquals(-64, ((Number) world.get("min-y")).intValue());
            assertEquals(320, ((Number) world.get("max-y")).intValue());
        }

        @Test
        void deeplyNestedTablesArePreserved() throws JsonProcessingException {
            ProfileConfig profile = profileWithToml("""
                    [a]
                    x = 1
                    [a.b]
                    y = 2
                    [a.b.c]
                    z = 3
                    """);

            String json = exporter.toJson(profile);
            @SuppressWarnings("unchecked")
            Map<String, Object> parsed = mapper.readValue(json, Map.class);

            @SuppressWarnings("unchecked")
            Map<String, Object> a = (Map<String, Object>) parsed.get("a");
            assertNotNull(a);
            assertEquals(1, ((Number) a.get("x")).intValue());

            @SuppressWarnings("unchecked")
            Map<String, Object> b = (Map<String, Object>) a.get("b");
            assertNotNull(b);
            assertEquals(2, ((Number) b.get("y")).intValue());

            @SuppressWarnings("unchecked")
            Map<String, Object> c = (Map<String, Object>) b.get("c");
            assertNotNull(c);
            assertEquals(3, ((Number) c.get("z")).intValue());
        }
    }

    // ---------------------------------------------------------------
    // exportToFile
    // ---------------------------------------------------------------
    @Nested
    class ExportToFile {

        @Test
        void writesJsonFileToDisk(@TempDir Path tempDir) throws IOException {
            ProfileConfig profile = profileWithToml("""
                    name = "FileExport"
                    version = "2.0"
                    count = 10
                    """);

            Path output = tempDir.resolve("exported.json");
            exporter.exportToFile(profile, output);

            assertTrue(Files.exists(output));
            String content = Files.readString(output);
            assertFalse(content.isBlank());

            // Verify the file contains valid JSON with the expected data
            @SuppressWarnings("unchecked")
            Map<String, Object> parsed = mapper.readValue(content, Map.class);
            assertEquals("FileExport", parsed.get("name"));
            assertEquals("2.0", parsed.get("version"));
            assertEquals(10, ((Number) parsed.get("count")).intValue());
        }

        @Test
        void overwritesExistingFile(@TempDir Path tempDir) throws IOException {
            Path output = tempDir.resolve("overwrite.json");
            Files.writeString(output, "old content");

            ProfileConfig profile = profileWithToml("key = \"new\"");
            exporter.exportToFile(profile, output);

            String content = Files.readString(output);
            assertFalse(content.contains("old content"));
            assertTrue(content.contains("new"));
        }

        @Test
        void emptyConfigProducesEmptyJsonFile(@TempDir Path tempDir) throws IOException {
            ProfileConfig profile = profileWithToml("");
            Path output = tempDir.resolve("empty.json");
            exporter.exportToFile(profile, output);

            assertTrue(Files.exists(output));
            String content = Files.readString(output);
            @SuppressWarnings("unchecked")
            Map<String, Object> parsed = mapper.readValue(content, Map.class);
            assertTrue(parsed.isEmpty());
        }
    }

    // ---------------------------------------------------------------
    // Roundtrip: TOML -> JSON -> verify all values
    // ---------------------------------------------------------------
    @Nested
    class Roundtrip {

        @Test
        void fullRoundtripPreservesAllData() throws JsonProcessingException {
            String toml = """
                    name = "RoundTrip"
                    version = "3.0"
                    enabled = true
                    count = 100
                    ratio = 2.718
                    tags = ["x", "y", "z"]

                    [world]
                    min-y = 0
                    max-y = 256

                    [world.settings]
                    generate-structures = true
                    """;

            ProfileConfig profile = profileWithToml(toml);
            String json = exporter.toJson(profile);

            // Parse back and verify every field
            @SuppressWarnings("unchecked")
            Map<String, Object> parsed = mapper.readValue(json, Map.class);

            assertEquals("RoundTrip", parsed.get("name"));
            assertEquals("3.0", parsed.get("version"));
            assertEquals(true, parsed.get("enabled"));
            assertEquals(100, ((Number) parsed.get("count")).intValue());
            assertEquals(2.718, ((Number) parsed.get("ratio")).doubleValue(), 0.001);

            @SuppressWarnings("unchecked")
            java.util.List<String> tags = (java.util.List<String>) parsed.get("tags");
            assertEquals(java.util.List.of("x", "y", "z"), tags);

            @SuppressWarnings("unchecked")
            Map<String, Object> world = (Map<String, Object>) parsed.get("world");
            assertEquals(0, ((Number) world.get("min-y")).intValue());
            assertEquals(256, ((Number) world.get("max-y")).intValue());

            @SuppressWarnings("unchecked")
            Map<String, Object> settings = (Map<String, Object>) world.get("settings");
            assertEquals(true, settings.get("generate-structures"));
        }
    }
}
