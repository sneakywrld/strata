package com.protectcord.strata.migrate.converter;

import com.protectcord.strata.migrate.report.MigrationReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PaletteConverterTest {

    private PaletteConverter converter;
    private MigrationReport report;

    @BeforeEach
    void setUp() {
        converter = new PaletteConverter();
        report = new MigrationReport();
    }

    // ── Basic conversion ────────────────────────────────────────────────

    @Test
    void convert_includesConvertedFromComment() {
        Map<String, Object> palette = basicPalette();
        String toml = converter.convert(palette, report);

        assertTrue(toml.contains("# Converted from Terra palette: PLAINS_SURFACE"));
    }

    @Test
    void convert_producesSurfacePaletteSectionWithStrataId() {
        Map<String, Object> palette = basicPalette();
        String toml = converter.convert(palette, report);

        assertTrue(toml.contains("[surface-palette.\"strata:plains_surface\"]"),
                "Should produce a surface-palette section with lowercased strata-prefixed id");
    }

    @Test
    void convert_registersInReport() {
        Map<String, Object> palette = basicPalette();
        converter.convert(palette, report);

        assertTrue(report.converted().contains("palette.PLAINS_SURFACE"));
    }

    // ── Layer conversion ────────────────────────────────────────────────

    @Test
    void convert_outputsCorrectNumberOfLayers() {
        Map<String, Object> palette = basicPalette();
        String toml = converter.convert(palette, report);

        // Count TOML array-of-table markers
        long layerCount = toml.lines()
                .filter(l -> l.contains("[[surface-palette.\"strata:plains_surface\".layers]]"))
                .count();
        assertEquals(3, layerCount,
                "Should produce 3 layer table entries for the 3-layer fixture");
    }

    @Test
    void convert_outputsLayerDepths() {
        Map<String, Object> palette = basicPalette();
        String toml = converter.convert(palette, report);

        assertTrue(toml.contains("depth = 1"));
        assertTrue(toml.contains("depth = 3"));
        assertTrue(toml.contains("depth = 256"));
    }

    @Test
    void convert_includesMaterialComments() {
        Map<String, Object> palette = basicPalette();
        String toml = converter.convert(palette, report);

        // Materials in the fixture are List<Map>, so they go through the list branch
        assertTrue(toml.contains("# material:"),
                "Should include material comments from list-type materials");
    }

    // ── Map-style materials (weighted blocks) ───────────────────────────

    @Test
    void convert_handlesMapStyleMaterials() {
        Map<String, Object> palette = new HashMap<>();
        palette.put("id", "MIXED");

        // Map-style materials: block -> weight
        Map<String, Object> mats = new HashMap<>();
        mats.put("minecraft:stone", 3);
        mats.put("minecraft:andesite", 1);

        Map<String, Object> layer = new HashMap<>();
        layer.put("materials", mats);
        layer.put("layers", 5);

        palette.put("layers", List.of(layer));

        String toml = converter.convert(palette, report);

        assertTrue(toml.contains("depth = 5"));
        // Map-style materials produce block = "..." lines
        assertTrue(toml.contains("block = \""),
                "Map-style materials should produce block = \"...\" lines");
    }

    // ── Default depth ───────────────────────────────────────────────────

    @Test
    void convert_defaultsLayerDepthTo1WhenMissing() {
        Map<String, Object> palette = new HashMap<>();
        palette.put("id", "THIN");

        Map<String, Object> layer = new HashMap<>();
        // no "layers" key -> should default to 1
        Map<String, Object> mats = new HashMap<>();
        mats.put("minecraft:sand", 1);
        layer.put("materials", mats);

        palette.put("layers", List.of(layer));

        String toml = converter.convert(palette, report);

        assertTrue(toml.contains("depth = 1"),
                "Missing layer depth should default to 1");
    }

    // ── Edge cases ──────────────────────────────────────────────────────

    @Test
    void convert_handlesEmptyLayersList() {
        Map<String, Object> palette = new HashMap<>();
        palette.put("id", "EMPTY_LAYERS");
        palette.put("layers", List.of());

        String toml = converter.convert(palette, report);

        assertTrue(toml.contains("[surface-palette.\"strata:empty_layers\"]"));
        assertFalse(toml.contains("[[surface-palette."),
                "Should not contain layer entries for empty layers list");
        assertTrue(report.converted().contains("palette.EMPTY_LAYERS"));
    }

    @Test
    void convert_handlesNoLayersKey() {
        Map<String, Object> palette = new HashMap<>();
        palette.put("id", "NO_LAYERS");

        String toml = converter.convert(palette, report);

        assertTrue(toml.contains("[surface-palette.\"strata:no_layers\"]"));
        assertFalse(toml.contains("depth ="),
                "Should not produce depth entries when layers key is absent");
        assertTrue(report.converted().contains("palette.NO_LAYERS"));
    }

    @Test
    void convert_handlesCompletelyEmptyMap() {
        Map<String, Object> palette = new HashMap<>();
        String toml = converter.convert(palette, report);

        assertTrue(toml.contains("[surface-palette.\"strata:unknown\"]"));
        assertTrue(report.converted().contains("palette.unknown"));
    }

    // ── Hyphenated ID normalization ─────────────────────────────────────

    @Test
    void convert_replacesHyphensInIdWithUnderscores() {
        Map<String, Object> palette = new HashMap<>();
        palette.put("id", "DARK-FOREST-FLOOR");

        String toml = converter.convert(palette, report);

        assertTrue(toml.contains("strata:dark_forest_floor"));
    }

    // ── Helpers ─────────────────────────────────────────────────────────

    /**
     * Creates a palette matching the minimal fixture: grass_block (1), dirt (3), stone (256).
     * Materials are in list-of-map form as SnakeYAML parses them from the fixture YAML.
     */
    private static Map<String, Object> basicPalette() {
        Map<String, Object> palette = new HashMap<>();
        palette.put("id", "PLAINS_SURFACE");

        // SnakeYAML parses "- { minecraft:grass_block: 1 }" as List<Map<String,Integer>>
        Map<String, Object> grassMat = new HashMap<>();
        grassMat.put("minecraft:grass_block", 1);
        Map<String, Object> dirtMat = new HashMap<>();
        dirtMat.put("minecraft:dirt", 1);
        Map<String, Object> stoneMat = new HashMap<>();
        stoneMat.put("minecraft:stone", 1);

        Map<String, Object> layer1 = new HashMap<>();
        layer1.put("materials", List.of(grassMat));
        layer1.put("layers", 1);

        Map<String, Object> layer2 = new HashMap<>();
        layer2.put("materials", List.of(dirtMat));
        layer2.put("layers", 3);

        Map<String, Object> layer3 = new HashMap<>();
        layer3.put("materials", List.of(stoneMat));
        layer3.put("layers", 256);

        palette.put("layers", List.of(layer1, layer2, layer3));
        return palette;
    }
}
