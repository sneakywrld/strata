# Changelog

All notable changes to Strata are documented here.

## [1.0.0] - Unreleased

### Added
- 14-stage composable generation pipeline
- TOML-based configuration throughout (no YAML)
- 3 shipped profiles: Elysium (191 biomes), Netherveil (48 biomes), Enderrift (55 biomes)
- 2 utility profiles: Void (arena/creative), Flat (testing/building)
- MMORPG zone difficulty system with 10 concentric zones
- Drainage-basin river algorithm with natural downhill flow
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
- In-game guide system with 12 topics
- bStats metrics integration
- Optional MythicMobs integration for zone-based custom mob spawning
- Pre-generation system with progress tracking
- Supports Minecraft 1.8.8 through 26.1.2
