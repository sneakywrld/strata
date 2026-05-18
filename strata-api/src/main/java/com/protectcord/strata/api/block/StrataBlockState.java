package com.protectcord.strata.api.block;

import com.protectcord.strata.api.core.NamespacedKey;

import java.util.Map;

/**
 * Represents a block state in a platform-agnostic way.
 * A block state is a block type plus optional property key-value pairs
 * (e.g., {@code minecraft:oak_stairs[facing=north,half=top]}).
 */
public record StrataBlockState(
        NamespacedKey blockId,
        Map<String, String> properties
) {

    /**
     * Creates a simple block state with no properties.
     */
    public static StrataBlockState of(NamespacedKey blockId) {
        return new StrataBlockState(blockId, Map.of());
    }

    /**
     * Creates a block state from a string like {@code "minecraft:stone"}
     * or {@code "minecraft:oak_stairs[facing=north,half=top]"}.
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
