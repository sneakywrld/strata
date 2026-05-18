package com.protectcord.strata.config.model;

import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.config.loader.TomlReader;

import java.nio.file.Path;

/**
 * Parsed profile configuration from a profile.toml file.
 *
 * @param key          the profile's namespaced key
 * @param displayName  the human-readable name
 * @param description  profile description
 * @param author       the profile author
 * @param extendsFrom  parent profile key (null if none)
 * @param directory    the profile's root directory on disk
 * @param rawConfig    the raw TOML reader for sub-section access
 */
public record ProfileConfig(
        NamespacedKey key,
        String displayName,
        String description,
        String author,
        String extendsFrom,
        Path directory,
        TomlReader rawConfig
) {}
