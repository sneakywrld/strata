package com.protectcord.strata.migrate.terra;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TerraPackParserTest {

    private Path fixtureDir(String name) throws URISyntaxException {
        return Path.of(getClass().getClassLoader()
                .getResource("terra-fixtures/" + name).toURI());
    }

    // ── Minimal pack tests ──────────────────────────────────────────────

    @Test
    void parseMinimalPack_returnsCorrectPackId() throws Exception {
        TerraPackParser parser = new TerraPackParser(fixtureDir("minimal-pack"));
        TerraPackData data = parser.parse();

        assertEquals("test-pack", data.packId());
    }

    @Test
    void parseMinimalPack_returnsCorrectAuthor() throws Exception {
        TerraPackParser parser = new TerraPackParser(fixtureDir("minimal-pack"));
        TerraPackData data = parser.parse();

        assertEquals("Test Author", data.author());
    }

    @Test
    void parseMinimalPack_loadsOneBiome() throws Exception {
        TerraPackParser parser = new TerraPackParser(fixtureDir("minimal-pack"));
        TerraPackData data = parser.parse();

        assertEquals(1, data.biomes().size());
    }

    @Test
    void parseMinimalPack_biomeHasExpectedId() throws Exception {
        TerraPackParser parser = new TerraPackParser(fixtureDir("minimal-pack"));
        TerraPackData data = parser.parse();

        Map<String, Object> biome = data.biomes().get(0);
        assertEquals("PLAINS", biome.get("id"));
    }

    @Test
    void parseMinimalPack_biomeHasVanillaMapping() throws Exception {
        TerraPackParser parser = new TerraPackParser(fixtureDir("minimal-pack"));
        TerraPackData data = parser.parse();

        Map<String, Object> biome = data.biomes().get(0);
        assertEquals("minecraft:plains", biome.get("vanilla"));
    }

    @Test
    void parseMinimalPack_biomeHasPaletteList() throws Exception {
        TerraPackParser parser = new TerraPackParser(fixtureDir("minimal-pack"));
        TerraPackData data = parser.parse();

        Map<String, Object> biome = data.biomes().get(0);
        assertInstanceOf(List.class, biome.get("palette"));
        List<?> palettes = (List<?>) biome.get("palette");
        assertFalse(palettes.isEmpty(), "palette list should not be empty");
    }

    @Test
    void parseMinimalPack_biomeHasTags() throws Exception {
        TerraPackParser parser = new TerraPackParser(fixtureDir("minimal-pack"));
        TerraPackData data = parser.parse();

        Map<String, Object> biome = data.biomes().get(0);
        assertInstanceOf(List.class, biome.get("tags"));
        List<?> tags = (List<?>) biome.get("tags");
        assertTrue(tags.contains("LAND"));
        assertTrue(tags.contains("OVERWORLD_SURFACE"));
    }

    @Test
    void parseMinimalPack_loadsOneNoise() throws Exception {
        TerraPackParser parser = new TerraPackParser(fixtureDir("minimal-pack"));
        TerraPackData data = parser.parse();

        assertEquals(1, data.noises().size());
    }

    @Test
    void parseMinimalPack_noiseHasExpectedFields() throws Exception {
        TerraPackParser parser = new TerraPackParser(fixtureDir("minimal-pack"));
        TerraPackData data = parser.parse();

        Map<String, Object> noise = data.noises().get(0);
        assertEquals("base", noise.get("id"));
        assertEquals("OpenSimplex2", noise.get("type"));
        assertEquals(0.005, ((Number) noise.get("frequency")).doubleValue(), 1e-9);
        assertEquals(2, ((Number) noise.get("dimensions")).intValue());
    }

    @Test
    void parseMinimalPack_loadsOnePalette() throws Exception {
        TerraPackParser parser = new TerraPackParser(fixtureDir("minimal-pack"));
        TerraPackData data = parser.parse();

        assertEquals(1, data.palettes().size());
    }

    @Test
    void parseMinimalPack_paletteHasExpectedId() throws Exception {
        TerraPackParser parser = new TerraPackParser(fixtureDir("minimal-pack"));
        TerraPackData data = parser.parse();

        Map<String, Object> palette = data.palettes().get(0);
        assertEquals("PLAINS_SURFACE", palette.get("id"));
    }

    @Test
    void parseMinimalPack_paletteHasThreeLayers() throws Exception {
        TerraPackParser parser = new TerraPackParser(fixtureDir("minimal-pack"));
        TerraPackData data = parser.parse();

        Map<String, Object> palette = data.palettes().get(0);
        assertInstanceOf(List.class, palette.get("layers"));
        List<?> layers = (List<?>) palette.get("layers");
        assertEquals(3, layers.size());
    }

    @Test
    void parseMinimalPack_emptyCollectionsForMissingSubdirs() throws Exception {
        TerraPackParser parser = new TerraPackParser(fixtureDir("minimal-pack"));
        TerraPackData data = parser.parse();

        assertTrue(data.flora().isEmpty(), "flora should be empty");
        assertTrue(data.structures().isEmpty(), "structures should be empty");
        assertTrue(data.carvers().isEmpty(), "carvers should be empty");
    }

    @Test
    void parseMinimalPack_biomeHasSourceFileMetadata() throws Exception {
        TerraPackParser parser = new TerraPackParser(fixtureDir("minimal-pack"));
        TerraPackData data = parser.parse();

        Map<String, Object> biome = data.biomes().get(0);
        String sourceFile = (String) biome.get("_source_file");
        assertNotNull(sourceFile, "_source_file metadata should be present");
        assertTrue(sourceFile.contains("plains.yml"),
                "_source_file should reference plains.yml, got: " + sourceFile);
    }

    // ── Empty pack tests ────────────────────────────────────────────────

    @Test
    void parseEmptyPack_returnsEmptyBiomes() throws Exception {
        TerraPackParser parser = new TerraPackParser(fixtureDir("empty-pack"));
        TerraPackData data = parser.parse();

        assertTrue(data.biomes().isEmpty());
    }

    @Test
    void parseEmptyPack_returnsEmptyNoise() throws Exception {
        TerraPackParser parser = new TerraPackParser(fixtureDir("empty-pack"));
        TerraPackData data = parser.parse();

        assertTrue(data.noises().isEmpty());
    }

    @Test
    void parseEmptyPack_returnsEmptyPalettes() throws Exception {
        TerraPackParser parser = new TerraPackParser(fixtureDir("empty-pack"));
        TerraPackData data = parser.parse();

        assertTrue(data.palettes().isEmpty());
    }

    @Test
    void parseEmptyPack_returnsCorrectPackId() throws Exception {
        TerraPackParser parser = new TerraPackParser(fixtureDir("empty-pack"));
        TerraPackData data = parser.parse();

        assertEquals("empty-pack", data.packId());
    }

    // ── Error handling ──────────────────────────────────────────────────

    @Test
    void parseNonExistentDirectory_throwsIOException() {
        Path bogus = Path.of("/does/not/exist/at/all");
        TerraPackParser parser = new TerraPackParser(bogus);

        assertThrows(IOException.class, parser::parse);
    }

    @Test
    void parseNonExistentDirectory_exceptionContainsPath() {
        Path bogus = Path.of("/does/not/exist/at/all");
        TerraPackParser parser = new TerraPackParser(bogus);

        IOException ex = assertThrows(IOException.class, parser::parse);
        assertTrue(ex.getMessage().contains(bogus.toString()),
                "Exception message should contain the path");
    }

    @Test
    void parseDirectoryWithNoPackYml_returnsDefaultMetadata(@TempDir Path tmp) throws Exception {
        // Create an empty directory that exists but has no pack.yml
        Path biomeDir = tmp.resolve("biomes");
        Files.createDirectories(biomeDir);

        TerraPackParser parser = new TerraPackParser(tmp);
        TerraPackData data = parser.parse();

        // With no pack.yml, getString defaults kick in
        assertEquals("unknown", data.packId());
        assertEquals("Unknown", data.author());
    }
}
