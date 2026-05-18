package com.protectcord.strata.paper.api;

import com.protectcord.strata.api.biome.Biome;
import com.protectcord.strata.api.carver.Carver;
import com.protectcord.strata.api.core.StrataAPI;
import com.protectcord.strata.api.event.EventBus;
import com.protectcord.strata.api.feature.Feature;
import com.protectcord.strata.api.noise.NoiseFunction;
import com.protectcord.strata.api.registry.Registry;
import com.protectcord.strata.api.structure.StructureDefinition;
import com.protectcord.strata.api.surface.SurfaceRule;
import com.protectcord.strata.api.world.WorldManager;
import com.protectcord.strata.config.registry.ConfigRegistry;
import com.protectcord.strata.core.registry.SimpleRegistry;
import com.protectcord.strata.paper.StrataPlugin;

/**
 * Implementation of the public StrataAPI interface.
 */
public final class StrataAPIImpl implements StrataAPI {

    private final StrataPlugin plugin;
    private final Registry<Biome> biomeRegistry = new SimpleRegistry<>("biome");
    private final Registry<NoiseFunction> noiseRegistry = new SimpleRegistry<>("noise");
    private final Registry<SurfaceRule> surfaceRuleRegistry = new SimpleRegistry<>("surface_rule");
    private final Registry<Carver> carverRegistry = new SimpleRegistry<>("carver");
    private final Registry<StructureDefinition> structureRegistry = new SimpleRegistry<>("structure");
    private final Registry<Feature> featureRegistry = new SimpleRegistry<>("feature");
    private final EventBusImpl eventBus = new EventBusImpl();
    private WorldManager worldManager;

    public StrataAPIImpl(StrataPlugin plugin, ConfigRegistry configRegistry) {
        this.plugin = plugin;
    }

    @Override public Registry<Biome> biomeRegistry() { return biomeRegistry; }
    @Override public Registry<NoiseFunction> noiseRegistry() { return noiseRegistry; }
    @Override public Registry<SurfaceRule> surfaceRuleRegistry() { return surfaceRuleRegistry; }
    @Override public Registry<Carver> carverRegistry() { return carverRegistry; }
    @Override public Registry<StructureDefinition> structureRegistry() { return structureRegistry; }
    @Override public Registry<Feature> featureRegistry() { return featureRegistry; }
    @Override public WorldManager worldManager() { return worldManager; }
    @Override public EventBus eventBus() { return eventBus; }
    @Override public String version() { return plugin.getDescription().getVersion(); }

    public void setWorldManager(WorldManager worldManager) {
        this.worldManager = worldManager;
    }
}
