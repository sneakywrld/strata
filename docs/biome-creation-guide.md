# Defining Custom Biomes

This guide walks through creating a custom biome for your Strata profile, from copying an existing template to testing it in-game.

## Before You Start

Make sure you have a working profile. If you do not have one yet, follow the [Profile Creation Guide](profile-creation-guide.md) first. All biome files live inside your profile's `biomes/` directory.

## Step 1: Copy an Existing Biome

The fastest way to create a biome is to start from an existing one. Pick a biome that is close to what you want and copy its TOML file:

```bash
cp plugins/Strata/profiles/myworld/biomes/sanctuary/sanctuary_meadow.toml \
   plugins/Strata/profiles/myworld/biomes/myzone/crystal_caverns.toml
```

You can organize biomes into subdirectories by zone (e.g., `biomes/myzone/`), or place them directly in `biomes/`. Strata scans the entire `biomes/` tree recursively.

## Step 2: Understand the Biome TOML Structure

Every biome TOML file has the same sections. Here is a complete walkthrough using a custom "Crystal Caverns" biome as the example.

### Biome Identity

```toml
[biome]
id = "strata:crystal_caverns"
display-name = "Crystal Caverns"
category = "CAVE"
zone = "abyssal"
```

| Key | What it does |
|-----|-------------|
| `id` | Unique namespaced identifier. Must be unique across the entire profile. Format: `"strata:<name>"` for built-in biomes, or `"myplugin:<name>"` for custom ones. |
| `display-name` | Human-readable name shown in F3 debug, `/strata info`, and zone-entry messages. |
| `category` | Broad biome classification. Affects ambient sounds, sky color, and mob behavior. Valid values: `NONE`, `PLAINS`, `FOREST`, `TAIGA`, `DESERT`, `SWAMP`, `SAVANNA`, `BADLANDS`, `JUNGLE`, `MOUNTAIN`, `OCEAN`, `RIVER`, `BEACH`, `CAVE`, `MUSHROOM`, `NETHER`, `END`. |
| `zone` | Which zone this biome belongs to. Must match a key in `zones/zones.toml`. |

### Climate Parameters

Climate is the heart of biome placement. Strata uses a 5-dimensional climate system. During world generation, the local climate noise values at each position are computed, and the biome whose climate point is nearest (in 5D space) is selected.

```toml
[biome.climate]
temperature = -0.3
humidity = 0.2
continentalness = 0.6
erosion = -0.5
weirdness = 0.4
```

Each axis ranges from **-1.0 to 1.0**:

| Axis | -1.0 | 0.0 | 1.0 | What it controls |
|------|------|-----|-----|-----------------|
| **temperature** | Freezing tundra | Temperate | Scorching desert | Hot vs. cold biomes |
| **humidity** | Parched arid | Moderate moisture | Soaking tropical | Dry vs. wet biomes |
| **continentalness** | Deep ocean | Coastline | Far inland | Distance from ocean |
| **erosion** | Steep mountains | Moderate hills | Pancake flat | Terrain roughness |
| **weirdness** | Standard terrain | Normal | Bizarre mutations | Unusual variants |

**How to choose values:**

- Think about where your biome should appear geographically. A frozen mountain biome wants low temperature, high continentalness, and low erosion. A tropical beach wants high temperature, low continentalness, and high erosion.
- If two biomes have similar climate points, they compete for the same locations. Spread your biomes apart in climate space for clean boundaries.
- The `weirdness` axis is useful for creating variants. A "normal" forest at weirdness 0.0 and a "mutated" forest at weirdness 0.8 will generate in similar areas, but the mutated version only appears where the weirdness noise is high.

**Testing tip:** Use `/strata biome` in-game to see which biome you are standing in. Walk around to see where your biome boundaries fall.

### Terrain Shape

```toml
[biome.terrain]
base-height = -20.0
height-variation = 8.0
```

| Key | Type | Default | Range | Description |
|-----|------|---------|-------|-------------|
| `base-height` | float | 4.0 | -64.0 to 256.0 | Y-level offset from sea level (63). Positive = raised terrain, negative = lowered. A cave biome at -20.0 sits well below the surface. |
| `height-variation` | float | 2.5 | 0.0 to 30.0 | How much terrain undulates. 0.0 = flat. 30.0 = extreme peaks and valleys. |

### Surface Blocks

```toml
[biome.surface]
top-block = "minecraft:calcite"
filler-block = "minecraft:smooth_basalt"
filler-depth = 4
underwater-block = "minecraft:clay"
```

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `top-block` | string | `"minecraft:grass_block"` | Block placed on the topmost surface layer. |
| `filler-block` | string | `"minecraft:dirt"` | Block placed below the top block. |
| `filler-depth` | int | 3 | How many blocks of filler sit beneath the top block. Range: 1 to 8. |
| `underwater-block` | string | `"minecraft:gravel"` | Block placed on the floor of water bodies within this biome. |

