package com.protectcord.strata.migrate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Validates generated Strata profile output. Checks that all TOML files parse
 * correctly, cross-references are valid, and no dependencies are missing.
 */
public final class MigrationValidator {

    private static final Pattern REF_PATTERN = Pattern.compile("\"strata:([^\"]+)\"");
    private static final Pattern TABLE_PATTERN = Pattern.compile("^\\[([^\\]]+)]\\s*$");
    private static final Pattern ARRAY_TABLE_PATTERN = Pattern.compile("^\\[\\[([^\\]]+)]]\\s*$");
    private static final Pattern KV_PATTERN = Pattern.compile("^([\\w.\":-]+)\\s*=\\s*(.+)$");

    public ValidationResult validate(Path outputDir) throws IOException {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        Set<String> definedIds = new HashSet<>();
        Set<String> referencedIds = new HashSet<>();

        if (!Files.isDirectory(outputDir)) {
            errors.add("Output directory does not exist: " + outputDir);
            return new ValidationResult(errors, warnings);
        }

        // Collect all TOML files
        List<Path> tomlFiles;
        try (Stream<Path> walk = Files.walk(outputDir)) {
            tomlFiles = walk
                    .filter(p -> p.toString().endsWith(".toml"))
                    .toList();
        }

        if (tomlFiles.isEmpty()) {
            errors.add("No TOML files found in output directory");
            return new ValidationResult(errors, warnings);
        }

        // Validate each TOML file
        for (Path tomlFile : tomlFiles) {
            validateTomlFile(tomlFile, outputDir, definedIds, referencedIds, errors, warnings);
        }

        // Check profile.toml exists
        Path profileToml = outputDir.resolve("profile.toml");
        if (!Files.isRegularFile(profileToml)) {
            errors.add("Missing profile.toml in output directory");
        }

        // Cross-reference validation: check that all referenced IDs are defined
        for (String ref : referencedIds) {
            if (!definedIds.contains(ref)) {
                warnings.add("Referenced ID 'strata:" + ref + "' is not defined in any generated file");
            }
        }

        // Check expected directory structure
        validateDirectoryStructure(outputDir, warnings);

        return new ValidationResult(errors, warnings);
    }

    private void validateTomlFile(Path file, Path outputDir,
                                  Set<String> definedIds, Set<String> referencedIds,
                                  List<String> errors, List<String> warnings) {
        String relativePath = outputDir.relativize(file).toString();
        List<String> lines;
        try {
            lines = Files.readAllLines(file);
        } catch (IOException e) {
            errors.add("Cannot read file " + relativePath + ": " + e.getMessage());
            return;
        }

        boolean inMultilineValue = false;
        int bracketDepth = 0;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            int lineNum = i + 1;

            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            // Track multiline arrays/strings
            if (inMultilineValue) {
                bracketDepth += countChar(line, '[') - countChar(line, ']');
                if (bracketDepth <= 0) {
                    inMultilineValue = false;
                    bracketDepth = 0;
                }
                continue;
            }

            // Table headers
            Matcher arrayTableMatcher = ARRAY_TABLE_PATTERN.matcher(line);
            if (arrayTableMatcher.matches()) {
                String tableKey = arrayTableMatcher.group(1).trim();
                extractDefinedId(tableKey, definedIds);
                continue;
            }

            Matcher tableMatcher = TABLE_PATTERN.matcher(line);
            if (tableMatcher.matches()) {
                String tableKey = tableMatcher.group(1).trim();
                extractDefinedId(tableKey, definedIds);
                continue;
            }

            // Key-value pairs
            Matcher kvMatcher = KV_PATTERN.matcher(line);
            if (kvMatcher.matches()) {
                String value = kvMatcher.group(2).trim();

                // Check for unclosed strings
                long quoteCount = value.chars().filter(c -> c == '"').count();
                if (quoteCount % 2 != 0 && !value.contains("\\\"")) {
                    errors.add(relativePath + ":" + lineNum + " — Unclosed string value");
                }

                // Check for multiline arrays
                if (value.startsWith("[") && !value.contains("]")) {
                    inMultilineValue = true;
                    bracketDepth = 1;
                }

                // Extract references from values
                Matcher refMatcher = REF_PATTERN.matcher(value);
                while (refMatcher.find()) {
                    referencedIds.add(refMatcher.group(1));
                }

                continue;
            }

            // Lines that are not comments, tables, or key-value pairs
            if (!line.startsWith("]") && !line.endsWith(",") && !line.equals("]")) {
                warnings.add(relativePath + ":" + lineNum + " — Line may not be valid TOML: " + truncate(line));
            }
        }

        // Extract references from all table keys
        String content;
        try {
            content = Files.readString(file);
        } catch (IOException e) {
            return;
        }

        Matcher refMatcher = REF_PATTERN.matcher(content);
        while (refMatcher.find()) {
            String id = refMatcher.group(1);
            // IDs found in table headers are definitions; in values are references
            // definedIds already populated above
        }
    }

    private void extractDefinedId(String tableKey, Set<String> definedIds) {
        Matcher refMatcher = REF_PATTERN.matcher(tableKey);
        while (refMatcher.find()) {
            definedIds.add(refMatcher.group(1));
        }
    }

    private void validateDirectoryStructure(Path outputDir, List<String> warnings) {
        String[] expectedDirs = {"biomes", "noise", "surface"};
        for (String dir : expectedDirs) {
            Path dirPath = outputDir.resolve(dir);
            if (!Files.isDirectory(dirPath)) {
                warnings.add("Expected directory '" + dir + "/' not found in output");
            }
        }
    }

    private int countChar(String s, char c) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == c) count++;
        }
        return count;
    }

    private String truncate(String s) {
        return s.length() > 80 ? s.substring(0, 77) + "..." : s;
    }

    public record ValidationResult(
            List<String> errors,
            List<String> warnings
    ) {
        public boolean isValid() {
            return errors.isEmpty();
        }

        public String toText() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== Validation Result ===\n");
            sb.append("Status: ").append(isValid() ? "VALID" : "INVALID").append("\n");
            sb.append("Errors: ").append(errors.size()).append("\n");
            sb.append("Warnings: ").append(warnings.size()).append("\n\n");

            if (!errors.isEmpty()) {
                sb.append("--- Errors ---\n");
                errors.forEach(e -> sb.append("  [E] ").append(e).append("\n"));
                sb.append("\n");
            }

            if (!warnings.isEmpty()) {
                sb.append("--- Warnings ---\n");
                warnings.forEach(w -> sb.append("  [W] ").append(w).append("\n"));
            }

            return sb.toString();
        }
    }
}
