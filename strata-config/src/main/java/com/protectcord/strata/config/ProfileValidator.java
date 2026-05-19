package com.protectcord.strata.config;

import com.protectcord.strata.config.loader.TomlReader;
import com.protectcord.strata.config.model.*;
import com.protectcord.strata.config.toml.TomlSchema;
import com.protectcord.strata.config.toml.TomlValidationError;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Validates an entire profile directory against schemas.
 * Checks structural integrity, file existence, and TOML schema compliance.
 */
public final class ProfileValidator {

    private static final Logger LOGGER = Logger.getLogger("Strata");
    private static final String PROFILE_FILE = "profile.toml";

    private final TomlSchema profileSchema = TomlSchema.fromRecord(ProfileConfig.class);
    private final TomlSchema biomeSchema = TomlSchema.fromRecord(BiomeConfig.class);
    private final TomlSchema noiseSchema = TomlSchema.fromRecord(NoiseConfig.class);
    private final TomlSchema terrainSchema = TomlSchema.fromRecord(TerrainConfig.class);
    private final TomlSchema carverSchema = TomlSchema.fromRecord(CarverConfig.class);
    private final TomlSchema structureSchema = TomlSchema.fromRecord(StructureConfig.class);
    private final TomlSchema featureSchema = TomlSchema.fromRecord(FeatureConfig.class);

    /**
     * Result of a full profile directory validation.
     *
     * @param errors   list of errors that prevent the profile from loading
     * @param warnings list of non-fatal issues
     */
    public record ValidationResult(List<String> errors, List<String> warnings) {
        public boolean isValid() {
            return errors.isEmpty();
        }
    }

    public ValidationResult validate(Path profileDir) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        if (!Files.isDirectory(profileDir)) {
            errors.add("Profile directory does not exist: " + profileDir);
            return new ValidationResult(errors, warnings);
        }

        Path profileFile = profileDir.resolve(PROFILE_FILE);
        if (!Files.isRegularFile(profileFile)) {
            errors.add("Missing required file: " + PROFILE_FILE);
            return new ValidationResult(errors, warnings);
        }

        validateProfileToml(profileFile, errors, warnings);
        validateSubdirectory(profileDir, "biomes", biomeSchema, errors, warnings);
        validateSubdirectory(profileDir, "noise", noiseSchema, errors, warnings);
        validateSubdirectory(profileDir, "terrain", terrainSchema, errors, warnings);
        validateSubdirectory(profileDir, "carvers", carverSchema, errors, warnings);
        validateSubdirectory(profileDir, "structures", structureSchema, errors, warnings);
        validateSubdirectory(profileDir, "features", featureSchema, errors, warnings);
        validateReferences(profileDir, errors, warnings);

