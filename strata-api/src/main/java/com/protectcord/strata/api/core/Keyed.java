package com.protectcord.strata.api.core;

/**
 * Represents any object that is uniquely identified by a {@link NamespacedKey}.
 *
 * <p>All registerable objects in Strata implement this interface, including
 * {@link com.protectcord.strata.api.biome.Biome biomes},
 * {@link com.protectcord.strata.api.noise.NoiseFunction noise functions},
 * {@link com.protectcord.strata.api.surface.SurfaceRule surface rules},
 * {@link com.protectcord.strata.api.carver.Carver carvers},
 * {@link com.protectcord.strata.api.structure.StructureDefinition structures},
 * {@link com.protectcord.strata.api.feature.Feature features}, and
 * {@link com.protectcord.strata.api.block.BlockPalette block palettes}.</p>
 *
 * <p>The key returned by {@link #key()} must be stable and unique within the
 * object's {@link com.protectcord.strata.api.registry.Registry Registry}. Two keyed objects
 * with the same key cannot coexist in the same registry.</p>
 *
 * @since 1.0.0
 * @see NamespacedKey
 * @see com.protectcord.strata.api.registry.Registry
 */
public interface Keyed {

    /**
     * Returns the unique namespaced identifier for this object.
     *
     * @return the {@link NamespacedKey} identifying this object, never {@code null}
     */
    NamespacedKey key();
}
