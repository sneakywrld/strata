package com.protectcord.strata.migrate.converter;

import com.protectcord.strata.migrate.report.MigrationReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class NoiseConverterTest {

    private NoiseConverter converter;
    private MigrationReport report;

    @BeforeEach
    void setUp() {
        converter = new NoiseConverter();
        report = new MigrationReport();
    }

    // ── Basic conversion ────────────────────────────────────────────────

    @Test
    void convert_includesConvertedFromComment() {
        Map<String, Object> noise = basicNoise();
        String toml = converter.convert(noise, report);

        assertTrue(toml.contains("# Converted from Terra noise: base"));
    }

    @Test
    void convert_producesNoiseSectionWithStrataId() {
        Map<String, Object> noise = basicNoise();
        String toml = converter.convert(noise, report);

        assertTrue(toml.contains("[noise.\"strata:base\"]"));
    }

    @Test
    void convert_outputsFrequency() {
        Map<String, Object> noise = basicNoise();
        String toml = converter.convert(noise, report);

        assertTrue(toml.contains("frequency = 0.005"));
    }

    @Test
    void convert_registersInReport() {
        Map<String, Object> noise = basicNoise();
        converter.convert(noise, report);

        assertTrue(report.converted().contains("noise.base"));
    }

    // ── Noise type mapping ──────────────────────────────────────────────

    @ParameterizedTest(name = "Terra type \"{0}\" maps to Strata \"{1}\"")
    @CsvSource({
            "SIMPLEX,     simplex",
            "FBM,         simplex",
            "PERLIN,      perlin",
            "OpenSimplex2, open_simplex_2",
            "OPENSIMPLEX2S, open_simplex_2",
            "CELLULAR,    cellular",
            "VORONOI,     cellular",
            "VALUE,       value",
            "RIDGED,      ridged_multi",
            "RIDGED_MULTI, ridged_multi",
            "WHITE,       white",
    })
    void convert_mapsNoiseTypeCorrectly(String terraType, String expectedStrataType) {
        Map<String, Object> noise = new HashMap<>();
        noise.put("id", "test_noise");
        noise.put("type", terraType);

        String toml = converter.convert(noise, report);

        assertTrue(toml.contains("type = \"" + expectedStrataType + "\""),
                "Expected type = \"" + expectedStrataType + "\" in output, got:\n" + toml);
    }

    @Test
    void convert_unknownTypeDefaultsToSimplex() {
        Map<String, Object> noise = new HashMap<>();
        noise.put("id", "exotic");
        noise.put("type", "SOME_FUTURE_TYPE");

        String toml = converter.convert(noise, report);

        assertTrue(toml.contains("type = \"simplex\""));
    }

    // ── Dimensions comment ──────────────────────────────────────────────

    @Test
    void convert_includesDimensionsComment() {
        Map<String, Object> noise = basicNoise();
        noise.put("dimensions", 2);
        String toml = converter.convert(noise, report);

        assertTrue(toml.contains("# Original Terra dimensions: 2"));
    }

    @Test
    void convert_omitsDimensionsCommentWhenMissing() {
        Map<String, Object> noise = new HashMap<>();
        noise.put("id", "no_dims");
        noise.put("type", "SIMPLEX");

        String toml = converter.convert(noise, report);

        assertFalse(toml.contains("Original Terra dimensions"));
    }

    // ── Fractal settings ────────────────────────────────────────────────

    @Test
    void convert_includesFractalSectionForMultipleOctaves() {
        Map<String, Object> noise = basicNoise();
        noise.put("octaves", 4);
        noise.put("lacunarity", 2.5);
        noise.put("gain", 0.4);

        String toml = converter.convert(noise, report);

        assertTrue(toml.contains(".fractal]"), "Should contain a fractal subsection");
        assertTrue(toml.contains("octaves = 4"));
        assertTrue(toml.contains("lacunarity = 2.5"));
        assertTrue(toml.contains("gain = 0.4"));
    }

    @Test
    void convert_omitsFractalSectionForSingleOctave() {
        Map<String, Object> noise = basicNoise();
        noise.put("octaves", 1);

        String toml = converter.convert(noise, report);

        assertFalse(toml.contains("fractal"),
                "Fractal section should not appear when octaves = 1");
    }

    @Test
    void convert_omitsFractalSectionWhenOctavesAbsent() {
        Map<String, Object> noise = basicNoise();
        // no "octaves" key at all

        String toml = converter.convert(noise, report);

        assertFalse(toml.contains("fractal"),
                "Fractal section should not appear when octaves is absent (defaults to 1)");
    }

    @Test
    void convert_fractalDefaultsLacunarityAndGain() {
        Map<String, Object> noise = basicNoise();
        noise.put("octaves", 3);
        // do not set lacunarity or gain -- should use defaults

        String toml = converter.convert(noise, report);

        assertTrue(toml.contains("lacunarity = 2.0"));
        assertTrue(toml.contains("gain = 0.5"));
    }

    // ── Default values for missing fields ───────────────────────────────

    @Test
    void convert_defaultsFrequencyTo1WhenMissing() {
        Map<String, Object> noise = new HashMap<>();
        noise.put("id", "bare");
        noise.put("type", "SIMPLEX");

        String toml = converter.convert(noise, report);

        assertTrue(toml.contains("frequency = 1.0"));
    }

    @Test
    void convert_defaultsTypeWhenMissing() {
        Map<String, Object> noise = new HashMap<>();
        noise.put("id", "no_type");
        // no type key

        String toml = converter.convert(noise, report);

        assertTrue(toml.contains("type = \"simplex\""),
                "Missing type should default to SIMPLEX which maps to simplex");
    }

    @Test
    void convert_handlesEmptyMap() {
        Map<String, Object> noise = new HashMap<>();
        String toml = converter.convert(noise, report);

        assertTrue(toml.contains("[noise.\"strata:unknown\"]"));
        assertTrue(toml.contains("type = \"simplex\""));
        assertTrue(report.converted().contains("noise.unknown"));
    }

    // ── Hyphenated ID normalization ─────────────────────────────────────

    @Test
    void convert_replacesHyphensInIdWithUnderscores() {
        Map<String, Object> noise = new HashMap<>();
        noise.put("id", "continent-base-noise");
        noise.put("type", "SIMPLEX");

        String toml = converter.convert(noise, report);

        assertTrue(toml.contains("[noise.\"strata:continent_base_noise\"]"));
    }

    // ── Helpers ─────────────────────────────────────────────────────────

    private static Map<String, Object> basicNoise() {
        Map<String, Object> noise = new HashMap<>();
        noise.put("id", "base");
        noise.put("type", "OpenSimplex2");
        noise.put("frequency", 0.005);
        return noise;
    }
}
