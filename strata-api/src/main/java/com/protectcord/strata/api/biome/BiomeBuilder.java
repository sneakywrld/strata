package com.protectcord.strata.api.biome;

import com.protectcord.strata.api.block.BlockPalette;
import com.protectcord.strata.api.core.NamespacedKey;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for constructing {@link Biome} instances programmatically.
 *
 * <p>Used by the TOML configuration loader and by third-party plugins via the API.
 * The builder uses a fluent interface, with {@link #key(NamespacedKey)},
 * {@link #climate(ClimateParameters)}, and {@link #vanillaMapping(NamespacedKey)} required
 * before calling {@link #build()}.</p>
 *
 * <p>Example:</p>
 * <pre>{@code
 * Biome biome = new BiomeBuilder()
 *     .key(NamespacedKey.of("myplugin", "autumn_forest"))
 *     .climate(new ClimateParameters(0.4, 0.6, 0.5, 0.3, 0.0))
 *     .vanillaMapping(NamespacedKey.minecraft("forest"))
 *     .dimension(BiomeDimension.OVERWORLD)
 *     .category(BiomeCategory.FOREST)
 *     .baseHeight(0.2)
 *     .heightVariation(0.4)
 *     .addFeature(NamespacedKey.of("myplugin", "autumn_trees"))
 *     .build();
 * }</pre>
 *
 * @since 1.0.0
 * @see Biome
 */
public final class BiomeBuilder {

    private NamespacedKey key;
    private ClimateParameters climate;
    private NamespacedKey vanillaMapping;
    private BiomeDimension dimension = BiomeDimension.OVERWORLD;
    private BiomeCategory category = BiomeCategory.PLAINS;
    private BlockPalette surfacePalette;
    private BlockPalette underwaterPalette;
    private final List<NamespacedKey> features = new ArrayList<>();
    private final List<NamespacedKey> carvers = new ArrayList<>();
    private final List<NamespacedKey> structures = new ArrayList<>();
    private final List<NamespacedKey> spawnRules = new ArrayList<>();
    private BiomeEffects effects = BiomeEffects.defaultOverworld();
    private double baseHeight = 0.1;
    private double heightVariation = 0.2;

    /**
     * Sets the unique identifier for this biome. <b>Required.</b>
     *
     * @param key the namespaced key (e.g., {@code NamespacedKey.of("myplugin", "autumn_forest")})
     * @return this builder for chaining
     */
    public BiomeBuilder key(NamespacedKey key) {
        this.key = key;
        return this;
    }

    /**
     * Sets the climate parameters for biome selection. <b>Required.</b>
     *
     * @param climate the 5-dimensional climate parameters
     * @return this builder for chaining
     * @see ClimateParameters
     */
    public BiomeBuilder climate(ClimateParameters climate) {
        this.climate = climate;
        return this;
    }

    /**
     * Sets the vanilla biome key used for client-side rendering. <b>Required.</b>
     *
     * @param vanillaMapping a vanilla biome key (e.g., {@code NamespacedKey.minecraft("forest")})
     * @return this builder for chaining
     */
    public BiomeBuilder vanillaMapping(NamespacedKey vanillaMapping) {
        this.vanillaMapping = vanillaMapping;
        return this;
    }

    /**
     * Sets the dimension this biome belongs to. Defaults to {@link BiomeDimension#OVERWORLD}.
     *
     * @param dimension the target dimension
     * @return this builder for chaining
     */
    public BiomeBuilder dimension(BiomeDimension dimension) {
        this.dimension = dimension;
        return this;
    }

    /**
     * Sets the biome category for classification. Defaults to {@link BiomeCategory#PLAINS}.
     *
     * @param category the biome category
     * @return this builder for chaining
     */
    public BiomeBuilder category(BiomeCategory category) {
        this.category = category;
        return this;
    }

    /**
     * Sets the surface block palette for the top terrain layers.
     *
     * @param palette the surface block palette, or {@code null} to use profile defaults
     * @return this builder for chaining
     */
    public BiomeBuilder surfacePalette(BlockPalette palette) {
        this.surfacePalette = palette;
        return this;
    }

    /**
     * Sets the underwater surface block palette (e.g., sand, gravel, clay).
     *
     * @param palette the underwater palette, or {@code null} to use profile defaults
     * @return this builder for chaining
     */
    public BiomeBuilder underwaterPalette(BlockPalette palette) {
        this.underwaterPalette = palette;
        return this;
    }

    /**
     * Adds a feature key to this biome's feature list. Features are placed in insertion order.
     *
     * @param feature a key referencing a registered {@link com.protectcord.strata.api.feature.Feature}
     * @return this builder for chaining
     */
    public BiomeBuilder addFeature(NamespacedKey feature) {
        this.features.add(feature);
        return this;
    }

    /**
     * Adds a carver key to this biome's carver list.
     *
     * @param carver a key referencing a registered {@link com.protectcord.strata.api.carver.Carver}
     * @return this builder for chaining
     */
    public BiomeBuilder addCarver(NamespacedKey carver) {
        this.carvers.add(carver);
        return this;
    }

    /**
     * Adds a structure key to this biome's structure list.
     *
     * @param structure a key referencing a registered {@link com.protectcord.strata.api.structure.StructureDefinition}
     * @return this builder for chaining
     */
    public BiomeBuilder addStructure(NamespacedKey structure) {
        this.structures.add(structure);
        return this;
    }

    /**
     * Adds a spawn rule key to this biome's spawn rule list.
     *
     * @param rule a key referencing a registered {@link com.protectcord.strata.api.entity.SpawnRule}
     * @return this builder for chaining
     */
    public BiomeBuilder addSpawnRule(NamespacedKey rule) {
        this.spawnRules.add(rule);
        return this;
    }

    /**
     * Sets the visual and ambient effects for this biome. Defaults to {@link BiomeEffects#defaultOverworld()}.
     *
     * @param effects the biome effects (fog, water color, sky, temperature, etc.)
     * @return this builder for chaining
     */
    public BiomeBuilder effects(BiomeEffects effects) {
        this.effects = effects;
        return this;
    }

    /**
     * Sets the base terrain height offset. Defaults to {@code 0.1}.
     *
     * @param baseHeight the height offset relative to sea level
     * @return this builder for chaining
     */
    public BiomeBuilder baseHeight(double baseHeight) {
        this.baseHeight = baseHeight;
        return this;
    }

    /**
     * Sets the terrain height variation scale. Defaults to {@code 0.2}.
     *
     * @param heightVariation the variation scale (higher = more dramatic elevation changes)
     * @return this builder for chaining
     */
    public BiomeBuilder heightVariation(double heightVariation) {
        this.heightVariation = heightVariation;
        return this;
    }

    /**
     * Builds and returns a new {@link Biome} instance from this builder's state.
     *
     * @return the constructed biome
     * @throws IllegalStateException if {@link #key(NamespacedKey)}, {@link #climate(ClimateParameters)},
     *                               or {@link #vanillaMapping(NamespacedKey)} have not been set
     */
    public Biome build() {
        if (key == null) throw new IllegalStateException("Biome key is required");
        if (climate == null) throw new IllegalStateException("Climate parameters are required");
        if (vanillaMapping == null) throw new IllegalStateException("Vanilla mapping is required");

        return new BiomeImpl(
                key, climate, vanillaMapping, dimension, category,
                surfacePalette, underwaterPalette,
                List.copyOf(features), List.copyOf(carvers),
                List.copyOf(structures), List.copyOf(spawnRules),
                effects, baseHeight, heightVariation
        );
    }
}
