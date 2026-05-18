package com.protectcord.strata.api.biome;

import com.protectcord.strata.api.block.BlockPalette;
import com.protectcord.strata.api.core.Keyed;
import com.protectcord.strata.api.core.NamespacedKey;

import java.util.List;
import java.util.Optional;

/**
 * Represents a biome definition in Strata's generation system.
 *
 * <p>A biome encapsulates all the properties needed for world generation within a climatic region:
 * {@linkplain #climate() climate parameters} for biome selection, {@linkplain #surfacePalette() surface blocks},
 * {@linkplain #features() features}, {@linkplain #carvers() carvers}, {@linkplain #structures() structures},
 * {@linkplain #spawnRules() spawn rules}, and {@linkplain #effects() visual/ambient effects}.</p>
 *
 * <p>Biomes are assigned to 4x4x4 cells during the
 * {@link com.protectcord.strata.api.pipeline.GenerationStage#BIOME_ASSIGNMENT BIOME_ASSIGNMENT} stage
 * based on a nearest-neighbor KD-tree lookup against their climate parameters.</p>
 *
 * <p>Use {@link BiomeBuilder} to construct new biome instances:</p>
 * <pre>{@code
 * Biome myBiome = new BiomeBuilder()
 *     .key(NamespacedKey.of("myplugin", "crystal_caves"))
 *     .climate(new ClimateParameters(0.5, 0.3, 0.7, -0.2, 0.0))
 *     .vanillaMapping(NamespacedKey.minecraft("lush_caves"))
 *     .category(BiomeCategory.CAVE)
 *     .build();
 * api.biomeRegistry().register(myBiome);
 * }</pre>
 *
 * @since 1.0.0
 * @see BiomeBuilder
 * @see ClimateParameters
 * @see BiomeCategory
 */
public interface Biome extends Keyed {

    /**
     * Returns the climate parameters that determine where this biome generates.
     *
     * <p>The biome selection algorithm uses a KD-tree to find the biome whose climate
     * parameters are closest (by Euclidean distance) to the sampled climate at each position.</p>
     *
     * @return the 5-dimensional climate parameters, never {@code null}
     * @see ClimateParameters
     */
    ClimateParameters climate();

    /**
     * Returns the vanilla biome key that this Strata biome maps to for client rendering.
     *
     * <p>Because Strata operates server-side, the client needs a vanilla biome reference to
     * determine grass color, foliage color, sky color, particles, and ambient sounds.</p>
     *
     * @return the vanilla biome {@link NamespacedKey} (e.g., {@code minecraft:plains}), never {@code null}
     */
    NamespacedKey vanillaMapping();

    /**
     * Returns the dimension this biome belongs to.
     *
     * @return the {@link BiomeDimension} (overworld, nether, or end), never {@code null}
     */
    BiomeDimension dimension();

    /**
     * Returns the high-level category of this biome, used for classification and filtering.
     *
     * @return the {@link BiomeCategory}, never {@code null}
     */
    BiomeCategory category();

    /**
     * Returns the surface block palette for the top terrain layers (e.g., grass, dirt, sand).
     *
     * <p>If empty, the biome inherits default surface rules from the world profile.</p>
     *
     * @return an {@link Optional} containing the surface {@link BlockPalette}, or empty if not set
     */
    Optional<BlockPalette> surfacePalette();

    /**
     * Returns the underwater surface palette used for submerged terrain
     * (e.g., sand, gravel, or clay for ocean floors and riverbeds).
     *
     * <p>If empty, the default underwater surface rules apply.</p>
     *
     * @return an {@link Optional} containing the underwater {@link BlockPalette}, or empty if not set
     */
    Optional<BlockPalette> underwaterPalette();

    /**
     * Returns an ordered list of feature keys to place in this biome.
     *
     * <p>Features are placed in list order during the
     * {@link com.protectcord.strata.api.pipeline.GenerationStage#FEATURE_DECORATION FEATURE_DECORATION}
     * stage. Each key must reference a registered {@link com.protectcord.strata.api.feature.Feature}.</p>
     *
     * @return an unmodifiable list of feature keys, never {@code null}
     */
    List<NamespacedKey> features();

    /**
     * Returns the list of carver keys that operate in this biome.
     *
     * <p>Each key must reference a registered {@link com.protectcord.strata.api.carver.Carver}.</p>
     *
     * @return an unmodifiable list of carver keys, never {@code null}
     */
    List<NamespacedKey> carvers();

    /**
     * Returns the list of structure keys that can generate in this biome.
     *
     * <p>Each key must reference a registered {@link com.protectcord.strata.api.structure.StructureDefinition}.</p>
     *
     * @return an unmodifiable list of structure keys, never {@code null}
     */
    List<NamespacedKey> structures();

    /**
     * Returns the entity spawn rules associated with this biome.
     *
     * <p>Each key must reference a registered {@link com.protectcord.strata.api.entity.SpawnRule}.</p>
     *
     * @return an unmodifiable list of spawn rule keys, never {@code null}
     */
    List<NamespacedKey> spawnRules();

    /**
     * Returns the visual and ambient settings for this biome, including fog color,
     * water color, sky color, temperature, and precipitation.
     *
     * @return the {@link BiomeEffects}, never {@code null}
     */
    BiomeEffects effects();

    /**
     * Returns the base terrain height offset for this biome.
     *
     * <p>This value shifts the terrain surface up or down relative to sea level.
     * Typical values range from {@code -1.0} (deep ocean) to {@code 2.0} (mountains).</p>
     *
     * @return the base height offset
     */
    double baseHeight();

    /**
     * Returns the terrain height variation scale for this biome.
     *
     * <p>Higher values produce more dramatic elevation changes within the biome.
     * A value of {@code 0.0} produces flat terrain; values above {@code 1.0} produce
     * extreme hills and valleys.</p>
     *
     * @return the height variation scale
     */
    double heightVariation();
}
