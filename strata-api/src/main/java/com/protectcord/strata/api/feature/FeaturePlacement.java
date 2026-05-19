package com.protectcord.strata.api.feature;

import java.util.List;
import java.util.Map;

/**
 * Describes how a {@link Feature} should be placed in the world.
 *
 * <p>Placement is defined as an ordered pipeline of {@link PlacementModifier}s that filter
 * and transform candidate positions. Each modifier can reject positions (e.g., biome filter),
 * multiply them (e.g., count), or adjust their coordinates (e.g., heightmap snap, Y offset).</p>
 *
 * <p>The {@code parameters} map provides configuration values consumed by each modifier,
 * keyed by parameter name (e.g., {@code "count"}, {@code "min_y"}, {@code "max_y"}).</p>
 *
 * @param modifiers  ordered list of {@link PlacementModifier}s to apply
 * @param parameters configuration parameters consumed by the modifiers
 * @since 1.0.0
 * @see PlacementModifier
 * @see Feature#placement()
 */
public record FeaturePlacement(
        List<PlacementModifier> modifiers,
        Map<String, Object> parameters
) {

    /**
     * Creates a simple heightmap placement that places the feature {@code count} times per chunk,
     * randomly spread within the chunk and snapped to the surface heightmap.
     *
     * @param count the number of placement attempts per chunk
     * @return a placement with {@link PlacementModifier#COUNT}, {@link PlacementModifier#IN_SQUARE},
     *         and {@link PlacementModifier#HEIGHTMAP} modifiers
     */
    public static FeaturePlacement heightmapCount(int count) {
        return new FeaturePlacement(
                List.of(PlacementModifier.COUNT, PlacementModifier.IN_SQUARE, PlacementModifier.HEIGHTMAP),
                Map.of("count", count)
        );
    }

    /**
     * Creates a uniform Y-range placement that places the feature {@code count} times per chunk,
     * at random Y coordinates uniformly distributed between {@code minY} and {@code maxY}.
     *
     * @param minY  the minimum Y coordinate for placement
     * @param maxY  the maximum Y coordinate for placement
     * @param count the number of placement attempts per chunk
     * @return a placement with {@link PlacementModifier#COUNT}, {@link PlacementModifier#IN_SQUARE},
     *         and {@link PlacementModifier#UNIFORM_Y} modifiers
     */
    public static FeaturePlacement uniformY(int minY, int maxY, int count) {
        return new FeaturePlacement(
                List.of(PlacementModifier.COUNT, PlacementModifier.IN_SQUARE, PlacementModifier.UNIFORM_Y),
                Map.of("count", count, "min_y", minY, "max_y", maxY)
        );
    }
}
