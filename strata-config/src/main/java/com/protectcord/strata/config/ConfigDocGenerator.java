package com.protectcord.strata.config;

import com.protectcord.strata.config.toml.TomlSchema;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Generates fully-commented template TOML files from schema definitions.
 * Each key gets a comment block with description, valid values, and default.
 */
public final class ConfigDocGenerator {

    public String generateTemplate(TomlSchema schema) {
        StringBuilder sb = new StringBuilder();
        sb.append("# ").append(schema.name()).append(" Configuration Template\n");
        sb.append("# Auto-generated from schema definition\n\n");

        for (TomlSchema.FieldSchema field : schema.fields().values()) {
            appendFieldDoc(sb, field);
        }

        return sb.toString();
    }

    public void generateTemplateFile(TomlSchema schema, Path output) throws IOException {
        String content = generateTemplate(schema);
        Files.createDirectories(output.getParent());
        Files.writeString(output, content);
    }

    private void appendFieldDoc(StringBuilder sb, TomlSchema.FieldSchema field) {
        if (!field.description().isEmpty()) {
            sb.append("# ").append(field.description()).append('\n');
        }

        sb.append("# Type: ").append(TomlSchema.toTomlTypeName(field.type()));
        if (field.required()) {
            sb.append(" (REQUIRED)");
        } else {
            sb.append(" (optional)");
        }
        sb.append('\n');

        if (!field.validRange().isEmpty()) {
            sb.append("# Valid range: ").append(field.validRange()).append('\n');
        }

        if (!field.defaultValue().isEmpty()) {
            sb.append("# Default: ").append(field.defaultValue()).append('\n');
        }

        sb.append(field.key()).append(" = ").append(defaultValueLiteral(field)).append("\n\n");
    }

    private String defaultValueLiteral(TomlSchema.FieldSchema field) {
        if (!field.defaultValue().isEmpty()) {
            return formatLiteral(field.type(), field.defaultValue());
        }
        return placeholderLiteral(field.type());
    }

    private String formatLiteral(Class<?> type, String value) {
        if (type == String.class) {
            return "\"" + value + "\"";
        }
        if (type == boolean.class || type == Boolean.class) {
            return value;
        }
        if (type == java.util.List.class) {
            return "[]";
        }
        if (type == java.util.Map.class) {
            return "{}";
        }
        return value;
    }

    private String placeholderLiteral(Class<?> type) {
        if (type == String.class) return "\"\"";
        if (type == int.class || type == Integer.class) return "0";
        if (type == long.class || type == Long.class) return "0";
        if (type == double.class || type == Double.class) return "0.0";
        if (type == boolean.class || type == Boolean.class) return "false";
        if (type == java.util.List.class) return "[]";
        if (type == java.util.Map.class) return "{}";
        return "\"\"";
    }
}
