package com.protectcord.strata.api.core;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A namespaced identifier following the {@code namespace:key} convention.
 *
 * <p>Similar to Minecraft's {@code ResourceLocation}, this ensures unique identification
 * of all registerable objects across plugins and profiles. Both the namespace and key
 * components must match the pattern {@code [a-z0-9_.-]+}.</p>
 *
 * <p>The default namespace for Strata built-in content is {@code "strata"}.
 * The Minecraft namespace {@code "minecraft"} is used for vanilla block and biome references.
 * Third-party plugins should use their own unique namespace (e.g., their plugin ID).</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * NamespacedKey custom = NamespacedKey.of("myplugin", "crystal_caves");
 * NamespacedKey builtin = NamespacedKey.strata("plains");
 * NamespacedKey parsed = NamespacedKey.parse("myplugin:crystal_caves");
 * }</pre>
 *
 * @since 1.0.0
 */
public final class NamespacedKey {

    public static final String STRATA_NAMESPACE = "strata";
    public static final String MINECRAFT_NAMESPACE = "minecraft";

    private static final Pattern VALID_PATTERN = Pattern.compile("^[a-z0-9_.-]+$");

    private final String namespace;
    private final String key;

    private NamespacedKey(String namespace, String key) {
        this.namespace = namespace;
        this.key = key;
    }

    /**
     * Creates a new NamespacedKey.
     *
     * @param namespace the namespace (e.g., "strata", "myplugin")
     * @param key       the key (e.g., "plains", "custom_biome")
     * @return the new NamespacedKey
     * @throws IllegalArgumentException if namespace or key contain invalid characters
     */
    public static NamespacedKey of(String namespace, String key) {
        Objects.requireNonNull(namespace, "namespace");
        Objects.requireNonNull(key, "key");
        if (!VALID_PATTERN.matcher(namespace).matches()) {
            throw new IllegalArgumentException("Invalid namespace: " + namespace);
        }
        if (!VALID_PATTERN.matcher(key).matches()) {
            throw new IllegalArgumentException("Invalid key: " + key);
        }
        return new NamespacedKey(namespace, key);
    }

    /**
     * Creates a {@code NamespacedKey} under the Strata namespace ({@code "strata"}).
     *
     * @param key the key portion (e.g., {@code "plains"}, {@code "continental_base"})
     * @return a new {@code NamespacedKey} with namespace {@code "strata"}
     * @throws IllegalArgumentException if the key contains invalid characters
     */
    public static NamespacedKey strata(String key) {
        return of(STRATA_NAMESPACE, key);
    }

    /**
     * Creates a {@code NamespacedKey} under the Minecraft namespace ({@code "minecraft"}).
     *
     * @param key the key portion (e.g., {@code "stone"}, {@code "water"})
     * @return a new {@code NamespacedKey} with namespace {@code "minecraft"}
     * @throws IllegalArgumentException if the key contains invalid characters
     */
    public static NamespacedKey minecraft(String key) {
        return of(MINECRAFT_NAMESPACE, key);
    }

    /**
     * Parses a string in the format {@code "namespace:key"}.
     *
     * <p>If no colon is present, the Strata namespace ({@code "strata"}) is assumed.
     * For example, {@code parse("plains")} is equivalent to {@code strata("plains")}.</p>
     *
     * @param input the string to parse (e.g., {@code "myplugin:custom_biome"} or {@code "plains"})
     * @return the parsed {@code NamespacedKey}
     * @throws NullPointerException     if {@code input} is {@code null}
     * @throws IllegalArgumentException if the namespace or key contains invalid characters
     */
    public static NamespacedKey parse(String input) {
        Objects.requireNonNull(input, "input");
        int colon = input.indexOf(':');
        if (colon < 0) {
            return strata(input);
        }
        return of(input.substring(0, colon), input.substring(colon + 1));
    }

    /**
     * Returns the namespace component of this key.
     *
     * @return the namespace (e.g., {@code "strata"}, {@code "minecraft"}, {@code "myplugin"})
     */
    public String namespace() {
        return namespace;
    }

    /**
     * Returns the key component of this identifier.
     *
     * @return the key (e.g., {@code "plains"}, {@code "stone"}, {@code "custom_biome"})
     */
    public String key() {
        return key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NamespacedKey other)) return false;
        return namespace.equals(other.namespace) && key.equals(other.key);
    }

    @Override
    public int hashCode() {
        return 31 * namespace.hashCode() + key.hashCode();
    }

    @Override
    public String toString() {
        return namespace + ":" + key;
    }
}
