package com.protectcord.strata.migrate.report;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MigrationReportTest {

    private MigrationReport report;

    @BeforeEach
    void setUp() {
        report = new MigrationReport();
    }

    // ── Converted items ─────────────────────────────────────────────────

    @Test
    void addConverted_appearsInList() {
        report.addConverted("biome.PLAINS");

        assertEquals(1, report.converted().size());
        assertTrue(report.converted().contains("biome.PLAINS"));
    }

    @Test
    void addConverted_multipleItems() {
        report.addConverted("biome.PLAINS");
        report.addConverted("biome.DESERT");
        report.addConverted("noise.base");

        assertEquals(3, report.converted().size());
    }

    // ── Approximated items ──────────────────────────────────────────────

    @Test
    void addApproximated_appearsInList() {
        report.addApproximated("structure.village", "Manual tuning needed");

        assertEquals(1, report.approximated().size());
    }

    @Test
    void addApproximated_formatsWithDash() {
        report.addApproximated("carvers", "Parameters were approximated");

        String entry = report.approximated().get(0);
        assertTrue(entry.contains("carvers"));
        assertTrue(entry.contains("\u2014")); // em dash
        assertTrue(entry.contains("Parameters were approximated"));
    }

    // ── Unsupported items ───────────────────────────────────────────────

    @Test
    void addUnsupported_appearsInList() {
        report.addUnsupported("feature.bamboo", "Not supported in Strata");

        assertEquals(1, report.unsupported().size());
    }

    @Test
    void addUnsupported_formatsWithDash() {
        report.addUnsupported("feature.bamboo", "Not supported in Strata");

        String entry = report.unsupported().get(0);
        assertTrue(entry.contains("feature.bamboo"));
        assertTrue(entry.contains("\u2014"));
        assertTrue(entry.contains("Not supported in Strata"));
    }

    // ── Warnings ────────────────────────────────────────────────────────

    @Test
    void addWarning_appearsInList() {
        report.addWarning("Palette STONE_MIX uses weighted random; approximated");

        assertEquals(1, report.warnings().size());
        assertEquals("Palette STONE_MIX uses weighted random; approximated",
                report.warnings().get(0));
    }

    @Test
    void addWarning_multipleWarnings() {
        report.addWarning("warn1");
        report.addWarning("warn2");

        assertEquals(2, report.warnings().size());
    }

    // ── Errors ──────────────────────────────────────────────────────────

    @Test
    void addError_appearsInList() {
        report.addError("Failed to convert biome JUNGLE: null pointer");

        assertEquals(1, report.errors().size());
        assertEquals("Failed to convert biome JUNGLE: null pointer",
                report.errors().get(0));
    }

    @Test
    void hasErrors_falseWhenNoErrors() {
        assertFalse(report.hasErrors());
    }

    @Test
    void hasErrors_trueWhenErrorsExist() {
        report.addError("something broke");

        assertTrue(report.hasErrors());
    }

    // ── Empty report ────────────────────────────────────────────────────

    @Test
    void freshReport_allListsEmpty() {
        assertTrue(report.converted().isEmpty());
        assertTrue(report.approximated().isEmpty());
        assertTrue(report.unsupported().isEmpty());
        assertTrue(report.warnings().isEmpty());
        assertTrue(report.errors().isEmpty());
    }

    @Test
    void freshReport_hasErrorsIsFalse() {
        assertFalse(report.hasErrors());
    }

    // ── toText output ───────────────────────────────────────────────────

    @Test
    void toText_containsHeader() {
        String text = report.toText();

        assertTrue(text.contains("=== Strata Migration Report ==="));
    }

    @Test
    void toText_showsCountsForEmptyReport() {
        String text = report.toText();

        assertTrue(text.contains("Converted: 0 items"));
        assertTrue(text.contains("Approximated: 0 items"));
        assertTrue(text.contains("Unsupported: 0 items"));
        assertTrue(text.contains("Warnings: 0"));
        assertTrue(text.contains("Errors: 0"));
    }

    @Test
    void toText_showsCorrectConvertedCount() {
        report.addConverted("biome.PLAINS");
        report.addConverted("noise.base");

        String text = report.toText();

        assertTrue(text.contains("Converted: 2 items"));
    }

    @Test
    void toText_includesConvertedItemsWithOkPrefix() {
        report.addConverted("biome.PLAINS");

        String text = report.toText();

        assertTrue(text.contains("[OK] biome.PLAINS"));
    }

    @Test
    void toText_includesApproximatedSectionHeader() {
        report.addApproximated("structure.village", "Approximate conversion");

        String text = report.toText();

        assertTrue(text.contains("--- Approximated (manual review recommended) ---"));
        assertTrue(text.contains("[~]"));
    }

    @Test
    void toText_includesUnsupportedSectionHeader() {
        report.addUnsupported("feature.bamboo", "Not supported");

        String text = report.toText();

        assertTrue(text.contains("--- Unsupported (manual conversion needed) ---"));
        assertTrue(text.contains("[!]"));
    }

    @Test
    void toText_includesWarningsSectionHeader() {
        report.addWarning("Something needs attention");

        String text = report.toText();

        assertTrue(text.contains("--- Warnings ---"));
        assertTrue(text.contains("[W] Something needs attention"));
    }

    @Test
    void toText_includesErrorsSectionHeader() {
        report.addError("Conversion failed: bad input");

        String text = report.toText();

        assertTrue(text.contains("--- Errors ---"));
        assertTrue(text.contains("[E] Conversion failed: bad input"));
    }

    @Test
    void toText_omitsEmptySections() {
        // Only add a converted item, nothing else
        report.addConverted("biome.PLAINS");

        String text = report.toText();

        assertTrue(text.contains("--- Successfully Converted ---"));
        assertFalse(text.contains("--- Approximated"));
        assertFalse(text.contains("--- Unsupported"));
        assertFalse(text.contains("--- Warnings ---"));
        assertFalse(text.contains("--- Errors ---"));
    }

    @Test
    void toText_fullReport() {
        report.addConverted("biome.PLAINS");
        report.addConverted("noise.base");
        report.addApproximated("structure.village", "Layout may differ");
        report.addUnsupported("feature.custom_tree", "Custom trees not supported");
        report.addWarning("Pack version is old");
        report.addError("Failed to read carver config");

        String text = report.toText();

        // Verify all counts
        assertTrue(text.contains("Converted: 2 items"));
        assertTrue(text.contains("Approximated: 1 items"));
        assertTrue(text.contains("Unsupported: 1 items"));
        assertTrue(text.contains("Warnings: 1"));
        assertTrue(text.contains("Errors: 1"));

        // Verify all sections present
        assertTrue(text.contains("--- Successfully Converted ---"));
        assertTrue(text.contains("--- Approximated (manual review recommended) ---"));
        assertTrue(text.contains("--- Unsupported (manual conversion needed) ---"));
        assertTrue(text.contains("--- Warnings ---"));
        assertTrue(text.contains("--- Errors ---"));

        // Verify specific entries
        assertTrue(text.contains("[OK] biome.PLAINS"));
        assertTrue(text.contains("[OK] noise.base"));
        assertTrue(text.contains("[~] structure.village"));
        assertTrue(text.contains("[!] feature.custom_tree"));
        assertTrue(text.contains("[W] Pack version is old"));
        assertTrue(text.contains("[E] Failed to read carver config"));
    }
}
