package com.protectcord.strata.config.toml;

import com.electronwill.nightconfig.core.Config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Schema definition derived from Java record annotations.
 * Used to validate TOML configuration files against expected structure and types.
 */
public final class TomlSchema {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.RECORD_COMPONENT)
    public @interface TomlKey {
        boolean required() default false;
        String defaultValue() default "";
        String description() default "";
        String validRange() default "";
    }

    private final String name;
    private final Map<String, FieldSchema> fields;

    private TomlSchema(String name, Map<String, FieldSchema> fields) {
        this.name = name;
        this.fields = Collections.unmodifiableMap(fields);
    }

    public record FieldSchema(
            String key,
            Class<?> type,
            boolean required,
            String defaultValue,
            String description,
            String validRange
    ) {}

    public static TomlSchema fromRecord(Class<?> recordClass) {
        if (!recordClass.isRecord()) {
            throw new IllegalArgumentException(recordClass.getName() + " is not a record");
        }

        Map<String, FieldSchema> fields = new LinkedHashMap<>();
        for (RecordComponent component : recordClass.getRecordComponents()) {
            String key = toTomlKey(component.getName());
            TomlKey annotation = component.getAnnotation(TomlKey.class);

            boolean required = annotation != null && annotation.required();
            String defaultValue = annotation != null ? annotation.defaultValue() : "";
            String description = annotation != null ? annotation.description() : "";
            String validRange = annotation != null ? annotation.validRange() : "";

            fields.put(key, new FieldSchema(
                    key,
                    component.getType(),
                    required,
                    defaultValue,
                    description,
                    validRange
            ));
        }

        return new TomlSchema(recordClass.getSimpleName(), fields);
    }

    public List<TomlValidationError> validate(Config tomlConfig) {
        return validate(tomlConfig, null);
    }

    public List<TomlValidationError> validate(Config tomlConfig, String filePath) {
        List<TomlValidationError> errors = new ArrayList<>();
        String path = filePath != null ? filePath : "";

        for (FieldSchema field : fields.values()) {
            if (field.required() && !tomlConfig.contains(field.key())) {
                errors.add(new TomlValidationError(
                        path, -1, field.key(),
                        toTomlTypeName(field.type()), "",
                        "required key is missing"
                ));
                continue;
            }

            if (!tomlConfig.contains(field.key())) {
                continue;
            }

            Object value = tomlConfig.get(field.key());
            if (value == null) {
                if (field.required()) {
                    errors.add(new TomlValidationError(
                            path, -1, field.key(),
                            toTomlTypeName(field.type()), "null",
                            "required key has null value"
                    ));
                }
                continue;
            }

            if (!isTypeCompatible(field.type(), value)) {
                errors.add(new TomlValidationError(
                        path, -1, field.key(),
                        toTomlTypeName(field.type()),
                        value.getClass().getSimpleName() + ": " + value,
                        "value has wrong type"
                ));
                continue;
            }

            if (!field.validRange().isEmpty() && value instanceof Number number) {
                String rangeError = validateRange(field, number);
                if (rangeError != null) {
                    errors.add(new TomlValidationError(
                            path, -1, field.key(),
                            toTomlTypeName(field.type()),
                            value.toString(),
                            rangeError
                    ));
                }
            }
        }

        return errors;
    }

    public String name() {
        return name;
    }

    public Map<String, FieldSchema> fields() {
        return fields;
    }

    private static boolean isTypeCompatible(Class<?> expected, Object value) {
        if (expected == String.class) {
            return value instanceof String;
        }
        if (expected == int.class || expected == Integer.class) {
            return value instanceof Number;
        }
        if (expected == long.class || expected == Long.class) {
            return value instanceof Number;
        }
        if (expected == double.class || expected == Double.class) {
            return value instanceof Number;
        }
        if (expected == boolean.class || expected == Boolean.class) {
            return value instanceof Boolean;
        }
        if (expected == List.class) {
            return value instanceof List;
        }
        if (expected == Map.class) {
            return value instanceof Map || value instanceof Config;
        }
        return true;
    }

    private static String validateRange(FieldSchema field, Number value) {
        String range = field.validRange();
        if (!range.contains("..")) {
            return null;
        }

        String[] parts = range.split("\\.\\.");
        if (parts.length != 2) {
            return null;
        }

        try {
            double min = parts[0].isEmpty() ? Double.NEGATIVE_INFINITY : Double.parseDouble(parts[0].trim());
            double max = parts[1].isEmpty() ? Double.POSITIVE_INFINITY : Double.parseDouble(parts[1].trim());
            double actual = value.doubleValue();

            if (actual < min || actual > max) {
                return "value " + actual + " is outside valid range [" + range + "]";
            }
        } catch (NumberFormatException ignored) {
            return null;
        }

        return null;
    }

    private static String toTomlKey(String javaFieldName) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < javaFieldName.length(); i++) {
            char c = javaFieldName.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    sb.append('_');
                }
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String toTomlTypeName(Class<?> type) {
        if (type == String.class) return "string";
        if (type == int.class || type == Integer.class) return "integer";
        if (type == long.class || type == Long.class) return "integer";
        if (type == double.class || type == Double.class) return "float";
        if (type == boolean.class || type == Boolean.class) return "boolean";
        if (type == List.class) return "array";
        if (type == Map.class) return "table";
        return type.getSimpleName();
    }
}
