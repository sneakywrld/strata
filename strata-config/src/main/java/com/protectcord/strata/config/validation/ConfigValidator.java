package com.protectcord.strata.config.validation;

import com.protectcord.strata.config.model.ProfileConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates loaded configuration for correctness and consistency.
 * Reports errors and warnings without throwing exceptions.
 */
public final class ConfigValidator {

    /**
     * Validation result containing errors and warnings.
     */
    public record ValidationResult(List<String> errors, List<String> warnings) {
        public boolean isValid() {
            return errors.isEmpty();
        }
    }

    /**
     * Validates a loaded profile configuration.
     */
    public ValidationResult validateProfile(ProfileConfig profile) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        if (profile.displayName() == null || profile.displayName().isBlank()) {
            errors.add("Profile '" + profile.key() + "' is missing a display name");
        }

        if (profile.directory() == null) {
            errors.add("Profile '" + profile.key() + "' has no directory");
        }

        if (profile.extendsFrom() != null && profile.extendsFrom().equals(profile.key().key())) {
            errors.add("Profile '" + profile.key() + "' cannot extend itself");
        }

        return new ValidationResult(errors, warnings);
    }
}
