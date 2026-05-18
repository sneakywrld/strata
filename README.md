# Strata

**The best Minecraft world generation plugin on the market.**

Strata is a modern, powerful Minecraft world generation platform built for PaperMC. It features a fully composable noise pipeline, TOML-based configuration, an MMORPG zone difficulty system, drainage-basin rivers, deep cave networks, and Terra migration support. Strata ships with three complete world profiles out of the box and supports Minecraft versions 1.8.8 through 26.1.2.

---

## Features

- 14-stage composable generation pipeline
- TOML-based configuration throughout (no YAML)
- 3 shipped profiles: Elysium (overworld, 183 biomes), Netherveil (nether, 48 biomes), Enderrift (end, 55 biomes)
- MMORPG zone difficulty system with 10 concentric zones radiating from spawn
- Drainage-basin river algorithm -- rivers flow naturally downhill to oceans
- 5D climate parameter biome selection via KD-tree lookup
- 7 noise algorithms: Simplex, Perlin, OpenSimplex2, Cellular, Value, Ridged Multifractal, White
- Deep cave systems: cheese caves, spaghetti tunnels, noodle crevices, ravines
- Aquifer system with 3D underground water and lava pockets
- Intentionally scarce rare ores (approximately 40% of vanilla diamond rate)
- Biome-aware sapling growth rules
- Villages 2-3x larger than vanilla with zone-appropriate architecture
- Hot-reload: edit TOML, reload in-game, see changes on next chunk generation
- Terra config pack migration (import existing Terra setups)
- Full API for third-party plugin integration
- Supports Minecraft 1.8.8 through 26.1.2

---

## Requirements

- **PaperMC** (or Folia, Purpur) 1.8.8+
- **Java 21** or newer

---

## Quick Start

1. Download `strata-paper-*.jar` from [Releases](https://github.com/sneakywrld/strata/releases).
2. Place the jar in your server's `plugins/` directory.
3. Start the server. Strata will generate its default configuration on first run.
4. Run `/strata create myworld elysium` to create a new world using the Elysium profile.
5. Run `/strata profiles` to list all available profiles.

---

## Commands

| Command | Description |
|---|---|
| `/strata create <name> <profile> [seed]` | Create a new world |
| `/strata profiles` | List available profiles |
| `/strata info [world]` | Show world and profile info |
| `/strata reload [profile]` | Hot-reload configuration |
| `/strata pregen <world> <radius> [threads]` | Pre-generate chunks |
| `/strata biome` | Show current biome info |
| `/strata guide [topic]` | In-game interactive guide |
| `/strata migrate <terra-pack> [name]` | Import a Terra config pack |

---

## Shipped Profiles

- **Elysium** -- MMORPG survival overworld. 10 difficulty zones, 183 biomes, drainage rivers, and deep caves. Author: SneakyWrld.
- **Netherveil** -- Reimagined Nether dimension. 6 zones, 48 biomes, lava seas, and fortress regions.
- **Enderrift** -- Custom End dimension. Floating islands, 7 zones, 55 biomes, and a central dragon arena.

---

## Configuration

All configuration in Strata is TOML-based. Every config key includes inline comments explaining its usage and valid values.

Profile directory structure:

```
profiles/<name>/
    profile.toml
    terrain/
    noise/
    biomes/
    structures/
    ...
```

To create a custom profile, copy an existing one and modify it:

```bash
cp -r profiles/elysium profiles/myworld
```

Edit the TOML files under `profiles/myworld/`, then use `/strata reload myworld` in-game to apply changes without restarting the server. New chunks will generate with the updated configuration.

---

## For Developers

Strata exposes a public API module for third-party plugin integration.

- **API module:** `strata-api` (pure interfaces, no implementation details)
- **Maven coordinates:** `com.protectcord.strata:strata-api:1.0.0-SNAPSHOT`

Through the `StrataAPI` entry point you can:

- Register custom biomes, noise functions, and structures
- Hook into the event bus for all generation stages
- Query world and biome data at runtime

Depend on `strata-api` only. Never depend on internal modules directly.

---

## Project Structure

```
strata-api/       -- Public API (interfaces only)
strata-noise/     -- Noise algorithms and caching
strata-config/    -- TOML parsing, validation, hot-reload
strata-core/      -- Generation engine, all pipeline stages
strata-nms/       -- NMS version adapters (1.8.8 - 26.1)
strata-paper/     -- PaperMC plugin entry point
strata-migrate/   -- Terra config pack importer
strata-starter/   -- Default world profiles (Elysium, Netherveil, Enderrift)
```

---

## Building from Source

```bash
git clone https://github.com/sneakywrld/strata.git
cd strata
./gradlew build
```

The output jar will be located at:

```
strata-paper/build/libs/strata-paper-*-all.jar
```

---

## Terra Migration

Strata includes a dedicated migration tool for importing existing Terra config packs.

**In-game:**

```
/strata migrate <terra-pack-path> [output-name]
```

**Standalone CLI:**

```bash
java -jar strata-migrate.jar <terra-pack> <output-dir>
```

The migration process converts biomes, noise configurations, palettes, and structures from Terra format to Strata TOML profiles. A detailed migration report is generated after each run, listing all converted elements and any items that require manual review.

---

## License and Contributing

See [LICENSE](LICENSE) for license terms.

For contribution guidelines, development setup, and code standards, see [CONTRIBUTING.md](CONTRIBUTING.md).
