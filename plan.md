# Strata - Master Implementation Plan

> **The best Minecraft world generation plugin on the market.**
> PaperMC | Minecraft 1.8.8 - 26.1.2 | Gradle | TOML Config | Terra Migration

---

## Phase 1: Project Scaffolding

> Gradle multi-module setup, build conventions, version catalog, wrapper.

- [x] Create `settings.gradle.kts` with all module includes
- [x] Create root `build.gradle.kts` (plugins block, group/version)
- [x] Create `gradle.properties` (group=com.protectcord.strata, version, Java 25 toolchain, author=SneakyWrld)
- [x] Create `gradle/libs.versions.toml` (version catalog: Paper API, TOMLy, Jackson, Caffeine, bStats, Adventure, JUnit, etc.)
- [x] Initialize Gradle wrapper (`gradle/wrapper/gradle-wrapper.properties`)
- [x] Create `buildSrc/build.gradle.kts` (kotlin-dsl plugin)
- [x] Create `buildSrc/src/main/kotlin/strata.base-conventions.gradle.kts` (Java 25 target, repos, static analysis)
- [x] Create `buildSrc/src/main/kotlin/strata.library-conventions.gradle.kts` (java-library, Maven publishing)
- [x] Create `buildSrc/src/main/kotlin/strata.nms-conventions.gradle.kts` (paperweight-userdev / special-source)
- [x] Create `buildSrc/src/main/kotlin/strata.paper-conventions.gradle.kts` (shadow plugin, resource filtering)
- [x] Create `strata-api/build.gradle.kts`
- [x] Create `strata-noise/build.gradle.kts`
- [x] Create `strata-config/build.gradle.kts`
- [x] Create `strata-core/build.gradle.kts`
- [x] Create `strata-nms/common/build.gradle.kts`
- [x] Create `strata-nms/v1_8_R3/build.gradle.kts`
- [x] Create `strata-nms/v1_12_R1/build.gradle.kts`
- [x] Create `strata-nms/v1_13_R2/build.gradle.kts`
- [x] Create `strata-nms/v1_16_R3/build.gradle.kts`
- [x] Create `strata-nms/v1_17_R1/build.gradle.kts`
- [x] Create `strata-nms/v1_18_R2/build.gradle.kts`
- [x] Create `strata-nms/v1_19_R3/build.gradle.kts`
- [x] Create `strata-nms/v1_20_R4/build.gradle.kts`
- [x] Create `strata-nms/v26_1/build.gradle.kts`
- [x] Create `strata-paper/build.gradle.kts`
- [x] Create `strata-migrate/build.gradle.kts`
- [x] Create `strata-starter/build.gradle.kts`
- [x] Verify `./gradlew build` compiles empty modules successfully

---

## Phase 2: API Module (`strata-api`)

> Pure interfaces, records, enums, and event definitions. Zero implementation. This is the developer-facing contract.

### 2.1 Core API
- [x] `com.protectcord.strata.api.StrataAPI` — Main entry point interface (registries, managers, pipeline accessor)
- [x] `com.protectcord.strata.api.StrataProvider` — Static accessor (`StrataProvider.get()`)
- [x] `com.protectcord.strata.api.NamespacedKey` — Tectonic's own namespaced key (not Bukkit's)
- [x] `com.protectcord.strata.api.Keyed` — Interface for registry entries
- [x] `com.protectcord.strata.api.Registry` — Generic registry interface (register, get, getAll, unregister, freeze)

### 2.2 Biome API
- [x] `com.protectcord.strata.api.biome.Biome` — Biome interface
- [x] `com.protectcord.strata.api.biome.BiomeBuilder` — Fluent builder for custom biomes
- [x] `com.protectcord.strata.api.biome.ClimateParameters` — Record: temperature, humidity, continentalness, erosion, weirdness
- [x] `com.protectcord.strata.api.biome.BiomeColors` — Record: fog, water, water_fog, grass_modifier, foliage
- [x] `com.protectcord.strata.api.biome.BiomeAmbience` — Record: particles, ambient sound, mood
- [x] `com.protectcord.strata.api.biome.BiomeCategory` — Enum: PLAINS, FOREST, DESERT, OCEAN, MOUNTAIN, TAIGA, SWAMP, JUNGLE, BADLANDS, BEACH, CAVE, NETHER, END, MUSHROOM, RIVER, CUSTOM
- [x] `com.protectcord.strata.api.biome.BiomeRegistry` — Registry&lt;Biome&gt;

### 2.3 Noise API
- [x] `com.protectcord.strata.api.noise.NoiseFunction` — Core noise interface (sample 2D/3D, composability: then, add, multiply, clamp, spline)
- [x] `com.protectcord.strata.api.noise.NoiseType` — Enum: SIMPLEX, PERLIN, OPEN_SIMPLEX2, CELLULAR, VALUE, RIDGED_MULTI, FRACTAL, DOMAIN_WARP, COMPOSITE
- [x] `com.protectcord.strata.api.noise.NoiseParameters` — Record: frequency, octaves, lacunarity, persistence, seed
- [x] `com.protectcord.strata.api.noise.CompositeNoise` — Builder for chained noise operations
- [x] `com.protectcord.strata.api.noise.NoiseRegistry` — Registry&lt;NoiseFunction&gt;

### 2.4 Terrain API
- [x] `com.protectcord.strata.api.terrain.DensityFunction` — Density function interface (evaluate at x,y,z)
- [x] `com.protectcord.strata.api.terrain.Spline` — Terrain height spline (control points, evaluate)
- [x] `com.protectcord.strata.api.terrain.TerrainSettings` — Record: base height, height variation, density modifier ref

### 2.5 Surface API
- [x] `com.protectcord.strata.api.surface.SurfaceRule` — Surface rule interface (apply conditions, output block)
- [x] `com.protectcord.strata.api.surface.SurfaceCondition` — Predicate: depth, Y-level, water proximity, slope, noise threshold, biome, block above
- [x] `com.protectcord.strata.api.surface.SurfaceRuleRegistry` — Registry&lt;SurfaceRule&gt;

### 2.6 Carver API
- [x] `com.protectcord.strata.api.carver.Carver` — Carver interface (carve method, shouldCarve, heightRange)
- [x] `com.protectcord.strata.api.carver.CarverContext` — Access to chunk data during carving
- [x] `com.protectcord.strata.api.carver.CarvingMask` — Bitmask of carved positions
- [x] `com.protectcord.strata.api.carver.CarverRegistry` — Registry&lt;Carver&gt;

### 2.7 Water API
- [x] `com.protectcord.strata.api.water.RiverSettings` — River generation parameters (threshold, width range, gradient smoothing, bed material rules)
- [x] `com.protectcord.strata.api.water.OceanSettings` — Ocean depth, shelf slope, trench noise, ridge noise, coral/kelp settings
- [x] `com.protectcord.strata.api.water.WaterfallSettings` — Gradient threshold, plunge pool size, mist effect toggle
- [x] `com.protectcord.strata.api.water.LakeSettings` — Min/max size, depth, bed materials, vegetation
- [x] `com.protectcord.strata.api.water.AquiferSettings` — 3D noise params, fluid type toggle, lava depth threshold

### 2.8 Structure API
- [x] `com.protectcord.strata.api.structure.StructureDefinition` — Structure interface (placement, type, adjustToTerrain)
- [x] `com.protectcord.strata.api.structure.StructurePlacement` — Record: grid spacing, offset, salt, biome filter, terrain check
- [x] `com.protectcord.strata.api.structure.StructureType` — Enum: JIGSAW, SCHEMATIC, PROCEDURAL
- [x] `com.protectcord.strata.api.structure.SchematicData` — Loaded schematic data
- [x] `com.protectcord.strata.api.structure.JigsawPool` — Jigsaw template pool
- [x] `com.protectcord.strata.api.structure.StructureRegistry` — Registry&lt;StructureDefinition&gt;

### 2.9 Feature API
- [x] `com.protectcord.strata.api.feature.Feature` — Feature interface (place method)
- [x] `com.protectcord.strata.api.feature.FeaturePlacement` — Feature + placement modifier chain
- [x] `com.protectcord.strata.api.feature.PlacementModifier` — Position modifier interface (count, rarity, height, biome, surface, noise)
- [x] `com.protectcord.strata.api.feature.FeatureStep` — Enum: RAW, ORES, UNDERGROUND_DECORATION, VEGETAL, SURFACE_DECORATION, FLUID_SPRINGS, TOP_LAYER
- [x] `com.protectcord.strata.api.feature.FeatureRegistry` — Registry&lt;Feature&gt;
- [x] `com.protectcord.strata.api.feature.SaplingRule` — Record: sapling type, allowed biomes, denied biomes, growth rate multiplier, tree variant override per biome
- [x] `com.protectcord.strata.api.feature.SaplingRegistry` — Registry of sapling→biome rules (controls what happens when a player plants a sapling)

### 2.10 Entity API
- [x] `com.protectcord.strata.api.entity.SpawnRule` — Record: mob type, weight, min/max group, conditions (light, block, time, height, structure)
- [x] `com.protectcord.strata.api.entity.SpawnCategory` — Enum: PASSIVE, HOSTILE, WATER_CREATURE, WATER_AMBIENT, AMBIENT, UNDERGROUND, AXOLOTL
- [x] `com.protectcord.strata.api.entity.EntitySpawnRegistry` — Spawn rule registry
- [x] `com.protectcord.strata.api.entity.SpawnZone` — Named spawn zone with bounds, biome filter, and difficulty scaling (controls vanilla mob density/types per zone)
- [x] `com.protectcord.strata.api.entity.MythicMobsHook` — Optional interface: if MythicMobs is present, zones can additionally reference MythicMobs mob tables (not required)

### 2.11 World API
- [x] `com.protectcord.strata.api.world.WorldProfile` — Complete world profile interface
- [x] `com.protectcord.strata.api.world.WorldManager` — Create/load/unload worlds, profile management
- [x] `com.protectcord.strata.api.world.StrataWorld` — Handle to a managed world
- [x] `com.protectcord.strata.api.world.WorldEnvironment` — Enum: NORMAL, NETHER, END, CUSTOM

### 2.12 Pipeline API
- [x] `com.protectcord.strata.api.pipeline.GenerationStage` — Stage interface (process ProtoChunk + context)
- [x] `com.protectcord.strata.api.pipeline.GenerationStageType` — Enum: INITIALIZATION, CONTINENTAL_SHAPE, CLIMATE_SAMPLING, BIOME_ASSIGNMENT, TERRAIN_SHAPING, AQUIFER_PLACEMENT, SURFACE_BUILDING, CARVING, WATER_SYSTEM, STRUCTURE_GENERATION, FEATURE_DECORATION, ENTITY_SPAWNING, LIGHTING, FINALIZATION
- [x] `com.protectcord.strata.api.pipeline.GenerationContext` — Per-chunk context (seed, profile, RNG, caches, chunk coord)
- [x] `com.protectcord.strata.api.pipeline.PipelineAccessor` — Runtime pipeline modification (replaceStage, wrapStage, insertBefore/After)
- [x] `com.protectcord.strata.api.pipeline.StageWrapper` — Decorator for stages

### 2.13 Event API
- [x] `com.protectcord.strata.api.event.StrataEvent` — Base event interface
- [x] `com.protectcord.strata.api.event.bus.EventBus` — Subscribe/publish interface
- [x] `com.protectcord.strata.api.event.bus.Subscribe` — Annotation for handler methods
- [x] `com.protectcord.strata.api.event.bus.EventPriority` — Enum: LOWEST, LOW, NORMAL, HIGH, HIGHEST, MONITOR
- [x] `com.protectcord.strata.api.event.generation.BiomeAssignmentEvent`
- [x] `com.protectcord.strata.api.event.generation.TerrainShapingEvent`
- [x] `com.protectcord.strata.api.event.generation.SurfaceBuildEvent`
- [x] `com.protectcord.strata.api.event.generation.CarverEvent`
- [x] `com.protectcord.strata.api.event.generation.WaterSystemEvent`
- [x] `com.protectcord.strata.api.event.generation.StructureStartEvent`
- [x] `com.protectcord.strata.api.event.generation.FeatureDecorationEvent`
- [x] `com.protectcord.strata.api.event.generation.EntitySpawnRuleEvent`
- [x] `com.protectcord.strata.api.event.generation.ChunkCompleteEvent`
- [x] `com.protectcord.strata.api.event.lifecycle.ProfileLoadEvent`
- [x] `com.protectcord.strata.api.event.lifecycle.ProfileReloadEvent`
- [x] `com.protectcord.strata.api.event.lifecycle.WorldCreateEvent`
- [x] `com.protectcord.strata.api.event.lifecycle.WorldUnloadEvent`
- [x] `com.protectcord.strata.api.event.lifecycle.ConfigReloadEvent`

### 2.14 Block & Chunk API
- [x] `com.protectcord.strata.api.block.StrataBlockState` — Record: namespace, key, properties map
- [x] `com.protectcord.strata.api.block.BlockPalette` — Compressed block storage interface
- [x] `com.protectcord.strata.api.chunk.ChunkCoord` — Record: x, z
- [x] `com.protectcord.strata.api.chunk.ProtoChunkAccess` — Read/write interface to generation chunk
- [x] `com.protectcord.strata.api.chunk.BlockAccess` — Get/set blocks interface
- [x] `com.protectcord.strata.api.chunk.BiomeAccess` — Get/set biomes interface

---

## Phase 3: Noise Module (`strata-noise`)

> Standalone math library. No Minecraft dependencies. All noise algorithms, fractal stacking, domain warping, caching.

### 3.1 Base Noise Algorithms
- [x] `com.protectcord.strata.noise.SimplexNoise` — Simplex noise implementation
- [x] `com.protectcord.strata.noise.PerlinNoise` — Perlin noise implementation
- [x] `com.protectcord.strata.noise.OpenSimplex2Noise` — OpenSimplex2 implementation (improved simplex variant)
- [x] `com.protectcord.strata.noise.CellularNoise` — Voronoi/Worley cellular noise
- [x] `com.protectcord.strata.noise.ValueNoise` — Value noise implementation
- [x] `com.protectcord.strata.noise.RidgedMultifractalNoise` — Ridged multifractal noise

