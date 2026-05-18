package com.protectcord.strata.migrate.report;

import java.util.ArrayList;
import java.util.List;

/**
 * Tracks the results of a Terra → Strata migration.
 */
public final class MigrationReport {

    private final List<String> converted = new ArrayList<>();
    private final List<String> approximated = new ArrayList<>();
    private final List<String> unsupported = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();

    public void addConverted(String item) { converted.add(item); }
    public void addApproximated(String item, String reason) {
        approximated.add(item + " — " + reason);
    }
    public void addUnsupported(String item, String reason) {
        unsupported.add(item + " — " + reason);
    }
    public void addWarning(String message) { warnings.add(message); }
    public void addError(String message) { errors.add(message); }

    public List<String> converted() { return converted; }
    public List<String> approximated() { return approximated; }
    public List<String> unsupported() { return unsupported; }
    public List<String> warnings() { return warnings; }
    public List<String> errors() { return errors; }

    public boolean hasErrors() { return !errors.isEmpty(); }

    /**
     * Generates a human-readable report.
     */
    public String toText() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Strata Migration Report ===\n\n");

        sb.append("Converted: ").append(converted.size()).append(" items\n");
        sb.append("Approximated: ").append(approximated.size()).append(" items\n");
        sb.append("Unsupported: ").append(unsupported.size()).append(" items\n");
        sb.append("Warnings: ").append(warnings.size()).append("\n");
        sb.append("Errors: ").append(errors.size()).append("\n\n");

        if (!converted.isEmpty()) {
            sb.append("--- Successfully Converted ---\n");
            converted.forEach(c -> sb.append("  [OK] ").append(c).append("\n"));
            sb.append("\n");
        }

        if (!approximated.isEmpty()) {
            sb.append("--- Approximated (manual review recommended) ---\n");
            approximated.forEach(a -> sb.append("  [~] ").append(a).append("\n"));
            sb.append("\n");
        }

        if (!unsupported.isEmpty()) {
            sb.append("--- Unsupported (manual conversion needed) ---\n");
            unsupported.forEach(u -> sb.append("  [!] ").append(u).append("\n"));
            sb.append("\n");
        }

        if (!warnings.isEmpty()) {
            sb.append("--- Warnings ---\n");
            warnings.forEach(w -> sb.append("  [W] ").append(w).append("\n"));
            sb.append("\n");
        }

        if (!errors.isEmpty()) {
            sb.append("--- Errors ---\n");
            errors.forEach(e -> sb.append("  [E] ").append(e).append("\n"));
        }

        return sb.toString();
    }
}
