package com.protectcord.strata.api.feature;

import java.util.List;
import java.util.Map;

/**
 * Describes how a feature should be placed in the world.
 * A sequence of placement modifiers filters and transforms positions.
 *
 * @param modifiers ordered list of placement modifiers to apply
 * @param parameters parameters for each modifier (keyed by modifier ordinal)
 */
public record FeaturePlacement(
        List<PlacementModifier> modifiers,
        Map<String, Object> parameters
) {

    /**
     * Creates a simple heightmap placement with the given count per chunk.
     */
    public static FeaturePlacement heightmapCount(int count) {
        return new FeaturePlacement(
                List.of(PlacementModifier.COUNT, PlacementModifier.IN_SQUARE, PlacementModifier.HEIGHTMAP),
                Map.of("count", count)
        );
    }

    /**
     * Creates a uniform Y-range placement.
     */
    public static FeaturePlacement uniformY(int minY, int maxY, int count) {
        return new FeaturePlacement(
                List.of(PlacementModifier.COUNT, PlacementModifier.IN_SQUARE, PlacementModifier.UNIFORM_Y),
                Map.of("count", count, "min_y", minY, "max_y", maxY)
        );
    }
}