### 3.2 Noise Combinators
- [x] `com.protectcord.strata.noise.FractalNoise` — Octave stacking for any base noise type
- [x] `com.protectcord.strata.noise.DomainWarping` — Distort input coordinates using another noise
- [x] `com.protectcord.strata.noise.InterpolatedNoise` — Coarse grid sampling + trilinear interpolation

### 3.3 Composite Noise System
- [x] `com.protectcord.strata.noise.composite.CompositeNoiseBuilder` — Fluent API for chaining operations
- [x] `com.protectcord.strata.noise.composite.NoiseOperation` — Enum: ABS, NEGATE, ADD, MULTIPLY, CLAMP, SPLINE, LERP, MIN, MAX, POWER, THRESHOLD
- [x] `com.protectcord.strata.noise.composite.NoiseGraph` — DAG of noise functions compiled from config

### 3.4 Math Utilities
- [x] `com.protectcord.strata.noise.math.MathUtil` — lerp, inverseLerp, clamp, smoothstep, fade, floor
- [x] `com.protectcord.strata.noise.math.SplineInterpolator` — Cubic spline evaluation
- [x] `com.protectcord.strata.noise.math.HashUtil` — Fast hash functions for noise seeding (xxHash-style)

### 3.5 Noise Caching
- [x] `com.protectcord.strata.noise.cache.NoiseCache` — Per-chunk noise value cache
- [x] `com.protectcord.strata.noise.cache.NoiseCacheRegion` — Cross-chunk LRU cache (Caffeine-backed)
- [x] `com.protectcord.strata.noise.cache.InterpolationGrid` — Coarse sample grid with trilinear interpolation lookup

---

## Phase 4: Config Module (`strata-config`)

> TOML-based configuration system with schema validation, hot-reload, profile inheritance. NOT YAML.

### 4.1 TOML Parsing
- [x] `com.protectcord.strata.config.toml.TomlReader` — Wraps TOMLy parser, returns typed maps
- [x] `com.protectcord.strata.config.toml.TomlSchema` — Schema definition from Java record annotations
- [x] `com.protectcord.strata.config.toml.TomlValidationError` — Error with file path, line number, key, expected type, actual value, message

### 4.2 Profile Loading
- [x] `com.protectcord.strata.config.ProfileLoader` — Discovers profile directories, parses `profile.toml`, builds WorldProfile objects
- [x] `com.protectcord.strata.config.ProfileValidator` — Validates entire profile against schema, reports all errors with human-readable messages and fix suggestions
- [x] `com.protectcord.strata.config.ConfigRegistry` — Central registry of all loaded profiles, biomes, noise functions, structures
- [x] `com.protectcord.strata.config.ConfigMerger` — Profile inheritance (`extends = "default-overworld"`), deep per-key merge
- [x] `com.protectcord.strata.config.ConfigExporter` — Serializes profiles/definitions to JSON for backup/sharing
- [x] `com.protectcord.strata.config.ConfigDocGenerator` — Generates fully-commented template TOML files from schema annotations. Every key gets a comment explaining what it does, valid values, default, and example.

### 4.3 Hot-Reload
- [x] `com.protectcord.strata.config.reload.FileWatcher` — NIO WatchService wrapper, detects file modifications
- [x] `com.protectcord.strata.config.reload.ReloadCoordinator` — Re-parses changed file, validates, swaps in registry, fires ConfigReloadEvent

### 4.4 Config Models (parsed TOML → Java records)
- [x] `com.protectcord.strata.config.model.ProfileConfig` — Profile metadata, world settings, file references
- [x] `com.protectcord.strata.config.model.BiomeConfig` — Biome definition: climate, terrain, surface, features, spawning, colors
- [x] `com.protectcord.strata.config.model.NoiseConfig` — Named noise function definition: type, params, composite operations
- [x] `com.protectcord.strata.config.model.TerrainConfig` — Density function graph, spline control points, continental params
- [x] `com.protectcord.strata.config.model.SurfaceConfig` — Surface rule chains with conditions
- [x] `com.protectcord.strata.config.model.CarverConfig` — Cave/ravine carver parameters
- [x] `com.protectcord.strata.config.model.WaterConfig` — Ocean, river, lake, waterfall, aquifer parameters
- [x] `com.protectcord.strata.config.model.StructureConfig` — Structure placement, type, schematic/jigsaw/procedural params
- [x] `com.protectcord.strata.config.model.FeatureConfig` — Feature definitions: ores, trees, vegetation, springs, top layer
- [x] `com.protectcord.strata.config.model.SaplingConfig` — Per-biome sapling rules: allowed/denied types, growth rate modifiers, tree shape variants
- [x] `com.protectcord.strata.config.model.EntityConfig` — Spawn rules per biome: mob type, weight, group size, conditions
- [x] `com.protectcord.strata.config.model.SpawnZoneConfig` — Named zones with difficulty scaling, biome filter, vanilla mob tables; optional MythicMobs table refs

---

## Phase 5: Core Engine (`strata-core`)

> The world generation engine. Platform-agnostic (no Bukkit imports). All pipeline stages.

### 5.1 Engine & Pipeline
- [x] `com.protectcord.strata.core.StrataEngine` — Main engine class, owns pipeline, initializes from WorldProfile
- [x] `com.protectcord.strata.core.pipeline.GenerationPipeline` — Ordered list of stages, executes them sequentially on ProtoChunk
- [x] `com.protectcord.strata.core.pipeline.PipelineBuilder` — Builds pipeline from WorldProfile (resolves stage config references)

### 5.2 Pipeline Stages (14 stages)
- [x] `com.protectcord.strata.core.pipeline.stage.InitializationStage` — Allocate ProtoChunk, resolve profile, seed RNG
- [x] `com.protectcord.strata.core.pipeline.stage.ContinentalShapeStage` — Sample continentalness noise, land/ocean/coast classification
- [x] `com.protectcord.strata.core.pipeline.stage.ClimateSamplingStage` — Sample temperature, humidity, erosion, weirdness noise
- [x] `com.protectcord.strata.core.pipeline.stage.BiomeAssignmentStage` — KD-tree climate→biome lookup at 4x4x4 resolution, edge blending
- [x] `com.protectcord.strata.core.pipeline.stage.TerrainShapingStage` — Density function evaluation, 3D terrain shape (stone vs air)
- [x] `com.protectcord.strata.core.pipeline.stage.AquiferPlacementStage` — Underground water/lava pockets from 3D noise
- [x] `com.protectcord.strata.core.pipeline.stage.SurfaceBuildingStage` — Apply biome surface rules (grass, sand, gravel, etc.)
- [x] `com.protectcord.strata.core.pipeline.stage.CarvingStage` — Execute all carvers (cheese, spaghetti, noodle, ravine, custom)
- [x] `com.protectcord.strata.core.pipeline.stage.WaterSystemStage` — Rivers, waterfalls, lakes, ocean features, swamp water
- [x] `com.protectcord.strata.core.pipeline.stage.StructureGenerationStage` — Structure starts + piece placement
- [x] `com.protectcord.strata.core.pipeline.stage.FeatureDecorationStage` — Ores, trees, vegetation, springs, snow/ice
- [x] `com.protectcord.strata.core.pipeline.stage.EntitySpawningStage` — Write per-biome spawn tables to chunk metadata
- [x] `com.protectcord.strata.core.pipeline.stage.LightingStage` — Sky light + block light propagation
- [x] `com.protectcord.strata.core.pipeline.stage.FinalizationStage` — Convert ProtoChunk, commit heightmaps, fire ChunkCompleteEvent

### 5.3 Biome System
- [x] `com.protectcord.strata.core.biome.BiomeLookupTable` — KD-tree mapping 5D climate parameters → Biome
- [x] `com.protectcord.strata.core.biome.BiomeBlender` — Weighted interpolation for smooth biome transitions
- [x] `com.protectcord.strata.core.biome.ClimateMapper` — Samples noise functions → ClimateParameters per column

### 5.4 Terrain System
- [x] `com.protectcord.strata.core.terrain.DensityEngine` — Evaluates composable DensityFunction graphs at each (x,y,z)
- [x] `com.protectcord.strata.core.terrain.SplineEvaluator` — Evaluates terrain height splines keyed by (continentalness, erosion, weirdness)
- [x] `com.protectcord.strata.core.terrain.TerrainModifiers` — Per-biome terrain modifiers (mesa plateaus, mountain jaggedness, etc.)

### 5.5 Surface System
- [x] `com.protectcord.strata.core.surface.SurfaceRuleEngine` — Evaluates surface rule condition chains, outputs block states
- [x] `com.protectcord.strata.core.surface.SlopeSampler` — Computes surface slope for steep-terrain rules

### 5.6 Carver System
- [x] `com.protectcord.strata.core.carver.CarverEngine` — Orchestrates all carvers in order
- [x] `com.protectcord.strata.core.carver.CheeseCaveCarver` — Large open chambers (3D low-frequency noise)
- [x] `com.protectcord.strata.core.carver.SpaghettiCaveCarver` — Long winding tunnels (2D worm paths + 3D width noise)
- [x] `com.protectcord.strata.core.carver.NoodleCaveCarver` — Thin crevices
- [x] `com.protectcord.strata.core.carver.RavineCarver` — Surface-cutting canyons

### 5.7 Water System (Marquee Feature)
- [x] `com.protectcord.strata.core.water.RiverNetworkBuilder` — Macro-scale flow path computation (drainage basins, flow accumulation)
- [x] `com.protectcord.strata.core.water.RiverNetwork` — Spatial index (R-tree) of river segments
- [x] `com.protectcord.strata.core.water.RiverCarver` — Per-chunk river channel carving (parabolic cross-section, bed material, banks)
- [x] `com.protectcord.strata.core.water.WaterfallDetector` — Identifies steep gradient segments along rivers
- [x] `com.protectcord.strata.core.water.WaterfallBuilder` — Carves vertical face, plunge pool, mist effect
- [x] `com.protectcord.strata.core.water.LakeDetector` — Finds terrain local minima for lake placement
- [x] `com.protectcord.strata.core.water.LakeFiller` — Basin-filling algorithm with overflow detection
- [x] `com.protectcord.strata.core.water.OceanFloorBuilder` — Continental shelf, slope, abyssal plain, ridges, trenches, seamounts
- [x] `com.protectcord.strata.core.water.CoralReefGenerator` — Poisson-disk seeded L-system coral clusters
- [x] `com.protectcord.strata.core.water.KelpForestGenerator` — Poisson-disk spaced kelp columns with noise height
- [x] `com.protectcord.strata.core.water.AquiferEngine` — 3D aquifer boundary noise, per-pocket water levels, lava below threshold
- [x] `com.protectcord.strata.core.water.SwampWaterHandler` — Raised water table, shallow pools, mud, lily pads

### 5.8 Structure System
- [x] `com.protectcord.strata.core.structure.StructurePlacementEngine` — Grid spacing + salt + biome filter for structure starts
- [x] `com.protectcord.strata.core.structure.JigsawAssembler` — Jigsaw piece assembly (depth-limited recursive placement, configurable max pieces for larger villages)
- [x] `com.protectcord.strata.core.structure.SchematicPlacer` — Paste schematic data into chunks
- [x] `com.protectcord.strata.core.structure.ProceduralStructureEngine` — Runtime-generated structures via API callbacks
- [x] `com.protectcord.strata.core.structure.VillageGenerator` — Enhanced village generation: larger piece pools, functional farm plots, animal pens, roads between buildings, terrain flattening under footprint
- [x] `com.protectcord.strata.core.structure.FarmGenerator` — Standalone farm placement: biome-appropriate crop selection, irrigated field layouts, animal pen templates, tool shed loot tables
- [x] `com.protectcord.strata.core.structure.RoadNetworkBuilder` — Connects nearby villages/structures with terrain-following paths, bridges over water/ravines, signpost placement at intersections

### 5.9 Feature System
- [x] `com.protectcord.strata.core.feature.FeatureEngine` — Orchestrates feature steps per biome
- [x] `com.protectcord.strata.core.feature.OreGenerator` — Ore vein placement (configurable size, count, height, discard chance)
- [x] `com.protectcord.strata.core.feature.TreeGenerator` — Tree placement (oak, birch, spruce, jungle, acacia, dark oak, cherry, mangrove, azalea + custom)
- [x] `com.protectcord.strata.core.feature.SaplingBiomeHandler` — Biome-aware sapling growth: controls which trees can grow in which biomes, growth rate modifiers, and tree shape variants per biome
- [x] `com.protectcord.strata.core.feature.VegetationGenerator` — Grass, flowers, bushes, cacti, sugar cane, bamboo, etc.
- [x] `com.protectcord.strata.core.feature.FluidSpringPlacer` — Water/lava source blocks in cave walls
- [x] `com.protectcord.strata.core.feature.SnowIcePlacer` — Temperature-based snow, ice, frosted surfaces

### 5.10 Entity System
- [x] `com.protectcord.strata.core.entity.SpawnTableBuilder` — Builds per-biome spawn tables from config, writes to chunk metadata

### 5.11 Chunk Internals
- [x] `com.protectcord.strata.core.chunk.StrataProtoChunk` — Internal mutable chunk (palette-compressed block storage)
- [x] `com.protectcord.strata.core.chunk.ProtoChunkPool` — Thread-local object pool for reuse (reduce GC pressure)
- [x] `com.protectcord.strata.core.chunk.ChunkRegionAccessor` — Lazy neighbor chunk access for cross-chunk reads
- [x] `com.protectcord.strata.core.chunk.PaletteStorage` — Palette-compressed block storage (like vanilla chunk sections)

### 5.12 Pre-generation
- [x] `com.protectcord.strata.core.pregen.PreGenerationTask` — Spiral pre-gen from spawn, progress reporting, pause/resume/cancel
- [x] `com.protectcord.strata.core.pregen.PreGenScheduler` — ForkJoinPool management, configurable thread count, batch disk saves

---

## Phase 6: NMS Abstraction + Adapters

> Version compatibility layer. ServiceLoader-based adapter loading. Covers 1.8.8 through 26.1.2.