        return new ValidationResult(errors, warnings);
    }

    private void validateProfileToml(Path profileFile, List<String> errors, List<String> warnings) {
        try {
            TomlReader reader = TomlReader.fromFile(profileFile);
            List<TomlValidationError> schemaErrors = profileSchema.validate(
                    reader.raw(), profileFile.toString());
            for (TomlValidationError err : schemaErrors) {
                errors.add(enrichErrorMessage(err, "profile"));
            }

            if (!reader.contains("name")) {
                errors.add(PROFILE_FILE + ": missing required field 'name'"
                        + " — this is the display name shown in /strata profiles; "
                        + "see the comments in profile.toml for required fields");
            }

            if (reader.contains("ore-scarcity-multiplier")) {
                double val = reader.getDouble("ore-scarcity-multiplier", 0.6);
                if (val < 0.0 || val > 10.0) {
                    errors.add(PROFILE_FILE + ": 'ore-scarcity-multiplier' must be 0.0-10.0, got " + val
                            + " — see profile.toml comments for valid range; "
                            + "lower values make ores rarer, 1.0 is vanilla rate");
                }
            }
        } catch (Exception e) {
            errors.add("Failed to parse " + PROFILE_FILE + ": " + e.getMessage()
                    + " — check for TOML syntax errors (unclosed quotes, bad table headers); "
                    + "run /strata guide profiles for valid profile.toml structure");
        }
    }

    private void validateSubdirectory(Path profileDir, String subDir, TomlSchema schema,
                                      List<String> errors, List<String> warnings) {
        Path dir = profileDir.resolve(subDir);
        if (!Files.isDirectory(dir)) {
            warnings.add("Optional directory missing: " + subDir + "/");
            return;
        }

        try (DirectoryStream<Path> files = Files.newDirectoryStream(dir, "*.toml")) {
            for (Path file : files) {
                validateTomlFile(file, schema, errors);
            }
        } catch (IOException e) {
            errors.add("Failed to read directory " + subDir + "/: " + e.getMessage()
                    + " — ensure the directory exists and is readable");
        }
    }

    private void validateTomlFile(Path file, TomlSchema schema, List<String> errors) {
        String configType = inferConfigType(file, schema);
        try {
            TomlReader reader = TomlReader.fromFile(file);
            List<TomlValidationError> schemaErrors = schema.validate(
                    reader.raw(), file.toString());
            for (TomlValidationError err : schemaErrors) {
                errors.add(enrichErrorMessage(err, configType));
            }
        } catch (Exception e) {
            errors.add("Failed to parse " + file.getFileName() + ": " + e.getMessage()
                    + " — check for TOML syntax errors; see the comments in "
                    + file.getFileName() + " for expected format");
        }
    }

    private void validateReferences(Path profileDir, List<String> errors, List<String> warnings) {
        Path biomesDir = profileDir.resolve("biomes");
        if (!Files.isDirectory(biomesDir)) {
            return;
        }

        try (DirectoryStream<Path> biomeFiles = Files.newDirectoryStream(biomesDir, "*.toml")) {
            for (Path biomeFile : biomeFiles) {
                validateBiomeReferences(profileDir, biomeFile, errors, warnings);
            }
        } catch (IOException e) {
            errors.add("Failed to read biomes directory: " + e.getMessage());
        }
    }

    private void validateBiomeReferences(Path profileDir, Path biomeFile,
                                         List<String> errors, List<String> warnings) {
        try {
            TomlReader reader = TomlReader.fromFile(biomeFile);
            String biomeId = biomeFile.getFileName().toString().replace(".toml", "");

            List<String> features = reader.getList("features");
            for (String feature : features) {
                if (!resolveReference(profileDir, "features", feature)) {
                    errors.add("Biome '" + biomeId + "' references unknown feature: " + feature
                            + " — create features/" + stripNamespace(feature) + ".toml "
                            + "or remove this entry from the biome's features list; "
                            + "run /strata guide features for valid feature configuration");
                }
            }

            List<String> carvers = reader.getList("carvers");
            for (String carver : carvers) {
                if (!resolveReference(profileDir, "carvers", carver)) {
                    errors.add("Biome '" + biomeId + "' references unknown carver: " + carver
                            + " — create carvers/" + stripNamespace(carver) + ".toml "
                            + "with type, min-y, max-y, and probability fields; "
                            + "valid types: CHEESE, SPAGHETTI, NOODLE, RAVINE, CUSTOM; "
                            + "see comment in carver TOML for valid values");
                }
            }

            List<String> structures = reader.getList("structures");
            for (String structure : structures) {
                if (!resolveReference(profileDir, "structures", structure)) {
                    errors.add("Biome '" + biomeId + "' references unknown structure: " + structure
                            + " — create structures/" + stripNamespace(structure) + ".toml "
                            + "with type, spacing, and separation fields; "
                            + "valid types: JIGSAW, SCHEMATIC, PROCEDURAL; "
                            + "run /strata guide structures for placement configuration");
                }
            }

            List<String> spawnRules = reader.getList("spawnRules");
            if (spawnRules.isEmpty()) {
                List<String> spawnRulesSnake = reader.getList("spawn_rules");
                spawnRules = spawnRulesSnake;
            }
            for (String rule : spawnRules) {
                if (!resolveReference(profileDir, "spawns", rule)) {
                    warnings.add("Biome '" + biomeId + "' references spawn rule not found locally: " + rule
                            + " — create spawns/" + stripNamespace(rule) + ".toml "
                            + "or this rule will be ignored at runtime");
                }
            }
        } catch (Exception e) {
            LOGGER.warning("Failed to validate references in " + biomeFile.getFileName() + ": " + e.getMessage());
        }
    }

    private boolean resolveReference(Path profileDir, String subDir, String ref) {
        String fileName = ref.contains(":") ? ref.substring(ref.indexOf(':') + 1) : ref;
        Path target = profileDir.resolve(subDir).resolve(fileName + ".toml");
        return Files.isRegularFile(target);
    }

    /**
     * Strips namespace prefix from a reference ID (e.g., "strata:cave_system" -> "cave_system").
     */
    private static String stripNamespace(String ref) {
        return ref.contains(":") ? ref.substring(ref.indexOf(':') + 1) : ref;
    }

    /**
     * Infers the config type from the file path and schema for use in error messages.
     */
    private static String inferConfigType(Path file, TomlSchema schema) {
        // Use the schema name to determine what kind of config this is
        String schemaName = schema.name().toLowerCase();
        if (schemaName.contains("biome")) return "biome";
        if (schemaName.contains("noise")) return "noise";
        if (schemaName.contains("terrain")) return "terrain";
        if (schemaName.contains("carver")) return "carver";
        if (schemaName.contains("structure")) return "structure";
        if (schemaName.contains("feature")) return "feature";

        // Fall back to parent directory name
        Path parent = file.getParent();
        if (parent != null) {
            return parent.getFileName().toString();
        }
        return "config";
    }

    /**
     * Enriches a schema validation error with helpful documentation references
     * and guidance specific to the config type.
     */
    private static String enrichErrorMessage(TomlValidationError err, String configType) {
        StringBuilder sb = new StringBuilder(err.toString());

        // Add type-specific guidance based on the error and config type
        String key = err.key();
        String message = err.message();

        if (message.contains("required key is missing")) {
            sb.append(" — this field is required; see the comment in ");
            sb.append(configType).append(" TOML for valid values");
        } else if (message.contains("value has wrong type")) {
            sb.append(" — see the comment in ").append(configType);
            sb.append(" TOML for expected type and valid values");
        } else if (message.contains("outside valid range")) {
            // Range errors already include the range, add context
            sb.append(" — see the comment in ").append(configType);
            sb.append(" TOML for valid range");
        }

        // Add field-specific guidance
        switch (configType) {
            case "biome" -> {
                if (key.equals("temperature") || key.equals("humidity")) {
                    sb.append("; valid range: 0.0-2.0");
                } else if (key.equals("continentalness") || key.equals("erosion") || key.equals("weirdness")) {
                    sb.append("; valid range: -2.0 to 2.0; these control biome placement on the climate grid");
                } else if (key.equals("base_height")) {
                    sb.append("; typical range: -2.0 to 2.0 relative to sea level");
                } else if (key.equals("height_variation")) {
                    sb.append("; valid range: 0.0-3.0; higher values create more dramatic terrain");
                } else if (key.equals("category")) {
                    sb.append("; valid values: none, taiga, extreme_hills, jungle, mesa, plains, savanna, ");
                    sb.append("icy, the_end, beach, forest, ocean, desert, river, swamp, mushroom, nether");
                } else if (key.equals("dimension")) {
                    sb.append("; valid values: overworld, nether, end");
                }
            }
            case "noise" -> {
                if (key.equals("type")) {
                    sb.append("; valid values: PERLIN, SIMPLEX, OPENSIMPLEX2, CELLULAR, VALUE, WHITE");
                } else if (key.equals("octaves")) {
                    sb.append("; valid range: 1-16; more octaves add detail but cost performance");
                } else if (key.equals("frequency")) {
                    sb.append("; valid range: 0.0001-10.0; lower values create larger features");
                } else if (key.equals("lacunarity")) {
                    sb.append("; valid range: 1.0-4.0; controls frequency multiplier between octaves");
                } else if (key.equals("persistence")) {
                    sb.append("; valid range: 0.0-1.0; controls amplitude decay between octaves");
                }
            }
            case "carver" -> {
                if (key.equals("type")) {
                    sb.append("; valid values: CHEESE, SPAGHETTI, NOODLE, RAVINE, CUSTOM");
                } else if (key.equals("probability")) {
                    sb.append("; valid range: 0.0-1.0; chance of carver placement per chunk");
                } else if (key.equals("width") || key.equals("height")) {
                    sb.append("; valid range: 0.1-10.0; radius multiplier for the carver shape");
                }
            }
            case "feature" -> {
                if (key.equals("step")) {
                    sb.append("; valid values: RAW_GENERATION, LAKES, LOCAL_MODIFICATIONS, ");
                    sb.append("UNDERGROUND_STRUCTURES, SURFACE_STRUCTURES, STRONGHOLDS, ");
                    sb.append("UNDERGROUND_ORES, UNDERGROUND_DECORATION, FLUID_SPRINGS, ");
                    sb.append("VEGETAL_DECORATION, TOP_LAYER_MODIFICATION");
                } else if (key.equals("rarity") || key.equals("discard_chance_on_air_exposure")) {
                    sb.append("; valid range: 0.0-1.0");
                } else if (key.equals("count")) {
                    sb.append("; valid range: 1-256; number of placement attempts per chunk");
                } else if (key.equals("size")) {
                    sb.append("; valid range: 1-64; vein size for ore features");
                }
            }
            case "structure" -> {
                if (key.equals("type")) {
                    sb.append("; valid values: JIGSAW, SCHEMATIC, PROCEDURAL");
                } else if (key.equals("spacing")) {
                    sb.append("; valid range: 1-500; average distance between structures in chunks");
                } else if (key.equals("separation")) {
                    sb.append("; valid range: 1-spacing; minimum distance, must be less than spacing");
                }
            }
            case "terrain" -> {
                if (key.equals("sea_level")) {
                    sb.append("; typical value: 63 for overworld; valid range: 0-320");
                } else if (key.equals("height_scale")) {
                    sb.append("; valid range: 0.1-10.0; multiplier for overall terrain height");
                }
            }
            default -> {
                // No additional field-specific guidance for unknown config types
            }
        }

        return sb.toString();
    }
}
