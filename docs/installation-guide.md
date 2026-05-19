# Installation Guide

## Requirements

- **PaperMC** server (or Folia, Purpur) version 1.8.8 or newer
- **Java 21** or newer

## Step 1: Download

Download the latest `strata-paper-*.jar` from the [Releases](https://github.com/sneakywrld/strata/releases) page. Only download from the official repository.

## Step 2: Install

1. Stop your server if it's running.
2. Place `strata-paper-*.jar` into your server's `plugins/` directory.
3. Start the server.

On first startup, Strata creates:
```
plugins/Strata/
├── strata.toml          ← Global config
├── profiles/            ← World generation profiles
│   ├── elysium/         ← Flagship overworld profile
│   ├── netherveil/      ← Custom nether profile
│   ├── enderrift/       ← Custom end profile
│   ├── void/            ← Empty void world
│   └── flat/            ← Flat world
└── worlds.json          ← World-profile mappings
```

## Step 3: Create Your First World

Run this command in-game or from the console:

```
/strata create myworld elysium
```

This creates a new world named "myworld" using the Elysium profile with 191 handcrafted biomes, MMORPG zone tiering, drainage-basin rivers, and deep cave systems.

## Step 4: Explore

Teleport to the new world:

```
/tp @s myworld 0 100 0
```

Use `/strata biome` to see what biome you're standing in. Use `/strata guide` for in-game help.

## Profiles Overview

| Profile | Description | Biomes |
|---------|-------------|--------|
| **Elysium** | MMORPG survival overworld with 10 zones | 191 |
| **Netherveil** | Reimagined Nether dimension | 48 |
| **Enderrift** | Custom End dimension | 55 |
| **Void** | Empty world for arenas/minigames | 1 |
| **Flat** | Configurable flat world | 1 |

## Custom Profiles

Copy an existing profile and modify it:

```bash
cp -r plugins/Strata/profiles/elysium plugins/Strata/profiles/myprofile
```

Edit the TOML files under `plugins/Strata/profiles/myprofile/`, then reload:

```
/strata reload myprofile
```

New chunks will generate with your changes. See `/strata guide profiles` for details.

## Troubleshooting

- **World not generating**: Ensure the profile name is correct in `/strata create`. Check server logs for config errors.
- **Config parse errors**: Strata logs the file path, line number, and error. Fix the TOML syntax and `/strata reload`.
- **Performance**: Reduce `noise-cache-size` in `strata.toml` for lower memory usage. Use `/strata pregen` to pre-generate chunks.
- **Version mismatch**: Check that your PaperMC version is supported (1.8.8 - 26.1.2). Update Strata if needed.
