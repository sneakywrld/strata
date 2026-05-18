package com.protectcord.strata.migrate.terra;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Reads a Terra YAML config pack directory and parses all config files.
 * Terra packs have a specific directory structure:
 * <pre>
 * pack.yml          - Pack metadata
 * biomes/           - Biome definitions
 * noise/            - Noise configurations
 * palettes/         - Block palettes
 * flora/            - Flora/tree definitions
 * structures/       - Structure definitions
 * carvers/          - Carver configurations
 * </pre>
 */
public final class TerraPackParser {

    private static final Logger LOGGER = Logger.getLogger("Strata-Migrate");
    private final Yaml yaml = new Yaml();

    private final Path packDir;

    public TerraPackParser(Path packDir) {
        this.packDir = packDir;
    }

    /**
     * Parses the entire Terra pack.
     */
    public TerraPackData parse() throws IOException {
        if (!Files.isDirectory(packDir)) {
            throw new IOException("Terra pack directory not found: " + packDir);
        }

        LOGGER.info("Parsing Terra pack at: " + packDir);

        // Parse pack.yml
        Map<String, Object> packMeta = parseYamlFile(packDir.resolve("pack.yml"));
        String packId = getString(packMeta, "id", "unknown");
        String packAuthor = getString(packMeta, "author", "Unknown");

        // Parse all subsections
        List<Map<String, Object>> biomes = parseDirectory("biomes");
        List<Map<String, Object>> noises = parseDirectory("noise");
        List<Map<String, Object>> palettes = parseDirectory("palettes");
        List<Map<String, Object>> flora = parseDirectory("flora");
        List<Map<String, Object>> structures = parseDirectory("structures");
        List<Map<String, Object>> carvers = parseDirectory("carvers");

        LOGGER.info("Parsed Terra pack '" + packId + "': " + biomes.size() + " biomes, "
                + noises.size() + " noise configs, " + palettes.size() + " palettes, "
                + flora.size() + " flora, " + structures.size() + " structures, "
                + carvers.size() + " carvers");

        return new TerraPackData(packId, packAuthor, biomes, noises, palettes, flora, structures, carvers);
    }

    private List<Map<String, Object>> parseDirectory(String subdir) throws IOException {
        Path dir = packDir.resolve(subdir);
        if (!Files.isDirectory(dir)) return List.of();

        List<Map<String, Object>> results = new ArrayList<>();
        try (Stream<Path> files = Files.walk(dir)) {
            files.filter(p -> p.toString().endsWith(".yml") || p.toString().endsWith(".yaml"))
                    .forEach(p -> {
                        try {
                            Map<String, Object> data = parseYamlFile(p);
                            if (data != null) {
                                data.put("_source_file", packDir.relativize(p).toString());
                                results.add(data);
                            }
                        } catch (Exception e) {
                            LOGGER.warning("Failed to parse " + p.getFileName() + ": " + e.getMessage());
                        }
                    });
        }
        return results;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseYamlFile(Path path) {
        if (!Files.isRegularFile(path)) return null;
        try (InputStream is = Files.newInputStream(path)) {
            Object result = yaml.load(is);
            if (result instanceof Map) {
                return (Map<String, Object>) result;
            }
        } catch (IOException e) {
            LOGGER.warning("Could not read " + path + ": " + e.getMessage());
        }
        return null;
    }

    private static String getString(Map<String, Object> map, String key, String defaultValue) {
        if (map == null) return defaultValue;
        Object val = map.get(key);
        return val != null ? val.toString() : defaultValue;
    }
}
