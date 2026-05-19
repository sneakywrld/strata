package com.protectcord.strata.migrate.terra;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Parsed representation of a Terra pack.yml manifest.
 */
public record TerraPackManifest(
        String id,
        String version,
        String author,
        String description
) {

    /**
     * Parses a Terra pack.yml file into a manifest record.
     */
    @SuppressWarnings("unchecked")
    public static TerraPackManifest fromYaml(Path packYml) throws IOException {
        if (!Files.isRegularFile(packYml)) {
            throw new IOException("pack.yml not found: " + packYml);
        }

        Yaml yaml = new Yaml();
        Map<String, Object> data;
        try (InputStream is = Files.newInputStream(packYml)) {
            Object result = yaml.load(is);
            if (!(result instanceof Map)) {
                throw new IOException("pack.yml does not contain a valid YAML mapping");
            }
            data = (Map<String, Object>) result;
        }

        return new TerraPackManifest(
                getString(data, "id", "unknown"),
                getString(data, "version", "1.0"),
                getString(data, "author", "Unknown"),
                getString(data, "description", "")
        );
    }

    private static String getString(Map<String, Object> map, String key, String defaultValue) {
        Object val = map.get(key);
        return val != null ? val.toString() : defaultValue;
    }
}
