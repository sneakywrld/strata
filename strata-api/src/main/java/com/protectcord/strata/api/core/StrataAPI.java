package com.protectcord.strata.api.core;

import com.protectcord.strata.api.biome.Biome;
import com.protectcord.strata.api.carver.Carver;
import com.protectcord.strata.api.feature.Feature;
import com.protectcord.strata.api.noise.NoiseFunction;
import com.protectcord.strata.api.registry.Registry;
import com.protectcord.strata.api.structure.StructureDefinition;
import com.protectcord.strata.api.surface.SurfaceRule;
import com.protectcord.strata.api.world.WorldManager;
import com.protectcord.strata.api.event.EventBus;

/**
 * The central access point for the Strata World Generation API.
 *
 * <p>Third-party plugins obtain a reference to this interface to register custom biomes,
 * noise functions, structures, features, surface rules, and carvers. It also provides
 * access to the {@link WorldManager} for creating and querying Strata-managed worlds,
 * and to the {@link EventBus} for subscribing to generation lifecycle events.</p>
 *
 * <p>Access the singleton instance via {@link StrataProvider#get()}:</p>
 * <pre>{@code
 * StrataAPI api = StrataProvider.get();
 * api.biomeRegistry().register(myBiome);
 * api.eventBus().subscribe(ChunkGeneratedEvent.class, e -> { ... });
 * }</pre>
 *
 * @since 1.0.0
 * @see StrataProvider
 * @see Registry
 */
public interface StrataAPI {

    /**
     * Returns the biome registry, used to register and look up {@link Biome} definitions.
     *
     * <p>Biomes registered here become available for assignment during the
     * {@link com.protectcord.strata.api.pipeline.GenerationStage#BIOME_ASSIGNMENT BIOME_ASSIGNMENT}
     * pipeline stage.</p>
     *
     * @return the biome {@link Registry}, never {@code null}
     */
    Registry<Biome> biomeRegistry();

    /**
     * Returns the noise function registry, used to register and look up {@link NoiseFunction} instances.
     *
     * <p>Noise functions registered here can be referenced by key in terrain settings,
     * composite noise chains, and water system configurations.</p>
     *
     * @return the noise function {@link Registry}, never {@code null}
     */
    Registry<NoiseFunction> noiseRegistry();

    /**
     * Returns the surface rule registry, used to register and look up {@link SurfaceRule} definitions.
     *
     * <p>Surface rules determine which blocks are placed at the terrain surface during the
     * {@link com.protectcord.strata.api.pipeline.GenerationStage#SURFACE_BUILDING SURFACE_BUILDING} stage.</p>
     *
     * @return the surface rule {@link Registry}, never {@code null}
     */
    Registry<SurfaceRule> surfaceRuleRegistry();

    /**
     * Returns the carver registry, used to register and look up {@link Carver} implementations.
     *
     * <p>Carvers registered here execute during the
     * {@link com.protectcord.strata.api.pipeline.GenerationStage#CARVING CARVING} stage to create
     * caves, ravines, and other subterranean voids.</p>
     *
     * @return the carver {@link Registry}, never {@code null}
     */
    Registry<Carver> carverRegistry();

    /**
     * Returns the structure definition registry, used to register and look up
     * {@link StructureDefinition} instances.
     *
     * <p>Structures registered here are placed during the
     * {@link com.protectcord.strata.api.pipeline.GenerationStage#STRUCTURE_GENERATION STRUCTURE_GENERATION}
     * stage.</p>
     *
     * @return the structure definition {@link Registry}, never {@code null}
     */
    Registry<StructureDefinition> structureRegistry();

    /**
     * Returns the feature registry, used to register and look up {@link Feature} implementations.
     *
     * <p>Features registered here are placed during the
     * {@link com.protectcord.strata.api.pipeline.GenerationStage#FEATURE_DECORATION FEATURE_DECORATION}
     * stage (e.g., ores, trees, vegetation).</p>
     *
     * @return the feature {@link Registry}, never {@code null}
     */
    Registry<Feature> featureRegistry();

    /**
     * Returns the world manager for creating, loading, and querying Strata-managed worlds
     * and their associated {@link com.protectcord.strata.api.world.WorldProfile profiles}.
     *
     * @return the {@link WorldManager}, never {@code null}
     */
    WorldManager worldManager();

    /**
     * Returns the event bus for subscribing to generation and lifecycle events.
     *
     * <p>Common events include {@link com.protectcord.strata.api.event.ChunkGeneratingEvent},
     * {@link com.protectcord.strata.api.event.ChunkGeneratedEvent},
     * {@link com.protectcord.strata.api.event.ProfileLoadedEvent}, and
     * {@link com.protectcord.strata.api.event.WorldCreatedEvent}.</p>
     *
     * @return the {@link EventBus}, never {@code null}
     */
    EventBus eventBus();

    /**
     * Returns the current Strata version string (e.g., {@code "1.0.0"}).
     *
     * @return the version string, never {@code null}
     */
    String version();
}