### 6.1 Common Abstraction (`strata-nms/common`)
- [x] `com.protectcord.strata.nms.NMSAdapter` — Main adapter interface (all sub-interfaces + version info)
- [x] `com.protectcord.strata.nms.NMSVersion` — Enum of supported version groups
- [x] `com.protectcord.strata.nms.VersionDetector` — Runtime version detection (Paper API + CraftBukkit package fallback)
- [x] `com.protectcord.strata.nms.adapter.ChunkAccessor` — Chunk read/write abstraction (handles section layout differences)
- [x] `com.protectcord.strata.nms.adapter.BlockStateMapper` — StrataBlockState ↔ NMS block state translation
- [x] `com.protectcord.strata.nms.adapter.BiomeInjector` — Biome writing (2D pre-1.16, 3D 1.16+, PalettedContainer 1.18+)
- [x] `com.protectcord.strata.nms.adapter.LightingEngine` — Light level computation abstraction
- [x] `com.protectcord.strata.nms.adapter.HeightmapWriter` — Heightmap writing (WORLD_SURFACE, OCEAN_FLOOR, MOTION_BLOCKING, etc.)
- [x] `com.protectcord.strata.nms.adapter.WorldHandle` — World-level operations abstraction
- [x] `com.protectcord.strata.nms.legacy.LegacyBlockTable` — Pre-1.13 numeric ID ↔ namespaced key bidirectional lookup
- [x] `com.protectcord.strata.nms.legacy.BlockSubstitution` — Missing block fallback table (modern blocks → closest legacy equivalent)
- [x] `src/main/resources/legacy_block_ids.json` — Bundled pre-1.13 block ID mapping data

### 6.2 Version Adapters
- [x] `strata-nms/v1_8_R3` — Adapter for 1.8.4-1.8.9 (Java 8 cross-compile, legacy block IDs, 256 height, 2D biomes)
- [x] `strata-nms/v1_12_R1` — Adapter for 1.9-1.12.2 (Java 8 cross-compile, legacy IDs, API differences)
- [x] `strata-nms/v1_13_R2` — Adapter for 1.13-1.15.2 (Java 8 cross-compile, post-flattening, namespaced IDs, 2D biomes)
- [x] `strata-nms/v1_16_R3` — Adapter for 1.16-1.16.5 (Java 11 cross-compile, 3D biomes, nether gen changes)
- [x] `strata-nms/v1_17_R1` — Adapter for 1.17-1.17.1 (Java 16 cross-compile, unversioned NMS packages)
- [x] `strata-nms/v1_18_R2` — Adapter for 1.18-1.19.3 (Java 17 cross-compile, extended height -64 to 320, aquifers)
- [x] `strata-nms/v1_19_R3` — Adapter for 1.19.4-1.20.4 (Java 17 cross-compile, BiomeProvider API)
- [x] `strata-nms/v1_20_R4` — Adapter for 1.20.5-1.21.x (Java 21 cross-compile, Mojang-mapped runtime)
- [x] `strata-nms/v26_1` — Adapter for 26.1-26.1.2+ (Java 25, new versioning, no Spigot remapping)
- [x] ServiceLoader `META-INF/services/com.protectcord.strata.nms.NMSAdapter` registration for each adapter

---

## Phase 7: Paper Plugin (`strata-paper`)

> PaperMC plugin entry point. Commands, listeners, ChunkGenerator/BiomeProvider, API implementation.

### 7.1 Plugin Core
- [x] `com.protectcord.strata.paper.StrataPlugin` — JavaPlugin entry point (onEnable, onDisable, lifecycle, first-run setup)
- [x] `src/main/resources/plugin.yml` — Plugin descriptor (name: Strata, commands, permissions)
- [x] `src/main/resources/paper-plugin.yml` — Paper-specific descriptor (for 1.19.4+)
- [x] First-run experience: on first startup, extract default profiles, generate `strata.toml` with guided comments, log a welcome message with getting-started steps to console

### 7.2 Generator Integration
- [x] `com.protectcord.strata.paper.generator.StrataChunkGenerator` — Extends ChunkGenerator, delegates to strata-core pipeline
- [x] `com.protectcord.strata.paper.generator.StrataBiomeProvider` — Extends BiomeProvider (1.19.4+), provides biome data from core

### 7.3 World Management
- [x] `com.protectcord.strata.paper.world.PaperWorldManager` — Implements WorldManager using Bukkit API (create, load, unload worlds)
- [x] `com.protectcord.strata.paper.world.WorldConfigStore` — Persistent world↔profile mappings (survives server restart)

### 7.4 Commands
- [x] `com.protectcord.strata.paper.command.StrataCommand` — `/strata` root command with tab completion
- [x] `com.protectcord.strata.paper.command.CreateWorldCommand` — `/strata create <name> <profile> [seed]`
- [x] `com.protectcord.strata.paper.command.ReloadCommand` — `/strata reload [profile]`
- [x] `com.protectcord.strata.paper.command.PreGenCommand` — `/strata pregen <world> <radius> [threads]`
- [x] `com.protectcord.strata.paper.command.InfoCommand` — `/strata info [world]` (shows profile, biome count, generation stats)
- [x] `com.protectcord.strata.paper.command.ProfileListCommand` — `/strata profiles` (lists available profiles)
- [x] `com.protectcord.strata.paper.command.MigrateCommand` — `/strata migrate <terra-pack-path> [output-name]`
- [x] `com.protectcord.strata.paper.command.GuideCommand` — `/strata guide [topic]` — In-game interactive guide with clickable pages (Adventure components). Topics: setup, profiles, biomes, noise, structures, features, ores, saplings, zones, api, troubleshooting
- [x] `com.protectcord.strata.paper.command.BiomeInfoCommand` — `/strata biome` — Shows current biome name, zone, config file path, mob spawns for the biome the player is standing in

### 7.5 Listeners
- [x] `com.protectcord.strata.paper.listener.WorldLoadListener` — Attaches Strata generator on world load
- [x] `com.protectcord.strata.paper.listener.ChunkLoadListener` — Optional monitoring/metrics for chunk generation
- [x] `com.protectcord.strata.paper.listener.SaplingGrowthListener` — Intercepts `StructureGrowEvent` / sapling growth, applies biome rules: deny growth in wrong biomes, modify growth rate, substitute tree variant based on biome

### 7.6 NMS Adapter Loading
- [x] `com.protectcord.strata.paper.adapter.NMSAdapterLoader` — ServiceLoader-based version detection and adapter selection

### 7.7 API Implementation
- [x] `com.protectcord.strata.paper.api.StrataAPIImpl` — Implements StrataAPI, wires registries, managers, pipeline
- [x] `com.protectcord.strata.paper.api.StrataProviderBootstrap` — Sets StrataProvider.get() at plugin enable
- [x] `com.protectcord.strata.paper.api.EventBusImpl` — Implements EventBus (concurrent subscriber list, priority ordering)

### 7.8 bStats Metrics
- [x] `com.protectcord.strata.paper.metrics.StrataMetrics` — bStats integration (shaded into jar)
- [x] Custom charts: active worlds count, profiles in use, total biomes loaded, MC version, Java version, avg chunk gen time, NMS adapter in use
- [x] Opt-out respects bStats global config (`plugins/bStats/config.yml`)
- [x] bStats plugin ID registered at bstats.org (ID: 31423)

### 7.9 In-Game Guide System
- [x] `com.protectcord.strata.paper.guide.GuideRegistry` — Loads guide pages from bundled resources, supports pagination
- [x] `com.protectcord.strata.paper.guide.GuidePage` — Single guide page: title, content (Adventure components), clickable links to related topics
- [x] `com.protectcord.strata.paper.guide.GuideRenderer` — Renders guide pages in chat with headers, color coding, hover tooltips, click-to-run commands
- [x] Guide topics (bundled as resources):
  - [x] `setup` — Step-by-step: install jar → first run → create world → play. Aimed at complete beginners.
  - [x] `profiles` — What profiles are, how to switch, how to create custom ones, `extends` inheritance
  - [x] `biomes` — How biomes work, how to customize, climate parameters explained, example TOML
  - [x] `noise` — Noise functions explained simply (what they do visually), how to compose them, example TOML
  - [x] `structures` — How structures work, jigsaw vs schematic, how to add custom structures
  - [x] `features` — Ores, trees, vegetation explained. How to adjust rates. Example TOML.
  - [x] `ores` — Ore distribution explained, how to adjust scarcity, per-biome overrides
  - [x] `saplings` — Sapling biome rules explained, how to configure allowed biomes and growth rates
  - [x] `zones` — Zone system explained, how to define zones, difficulty scaling, MythicMobs optional hook
  - [x] `water` — Rivers, oceans, waterfalls, lakes, aquifers — how each works and how to configure
  - [x] `api` — Quick start for developers: depend on strata-api, register custom biome, listen to events
  - [x] `troubleshooting` — Common issues: world not generating, config errors, performance, version mismatches
- [x] `/strata guide` with no args shows topic index with clickable links
- [x] Each guide page links to related pages (e.g., "biomes" links to "noise", "ores", "saplings")

### 7.10 Optional Hooks
- [x] `com.protectcord.strata.paper.hook.MythicMobsHookImpl` — If MythicMobs detected at runtime, allows zones to reference MythicMobs mob tables alongside vanilla spawns
- [x] Soft dependency in plugin.yml (`softdepend: [MythicMobs]`) — purely optional, plugin is fully functional without it

### 7.11 Build
- [x] Shadow jar configuration (bundles all modules + bStats into single distributable plugin jar)
- [x] Resource filtering for plugin.yml (inject version, name at build time)

---

## Phase 8: Terra Migration Module (`strata-migrate`)

> Import Terra YAML config packs and convert them to Strata TOML profiles. In-game command + standalone CLI.

### 8.1 Terra Pack Parsing
- [x] `com.protectcord.strata.migrate.TerraPackParser` — Reads Terra pack directory structure, identifies all YAML config files
- [x] `com.protectcord.strata.migrate.terra.TerraPackManifest` — Parses Terra's `pack.yml` (pack metadata, version, author)
- [x] `com.protectcord.strata.migrate.terra.TerraBiomeParser` — Parses Terra biome YAML (climate, palette refs, flora refs, terrain)
- [x] `com.protectcord.strata.migrate.terra.TerraNoiseParser` — Parses Terra noise config (noise-equation expressions, sampler configs)
- [x] `com.protectcord.strata.migrate.terra.TerraPaletteParser` — Parses Terra palette YAML (block layer definitions)
- [x] `com.protectcord.strata.migrate.terra.TerraFloraParser` — Parses Terra flora/tree YAML (placement, block types)
- [x] `com.protectcord.strata.migrate.terra.TerraStructureParser` — Parses Terra structure definitions
- [x] `com.protectcord.strata.migrate.terra.TerraCarverParser` — Parses Terra carver YAML
- [x] `com.protectcord.strata.migrate.terra.TerraScriptParser` — Parses TerraScript files (best-effort AST analysis)

### 8.2 Converters (Terra → Strata)
- [x] `com.protectcord.strata.migrate.converter.BiomeConverter` — Terra biome YAML → Strata biome TOML
- [x] `com.protectcord.strata.migrate.converter.NoiseConverter` — Terra noise expressions → Strata noise function TOML
- [x] `com.protectcord.strata.migrate.converter.PaletteConverter` — Terra palettes → Strata surface rules TOML
- [x] `com.protectcord.strata.migrate.converter.FloraConverter` — Terra flora/trees → Strata feature TOML
- [x] `com.protectcord.strata.migrate.converter.StructureConverter` — Terra structures → Strata structure TOML
- [x] `com.protectcord.strata.migrate.converter.CarverConverter` — Terra carvers → Strata carver TOML
- [x] `com.protectcord.strata.migrate.converter.ScriptTranslator` — TerraScript → Strata schematic + config (best-effort, flags unsupported)
- [x] `com.protectcord.strata.migrate.converter.ProfileAssembler` — Assembles converted components into complete Strata profile

### 8.3 Reporting & Validation
- [x] `com.protectcord.strata.migrate.MigrationReport` — Tracks: converted, approximated, unsupported, warnings, errors
- [x] `com.protectcord.strata.migrate.MigrationValidator` — Runs generated profile through Strata's config validator
- [x] Report output: human-readable text file + JSON machine-readable summary

