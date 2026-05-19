# Creating Your First Strata World Profile

This guide walks through creating a custom world profile from scratch.

## What is a Profile?

A profile is a complete world generation configuration. It defines terrain shape, biomes, caves, water systems, structures, ores, and mob spawning. Each world uses exactly one profile.

## Method 1: Copy and Modify (Recommended)

The easiest way to create a profile is to copy an existing one:

```bash
cp -r plugins/Strata/profiles/elysium plugins/Strata/profiles/myworld
```

Then edit `plugins/Strata/profiles/myworld/profile.toml`:

```toml
name = "My World"
description = "My custom survival world"
author = "YourName"
version = "1.0.0"
environment = "NORMAL"
```

Reload and create:

```
/strata reload myworld
/strata create testworld myworld
```

## Method 2: Minimal Profile

Create a profile directory with just a `profile.toml`:

```
plugins/Strata/profiles/minimal/
└── profile.toml
```

```toml
name = "Minimal"
description = "Bare minimum profile"
author = "YourName"
version = "1.0.0"
environment = "NORMAL"

[world]
min-y = -64
max-y = 320
sea-level = 63
```

Strata uses sensible defaults for everything not specified.

## Profile Directory Structure

A full profile contains these TOML files:

```
profiles/myworld/
├── profile.toml          ← Required: metadata, world bounds
├── terrain/
│   ├── density.toml      ← 3D terrain density functions
│   ├── splines.toml      ← Height spline control points
│   └── continents.toml   ← Land/ocean/coast distribution
├── noise/
│   └── functions.toml    ← Named noise functions
├── surface/
│   └── rules.toml        ← Surface block rules (grass, sand, etc.)
├── carvers/
│   ├── caves.toml        ← Cave generation parameters
│   └── ravines.toml      ← Ravine parameters
├── water/
│   ├── rivers.toml       ← River system config
│   ├── oceans.toml       ← Ocean depth and features
│   ├── lakes.toml        ← Lake placement
│   └── aquifers.toml     ← Underground water/lava
├── biomes/
│   └── *.toml            ← One file per biome
├── structures/
│   └── *.toml            ← Structure placement configs
├── features/
│   ├── ores.toml         ← Ore distribution
│   ├── trees.toml        ← Tree placement
│   └── vegetation.toml   ← Grass, flowers, etc.
├── entities/
│   └── spawning.toml     ← Mob spawn tables
└── zones.toml            ← Zone difficulty tiering
```

## Profile Inheritance

Profiles can extend other profiles using the `extends` key:

```toml
name = "Hard Mode"
extends = "elysium"

[features.ores]
global-scarcity-multiplier = 0.3  # 30% of normal ore rates
```

Only the keys you specify are overridden. Everything else is inherited from the parent profile.

## Hot-Reload

After editing any TOML file:

```
/strata reload myworld
```

Changes apply to newly generated chunks. Already-generated chunks are not affected.

## Tips

- Every TOML key has a comment explaining what it does. Read the comments in the default profiles.
- Start by tweaking small values (ore rates, tree density) before changing terrain shape.
- Use `/strata biome` to check which biome you're in while testing.
- Use `/strata guide` for in-game help on any topic.
