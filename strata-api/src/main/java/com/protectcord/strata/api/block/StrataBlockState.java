package com.protectcord.strata.api.block;

import com.protectcord.strata.api.core.NamespacedKey;

import java.util.Map;

/**
 * Represents a block state in a platform-agnostic way.
 *
 * <p>A block state consists of a block type identifier ({@link NamespacedKey}) plus optional
 * property key-value pairs representing block state variants. For example:</p>
 * <ul>
 *   <li>{@code minecraft:stone} -- no properties</li>
 *   <li>{@code minecraft:oak_stairs[facing=north,half=top]} -- with directional properties</li>
 * </ul>
 *
 * <p>Block states are used throughout the API for surface rules, block palettes, aquifer
 * configuration, and feature placement.</p>
 *
 * @param blockId    the namespaced block type identifier (e.g., {@code minecraft:stone})
 * @param properties an unmodifiable map of block state properties (e.g., {@code facing=north})
 * @since 1.0.0
 * @see BlockPalette
 */
public record StrataBlockState(
        NamespacedKey blockId,
        Map<String, String> properties
) {

    /**
     * Creates a simple block state with no properties.
     *
     * @param blockId the block type identifier (e.g., {@code NamespacedKey.minecraft("stone")})
     * @return a new block state with an empty property map
     */
    public static StrataBlockState of(NamespacedKey blockId) {
        return new StrataBlockState(blockId, Map.of());
    }

    /**
     * Parses a block state from a string representation.
     *
     * <p>Accepted formats:</p>
     * <ul>
     *   <li>{@code "minecraft:stone"} -- block with no properties</li>
     *   <li>{@code "minecraft:oak_stairs[facing=north,half=top]"} -- block with properties</li>
     *   <li>{@code "stone"} -- shorthand using the Strata namespace</li>
     * </ul>
     *
     * @param input the string to parse
     * @return the parsed block state
     * @throws IllegalArgumentException if the block ID contains invalid characters
     */
    public static StrataBlockState parse(String input) {
        int bracketStart = input.indexOf('[');
        if (bracketStart < 0) {
            return of(NamespacedKey.parse(input));
        }
        NamespacedKey id = NamespacedKey.parse(input.substring(0, bracketStart));
        String propsStr = input.substring(bracketStart + 1, input.length() - 1);
        var props = new java.util.HashMap<String, String>();
        for (String pair : propsStr.split(",")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                props.put(kv[0].trim(), kv[1].trim());
            }
        }
        return new StrataBlockState(id, Map.copyOf(props));
    }

    @Override
    public String toString() {
        if (properties.isEmpty()) {
            return blockId.toString();
        }
        StringBuilder sb = new StringBuilder(blockId.toString()).append('[');
        var iter = properties.entrySet().iterator();
        while (iter.hasNext()) {
            var entry = iter.next();
            sb.append(entry.getKey()).append('=').append(entry.getValue());
            if (iter.hasNext()) sb.append(',');
        }
        return sb.append(']').toString();
    }
}
