package com.protectcord.strata.config.loader;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.electronwill.nightconfig.toml.TomlParser;

import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * TOML reader wrapping Night-Config's TOML parser.
 * Provides a simpler API for reading typed values from TOML files.
 */
public final class TomlReader {

    private final Config config;

    private TomlReader(Config config) {
        this.config = config;
    }

    /**
     * Reads a TOML file from disk.
     */
    public static TomlReader fromFile(Path path) {
        try (FileConfig fc = FileConfig.of(path)) {
            fc.load();
            return new TomlReader(fc);
        }
    }

    /**
     * Parses TOML from a string.
     */
    public static TomlReader fromString(String toml) {
        TomlParser parser = new TomlParser();
        Reader reader = new StringReader(toml);
        Config config = parser.parse(reader);
        return new TomlReader(config);
    }

    public String getString(String path) {
        return config.get(path);
    }

    public Optional<String> getOptionalString(String path) {
        return Optional.ofNullable(config.get(path));
    }

    public int getInt(String path, int defaultValue) {
        Number val = config.get(path);
        return val != null ? val.intValue() : defaultValue;
    }

    public double getDouble(String path, double defaultValue) {
        Number val = config.get(path);
        return val != null ? val.doubleValue() : defaultValue;
    }

    public boolean getBoolean(String path, boolean defaultValue) {
        Object val = config.get(path);
        return val != null ? (Boolean) val : defaultValue;
    }

    public long getLong(String path, long defaultValue) {
        Number val = config.get(path);
        return val != null ? val.longValue() : defaultValue;
    }

    public <T> List<T> getList(String path) {
        List<T> list = config.get(path);
        return list != null ? list : List.of();
    }

    public Optional<TomlReader> getSubSection(String path) {
        Config sub = config.get(path);
        return sub != null ? Optional.of(new TomlReader(sub)) : Optional.empty();
    }

    public boolean contains(String path) {
        return config.contains(path);
    }

    /**
     * Returns the raw Night-Config object for advanced access.
     */
    public Config raw() {
        return config;
    }

    /**
     * Returns all top-level keys.
     */
    public Map<String, Object> toMap() {
        return config.valueMap();
    }
}
