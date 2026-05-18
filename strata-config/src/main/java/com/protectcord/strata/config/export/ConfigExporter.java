package com.protectcord.strata.config.export;

import com.electronwill.nightconfig.core.Config;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.protectcord.strata.config.model.ProfileConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Exports configuration to JSON format for machine consumption,
 * debugging, and profile sharing.
 */
public final class ConfigExporter {

    private final ObjectMapper mapper;

    public ConfigExporter() {
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Exports a profile's raw config to a JSON string.
     */
    public String toJson(ProfileConfig profile) throws JsonProcessingException {
        Map<String, Object> data = toPlainMap(profile.rawConfig().toMap());
        return mapper.writeValueAsString(data);
    }

    private static Map<String, Object> toPlainMap(Map<String, Object> source) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Config cfg) {
                result.put(entry.getKey(), toPlainMap(cfg.valueMap()));
            } else {
                result.put(entry.getKey(), value);
            }
        }
        return result;
    }

    /**
     * Exports a profile's config to a JSON file.
     */
    public void exportToFile(ProfileConfig profile, Path outputPath) throws IOException {
        String json = toJson(profile);
        Files.writeString(outputPath, json);
    }
}