### 8.4 Entry Points
- [x] In-game command integration (registered in strata-paper's MigrateCommand)
- [x] `com.protectcord.strata.migrate.cli.MigrateCLI` — Standalone `main()` for offline use: `java -jar strata-migrate.jar <terra-pack> <output-dir>`
- [x] Standalone jar build configuration in `strata-migrate/build.gradle.kts`

---

## Phase 9: Starter Pack (`strata-starter`)

> Survival-focused, MMORPG-themed default profiles. Beautiful terrain, great for base building, exploration, deep cave systems, and MythicMobs-ready spawn zones. These profiles are the showcase — they prove what Strata can do out of the box.
>
> **Config Documentation Principle:** Every single TOML file shipped in the starter pack must be **heavily commented**. Every key has a comment above it explaining what it does, what values are valid, what the default is, and a brief example of why you'd change it. A new user should be able to open any config file, read the comments, and understand exactly what to change without ever leaving the file. Advanced users get full power; beginners get full guidance. No config key is left unexplained.

### 9.1 Flagship Profile: "Elysium" (MMORPG Survival Overworld)

> The main attraction. A massive, explorable overworld with MMORPG-style zone tiering, stunning landscapes for base building, rivers that flow to oceans, deep multi-layered cave systems, and MythicMobs-ready spawn zones. Every biome is handcrafted to feel rewarding to explore.

#### 9.1.1 Profile & World Config
- [x] `profiles/elysium/profile.toml` — Profile metadata, author: SneakyWrld, MMORPG zone system enabled
- [x] Terrain config: `density.toml`, `splines.toml`, `continents.toml`
- [x] Surface config: `rules.toml`
- [x] Noise config: `functions.toml`
- [x] Carver config: `caves.toml`, `ravines.toml`
- [x] Water config: `oceans.toml`, `rivers.toml`, `lakes.toml`, `aquifers.toml`
- [x] Feature config: `ores.toml`, `trees.toml`, `vegetation.toml`, `saplings.toml`
- [x] Entity config: `spawning.toml`
- [x] Spawn zones config: `zones.toml` — Zone definitions with vanilla mob tables and difficulty scaling

#### 9.1.2 Zone System (Difficulty Tiering)
Zones radiate outward from spawn, increasing in difficulty. Each zone has distinct biome palettes, terrain character, vanilla mob tables with scaled density, and optional MythicMobs table references.

- [x] **Sanctuary (Spawn Zone)** — Safe starting area. Gentle rolling plains, flower meadows, shallow streams, scattered oak/birch groves. Perfect flat-ish terrain for first bases. Passive mobs dominate. Warm, welcoming color palette. Reduced hostile spawn rates.
- [x] **Verdant Expanse** — Lush forests, open meadows, river valleys. Classic survival biomes: birch forests, sunflower plains, gentle hills. Standard hostile mobs at night. Great farming land, abundant wood. Rivers with lily pads and sugar cane.
- [x] **Whispering Woodlands** — Dense old-growth forests, dark forest canopies, mossy ravines. Visibility drops under the canopy. Forest temples, overgrown ruins. Higher spider/witch spawns. Atmospheric fog in valleys.
- [x] **Stormbreak Coast** — Dramatic coastline: sea cliffs, tidal caves, rocky beaches, coral coves. Coastal villages on stilts. Ocean monuments offshore. Drowned spawns in tidal caves. Stunning ocean views for clifftop bases.
- [x] **Scorched Badlands** — Mesa plateaus, terracotta canyons, dried riverbeds, sparse dead trees. Extreme heat theme. Mineshafts exposed in canyon walls. Desert temples. Husk-heavy spawns. Rich ore veins in canyon walls.
- [x] **Frostfang Peaks** — Towering snow-capped mountains, frozen waterfalls, ice caves, alpine meadows at treeline. Goat paths along ridges. Igloos, frozen outposts. Stray spawns, powdered snow traps. Breathtaking mountain base locations.
- [x] **Shadowmire Swamps** — Murky mangrove swamps, bubbling mud pools, dead trees, eerie fog. Witch huts, ruined bridges. Raised water table, shallow pools everywhere. Slime, phantom, witch spawns. Challenging terrain for building.
- [x] **Embercrag Volcanic** — Active volcanic terrain: basalt columns, lava rivers, obsidian fields, ash-covered forests. Ruined nether portals scattered naturally. Magma cubes, strays, high mob density. Rare ores near lava flows.
- [x] **Abyssal Depths** — Deep underground zone (Y -64 to 0). Massive cave networks: cheese caves with underground lakes, crystal grottos, sculk-infested deep dark zones, lava chambers. Warden territory. Ancient city structures. Deepslate and tuff variety.
- [x] **The Shattered Reach** — Endgame zone at the world's edge. Floating island fragments, gravity-defying terrain, ender-touched corruption spreading through the land. End gateway ruins. Endermen, shulker spawns. The most dangerous and visually spectacular biomes.

#### 9.1.3 Biomes — Sanctuary Zone (12 biomes)
- [x] `sanctuary_meadow` — Soft grass, wildflowers, gentle rolling hills, scattered apple trees
- [x] `sanctuary_grove` — Small oak/birch grove with dappled sunlight, berry bushes, rabbits
- [x] `sanctuary_pond` — Peaceful pond with lily pads, clay bottom, dragonflies (particles), sugar cane
- [x] `sanctuary_farmland` — Pre-cleared flat terrain with rich soil (coarse dirt patches), ideal first base
- [x] `sanctuary_orchard` — Rows of oak and cherry trees on gentle slopes, bee nests, flowers
- [x] `sanctuary_wildflower_field` — Dense mixed flowers (poppies, dandelions, cornflowers, alliums), beehives
- [x] `sanctuary_birch_park` — Open birch woodland, tall grass, paths of coarse dirt, park-like spacing
- [x] `sanctuary_sunlit_clearing` — Wide open clearing surrounded by trees, campfire smoke particles, perfect build site
- [x] `sanctuary_stream` — Narrow shallow stream with sand/gravel bed, stepping stones, berry bushes along banks
- [x] `sanctuary_hillside` — Gentle grassy slopes with exposed stone patches, sheep, views over the meadow
- [x] `sanctuary_lavender_field` — Rolling purple-tinted fields (allium-heavy), bees, aromatic theme
- [x] `sanctuary_mushroom_hollow` — Sheltered depression with brown/red mushrooms, mycelium patch, mooshroom spawn

#### 9.1.4 Biomes — Verdant Expanse (16 biomes)
- [x] `verdant_plains` — Wide open grasslands, sunflowers, cow/sheep herds, great for farming
- [x] `verdant_forest` — Mixed oak/birch, mushroom patches, clearings ideal for base building
- [x] `verdant_river_valley` — Wide river with sandy banks, sugar cane, lily pads, fishing spots
- [x] `verdant_sunflower_fields` — Endless sunflower plains, bee colonies, golden color palette
- [x] `verdant_rolling_hills` — Gentle terrain undulation, scattered trees, panoramic views, exposed coal
- [x] `verdant_oak_woodland` — Dense oak forest with thick canopy, fallen logs, fern undergrowth
- [x] `verdant_birch_highlands` — Tall birch on elevated terrain, wildflowers beneath, light and airy feel
- [x] `verdant_flower_valley` — Lush valley between hills, dense mixed flowers, butterflies (particles), beehives
- [x] `verdant_bamboo_thicket` — Dense bamboo groves, panda spawns, jungle-like lushness in temperate zone
- [x] `verdant_pumpkin_patch` — Flat terrain with scattered pumpkins, hay bales, scarecrow structures (armor stands)
- [x] `verdant_apple_orchard` — Dense apple-dropping oak trees on flat ground, leaf litter, harvest theme
- [x] `verdant_clover_field` — Short grass plains with dense flower ground cover, rabbits, gentle wind
- [x] `verdant_wetland` — Shallow marshy area at river edges, reeds (sugar cane), frogs, clay deposits
- [x] `verdant_old_growth_oak` — Massive 2x2 oak trees, thick root networks (root blocks), canopy gaps
- [x] `verdant_cherry_blossom` — Cherry trees with pink petal particles, soft grass, romantic atmosphere
- [x] `verdant_pastoral_ridge` — Long ridge with grazing animals both sides, shepherd path, wind-swept grass

#### 9.1.5 Biomes — Whispering Woodlands (14 biomes)
- [x] `whispering_dark_forest` — Tall dark oak canopy, thick undergrowth, mushrooms, near-zero light at ground
- [x] `whispering_ancient_woods` — Giant 2x2 spruce/oak, hanging vines, mossy boulders, ruins peeking through
- [x] `whispering_mossy_ravine` — Deep narrow ravine with moss carpet, dripleaf, glow berries, hidden cave entrances
- [x] `whispering_fern_gully` — Dense fern-covered valley floor, damp atmosphere, cave spider dens
- [x] `whispering_twilight_canopy` — Extremely dense canopy blocks all sky light, permanent darkness, hostile spawns 24/7
- [x] `whispering_mushroom_thicket` — Giant brown/red mushrooms mixed with dark oak, mycelium floor patches
- [x] `whispering_overgrown_ruins` — Stone brick ruins consumed by roots and vines, mob spawner dungeons
- [x] `whispering_misty_hollow` — Low valley with perpetual fog (particles), reduced visibility, eerie ambience
- [x] `whispering_spider_nest` — Cobweb-heavy area, cave spider spawners near surface, string loot
- [x] `whispering_witch_wood` — Cauldrons, potion ingredients scattered, witch huts, purple-tinted foliage
- [x] `whispering_tangled_briar` — Dense sweet berry bushes, thorny undergrowth, fox dens, slow movement
- [x] `whispering_pale_forest` — Pale oak variant, ghostly white bark, pale moss, pale garden atmosphere
- [x] `whispering_root_maze` — Mangrove-style root networks above ground, maze-like, treasure hidden within
- [x] `whispering_firefly_grove` — Scattered glow lichen on trees at night, warm amber particles, peaceful pocket

#### 9.1.6 Biomes — Stormbreak Coast (12 biomes)
- [x] `stormbreak_sea_cliffs` — 40-60 block tall sheer cliffs, crashing waves, seabird nests (chicken spawns)
- [x] `stormbreak_tidal_caves` — Sea-level caves that flood at depth, drowned spawns, buried treasure
- [x] `stormbreak_coral_cove` — Sheltered bay with warm shallow water, vibrant coral reefs, tropical fish
- [x] `stormbreak_rocky_beach` — Gravel and stone beach, tide pools (water-logged slabs), shipwreck debris
- [x] `stormbreak_sandy_beach` — Classic sand beach, palm trees (jungle saplings), turtles, warm and inviting
- [x] `stormbreak_driftwood_shore` — Sandy beach with dead logs, sea grass, shells (nautilus), beachcombing theme
- [x] `stormbreak_mangrove_coast` — Where swamp meets sea, mangrove roots in salt water, crab (spider) spawns
- [x] `stormbreak_lighthouse_point` — Rocky headland with dramatic views, lighthouse structure, navigation landmark
- [x] `stormbreak_harbor_bay` — Natural harbor formation, calm water, ideal for port base building
- [x] `stormbreak_sea_stack_field` — Tall stone pillars rising from shallow ocean, arches, dramatic scenery
- [x] `stormbreak_windswept_dunes` — Sand dunes with beach grass, wind particles, buried ruins beneath sand
- [x] `stormbreak_cliff_forest` — Oak/spruce growing on cliff tops, roots exposed at cliff edge, dramatic views down

#### 9.1.7 Biomes — Ocean & Deep Water (14 biomes)
- [x] `elysium_warm_ocean` — Shallow turquoise water, coral, sea pickles, sea turtles, tropical fish
- [x] `elysium_lukewarm_ocean` — Moderate depth, scattered coral, mixed fish, sea grass beds
- [x] `elysium_temperate_ocean` — Standard ocean, kelp forests begin, cod/salmon, squid
- [x] `elysium_cold_ocean` — Deep green water, dense kelp forests, cod schools, cooler color palette
- [x] `elysium_frozen_sea` — Pack ice surface, icebergs, polar bears, frozen shipwrecks, strays on ice
- [x] `elysium_deep_ocean` — Continental shelf drop-off, deep blue, ocean monuments, guardian spawns
- [x] `elysium_deep_frozen_ocean` — Deep water under ice sheet, underwater ice formations, drowned
- [x] `elysium_ocean_trench` — Narrow deep cuts below Y=10, darkness, rare ores in trench walls, magma vents
- [x] `elysium_coral_reef_flat` — Expansive shallow reef system, every coral type, sea pickles, sponge
- [x] `elysium_kelp_cathedral` — Dense tall kelp forming underwater "columns", filtered light, cod schools
- [x] `elysium_seagrass_meadow` — Shallow sandy bottom covered in seagrass, turtles, peaceful underwater plain
- [x] `elysium_underwater_ridge` — Mid-ocean ridge with volcanic vents (magma blocks), unique ore deposits
- [x] `elysium_sunken_ruins` — Ocean floor with stone brick ruins, treasure, drowned guardians
- [x] `elysium_abyssal_plain` — Deepest ocean floor, minimal light, gravel/sand bottom, rare deep-sea features

#### 9.1.8 Biomes — Islands (8 biomes)
- [x] `elysium_tropical_island` — Small palm island with sand, sugarcane, melons, turtles, parrots
- [x] `elysium_volcanic_island` — Basalt/blackstone island with smoking crater, obsidian, magma vents
- [x] `elysium_mushroom_island` — Mycelium island, mooshrooms, giant mushrooms, zero hostile spawns
- [x] `elysium_jungle_island` — Dense jungle vegetation, parrots, ocelots, cocoa, hidden temple
- [x] `elysium_ice_island` — Frozen island with igloos, packed ice spikes, polar bears, blue ice
- [x] `elysium_cherry_island` — Cherry blossom trees on small island, pink petals, serene base location
- [x] `elysium_sand_atoll` — Ring-shaped sand island around shallow lagoon, coral inside, open ocean outside
- [x] `elysium_pirate_cove` — Hidden bay between cliffs, shipwreck-rich, treasure, drowned guardians

#### 9.1.9 Biomes — River Variants (8 biomes)
- [x] `elysium_mountain_stream` — Narrow fast-flowing water, boulders, gravel bed, steep gradient, waterfall origins
- [x] `elysium_forest_river` — Gentle river under tree canopy, fallen log bridges, lily pads, fishing
- [x] `elysium_wide_river` — Broad slow river, sand banks, river islands, sugar cane, great fishing
- [x] `elysium_rapids` — Fast turbulent section, exposed rocks, foam (white carpet), salmon jumping
- [x] `elysium_frozen_river` — Ice-covered river, packed ice banks, stray spawns, ice fishing holes
- [x] `elysium_swamp_river` — Murky slow-moving water, mangrove-lined, mud banks, slimes
- [x] `elysium_canyon_river` — River cutting through deep ravine, exposed ores in walls, dramatic depth
- [x] `elysium_river_delta` — Where river meets ocean, wide shallow estuary, mixed sand/clay, rich fishing

#### 9.1.10 Biomes — Scorched Badlands (12 biomes)
- [x] `scorched_mesa` — Flat-topped terracotta plateaus, exposed ore bands, dead bushes, dry heat
- [x] `scorched_canyon` — Deep narrow canyon with layered terracotta walls, mineshaft entrances
- [x] `scorched_oasis` — Rare tiny lake surrounded by palms and sugarcane amid scorched earth
- [x] `scorched_sand_desert` — Flat sand desert, sand dunes, dead bushes, husks, buried structures
- [x] `scorched_red_desert` — Red sand variant, terracotta outcrops, different ore distribution
- [x] `scorched_cracked_flats` — Flat dried lakebed (brown terracotta), cracks, fossils, extreme heat
- [x] `scorched_pillar_valley` — Tall terracotta pillars (hoodoos), narrow passages between, ambush terrain
- [x] `scorched_fossil_fields` — Bone blocks partially exposed, archaeological dig sites, unique loot
- [x] `scorched_dust_bowl` — Bare coarse dirt, dust particles, zero vegetation, challenging survival
- [x] `scorched_cactus_forest` — Dense tall cacti, sparse sand, limited visibility, prickly traversal
- [x] `scorched_salt_flat` — White concrete powder plains, blindingly bright, flat as glass, mirages
- [x] `scorched_badlands_spires` — Tall narrow terracotta spires, eroded arches, dramatic silhouettes

#### 9.1.11 Biomes — Frostfang Peaks (14 biomes)
- [x] `frostfang_summit` — Above treeline: bare rock, snow, ice, packed ice, extreme winds (snow particles)
- [x] `frostfang_alpine_meadow` — Just below treeline: short grass, wildflowers, mountain goats, gentle slopes
- [x] `frostfang_frozen_waterfall` — Frozen river with ice columns, blue ice, packed ice formations
- [x] `frostfang_ice_caves` — Glacial caves with blue/packed ice, frozen aquifers, crystal-clear underground pools
- [x] `frostfang_snowfield` — Deep snow layers, powdered snow traps, igloos, sparse spruce
- [x] `frostfang_spruce_slopes` — Dense spruce forest on mountain flanks, wolves, snow-dusted canopy
- [x] `frostfang_glacier` — Massive blue/packed ice formation, crevasses, ancient frozen structures within
- [x] `frostfang_frozen_lake` — Flat ice lake at high altitude, ice fishing, perfect for ice castle bases
- [x] `frostfang_ridge_trail` — Narrow mountain ridge path, stone exposed, goats, vertigo-inducing views
- [x] `frostfang_avalanche_chute` — Steep loose-gravel slope, snow layers, dangerous terrain, exposed minerals
- [x] `frostfang_hot_springs` — Rare warm pocket at altitude, steaming water (soul campfire smoke), lush patch amid snow
- [x] `frostfang_crystal_peak` — Mountain peak with exposed amethyst/ice crystal formations, beautiful and rare
- [x] `frostfang_wind_carved_rocks` — Eroded stone formations shaped by wind, arches, natural bridges between peaks
- [x] `frostfang_northern_lights_plateau` — High flat area, aurora-like particles at night, spiritual atmosphere

#### 9.1.12 Biomes — Shadowmire Swamps (10 biomes)
- [x] `shadowmire_mangrove` — Dense mangrove roots in murky water, fireflies (particles), frogs
- [x] `shadowmire_dead_marsh` — Leafless trees in stagnant water, soul sand patches, witch huts
- [x] `shadowmire_bog` — Deep mud blocks, peat, mushroom logs, will-o-wisp particles, difficult terrain
- [x] `shadowmire_cypress_bayou` — Tall dark oak "cypress" with hanging vines, knee-deep water, foggy
- [x] `shadowmire_fungal_swamp` — Giant mushrooms in swamp water, mycelium islands, mooshrooms, spore particles
- [x] `shadowmire_peat_marsh` — Flat muddy terrain, podzol/mud mix, cranberry bushes (sweet berries), foxes
- [x] `shadowmire_drowning_pools` — Deep scattered water holes in otherwise solid ground, drowned traps, dangerous
- [x] `shadowmire_vine_curtain` — Dense hanging vine canopy over shallow water, near-zero visibility, ambush mobs
- [x] `shadowmire_lantern_bog` — Scattered jack-o-lanterns and shroomlights in the swamp, eerie warm glow
- [x] `shadowmire_ancient_tree` — Massive central tree (giant dark oak) surrounded by swamp, treehouse base potential

#### 9.1.13 Biomes — Embercrag Volcanic (12 biomes)
- [x] `embercrag_lava_fields` — Flowing lava rivers, basalt columns, magma blocks, fire particles
- [x] `embercrag_ash_forest` — Charred tree trunks, ash (gray concrete powder), ember particles, dead
- [x] `embercrag_obsidian_waste` — Obsidian plains with crying obsidian veins, ruined portal fragments
- [x] `embercrag_basalt_columns` — Tall basalt hexagonal columns, narrow passages, dramatic geometry
- [x] `embercrag_caldera` — Massive volcanic crater with lava lake, rim of blackstone, ore-rich walls
- [x] `embercrag_fumarole_field` — Steam vents (campfire smoke), bubbling mud, sulfur-colored blocks, dangerous gases
- [x] `embercrag_scorched_woodland` — Half-burned forest, mix of live and dead trees, fire spread theme
- [x] `embercrag_magma_tubes` — Surface-level lava tubes, basalt tunnels, magma block floors, nether-feel
- [x] `embercrag_cinder_cone` — Small volcanic peaks dotted across terrain, each with small lava pool
- [x] `embercrag_nether_scar` — Area where nether is "bleeding through" — netherrack patches, nether vegetation, ruined portals
- [x] `embercrag_black_sand_beach` — Volcanic beach with black concrete powder sand, warm water, steam at shore
- [x] `embercrag_ember_grove` — Warped/crimson fungus growing on overworld terrain, nether-overworld hybrid zone

#### 9.1.14 Biomes — Abyssal Depths / Underground (18 biomes)
- [x] `abyssal_lush_cave` — Glow berries, dripleaf, azalea, moss carpet, cave streams, axolotls (Y 30-63)
- [x] `abyssal_dripstone_gallery` — Massive stalactite/stalagmite formations, pointed dripstone, copper ore (Y 0-50)
- [x] `abyssal_crystal_grotto` — Amethyst geode rooms, budding amethyst, calcite shells, serene cave lake
- [x] `abyssal_fungal_depths` — Giant underground mushrooms, mycelium floor, mooshrooms, spore particles
- [x] `abyssal_sculk_hollow` — Sculk-covered deep dark, ancient city corridors, warden territory (Y -64 to -30)
- [x] `abyssal_lava_chamber` — Vast open lava lake chambers, magma floor, diamond/gold veins in walls
- [x] `abyssal_flooded_cavern` — Aquifer-fed cave with underwater passages, drowned, hidden air pockets
- [x] `abyssal_moss_cathedral` — Enormous cave chamber with moss-covered walls, glow lichen ceiling, underground waterfall
- [x] `abyssal_ice_cavern` — Frozen underground cave, blue ice formations, packed ice, underground frozen lake
- [x] `abyssal_ore_vein` — Dense ore-rich cave section, exposed iron/gold/diamond clusters, mining paradise
- [x] `abyssal_underground_river` — Flowing underground waterway carving through caves, connects cave systems
- [x] `abyssal_spider_den` — Cobweb-choked cave, cave spider spawners, string, dense hostile spawns
- [x] `abyssal_bone_crypt` — Skeleton spawner caves, bone block deposits, fossils, skull decorations
- [x] `abyssal_root_cellar` — Tree roots penetrating from surface, hanging roots, azalea roots, dripleaf
- [x] `abyssal_echo_chamber` — Massive empty spherical cave, sculk sensors, sound-based trap theme
- [x] `abyssal_tuff_gallery` — Tuff-walled caves, calcite veins, smooth aesthetic, iron ore clusters
- [x] `abyssal_deepslate_labyrinth` — Narrow winding deepslate tunnels, maze-like, claustrophobic, redstone ore
- [x] `abyssal_magma_rift` — Deep crack in the earth with magma, lava drips from ceiling, ancient debris

#### 9.1.15 Biomes — The Shattered Reach / Endgame (10 biomes)
- [x] `shattered_reach_islands` — Floating terrain chunks, end stone traces, chorus plants at edges
- [x] `shattered_reach_void_forest` — Warped/twisted trees, purple grass, ender particles, endermen
- [x] `shattered_reach_boss_arena` — Flattened circular clearing with obsidian pillars, designed for boss encounters
- [x] `shattered_reach_crystal_wastes` — End stone ground, amethyst crystal fields, shulker spawns, alien landscape
- [x] `shattered_reach_corruption_edge` — Where normal terrain transitions to end-touched, gradual block palette shift
- [x] `shattered_reach_ender_spires` — Tall obsidian/purpur spires, end rods, shulker boxes as natural loot
- [x] `shattered_reach_void_lake` — Terrain gap with void below, end stone rim, dragon egg pedestal structures
- [x] `shattered_reach_chorus_garden` — Dense chorus plant forest, chorus fruit harvest, endermite spawns
- [x] `shattered_reach_gravity_well` — Terrain warps upward/downward unnaturally, disorienting, levitation hazard areas
- [x] `shattered_reach_fallen_tower` — Collapsed end city fragments embedded in terrain, rare endgame loot

#### 9.1.16 Biomes — Zone Transitions (10 biomes)
Smooth gradient biomes that sit between major zones, preventing jarring changes.

- [x] `transition_forest_edge` — Verdant → Whispering: thinning canopy, first dark oak appearing, slightly denser undergrowth
- [x] `transition_coastal_bluff` — Verdant → Stormbreak: grassy cliff tops overlooking the ocean, windswept trees
- [x] `transition_arid_scrub` — Verdant → Scorched: dying grass, scattered dead bushes, soil turning to sand
- [x] `transition_highland_approach` — Verdant → Frostfang: terrain rising, spruce replacing oak, first snow patches
- [x] `transition_marsh_fringe` — Whispering → Shadowmire: ground getting soggy, first mud blocks, cattails
- [x] `transition_volcanic_foothills` — Scorched → Embercrag: terracotta giving way to blackstone, first lava cracks
- [x] `transition_frozen_descent` — Frostfang → Shadowmire: snow melting into bogs, icy water, dead spruce
- [x] `transition_scorched_thaw` — Scorched → Frostfang: extreme temperature contrast zone, dramatic weather
- [x] `transition_corruption_creep` — Any → Shattered: end stone patches appearing, chorus fragments, purple particles
- [x] `transition_cave_mouth` — Surface → Abyssal: large cave openings in hillsides, visible depth, drafts

#### 9.1.17 Biomes — Special / Rare (8 biomes)
Rare biomes that spawn infrequently, rewarding exploration.

- [x] `rare_ancient_battlefield` — Flat field scattered with armor stands, broken swords (item frames), bone blocks, haunted
- [x] `rare_giant_tree` — Single massive tree (40+ blocks tall, 10+ trunk radius), treehouse village potential
- [x] `rare_crystal_springs` — Surface hot springs with amethyst clusters, warm water, regeneration-themed lush area
- [x] `rare_sky_pillar` — Single narrow terrain spike reaching Y=200+, challenge climb, treasure at top
- [x] `rare_meteorite_crater` — Impact crater with iron/gold blocks at center, deepslate rim, rare loot
- [x] `rare_enchanted_glade` — Unnaturally beautiful clearing, all flower types, permanent golden hour lighting feel
- [x] `rare_fossil_gorge` — Exposed massive fossil (bone blocks) in canyon wall, archaeological treasure
- [x] `rare_abandoned_village` — Overgrown zombie village variant, cobwebs, zombie villagers, salvageable loot

#### 9.1.7 Terrain Design
- [x] Smooth zone transitions: biomes blend gradually over 64-128 blocks, no harsh boundaries
- [x] Base building terrain: each zone has at least 2 biomes with flat/gentle areas ideal for building
- [x] Mountain variety: rolling hills (Verdant) → foothills (Whispering) → dramatic peaks (Frostfang) → volcanic (Embercrag)
- [x] River system: rivers originate in Frostfang mountains, flow through all zones to Stormbreak coast with waterfalls at zone transitions
- [x] Underground depth layers: surface caves (Y 30-63), mid caves (Y 0-30), deep caves (Y -64 to 0) each with distinct character

#### 9.1.7a Ore & Resource Distribution (Survival-Balanced)
Resources are harder to find than vanilla but never impossible. Mining is rewarding but requires effort and exploration. Rarer ores are gated behind depth AND distance from spawn.

- [x] **Coal** — Abundant everywhere (Y 0-192). Slightly denser near Sanctuary/Verdant to help early game. Exposed in cliff faces and ravines.
- [x] **Copper** — Common in Verdant and Stormbreak zones (Y -16 to 112). Dripstone caves have dense deposits. ~80% of vanilla rate.
- [x] **Iron** — Available in all zones but at ~70% vanilla rate. Richer veins in Frostfang (mountain iron) and Abyssal caves. Large iron veins exist but are rarer.
- [x] **Gold** — Scarce at ~50% vanilla rate. Concentrated in Scorched Badlands (mesa gold bonus, matching vanilla badlands behavior) and Abyssal depths. Scattered small veins elsewhere.
- [x] **Lapis** — ~60% vanilla rate. Found mainly in Abyssal caves (Y -64 to 64). Slightly more common near Whispering zone underground.
- [x] **Redstone** — ~60% vanilla rate. Deep Abyssal caves only (Y -64 to 16). Larger veins in `abyssal_deepslate_labyrinth` biome.
- [x] **Diamond** — **~40% vanilla rate**. Only below Y=16, and significantly rarer in Sanctuary/Verdant underground. Concentration increases in Abyssal Depths biomes, especially `abyssal_ore_vein` and `abyssal_lava_chamber`. Players must venture deep AND far to find reliable diamond sources.
- [x] **Emerald** — Frostfang Peaks only (mountain biomes), ~50% vanilla rate. Matching vanilla's mountain-exclusive behavior but scarcer.
- [x] **Ancient Debris** — Embercrag zone exclusively (Y -64 to 24), at ~30% vanilla nether rate. Extremely rare but present in overworld. `embercrag_nether_scar` and `embercrag_magma_tubes` have slightly elevated rates. This is endgame material.
- [x] **Amethyst Geodes** — Spawn in `abyssal_crystal_grotto` and `frostfang_crystal_peak` biomes. ~60% vanilla rate elsewhere underground. Budding amethyst intact for renewable harvesting.
- [x] **Ore vein mega-features** — Rare large ore veins (iron, copper) spawn as 100+ block formations in deep caves, rewarding dedicated mining expeditions. ~1 per 4 chunks at appropriate depths.
- [x] Per-biome ore override tables in each biome's TOML (allows full customization by server owners)
- [x] Global ore scarcity multiplier in `profile.toml` for easy server-wide tuning (default: 0.6 = 60% of vanilla rates)

#### 9.1.8 Structures — Villages (Jigsaw-Based, Larger Than Vanilla)
All villages use the jigsaw structure system for modular, randomized layouts. Villages are 2-3x larger than vanilla, with more houses, functional farms, proper roads, and varied building types. Each zone's village has a unique architectural style and block palette.

- [x] `sanctuary_village` — **Starter village**. 15-25 buildings: wooden cottages, town hall, blacksmith, bakery, church, well, library. Multiple large farms (wheat, carrots, potatoes, beetroot). Fenced animal pens (cows, sheep, chickens, pigs). Cobblestone paths connecting all buildings. Lantern-lit streets. Villagers with starter trades. Warm oak/birch palette.
- [x] `verdant_village` — **Plains village**. 12-20 buildings: farmhouses, grain silo (hay bales), windmill, market stalls, stables with horses, shepherd's hut. Large irrigated crop fields (wheat, melon, pumpkin). Flower gardens between buildings. Open village green in center. Oak/stripped oak palette.
- [x] `verdant_hamlet` — **Small 4-8 house cluster**. Cozy farmstead group: shared barn, communal crop field, well, cart (fence + chest). Spawns more frequently than full villages. Great for early looting.
- [x] `whispering_forest_village` — **Woodland village**. 10-16 buildings: treehouses connected by rope bridges (fence walkways), ground-level workshops, forester's lodge, potion brewery. Lantern-lit paths through trees. Dark oak/spruce palette. Overgrown mossy stone accents.
- [x] `stormbreak_fishing_village` — **Coastal village**. 10-18 buildings: stilted houses over water, fish market, dockyard with boats, net-drying racks (fence+string), lighthouse, harbormaster's office. Fish farms (water with tropical fish). Dock extends into water. Spruce/dark oak palette, prismarine accents.
- [x] `scorched_desert_village` — **Desert village**. 12-20 buildings: sandstone houses with flat roofs, covered market bazaar, water cistern (underground water tank), palm-shaded courtyard, guard tower. Irrigated farms behind walls (cactus, wheat, melon). Sandstone/cut sandstone palette.
- [x] `frostfang_mountain_village` — **Alpine village**. 8-14 buildings: stone/cobblestone lodges with spruce roofs, hot spring bath house, blacksmith forge, watchtower, underground root cellar storage. Animal pens with goats/sheep. Hay-insulated walls. Cobblestone/spruce palette, snow layers on roofs.
- [x] `shadowmire_stilt_village` — **Swamp village**. 8-12 buildings: raised stilt houses above water level, connected by plank walkways, witch doctor's hut, mushroom farm, fishing platforms. Mangrove wood palette, soul lantern lighting. Brewing stands in multiple houses.

#### 9.1.8a Structures — Farms (Standalone, Randomly Spawning)
Standalone farms spawn independently of villages throughout the world. They feel lived-in with functional crop fields, animal pens, and storage. Each biome has appropriate farm types.

- [x] `farm_wheat_field` — Large fenced wheat field (16x16 to 24x24) with irrigated rows, scarecrow (armor stand), small tool shed with basic loot. Spawns in Sanctuary, Verdant.
- [x] `farm_pumpkin_patch` — Fenced pumpkin/melon field with hay bales, composters, chest with seeds. Spawns in Verdant.
- [x] `farm_animal_ranch` — Large fenced pasture with barn, hay bales, water trough, mixed animals (cows, sheep, pigs, chickens). Separate pens per animal type. Spawns in Sanctuary, Verdant.
- [x] `farm_horse_stable` — Stable building with 4-6 stalls, hay, saddle/lead loot, fenced paddock, horses/donkeys. Spawns in Verdant, Scorched.
- [x] `farm_bee_apiary` — Cluster of beehives/bee nests around flower garden, honey bottles in chests, campfire underneath. Spawns in Sanctuary, Verdant.
- [x] `farm_mushroom_farm` — Underground or shaded mushroom cultivation, mycelium floor, mushroom stew ingredients. Spawns in Whispering, Shadowmire.
- [x] `farm_cactus_plantation` — Sand-filled fenced area with cactus rows, collection system (water channels), desert-themed storage shed. Spawns in Scorched.
- [x] `farm_sugar_cane_field` — Riverside sugar cane field with water channels, paper-making supplies in chest. Spawns near rivers in any warm zone.
- [x] `farm_bamboo_grove` — Cultivated bamboo farm with scaffolding storage, pandas nearby. Spawns in Verdant bamboo biomes.
- [x] `farm_berry_bushes` — Fenced sweet berry farm, fox dens nearby, berry-related loot. Spawns in Verdant, Whispering, Frostfang.
- [x] `farm_kelp_farm` — Underwater kelp cultivation with stone brick water channels, dried kelp furnace setup. Spawns in Stormbreak shallow areas.
- [x] `farm_vineyard` — Terraced crop rows on hillside, wine cellar below (potion ingredients), grape-themed (sweet berries). Spawns in Verdant hills.
- [x] `farm_greenhouse` — Glass-roofed building with mixed crops inside, warm biome crops growing in cold zones. Spawns in Frostfang, transition zones.
- [x] `farm_abandoned_farmstead` — Overgrown ruined farm, broken fences, zombie villagers, salvageable crops and loot. Spawns anywhere — uncommon variant.

#### 9.1.8b Structures — Outposts & Exploration
- [x] `outpost_watchtower` — Stone brick tower (3-4 floors) with lookout platform, banner, chest with maps/compass. Spawns in Verdant, transition zones.
- [x] `outpost_ranger_camp` — Tent (wool/fence frame), campfire, crafting table, weapon rack (armor stand), small loot. Spawns in Whispering, Frostfang.
- [x] `outpost_mining_camp` — Entrance to mine shaft, pickaxe/torch loot, rail tracks leading underground, ore samples. Spawns in Scorched, Frostfang.
- [x] `outpost_trader_caravan` — Wandering trader camp: tent, llamas, chest with exotic trades, campfire. Spawns on roads between zones.
- [x] `outpost_hermit_cabin` — Lone cabin in remote biome, enchanting table, book loot, cat, small garden. Spawns in Whispering, Frostfang.
- [x] `outpost_hunter_lodge` — Spruce cabin with trophy heads (mob heads), leather armor, bow/arrow loot. Spawns in Verdant, Whispering.
- [x] `outpost_fisherman_dock` — Small dock with fishing rod loot, boat, fish barrel, lantern. Spawns on riverbanks and lakeshores.

#### 9.1.8c Structures — Dungeons & Ruins
- [x] `ruin_overgrown_temple` — Mossy stone brick temple, mob spawners, moderate loot, vines covering everything. Spawns in Whispering.
- [x] `ruin_sunken_shrine` — Underwater stone structure with prismarine accents, drowned, treasure. Spawns in ocean biomes.
- [x] `ruin_desert_pyramid` — Classic desert temple expanded: larger, more rooms, more traps, better loot. Spawns in Scorched.
- [x] `ruin_frozen_crypt` — Ice-entombed burial chamber, skeleton spawners, packed ice, frozen loot. Spawns in Frostfang.
- [x] `ruin_volcanic_forge` — Blackstone/deepslate smithing ruins, netherite scrap loot (very rare), lava moat. Spawns in Embercrag.
- [x] `ruin_collapsed_tower` — Half-destroyed stone tower, rubble, mob nest inside, moderate loot. Spawns in any zone.
- [x] `ruin_ancient_library` — Underground library with enchanted book loot, bookshelves, puzzle door. Spawns in Abyssal.
- [x] `ruin_battlefield_memorial` — Stone pillars, armor stands with worn gear, memorial plaques (signs), bone blocks. Spawns in transition zones.
- [x] `ruin_bandit_camp` — Hostile outpost with TNT traps, stolen loot chests, mob spawners, cage with villager. Spawns in Whispering, Scorched.
- [x] `dungeon_spider_nest` — Large cobweb-filled room, multiple cave spider spawners, string and eye loot. Spawns underground.
- [x] `dungeon_skeleton_catacombs` — Multi-room underground dungeon, skeleton/skeleton horse spawners, bone-themed. Spawns in Abyssal.
- [x] `dungeon_zombie_warren` — Cramped underground tunnels, zombie spawners, rotten flesh and iron loot. Spawns underground.
- [x] `dungeon_drowned_grotto` — Flooded cave dungeon, drowned spawners, trident/nautilus loot (rare). Spawns in Stormbreak underground.
- [x] `abyssal_ancient_city` — Deep underground city ruins, sculk sensors, elite loot (references vanilla ancient city). Spawns in Abyssal sculk biomes.
- [x] `shattered_boss_tower` — Tall obsidian/end stone tower designed as boss encounter arena. Spawns in Shattered Reach.
- [x] `stormbreak_lighthouse` — Coastal lighthouse on cliff edge, navigation landmark, small loot, spyglass.
- [x] `stormbreak_shipwreck` — Beached/sunken ships with treasure maps and ocean loot.
- [x] `embercrag_nether_ruin` — Large ruined portal complex with nether-themed loot.

#### 9.1.8d Structures — Roads & Connectors
- [x] `road_cobblestone_path` — Cobblestone/gravel paths connecting nearby villages and structures. Generates along terrain, respecting slopes.
- [x] `road_bridge_small` — Wooden plank bridge over rivers/ravines (up to 12 blocks wide). Auto-generated on paths crossing water.
- [x] `road_bridge_large` — Stone arch bridge over wide rivers/canyons (up to 30 blocks). Rare, impressive, functional.
- [x] `road_signpost` — Oak post with sign pointing toward nearest villages/structures with distance. Spawns at path intersections.

#### 9.1.9 Cave Systems
- [x] Lush surface caves (Y 30-63): Glow berries, dripleaf, azalea trees, underground streams, axolotl spawns
- [x] Dripstone mid-caves (Y 0-30): Stalactites/stalagmites, pointed dripstone, underground waterfalls, copper ore
- [x] Crystal grottos (random, Y 0-50): Amethyst geode rooms with budding amethyst, calcite shells, beautiful lighting
- [x] Mushroom caves (Y -20 to 20): Giant mushrooms underground, mycelium floor, mooshrooms, unique atmosphere
- [x] Sculk depths (Y -64 to -30): Sculk-covered deep dark, ancient city corridors, warden territory, best loot
- [x] Lava chambers (Y -64 to 0): Open lava lakes, magma block floors, diamond/gold veins in walls, dangerous traversal
- [x] Flooded caves: Aquifer-fed cave sections with underwater passages, drowned, hidden air pockets

#### 9.1.10 Spawn Zone Config
- [x] `zones.toml` — Zone definitions with: zone name, biome list, vanilla mob spawn tables, mob density modifier, difficulty scaling
- [x] Each zone has fully configured vanilla spawn tables (works standalone, no dependencies)
- [x] Optional `mythicmobs_table` key per zone — ignored if MythicMobs isn't installed, used as additional spawns if it is

### 9.2 Profile: "Netherveil" (Custom Nether)

> A completely reimagined Nether built with Strata's terrain engine. Not a vanilla reskin — a unique hellscape with survival-focused zones, custom structures, and terrain that showcases Strata's 3D noise, lava systems, and carver capabilities. Environment: NETHER.

#### 9.2.1 Profile & World Config
- [x] `profiles/netherveil/profile.toml` — Nether environment, ceiling at Y=128, bedrock floor/ceiling, lava sea level at Y=32
- [x] Terrain config: `density.toml` (cavern ceiling shape, pillar density), `splines.toml`
- [x] Surface config: `rules.toml` (netherrack, soul sand, basalt surfaces)
- [x] Noise config: `functions.toml`
- [x] Carver config: `caves.toml` (massive nether caverns, lava tubes)
- [x] Water config: `lava_oceans.toml`, `lava_falls.toml` (lava replaces water in nether context)
- [x] Structure config: `fortresses.toml`, `bastions.toml`, `ruins.toml`
- [x] Feature config: `ores.toml`, `vegetation.toml` (nether flora)
- [x] Entity config: `spawning.toml`
- [x] Spawn zones config: `zones.toml`

#### 9.2.2 Zone System
Zones spread outward from the nether portal spawn point, increasing in hostility.

- [x] **Ashen Threshold** — Entry zone. Netherrack plains with scattered fire, crimson fungi, relatively open sight lines. Zombie piglin herds, moderate danger. Players orient themselves here.
- [x] **Crimson Dominion** — Dense crimson forest, nether wart blocks, weeping vines, shroomlight canopy. Hoglins, piglins, piglin brutes. Bartering economy. Bastion remnant spawns.
- [x] **Warped Depths** — Eerie warped forest, twisted fungi, warped wart blocks, enderman-heavy spawns. Safer from piglins but endermen are aggressive. Otherworldly atmosphere.
- [x] **Soul Barrens** — Soul sand valleys, soul fire, basalt pillars, fossil formations. Ghasts, skeletons, wither skeletons. Soul speed territory. Nether fortress spawns.
- [x] **Magma Crucible** — Lava ocean zone. Vast open lava lakes with basalt delta islands, magma cube swarms, striders. Extreme danger, rich resources (gold, quartz). Navigation-heavy.
- [x] **Blackstone Citadels** — Basalt delta terrain mixed with blackstone formations. Piglin strongholds, dense hostile spawns. Ancient debris concentrated here. Fortress-like natural terrain.
- [x] **The Wither's Domain** — Endgame zone. Soul sand/soul soil wasteland, wither skeleton fortresses, wither rose fields. Highest wither skeleton density. Ancient debris at peak rates.

#### 9.2.3 Biomes — Ashen Threshold (6 biomes)
- [x] `nether_ashen_wastes` — Open netherrack plains, scattered fire, lava pools, basic nether quartz. Entry biome.
- [x] `nether_ember_fields` — Netherrack with dense fire patches, magma blocks, glowstone clusters on ceiling
- [x] `nether_gravel_shoals` — Gravel beaches along lava shores, soul sand pockets, nether gold ore exposed
- [x] `nether_fungal_clearing` — Mix of crimson/warped fungi in open area, shroomlights, neutral zone
- [x] `nether_lava_springs` — Small lava source blocks bubbling up through netherrack, magma block vents, striders
- [x] `nether_ruined_portal_field` — Cluster of broken portal frames, crying obsidian, gold blocks, lore-rich

#### 9.2.4 Biomes — Crimson & Warped Forests (8 biomes)
- [x] `nether_crimson_forest` — Dense crimson stems, nether wart block canopy, weeping vines, hoglins, piglins
- [x] `nether_crimson_grove` — Sparser crimson trees, more open, shroomlight-lit clearings, piglin camps
- [x] `nether_crimson_thicket` — Impenetrable crimson undergrowth, vines block sight, hoglin ambushes
- [x] `nether_fungal_canopy` — Highest crimson trees forming ceiling-to-floor columns, vertical traversal
- [x] `nether_warped_forest` — Dense warped stems, warped wart blocks, twisted vines, enderman territory
- [x] `nether_warped_marsh` — Low warped fungi in waterlogged-style terrain (lava-logged), eerie blue glow
- [x] `nether_warped_spires` — Tall thin warped stems reaching ceiling, gaps between, vertical biome
- [x] `nether_mixed_fungal` — Crimson/warped boundary zone, both fungi types, neutral mob mix

#### 9.2.5 Biomes — Soul Barrens (6 biomes)
- [x] `nether_soul_sand_valley` — Wide open soul sand valley, soul fire, basalt pillars, ghast flightpaths
- [x] `nether_fossil_graveyard` — Massive bone block fossil formations, soul sand floor, skeleton spawns
- [x] `nether_soul_fire_lake` — Soul fire burning on soul soil "lake" beds, eerie blue glow, dangerous traversal
- [x] `nether_basalt_pillars` — Dense basalt column forest, narrow passages, ambush territory, echoing sound
- [x] `nether_wither_rose_field` — Soul soil covered in wither roses, instant damage, wither skeleton patrols
- [x] `nether_soul_glacier` — Packed soul sand compressed into glacier-like formations, blue ice equivalent for nether

#### 9.2.6 Biomes — Magma Crucible (6 biomes)
- [x] `nether_lava_ocean` — Vast open lava sea, rare basalt islands, striders, magma cubes, navigation challenge
- [x] `nether_basalt_delta` — Basalt and blackstone terrain above lava, magma block vents, treacherous footing
- [x] `nether_lava_falls` — Lava pouring from ceiling through massive cavern, curtains of falling lava, beautiful/deadly
- [x] `nether_obsidian_platform` — Cooled lava forming obsidian platforms above lava ocean, stable building ground
- [x] `nether_magma_tubes` — Tunnel networks with magma block floors, lava dripping from ceiling, ore-rich walls
- [x] `nether_volcanic_vent` — Active lava geyser area, periodic lava source block placement (fire charge mechanics), glowstone ceiling

#### 9.2.7 Biomes — Blackstone Citadels & Wither's Domain (8 biomes)
- [x] `nether_blackstone_fortress` — Natural blackstone formations resembling ruined fortifications, gilded blackstone veins
- [x] `nether_gilded_halls` — Blackstone corridors with gilded blackstone ore, piglin brute patrols, bastion loot
- [x] `nether_ancient_debris_vein` — Deep pockets with elevated ancient debris rates, surrounded by lava, high risk/reward
- [x] `nether_quartz_cavern` — Nether quartz-rich cave formation, crystal-like quartz clusters, beautiful mining area
- [x] `nether_wither_fortress` — Nether brick fortress biome, blaze spawners, wither skeletons, fortress loot
- [x] `nether_charred_barrens` — Blackened netherrack, ash particles, zero vegetation, highest mob density
- [x] `nether_bone_spire_valley` — Tall bone block spires rising from soul sand, skeletal architecture, wither skeleton territory
- [x] `nether_crying_obsidian_cavern` — Rare cavern with crying obsidian walls/ceiling, respawn anchor loot, purple glow

#### 9.2.8 Biomes — Nether Transitions & Rare (6 biomes)
- [x] `nether_transition_crimson_soul` — Crimson forest fading into soul sand, dying fungi, mixed mob spawns
- [x] `nether_transition_warped_basalt` — Warped forest meeting basalt delta, fungi growing on basalt, endermen on lava edge
- [x] `nether_transition_ash_magma` — Ashen wastes crumbling into magma crucible, unstable ground, lava cracks
- [x] `nether_rare_glowstone_cathedral` — Massive glowstone formation on ceiling, cathedral-like cavern below, stunning
- [x] `nether_rare_nether_garden` — Impossible lush pocket: all nether vegetation dense, shroomlights everywhere, peaceful
- [x] `nether_rare_ancient_portal` — Massive intact ruined portal structure, crying obsidian, gold blocks, lore

#### 9.2.9 Netherveil Structures
- [x] `nether_fortress_expanded` — Enhanced nether fortress: larger, more rooms, blaze spawners, wither skeleton spawners, nether wart farms, better loot tables
- [x] `nether_bastion_remnant` — Enhanced bastion: piglin barracks, treasure rooms, hoglin stables, gilded blackstone, gold blocks
- [x] `nether_piglin_outpost` — Small piglin camp with gold stockpile, bartering table, hoglin pen
- [x] `nether_blaze_tower` — Standalone blaze spawner tower, blaze rod loot, fire charge traps
- [x] `nether_wither_shrine` — Soul sand/soul soil shrine with wither skeleton skulls, summoning circle layout
- [x] `nether_strider_stable` — Warped fungus farm, strider pen, saddle loot, safe lava crossing point
- [x] `nether_lava_bridge` — Blackstone bridge spanning lava ocean, guard towers, toll-gate feel
- [x] `nether_ancient_mine` — Ancient debris mining operation, minecart tracks, deepslate tools, netherite scrap

#### 9.2.10 Netherveil Terrain & Resources
- [x] Massive cavern system with ceiling at Y=128, floor at Y=0-10 (above bedrock)
- [x] Lava sea at Y=32 (configurable), lava falls from ceiling common
- [x] 3D terrain: overhangs, arches, bridges, pillars connecting floor to ceiling
- [x] Nether quartz: abundant (main mining resource), ~120% vanilla rate
- [x] Nether gold ore: common in Blackstone Citadels, ~80% vanilla rate elsewhere
- [x] Ancient debris: ~60% vanilla rate overall, concentrated in Wither's Domain and deep Blackstone
- [x] Glowstone: ceiling clusters, ~100% vanilla rate, harvestable renewable via witch farms
- [x] Magma cream, blaze rods, wither skeleton skulls: gated by structure discovery and mob difficulty

---

### 9.3 Profile: "Enderrift" (Custom End)

> A reimagined End dimension built for endgame survival exploration. Not just void islands — a full world with unique terrain, progression, and reasons to build permanent bases. Showcases Strata's floating island generation, void terrain, and alien biome capabilities. Environment: THE_END.

#### 9.3.1 Profile & World Config
- [x] `profiles/enderrift/profile.toml` — End environment, void below Y=0, main island at Y=64, outer islands Y=40-200
- [x] Terrain config: `density.toml` (island shape functions, void gaps, bridge connections)
- [x] Surface config: `rules.toml` (end stone, purpur, chorus surfaces)
- [x] Noise config: `functions.toml`
- [x] Carver config: `caves.toml` (hollow island interiors, tunnels through islands)
- [x] Feature config: `ores.toml`, `vegetation.toml` (chorus, end-exclusive flora)
- [x] Structure config: `end_cities.toml`, `gateways.toml`, `ruins.toml`
- [x] Entity config: `spawning.toml`
- [x] Spawn zones config: `zones.toml`

#### 9.3.2 Zone System
Radiates outward from the central island (dragon fight area), with outer zones reached via end gateways.

- [x] **The Obsidian Spire (Central Island)** — Dragon fight arena. Classic obsidian pillars, end crystal cages, exit portal, bedrock fountain. Redesigned to be more dramatic: wider platform, deeper void around it, end stone pathways.
- [x] **The Shattered Ring** — First outer ring. Fragmented islands close together, easy bridging/pearl-throwing. Chorus forests begin. Endermen moderate density. Starter end loot.
- [x] **The Pale Expanse** — Wide open end stone plains on large islands. Chorus groves, end city spawns begin. Shulker territory. Good base building platforms.
- [x] **The Crystalline Reach** — Amethyst-infused end islands, crystal formations, purpur structures. Denser end cities, better loot. Beautiful alien landscape.
- [x] **The Chorus Wilds** — Massive chorus plant forests, towering chorus trees, dense endermite spawns. Navigation challenge, chorus fruit essential. Rich in shulker shells.
- [x] **The Void Frontier** — Extreme outer ring. Tiny scattered islands over deep void, long gaps between, elytra essential. Best end city loot, dragon head spawns. Maximum danger.

#### 9.3.3 Biomes — Central Island (4 biomes)
- [x] `end_obsidian_arena` — The dragon fight platform, obsidian pillars, end crystals, bedrock structures
- [x] `end_gateway_ring` — Ring of end gateways around central island, end stone path between them
- [x] `end_barren_isle` — Small barren end stone islands near center, first enderman spawns
- [x] `end_bridge_fragments` — Narrow end stone bridges connecting central area to first outer ring

#### 9.3.4 Biomes — The Shattered Ring (8 biomes)
- [x] `end_shattered_plateau` — Flat-topped broken island, end stone, first chorus plants, moderate endermen
- [x] `end_chorus_sprout` — Young chorus forests, short plants, chorus fruit harvest, endermite nests
- [x] `end_stone_pillars` — Tall thin end stone pillars, parkour-like terrain, gaps to void
- [x] `end_fractured_cliff` — Sheer cliff faces where islands broke apart, exposed interior layers
- [x] `end_moss_patch` — End stone with moss-like purple ground cover (purple carpet), alien lawn
- [x] `end_pearl_shoals` — Low flat islands with ender pearl drops common, enderman gathering grounds
- [x] `end_void_edge` — Island edges crumbling into void, unstable blocks (gravity blocks), dangerous rim
- [x] `end_debris_field` — Floating small blocks and rubble between larger islands, stepping stones

#### 9.3.5 Biomes — The Pale Expanse (8 biomes)
- [x] `end_pale_plains` — Large flat end stone platforms, open sky, good base building, moderate endermen
- [x] `end_chorus_grove` — Organized chorus plant clusters, fruit farming area, peaceful endgame farming
- [x] `end_end_city_district` — Biome where end cities generate, purpur roads between structures, shulkers
- [x] `end_purpur_garden` — Decorative purpur block formations, end rod lighting, alien garden aesthetic
- [x] `end_ender_pearl_lake` — Depression in island filled with chorus plants and endermen, "lake" of purple
- [x] `end_obsidian_vein` — End stone with obsidian veins running through, dark dramatic streaks, mining resource
- [x] `end_wind_swept_edge` — Exposed island edge with swept-back chorus plants, elytra launch points
- [x] `end_hollow_island` — Large island with carved-out interior, natural shelter, base building inside

#### 9.3.6 Biomes — The Crystalline Reach (8 biomes)
- [x] `end_crystal_fields` — End stone with amethyst cluster formations, purple-tinted, shimmering aesthetic
- [x] `end_amethyst_cavern` — Hollow island interior lined with amethyst, budding amethyst, geode atmosphere
- [x] `end_purpur_towers` — Tall purpur pillar formations, end rod caps, alien skyscraper feel
- [x] `end_ender_coral` — Chorus-plant variant growing in branching coral-like formations, dense and colorful
- [x] `end_dragon_egg_shrine` — Rare: small obsidian platform with dragon egg pedestal (empty), end crystal fragments
- [x] `end_star_terrace` — Stepped purpur/end stone terraces, open to void sky, stargazing platforms
- [x] `end_crystal_bridge` — Natural amethyst bridge connecting two islands, beautiful and fragile-looking
- [x] `end_shulker_nest` — Dense shulker spawns, purpur block structures, shell farming territory

#### 9.3.7 Biomes — Chorus Wilds (6 biomes)
- [x] `end_chorus_forest` — Massive chorus trees (20+ blocks tall), dense canopy of chorus flowers, endermite nests
- [x] `end_chorus_jungle` — Impenetrable chorus growth, chorus fruit essential for navigation, maze-like
- [x] `end_ancient_chorus` — Giant ancient chorus plants (40+ blocks, thick trunks), oldest growth, rare fruits
- [x] `end_withered_chorus` — Dead/dying chorus plants, brown/gray palette, endermite infestation, decay theme
- [x] `end_chorus_canopy` — Top of chorus forest, walking on flower tops, void visible below, elytra territory
- [x] `end_spore_cloud` — Chorus spore particles dense in air, reduced visibility, eerie atmosphere

#### 9.3.8 Biomes — Void Frontier (6 biomes)
- [x] `end_void_isle` — Tiny isolated end stone islands, massive void gaps, elytra mandatory
- [x] `end_dragon_perch` — Rare elevated platform with dragon head loot, obsidian/end stone mix, trophy site
- [x] `end_gateway_nexus` — Cluster of end gateways, teleportation hub, end city access point
- [x] `end_void_pillar` — Single tall thin pillar rising from void, challenge climb, ender chest at top
- [x] `end_elytra_course` — Natural ring formations and arches perfect for elytra flight, end rod gates
- [x] `end_final_city` — Largest end city variant, multiple ships, max shulker density, best loot in dimension

#### 9.3.9 Biomes — End Transitions & Rare (6 biomes)
- [x] `end_transition_shattered_pale` — Ring fragments getting larger, chorus density increasing
- [x] `end_transition_pale_crystal` — Amethyst starting to appear in end stone, purple tint increasing
- [x] `end_transition_crystal_chorus` — Crystal formations amid chorus growth, hybrid aesthetic
- [x] `end_transition_chorus_void` — Chorus forests thinning out, islands getting smaller, void encroaching
- [x] `end_rare_void_garden` — Impossible lush pocket: chorus, amethyst, purpur, end rods, all vegetation dense, serene
- [x] `end_rare_dragon_graveyard` — Massive bone block (dragon skeleton) formation on end stone, dragon breath particles, legendary loot

#### 9.3.10 Enderrift Structures
- [x] `end_city_expanded` — Enhanced end cities: larger, more rooms, better loot tables, more shulkers, guaranteed elytra in ship
- [x] `end_city_tower` — Standalone tall purpur tower, shulker guards, single chest with rare loot at top
- [x] `end_gateway_station` — Decorated gateway with end stone brick platform, end rod lighting, safe landing zone
- [x] `end_chorus_farm` — Cultivated chorus plant rows on end stone, chorus fruit stockpile, farm structure
- [x] `end_ender_chest_vault` — Small obsidian vault with ender chests, shulker box loot, secure storage theme
- [x] `end_dragon_shrine` — Obsidian/crying obsidian shrine with end crystals, dragon egg pedestal, boss-fight memento
- [x] `end_watchtower` — Purpur/end stone tower on island edge, spyglass, map loot, navigation aid
- [x] `end_void_bridge` — Long narrow bridge spanning void between distant islands, guard shulkers, dramatic crossing
- [x] `end_crashed_ship` — End ship crashed into island surface, partial elytra loot, shulker shells, damaged

#### 9.3.11 Enderrift Terrain & Resources
- [x] Floating island terrain: islands at Y=40-200, void below Y=0, no bedrock floor
- [x] Island sizes scale with zone: small fragments near center → massive platforms mid-ring → tiny specks at frontier
- [x] 3D island shapes: overhangs, arches, hollow interiors, natural bridges between close islands
- [x] Chorus plants: primary renewable resource, all zones, density increases outward
- [x] Obsidian: veins in end stone, mineable resource, concentrated near center
- [x] Amethyst: Crystalline Reach exclusive, budding amethyst for renewable harvesting
- [x] Purpur: end city structures are the source, no natural purpur deposits (must raid cities)
- [x] Shulker shells: gated by end city raiding, density increases in outer zones
- [x] Elytra: guaranteed one per end ship, ships more common in Void Frontier
- [x] Dragon breath: central island only, dragon fight required
- [x] End-exclusive ore (optional): configurable end stone variant ore for modded/custom item servers

### 9.4 Utility Profiles

- [x] `profiles/void/profile.toml` — Single bedrock layer at Y 64, optional spawn platform, single biome. For creative servers, minigame arenas, MythicMobs boss arenas.
- [x] `profiles/flat/profile.toml` — Configurable layer stack, optional structures and vegetation. For testing, building, redstone.

### 9.5 Example Custom Content
- [x] `structures/custom/example_tower.toml` + `example_tower.schem` — Demonstration watchtower structure
- [x] `structures/custom/example_boss_arena.toml` + `example_boss_arena.schem` — Boss arena template (usable with or without MythicMobs)
- [x] Example custom biome definition (shows how to create a zone biome from scratch)
- [x] Example custom noise function (terraced terrain for dramatic mesa effects)
- [x] Example custom carver (wide spiraling cave system for dungeon crawling)
- [x] Example `zones.toml` with MythicMobs integration showing full zone setup

---

## Phase 10: Testing & Performance

> Unit tests, integration tests, benchmarks, multi-version testing.

### 10.1 Noise Tests
- [x] Unit tests for each noise algorithm (known-value regression tests)
- [x] Unit tests for fractal noise octave stacking
- [x] Unit tests for composite noise operations
- [x] Unit tests for spline interpolation
- [x] Benchmark: noise sampling throughput (samples/sec for each algorithm)

### 10.2 Config Tests
- [x] Unit tests for TOML parsing (valid files, malformed files, edge cases)
- [x] Unit tests for schema validation (type errors, missing required fields, range violations)
- [x] Unit tests for profile inheritance/merging
- [x] Unit tests for config export (TOML → load → export → load roundtrip)
- [x] Unit tests for hot-reload (FileWatcher triggers, ReloadCoordinator validation)

### 10.3 Core Engine Tests
- [x] Unit tests for BiomeLookupTable (KD-tree correctness)
- [x] Unit tests for DensityEngine (known-input density evaluation)
- [x] Unit tests for SurfaceRuleEngine (condition chain evaluation)
- [x] Integration tests for full pipeline (generate a chunk, verify blocks at known positions)
- [x] Integration tests for water system (river network building, waterfall detection, lake filling)
- [x] Integration tests for structure placement (grid spacing verification)

### 10.4 Terra Migration Tests
- [x] Sample Terra pack fixtures (minimal valid packs for each Terra config type)
- [x] Unit tests for each converter (Terra YAML → expected Strata TOML output)
- [x] Integration test: full pack migration → load generated profile → verify it's valid
- [x] Edge case tests: missing fields, unsupported Terra features, malformed YAML

### 10.5 Performance
- [x] Benchmark: single chunk generation time (break down by stage)
- [x] Benchmark: 1000 chunk batch generation throughput
- [x] Benchmark: noise cache hit rates
- [x] Memory profiling: per-chunk memory footprint, cache memory usage
- [ ] Pre-generation stress test: generate 10,000 chunk radius, monitor memory/CPU

### 10.6 Multi-Version Testing
- [ ] Test on PaperMC 1.20.4 (current stable, widest user base)
- [ ] Test on PaperMC 26.1.2 (latest)
- [ ] Test on PaperMC 1.18.2 (first extended-height version)
- [ ] Test on PaperMC 1.16.5 (pre-extended-height, 3D biomes)
- [ ] Test on PaperMC 1.12.2 (pre-flattening, legacy block IDs)
- [ ] Test on PaperMC 1.8.8 (oldest supported)

---

## Phase 11: Documentation & Polish

> Documentation lives in three places: (1) inline TOML comments in every config file, (2) the in-game `/strata guide` system, (3) external docs. All three must stay in sync.

### 11.1 Inline Config Documentation (Most Important)
Every shipped TOML file is its own documentation. This is the primary way users learn Strata.

- [x] Every key in every TOML file has a comment above it: what it does, valid values/range, default, brief "why you'd change this"
- [x] Section headers have block comments explaining the section's purpose and how it fits into generation
- [x] Complex configs (noise functions, density graphs, surface rules) include "how it works" comment blocks with ASCII diagrams where helpful
- [x] Example: a biome TOML starts with a comment block explaining "This file defines a single biome. Copy this file and modify it to create a new biome."
- [x] Validation errors reference the comment/docs for the key that failed (e.g., "temperature must be 0.0-2.0, see comment in biome TOML")
- [x] `strata.toml` (global config) is the most heavily documented file — every setting explained, sections for beginners vs advanced

### 11.2 In-Game Guide Content
Content for the `/strata guide` system (see Phase 7.9). Written as bundled resource files.

- [x] 12 guide topics written in clear, beginner-friendly language with examples
- [x] Each topic includes: overview, step-by-step instructions, example commands, links to related topics
- [x] Troubleshooting topic covers the 10 most common issues with solutions
- [x] Guide content reviewed for clarity — assume the reader has never used a world gen plugin before

### 11.3 API Documentation
- [x] Javadoc for all public API interfaces in `strata-api`
- [x] API usage guide: "Getting Started with Strata API"
- [x] Example plugin project: registers a custom biome, noise function, and structure via the API

### 11.4 External Documentation (Wiki / README)
- [x] README.md — Project overview, features list, installation steps (detailed, step-by-step for beginners), quick start, screenshots
- [x] Installation guide: download jar → place in plugins → start server → first-run output explained → create first world
- [x] Profile creation guide: "Creating Your First Strata World Profile" (with full annotated example)
- [x] Biome creation guide: "Defining Custom Biomes" (copy existing → modify → reload)
- [x] Noise function guide: "Composing Noise Functions" (visual examples of what each noise type looks like)
- [x] Structure guide: "Adding Custom Structures" (schematic + TOML walkthrough)
- [x] Config reference: auto-generated from schema annotations, all TOML keys, types, defaults, descriptions
- [x] FAQ: "Do I need to configure anything to start?" (No — Elysium works out of the box), common questions answered

### 11.5 Terra Migration Documentation
- [x] Migration guide: "Migrating from Terra to Strata" (step-by-step with screenshots)
- [x] Feature comparison table (Terra feature → Strata equivalent)
- [x] Known limitations and manual steps required after migration

### 11.6 Project Documentation
- [x] CONTRIBUTING.md — How to contribute, code style, PR process
- [x] LICENSE — Choose appropriate license
- [x] Performance tuning guide: server operators' guide to Strata config for best performance
- [x] Changelog: maintained per release

---

## Architecture Reference

### Module Dependency Graph

```
strata-api          (zero dependencies - published jar)
    ^
    |
strata-noise        (depends on: api)
    ^
    |
strata-config       (depends on: api, noise)
    ^
    |
strata-core         (depends on: api, noise, config)
    ^
    |
strata-nms:common   (depends on: api, core)
    ^
    |
strata-nms:v*       (each depends on: nms:common + version-specific CraftBukkit)
    ^
    |
strata-paper        (depends on: core, config, nms:common; runtime: all nms:v*)
    ^
    |
strata-migrate      (depends on: config, api)
    |
strata-starter      (depends on: api; ships TOML/JSON resources only)
```

### Package Structure

```
com.protectcord.strata.api.*        — Public developer API (interfaces, records, enums, events)
com.protectcord.strata.noise.*      — Noise generation library
com.protectcord.strata.config.*     — Configuration system
com.protectcord.strata.core.*       — World generation engine
com.protectcord.strata.nms.*        — NMS abstraction + version adapters
com.protectcord.strata.paper.*      — PaperMC plugin
com.protectcord.strata.migrate.*    — Terra migration tool
```

### Key Design Decisions

| Decision | Choice | Why |
|----------|--------|-----|
| Config format | TOML + JSON | Readable, strongly typed, differentiates from Terra's YAML |
| Noise library | Custom `strata-noise` | Full control, no external deps, cacheable |
| NMS strategy | ServiceLoader + grouped adapters | Clean separation, runtime detection, covers 1.8.8-26.1.2 |
| Threading | Stateless stages, per-chunk contexts | Thread-safe by design for Paper async chunk gen |
| Caching | Per-chunk + LRU + interpolation grid | Three-tier covers all hot paths |
| River algorithm | Macro flow-path + micro noise carving | Rivers that actually flow downhill to oceans |
| Ocean model | Continental shelf bathymetry | Realistic depth variation, not flat floors |
| API surface | Interface-only module, own event bus | Clean third-party dependency, async-safe events |
| Pipeline | Ordered composable stages (wrap/replace/insert) | Maximum extensibility |
| Block representation | Namespaced `StrataBlockState` everywhere | Version-agnostic core, adapters translate |
| Java baseline | Java 25 (cross-compile to 8/11/16/17/21 for legacy NMS) | Latest LTS, full modern features |
| Build system | Gradle Kotlin DSL + buildSrc + version catalog | Modern, type-safe, no allprojects anti-patterns |

### Version Compatibility Matrix

| Adapter Module | MC Versions | Java | World Height | Biomes | Block IDs |
|---------------|-------------|------|-------------|--------|-----------|
| `v1_8_R3` | 1.8.4-1.8.9 | 8 | 0-255 | 2D | Numeric |
| `v1_12_R1` | 1.9-1.12.2 | 8 | 0-255 | 2D | Numeric |
| `v1_13_R2` | 1.13-1.15.2 | 8 | 0-255 | 2D | Namespaced |
| `v1_16_R3` | 1.16-1.16.5 | 11 | 0-255 | 3D (4x4x4) | Namespaced |
| `v1_17_R1` | 1.17-1.17.1 | 16 | 0-255* | 3D | Namespaced |
| `v1_18_R2` | 1.18-1.19.3 | 17 | -64 to 320 | 3D | Namespaced |
| `v1_19_R3` | 1.19.4-1.20.4 | 17 | -64 to 320 | 3D | Namespaced |
| `v1_20_R4` | 1.20.5-1.21.x | 21 | -64 to 320 | 3D | Namespaced |
| `v26_1` | 26.1-26.1.2+ | 25 | -64 to 320 | 3D | Namespaced |

*1.17 supported extended height experimentally but defaulted to 0-255

### 14-Stage Generation Pipeline

```
 0. INITIALIZATION      → Allocate ProtoChunk, resolve profile, seed RNG
 1. CONTINENTAL_SHAPE   → Land vs ocean vs coast classification
 2. CLIMATE_SAMPLING    → Temperature, humidity, erosion, weirdness noise
 3. BIOME_ASSIGNMENT    → 5D climate → biome via KD-tree at 4x4x4 resolution
 4. TERRAIN_SHAPING     → 3D density function evaluation (stone vs air)
 5. AQUIFER_PLACEMENT   → Underground water/lava pocket boundaries
 6. SURFACE_BUILDING    → Biome surface rules (grass, sand, gravel layers)
 7. CARVING            → Caves (cheese, spaghetti, noodle) + ravines
 8. WATER_SYSTEM       → Rivers, waterfalls, lakes, ocean features, swamp water
 9. STRUCTURE_GEN      → Structure starts + piece placement
10. FEATURE_DECORATION  → Ores, trees, vegetation, springs, snow/ice
11. ENTITY_SPAWNING    → Per-biome spawn tables written to chunk
12. LIGHTING           → Sky light + block light propagation
13. FINALIZATION       → Convert to NMS chunk, commit heightmaps, fire event
```

### Configuration Directory Layout

```
plugins/Strata/
  strata.toml                         # Global plugin settings
  profiles/
    default-overworld/
      profile.toml                    # Profile metadata + world settings
      biomes/*.toml                   # One file per biome
      terrain/density.toml            # Density function graph
      terrain/splines.toml            # Height spline control points
      terrain/continents.toml         # Continental shape parameters
      surface/rules.toml              # Surface rule chains
      noise/functions.toml            # Named noise function definitions
      carvers/caves.toml              # Cave carver parameters
      carvers/ravines.toml            # Ravine carver parameters
      water/oceans.toml               # Ocean depth, shelf, features
      water/rivers.toml               # River generation parameters
      water/lakes.toml                # Lake generation parameters
      water/aquifers.toml             # Underground water parameters
      structures/*.toml               # Structure definitions
      structures/custom/*.schem       # Schematic files
      features/ores.toml              # Ore distribution
      features/trees.toml             # Tree placement
      features/vegetation.toml        # Grass, flowers, etc.
      features/saplings.toml          # Sapling biome rules
      entities/spawning.toml          # Mob spawn rules
```
