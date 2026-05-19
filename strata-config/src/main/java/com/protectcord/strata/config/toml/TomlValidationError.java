package com.protectcord.strata.config.toml;

/**
 * Describes a single validation error found during TOML schema validation.
 *
 * @param filePath    path to the TOML file that failed validation
 * @param lineNumber  line number of the error, or -1 if unknown
 * @param key         the TOML key that failed validation
 * @param expectedType the type that was expected
 * @param actualValue  string representation of the actual value found
 * @param message      human-readable error message
 */
public record TomlValidationError(
        String filePath,
        int lineNumber,
        String key,
        String expectedType,
        String actualValue,
        String message
) {
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[ERROR] ");
        if (filePath != null && !filePath.isEmpty()) {
            sb.append(filePath);
            if (lineNumber >= 0) {
                sb.append(':').append(lineNumber);
            }
            sb.append(" - ");
        }
        sb.append("key '").append(key).append("': ").append(message);
        if (expectedType != null && !expectedType.isEmpty()) {
            sb.append(" (expected: ").append(expectedType);
            if (actualValue != null && !actualValue.isEmpty()) {
                sb.append(", got: ").append(actualValue);
            }
            sb.append(')');
        }
        return sb.toString();
    }
}
