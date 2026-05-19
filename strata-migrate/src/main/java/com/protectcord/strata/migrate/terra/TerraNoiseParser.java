package com.protectcord.strata.migrate.terra;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses Terra noise YAML files. Extracts noise type, frequency, amplitude,
 * octaves, lacunarity, and seed. Handles noise-equation expression strings.
 */
public final class TerraNoiseParser {

    private static final Pattern ADD_PATTERN = Pattern.compile("(\\w+)\\s*\\+\\s*(\\w+)");
    private static final Pattern MUL_PATTERN = Pattern.compile("(\\w+)\\s*\\*\\s*([\\d.]+)");
    private static final Pattern ABS_PATTERN = Pattern.compile("abs\\(([^)]+)\\)");
    private static final Pattern FUNC_REF_PATTERN = Pattern.compile("noise[23]d\\(([^,)]+)");

    private final Yaml yaml = new Yaml();

    @SuppressWarnings("unchecked")
    public TerraParseResult parse(Path noiseFile) throws IOException {
        List<String> warnings = new ArrayList<>();
        Map<String, Object> result = new LinkedHashMap<>();

        Map<String, Object> raw;
        try (InputStream is = Files.newInputStream(noiseFile)) {
            Object loaded = yaml.load(is);
            if (!(loaded instanceof Map)) {
                throw new IOException("Noise file does not contain a valid YAML mapping: " + noiseFile);
            }
            raw = (Map<String, Object>) loaded;
        }

        result.put("id", getString(raw, "id", deriveIdFromPath(noiseFile)));

        // Sampler type / noise type
        String type = getString(raw, "type", null);
        if (type == null) {
            type = getString(raw, "sampler-type", "SIMPLEX");
        }
        result.put("type", type);

        // Core parameters
        if (raw.containsKey("frequency")) {
            result.put("frequency", getDouble(raw, "frequency", 1.0));
        }
        if (raw.containsKey("amplitude")) {
            result.put("amplitude", getDouble(raw, "amplitude", 1.0));
        }
        if (raw.containsKey("octaves")) {
            result.put("octaves", getInt(raw, "octaves", 1));
        }
        if (raw.containsKey("lacunarity")) {
            result.put("lacunarity", getDouble(raw, "lacunarity", 2.0));
        }
        if (raw.containsKey("gain")) {
            result.put("gain", getDouble(raw, "gain", 0.5));
        }
        if (raw.containsKey("seed")) {
            result.put("seed", raw.get("seed"));
        }
        if (raw.containsKey("dimensions")) {
            result.put("dimensions", getInt(raw, "dimensions", 2));
        }

        // Nested sampler definitions (Terra uses sampler objects)
        if (raw.containsKey("sampler")) {
            Object samplerObj = raw.get("sampler");
            if (samplerObj instanceof Map<?, ?> samplerMap) {
                Map<String, Object> sampler = new LinkedHashMap<>();
                for (var entry : samplerMap.entrySet()) {
                    sampler.put(entry.getKey().toString(), entry.getValue());
                }
                result.put("sampler", sampler);
            }
        }

        // Noise equation parsing
        if (raw.containsKey("noise-equation")) {
            String equation = raw.get("noise-equation").toString();
            result.put("noise-equation", equation);
            result.put("equation-analysis", parseEquation(equation, warnings));
        }

        return new TerraParseResult(result, warnings);
    }

    private Map<String, Object> parseEquation(String equation, List<String> warnings) {
        Map<String, Object> analysis = new LinkedHashMap<>();
        List<String> operations = new ArrayList<>();
        List<String> functionRefs = new ArrayList<>();

        // Detect add operations
        Matcher addMatcher = ADD_PATTERN.matcher(equation);
        while (addMatcher.find()) {
            operations.add("add(" + addMatcher.group(1) + ", " + addMatcher.group(2) + ")");
        }

        // Detect multiply operations
        Matcher mulMatcher = MUL_PATTERN.matcher(equation);
        while (mulMatcher.find()) {
            operations.add("multiply(" + mulMatcher.group(1) + ", " + mulMatcher.group(2) + ")");
        }

        // Detect abs wrapping
        Matcher absMatcher = ABS_PATTERN.matcher(equation);
        while (absMatcher.find()) {
            operations.add("abs(" + absMatcher.group(1) + ")");
        }

        // Detect noise function references
        Matcher funcMatcher = FUNC_REF_PATTERN.matcher(equation);
        while (funcMatcher.find()) {
            functionRefs.add(funcMatcher.group(1).trim());
        }

        if (operations.isEmpty() && functionRefs.isEmpty()) {
            warnings.add("Could not parse noise-equation: " + equation);
        }

        analysis.put("operations", operations);
        analysis.put("function-references", functionRefs);
        analysis.put("raw", equation);
        return analysis;
    }

    private String deriveIdFromPath(Path file) {
        String name = file.getFileName().toString();
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }

    private static String getString(Map<?, ?> map, String key, String defaultValue) {
        Object val = map.get(key);
        return val != null ? val.toString() : defaultValue;
    }

    private static double getDouble(Map<?, ?> map, String key, double defaultValue) {
        Object val = map.get(key);
        if (val instanceof Number n) return n.doubleValue();
        return defaultValue;
    }

    private static int getInt(Map<?, ?> map, String key, int defaultValue) {
        Object val = map.get(key);
        if (val instanceof Number n) return n.intValue();
        return defaultValue;
    }
}
