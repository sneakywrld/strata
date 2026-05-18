package com.protectcord.strata.migrate.converter;

import com.protectcord.strata.migrate.report.MigrationReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BiomeConverterTest {

    private BiomeConverter converter;
    private MigrationReport report;

    @BeforeEach
    void setUp() {
        converter = new BiomeConverter();
        report = new MigrationReport();
    }

    // ── Basic conversion ────────────────────────────────────────────────

    @Test
    void convert_includesConvertedFromComment() {
        Map<String, Object> biome = basicBiome();
        String toml = converter.convert(biome, report);

        assertTrue(toml.contains("# Converted from Terra biome: PLAINS"));
    }

    @Test
    void convert_producesBiomeSection() {
        Map<String, Object> biome = basicBiome();
        String toml = converter.convert(biome, report);

        assertTrue(toml.contains("[biome]"));
    }

    @Test
    void convert_producesStrataIdFromTerraId() {
        Map<String, Object> biome = basicBiome();
        String toml = converter.convert(biome, report);

        assertTrue(toml.contains("id = \"strata:plains\""),
                "Should lowercase the Terra id and prefix with strata:");
    }

    @Test
    void convert_mapsVanillaField() {
        Map<String, Object> biome = basicBiome();
        String toml = converter.convert(biome, report);

        assertTrue(toml.contains("vanilla-mapping = \"minecraft:plains\""));
    }

    @Test
    void convert_includesClimateSection() {
        Map<String, Object> biome = basicBiome();
        String toml = converter.convert(biome, report);

        assertTrue(toml.contains("[biome.climate]"));
    }

    @Test
    void convert_defaultsClimateWhenMissing() {
        Map<String, Object> biome = new HashMap<>();
        biome.put("id", "EMPTY");
        String toml = converter.convert(biome, report);

        // Should use default values 0.5 for both
        assertTrue(toml.contains("temperature = 0.5"));
        assertTrue(toml.contains("humidity = 0.5"));
    }

    @Test
    void convert_defaultsVanillaWhenMissing() {
        Map<String, Object> biome = new HashMap<>();
        biome.put("id", "EMPTY");
        String toml = converter.convert(biome, report);

        assertTrue(toml.contains("vanilla-mapping = \"minecraft:plains\""),
                "Should default to minecraft:plains when vanilla is missing");
    }

    @Test
    void convert_registersConvertedItemInReport() {
        Map<String, Object> biome = basicBiome();
        converter.convert(biome, report);

        assertTrue(report.converted().contains("biome.PLAINS"),
                "Report should contain the biome conversion entry");
    }

    // ── Hyphenated IDs ──────────────────────────────────────────────────

    @Test
    void convert_replacesHyphensWithUnderscoresInId() {
        Map<String, Object> biome = new HashMap<>();
        biome.put("id", "DARK-FOREST-EDGE");
        biome.put("vanilla", "minecraft:dark_forest");
        String toml = converter.convert(biome, report);

        assertTrue(toml.contains("id = \"strata:dark_forest_edge\""),
                "Hyphens should become underscores in the Strata id");
    }

    // ── Terrain section ─────────────────────────────────────────────────

    @Test
    void convert_includesTerrainWhenPresent() {
        Map<String, Object> biome = basicBiome();
        Map<String, Object> terrain = new HashMap<>();
        terrain.put("base", 0.3);
        terrain.put("variation", 0.15);
        biome.put("terrain", terrain);

        String toml = converter.convert(biome, report);

        assertTrue(toml.contains("[biome.terrain]"));
        assertTrue(toml.contains("base-height = 0.3"));
        assertTrue(toml.contains("height-variation = 0.15"));
    }

    @Test
    void convert_omitsTerrainWhenAbsent() {
        Map<String, Object> biome = basicBiome();
        String toml = converter.convert(biome, report);

        assertFalse(toml.contains("[biome.terrain]"),
                "Terrain section should not appear when the source biome has no terrain map");
    }

    // ── Palette references ──────────────────────────────────────────────

    @Test
    void convert_includesPaletteRefsWhenPresent() {
        Map<String, Object> biome = basicBiome();

        Map<String, Object> paletteEntry = new HashMap<>();
        paletteEntry.put("PLAINS_SURFACE", 0);
        biome.put("palette", List.of(paletteEntry));

        String toml = converter.convert(biome, report);

        assertTrue(toml.contains("[biome.surface]"));
        assertTrue(toml.contains("# palette-ref:"));
    }

    @Test
    void convert_registersPaletteConversionInReport() {
        Map<String, Object> biome = basicBiome();

        Map<String, Object> paletteEntry = new HashMap<>();
        paletteEntry.put("PLAINS_SURFACE", 0);
        biome.put("palette", List.of(paletteEntry));

        converter.convert(biome, report);

        assertTrue(report.converted().contains("biome.PLAINS.palette"),
                "Report should track palette sub-conversion");
    }

    // ── Flora references ────────────────────────────────────────────────

    @Test
    void convert_includesFloraRefsWhenPresent() {
        Map<String, Object> biome = basicBiome();

        Map<String, Object> flora = new HashMap<>();
        flora.put("TALL_GRASS", 80);
        flora.put("POPPY", 20);
        biome.put("flora", flora);

        String toml = converter.convert(biome, report);

        assertTrue(toml.contains("[biome.features]"));
        assertTrue(toml.contains("# flora-ref: TALL_GRASS"));
        assertTrue(toml.contains("# flora-ref: POPPY"));
    }

    @Test
    void convert_registersFloraConversionInReport() {
        Map<String, Object> biome = basicBiome();

        Map<String, Object> flora = new HashMap<>();
        flora.put("TALL_GRASS", 80);
        biome.put("flora", flora);

        converter.convert(biome, report);

        assertTrue(report.converted().contains("biome.PLAINS.flora"),
                "Report should track flora sub-conversion");
    }

    // ── Missing fields ──────────────────────────────────────────────────

    @Test
    void convert_handlesCompletelyEmptyMap() {
        Map<String, Object> biome = new HashMap<>();
        String toml = converter.convert(biome, report);

        // Should produce valid output using defaults, not throw
        assertTrue(toml.contains("id = \"strata:unknown\""));
        assertTrue(toml.contains("[biome.climate]"));
        assertTrue(report.converted().contains("biome.unknown"));
    }

    @Test
    void convert_doesNotThrowWhenIdIsNull() {
        Map<String, Object> biome = new HashMap<>();
        biome.put("vanilla", "minecraft:forest");

        assertDoesNotThrow(() -> converter.convert(biome, report));
    }

    // ── Helpers ─────────────────────────────────────────────────────────

    private static Map<String, Object> basicBiome() {
        Map<String, Object> biome = new HashMap<>();
        biome.put("id", "PLAINS");
        biome.put("vanilla", "minecraft:plains");
        return biome;
    }
}
