package com.protectcord.strata.migrate.terra;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Best-effort parser for TerraScript (.tesf) files. Does not execute scripts,
 * but structurally analyzes them to identify block placements and structure references.
 */
public final class TerraScriptParser {

    private static final Pattern BLOCK_PATTERN =
            Pattern.compile("block\\s*\\(\\s*(?:x\\s*[+\\-]\\s*)?(\\d+)\\s*,\\s*(?:y\\s*[+\\-]\\s*)?(\\d+)\\s*,\\s*(?:z\\s*[+\\-]\\s*)?(\\d+)\\s*,\\s*\"([^\"]+)\"\\s*\\)");

    private static final Pattern BLOCK_SIMPLE_PATTERN =
            Pattern.compile("block\\s*\\(\\s*([^,]+)\\s*,\\s*([^,]+)\\s*,\\s*([^,]+)\\s*,\\s*\"([^\"]+)\"\\s*\\)");

    private static final Pattern STRUCTURE_PATTERN =
            Pattern.compile("structure\\s*\\(\\s*\"([^\"]+)\"");

    private static final Pattern IF_PATTERN =
            Pattern.compile("\\bif\\s*\\(");

    private static final Pattern FOR_PATTERN =
            Pattern.compile("\\bfor\\s*\\(");

    private static final Pattern FUNCTION_DEF_PATTERN =
            Pattern.compile("\\bfun\\s+(\\w+)\\s*\\(");

    private static final Pattern VARIABLE_PATTERN =
            Pattern.compile("\\b(num|str|bool)\\s+(\\w+)\\s*=");

    private static final Pattern CHECK_BLOCK_PATTERN =
            Pattern.compile("check\\s*\\(");

    private static final Pattern STATE_PATTERN =
            Pattern.compile("state\\s*\\(");

    private static final Pattern LOOT_PATTERN =
            Pattern.compile("loot\\s*\\(");

    private static final Pattern PULL_PATTERN =
            Pattern.compile("pull\\s*\\(");

    private static final Pattern ENTITY_PATTERN =
            Pattern.compile("entity\\s*\\(");

    public ScriptAnalysis parse(Path scriptFile) throws IOException {
        String content = Files.readString(scriptFile);
        String scriptId = deriveIdFromPath(scriptFile);

        List<Map<String, Object>> blockPlacements = new ArrayList<>();
        List<String> structureReferences = new ArrayList<>();
        List<String> unsupportedConstructs = new ArrayList<>();

        // Extract block placements
        Matcher blockMatcher = BLOCK_SIMPLE_PATTERN.matcher(content);
        while (blockMatcher.find()) {
            Map<String, Object> placement = new LinkedHashMap<>();
            placement.put("x", blockMatcher.group(1).trim());
            placement.put("y", blockMatcher.group(2).trim());
            placement.put("z", blockMatcher.group(3).trim());
            placement.put("block", blockMatcher.group(4));
            blockPlacements.add(placement);
        }

        // Extract structure references
        Matcher structMatcher = STRUCTURE_PATTERN.matcher(content);
        while (structMatcher.find()) {
            structureReferences.add(structMatcher.group(1));
        }

        // Identify unsupported constructs
        if (FOR_PATTERN.matcher(content).find()) {
            unsupportedConstructs.add("for-loops (dynamic iteration cannot be statically resolved)");
        }

        if (IF_PATTERN.matcher(content).find()) {
            unsupportedConstructs.add("conditional-blocks (runtime branching cannot be statically resolved)");
        }

        if (FUNCTION_DEF_PATTERN.matcher(content).find()) {
            unsupportedConstructs.add("user-defined-functions (function calls require execution context)");
        }

        if (CHECK_BLOCK_PATTERN.matcher(content).find()) {
            unsupportedConstructs.add("check() (runtime block queries unsupported in static analysis)");
        }

        if (STATE_PATTERN.matcher(content).find()) {
            unsupportedConstructs.add("state() (block state manipulation requires execution context)");
        }

        if (LOOT_PATTERN.matcher(content).find()) {
            unsupportedConstructs.add("loot() (loot table references need manual configuration)");
        }

        if (PULL_PATTERN.matcher(content).find()) {
            unsupportedConstructs.add("pull() (structure pulling requires runtime context)");
        }

        if (ENTITY_PATTERN.matcher(content).find()) {
            unsupportedConstructs.add("entity() (entity spawning needs manual configuration)");
        }

        // Detect variable usage with expressions (non-trivial assignments)
        Matcher varMatcher = VARIABLE_PATTERN.matcher(content);
        Set<String> dynamicVars = new HashSet<>();
        while (varMatcher.find()) {
            dynamicVars.add(varMatcher.group(2));
        }
        if (!dynamicVars.isEmpty()) {
            unsupportedConstructs.add("dynamic-variables (" + dynamicVars.size() +
                    " variables; block coordinates may be computed at runtime)");
        }

        return new ScriptAnalysis(scriptId, blockPlacements, structureReferences, unsupportedConstructs);
    }

    private String deriveIdFromPath(Path file) {
        String name = file.getFileName().toString();
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }
}