If you omit the `[biome.surface]` section entirely, the global rules from `surface/rules.toml` apply.

### Feature Decoration

```toml
[biome.features]
trees = { types = ["azalea"], density = 0.3 }
grass-density = 2.0
flower-density = 0.0
flower-types = []
pumpkin-chance = 0.0
```

| Key | Type | Description |
|-----|------|-------------|
| `trees` | table | `types` is a list of tree keys from `features/trees.toml`. `density` is trees per chunk (0.0 to 30.0+). |
| `grass-density` | float | Grass patches per chunk (0.0 to 20.0). |
| `flower-density` | float | Flower patches per chunk (0.0 to 15.0). |
| `flower-types` | array | List of flower keys from `vegetation.toml`. Overrides the global weighted list. |
| `pumpkin-chance` | float | Per-chunk probability of a pumpkin (0.0 to 1.0). |

### Mob Spawning Overrides

```toml
[biome.spawning]
bat = { weight = 20, min-group = 2, max-group = 5 }
glow_squid = { weight = 8, min-group = 1, max-group = 3 }
```

Spawn entries here add to or override the global table in `entities/spawning.toml`. Only list the mobs you want to change. All others keep their global defaults, scaled by the zone's `passive-density` and `hostile-density` multipliers.

Each entry takes:

| Key | Type | Description |
|-----|------|-------------|
| `weight` | int | Relative spawn chance. Higher = more common. |
| `min-group` | int | Minimum pack size. |
| `max-group` | int | Maximum pack size. |

## Step 3: Add the Biome to a Zone

Your biome's `zone` key must reference a zone defined in `zones/zones.toml`. The biome file also needs to live in (or under) the directory specified by that zone's `biome-dir`.

For example, if your zone config says:

```toml
[zones.abyssal]
biome-dir = "abyssal"
```

Then your biome file should be placed at:

```
profiles/myworld/biomes/abyssal/crystal_caverns.toml
```

And the biome TOML should have:

```toml
zone = "abyssal"
```

If you are creating a new zone, add it to `zones/zones.toml` first. See the zones config for the required fields (`display-name`, `distance-range` or `y-range`, `difficulty`, `biome-dir`, etc.).

## Step 4: Test

Reload the profile:

```
/strata reload myworld
```

Changes apply to **newly generated chunks only**. Already-generated chunks keep their original biome assignments.

To test your new biome, teleport to an unexplored area and walk around:

```
/tp @s myworld 10000 100 10000
/strata biome
```

If the biome does not appear:

1. **Check server logs.** Strata reports TOML parse errors with file path and line number.
2. **Check climate overlap.** Your biome might be "losing" to a nearby biome in climate space. Try moving your climate point further from competing biomes.
3. **Check the zone.** Make sure the biome's `zone` matches an active zone and the biome file is in the correct directory.

## Complete Example

Here is a full biome TOML for a custom "Sunken Reef" biome:

```toml
# ============================================================
#  Biome: Sunken Reef — Stormbreak Zone
# ============================================================
# A shallow underwater biome with vibrant coral and bright
# tropical fish. Found along warm coastlines.
# ============================================================

[biome]
id = "strata:sunken_reef"
display-name = "Sunken Reef"
category = "OCEAN"
zone = "stormbreak"

[biome.climate]
temperature = 0.8
humidity = 0.9
continentalness = -0.4
erosion = 0.5
weirdness = 0.1

[biome.terrain]
base-height = -15.0
height-variation = 3.0

[biome.surface]
top-block = "minecraft:sand"
filler-block = "minecraft:sandstone"
filler-depth = 3
underwater-block = "minecraft:sand"

[biome.features]
trees = { types = [], density = 0.0 }
grass-density = 0.0
flower-density = 0.0
flower-types = []
pumpkin-chance = 0.0

[biome.spawning]
tropical_fish = { weight = 30, min-group = 4, max-group = 10 }
pufferfish = { weight = 8, min-group = 1, max-group = 2 }
squid = { weight = 5, min-group = 1, max-group = 3 }
```

## Tips

- **Start small.** Change one or two values from the template, reload, and test. This makes it easy to see the effect of each change.
- **Read the comments.** Every key in the default biome files has inline documentation explaining its purpose, valid values, and default.
- **Use climate diagrams.** Sketch your biomes on a temperature-humidity grid to visualize spacing and avoid overlap.
- **Profile inheritance works for biomes too.** If your profile extends another (via `extends` in `profile.toml`), you only need biome files for biomes you want to add or override.
