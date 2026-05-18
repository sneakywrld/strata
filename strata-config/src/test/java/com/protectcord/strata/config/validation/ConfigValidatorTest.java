package com.protectcord.strata.config.validation;

import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.config.loader.TomlReader;
import com.protectcord.strata.config.model.ProfileConfig;
import com.protectcord.strata.config.validation.ConfigValidator.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ConfigValidatorTest {

    private ConfigValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ConfigValidator();
    }

    private TomlReader minimalReader() {
        return TomlReader.fromString("name = \"test\"");
    }

    private ProfileConfig profileWith(String displayName, String extendsFrom, Path directory) {
        return new ProfileConfig(
                NamespacedKey.strata("test"),
                displayName,
                "description",
                "author",
                extendsFrom,
                directory,
                minimalReader()
        );
    }

    // ---------------------------------------------------------------
    // Valid profiles
    // ---------------------------------------------------------------
    @Nested
    class ValidProfiles {

        @Test
        void fullyPopulatedProfileIsValid() {
            ProfileConfig config = new ProfileConfig(
                    NamespacedKey.strata("valid"),
                    "Valid Profile",
                    "A well-formed profile",
                    "Author",
                    null,
                    Path.of("/tmp/valid"),
                    minimalReader()
            );

            ValidationResult result = validator.validateProfile(config);
            assertTrue(result.isValid());
            assertTrue(result.errors().isEmpty());
        }

        @Test
        void profileWithExtendsFromDifferentKeyIsValid() {
            ProfileConfig config = new ProfileConfig(
                    NamespacedKey.strata("child"),
                    "Child Profile",
                    "Extends another",
                    "Author",
                    "parent",
                    Path.of("/tmp/child"),
                    minimalReader()
            );

            ValidationResult result = validator.validateProfile(config);
            assertTrue(result.isValid());
        }

        @Test
        void profileWithNullExtendsFromIsValid() {
            ProfileConfig config = profileWith("Root", null, Path.of("/tmp/root"));
            ValidationResult result = validator.validateProfile(config);
            assertTrue(result.isValid());
        }
    }

    // ---------------------------------------------------------------
    // Missing display name
    // ---------------------------------------------------------------
    @Nested
    class MissingDisplayName {

        @Test
        void nullDisplayNameIsInvalid() {
            ProfileConfig config = profileWith(null, null, Path.of("/tmp/nd"));
            ValidationResult result = validator.validateProfile(config);

            assertFalse(result.isValid());
            assertEquals(1, result.errors().size());
            assertTrue(result.errors().get(0).contains("missing a display name"));
        }

        @Test
        void emptyDisplayNameIsInvalid() {
            ProfileConfig config = profileWith("", null, Path.of("/tmp/empty"));
            ValidationResult result = validator.validateProfile(config);

            assertFalse(result.isValid());
            assertTrue(result.errors().stream()
                    .anyMatch(e -> e.contains("missing a display name")));
        }

        @Test
        void blankDisplayNameIsInvalid() {
            ProfileConfig config = profileWith("   ", null, Path.of("/tmp/blank"));
            ValidationResult result = validator.validateProfile(config);

            assertFalse(result.isValid());
            assertTrue(result.errors().stream()
                    .anyMatch(e -> e.contains("missing a display name")));
        }
    }

    // ---------------------------------------------------------------
    // Null directory
    // ---------------------------------------------------------------
    @Nested
    class NullDirectory {

        @Test
        void nullDirectoryIsInvalid() {
            ProfileConfig config = profileWith("Name", null, null);
            ValidationResult result = validator.validateProfile(config);

            assertFalse(result.isValid());
            assertTrue(result.errors().stream()
                    .anyMatch(e -> e.contains("has no directory")));
        }
    }

    // ---------------------------------------------------------------
    // Self-extension
    // ---------------------------------------------------------------
    @Nested
    class SelfExtension {

        @Test
        void profileCannotExtendItself() {
            // NamespacedKey.strata("test").key() => "test"
            // extendsFrom => "test"
            ProfileConfig config = new ProfileConfig(
                    NamespacedKey.strata("test"),
                    "Test",
                    "desc",
                    "author",
                    "test",  // same as key
                    Path.of("/tmp/self"),
                    minimalReader()
            );

            ValidationResult result = validator.validateProfile(config);
            assertFalse(result.isValid());
            assertTrue(result.errors().stream()
                    .anyMatch(e -> e.contains("cannot extend itself")));
        }

        @Test
        void extendsFromDifferentKeyIsNotSelfExtension() {
            ProfileConfig config = new ProfileConfig(
                    NamespacedKey.strata("child"),
                    "Child",
                    "desc",
                    "author",
                    "parent",
                    Path.of("/tmp/child"),
                    minimalReader()
            );

            ValidationResult result = validator.validateProfile(config);
            // No self-extension error
            assertTrue(result.errors().stream()
                    .noneMatch(e -> e.contains("cannot extend itself")));
        }
    }

    // ---------------------------------------------------------------
    // Multiple errors at once
    // ---------------------------------------------------------------
    @Nested
    class MultipleErrors {

        @Test
        void multipleIssuesProduceMultipleErrors() {
            // null displayName + null directory + self-extension
            ProfileConfig config = new ProfileConfig(
                    NamespacedKey.strata("broken"),
                    null,
                    "desc",
                    "author",
                    "broken",  // self-extension
                    null,      // null directory
                    minimalReader()
            );

            ValidationResult result = validator.validateProfile(config);
            assertFalse(result.isValid());
            assertTrue(result.errors().size() >= 2,
                    "Expected at least 2 errors, got: " + result.errors());
        }
    }

    // ---------------------------------------------------------------
    // ValidationResult record
    // ---------------------------------------------------------------
    @Nested
    class ValidationResultTests {

        @Test
        void isValidReturnsTrueWhenErrorsEmpty() {
            ValidationResult result = new ValidationResult(
                    java.util.List.of(), java.util.List.of("a warning")
            );
            assertTrue(result.isValid());
        }

        @Test
        void isValidReturnsFalseWhenErrorsPresent() {
            ValidationResult result = new ValidationResult(
                    java.util.List.of("an error"), java.util.List.of()
            );
            assertFalse(result.isValid());
        }

        @Test
        void warningsDoNotAffectValidity() {
            ValidationResult result = new ValidationResult(
                    java.util.List.of(),
                    java.util.List.of("warn1", "warn2", "warn3")
            );
            assertTrue(result.isValid());
        }
    }
}
