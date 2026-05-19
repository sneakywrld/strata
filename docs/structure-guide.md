# Adding Custom Structures

This guide explains how to add structures to your Strata profile. Strata supports three structure types: Jigsaw (modular piece-based), Schematic (pre-built .schem files), and Procedural (algorithm-generated). Each type serves a different purpose.

## Structure Types at a Glance

| Type | How it works | Best for |
|------|-------------|----------|
| **Jigsaw** | Assembles modular pieces together at generation time | Villages, mansions, monuments, any structure that should vary each time |
| **Schematic** | Places a pre-built .schem file directly into the world | Temples, ruins, small buildings, anything with a fixed design |
| **Procedural** | Generates shapes algorithmically (code-defined) | Mineshafts, dungeons, anything that needs infinite variation |

## Where Structure Config Lives

All structure placement rules go in `structures/structures.toml` inside your profile. Schematic files (.schem) go in a `structures/schematics/` directory. Jigsaw template pools go in `structures/pools/`.

```
profiles/myworld/
├── structures/
│   ├── structures.toml      ← Placement rules for all structures
│   ├── schematics/          ← .schem files for schematic structures
│   │   ├── desert_shrine.schem
│   │   └── watchtower.schem
│   └── pools/               ← Jigsaw template pool definitions
│       ├── village_plains/
│       │   ├── streets.toml
│       │   ├── houses.toml
│       │   └── decorations.toml
│       └── my_dungeon/
│           ├── rooms.toml
│           └── corridors.toml
```

## Creating a Schematic Structure

Schematic structures are the simplest type. You design a build in-game, save it as a `.schem` file using WorldEdit, and tell Strata where and how often to place it.

### Step 1: Build and Save the Schematic

1. Build your structure in a creative world.
2. Select it with WorldEdit (`//wand`, left-click corner A, right-click corner B).
3. Copy it: `//copy`
4. Save it: `//schem save desert_shrine`

The `.schem` file is saved to `plugins/WorldEdit/schematics/desert_shrine.schem`. Copy it to your profile:

```bash
cp plugins/WorldEdit/schematics/desert_shrine.schem \
   plugins/Strata/profiles/myworld/structures/schematics/desert_shrine.schem
```

### Step 2: Add Placement Config

Add an entry to `structures/structures.toml`:

```toml
# -------------------------------------------------------
# Desert Shrine
# -------------------------------------------------------
[structures.desert_shrine]
# type: "schematic" places a .schem file directly.
type = "schematic"

# schematic: Path to the .schem file, relative to the structures/ directory.
schematic = "schematics/desert_shrine.schem"

# spacing: Grid spacing for placement attempts (in chunks).
# 32 = one attempt every 32x32 chunk area.
spacing = 32

# separation: Minimum distance between instances (in chunks).
# Must be less than spacing.
separation = 8

# salt: Random salt for the placement grid.
# Each structure MUST have a unique salt to avoid all structures
# clustering in the same locations.
salt = 73481920

# biome-filter: List of biome IDs or zone names where this structure can generate.
# Zone names match all biomes within that zone.
# Empty list [] = generate everywhere.
biome-filter = ["scorched"]

# y-offset: Vertical offset for placement (in blocks).
# 0 = place at surface. Negative = bury partially. Positive = float above ground.
# Default: 0
y-offset = -2

# rotation: Whether the structure can be randomly rotated.
# Options: "none", "random_90", "random_180", "random"
# Default: "random"
rotation = "random"

# mirror: Whether the structure can be randomly mirrored.
# Options: "none", "random_x", "random_z", "random"
# Default: "none"
mirror = "none"

# terrain-adaptation: How the structure interacts with surrounding terrain.
# "none" = no terrain modification (structure floats or clips into hills).
# "beard_thin" = fills gaps underneath with terrain (natural look).
# "beard_box" = fills a box underneath.
# "bury" = lowers surrounding terrain to match structure base.
# Default: "beard_thin"
terrain-adaptation = "beard_thin"
```

### Step 3: Test

```
/strata reload myworld
```

Teleport to an area within the biome filter and explore. Structures generate on a grid, so you may need to walk a few hundred blocks to find one. Check server logs for any errors loading the schematic.

### Schematic Placement Keys Reference

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `type` | string | required | Must be `"schematic"` |
| `schematic` | string | required | Path to .schem file relative to `structures/` |
| `spacing` | int | required | Grid spacing in chunks |
| `separation` | int | required | Minimum distance between instances in chunks |
| `salt` | int | required | Unique random salt |
| `biome-filter` | array | `[]` | Biome/zone filter. Empty = everywhere |
| `y-offset` | int | `0` | Vertical placement offset |
| `rotation` | string | `"random"` | Rotation mode |
| `mirror` | string | `"none"` | Mirror mode |
| `terrain-adaptation` | string | `"beard_thin"` | Terrain blending |
| `chance` | float | `1.0` | Per-attempt placement probability (0.0 to 1.0) |

## Creating a Jigsaw Structure

Jigsaw structures are modular. You define pools of building pieces, and Strata assembles them together at generation time. Each placement is unique because pieces are chosen randomly from their pools.

This is how villages, woodland mansions, and ocean monuments work.

### Concepts

- **Template Pool:** A collection of structure pieces that can be chosen from. For example, a "houses" pool might contain 8 different house designs.
- **Jigsaw Connector:** A marked position on each piece where another piece can attach. Connectors have a name (e.g., `"street_end"`) and a facing direction. Two pieces connect when their connector names match and they face each other.
- **Depth:** How many levels of piece connections to follow. Depth 1 = start piece only. Depth 6 = six layers of connected pieces.

### Step 1: Create the Pieces

Build each piece in a creative world with WorldEdit. Each piece needs **jigsaw blocks** (or marked positions in the schematic metadata) at connection points.

In practice, you build the piece, mark connection points by placing jigsaw blocks at the edges where other pieces should attach, and save each piece as its own `.schem`:

```bash
//schem save village_house_01
//schem save village_house_02
//schem save village_street_straight
//schem save village_street_corner
//schem save village_well
```

Copy all schematic files to your profile's `structures/schematics/` directory.

### Step 2: Define Template Pools

Create pool definition files in `structures/pools/`. Each pool is a TOML file listing available pieces and their weights.

**`structures/pools/my_village/streets.toml`:**

```toml
# Template Pool: Village Streets
[pool]
id = "my_village/streets"

[[pool.entries]]
schematic = "schematics/village_street_straight.schem"
weight = 10
# connectors: Define where other pieces attach.
[[pool.entries.connectors]]
name = "street_end"
position = [0, 0, 7]    # relative position within the piece
direction = "south"

[[pool.entries.connectors]]
name = "street_end"
position = [0, 0, -1]
direction = "north"

[[pool.entries.connectors]]
name = "building_side"
position = [4, 0, 3]
direction = "east"

[[pool.entries]]
schematic = "schematics/village_street_corner.schem"
weight = 3
[[pool.entries.connectors]]
name = "street_end"
position = [0, 0, 7]
direction = "south"

[[pool.entries.connectors]]
name = "street_end"
position = [7, 0, 0]
direction = "east"
```

**`structures/pools/my_village/houses.toml`:**

```toml
[pool]
id = "my_village/houses"

[[pool.entries]]
schematic = "schematics/village_house_01.schem"
weight = 5
[[pool.entries.connectors]]
name = "building_side"
position = [-1, 0, 2]
direction = "west"

[[pool.entries]]
schematic = "schematics/village_house_02.schem"
weight = 5
[[pool.entries.connectors]]
name = "building_side"
position = [-1, 0, 3]
direction = "west"
```

### Step 3: Add Placement Config

In `structures/structures.toml`:

```toml
[structures.my_village]
type = "jigsaw"

# start-pool: The template pool used for the first piece.
start-pool = "my_village/streets"

# start-piece: Specific piece from the start pool to always use first.
# If omitted, a random piece from the start pool is chosen.
# start-piece = "schematics/village_well.schem"

# spacing: Grid spacing in chunks.
spacing = 28

# separation: Minimum distance between villages in chunks.
separation = 8

# salt: Unique random salt.
salt = 10387312

# max-pieces: Maximum total pieces per structure instance.
# Controls overall structure size. More pieces = larger structure.
max-pieces = 30

# max-depth: Maximum jigsaw assembly depth.
# Depth 1 = start piece only. Higher = more connected pieces.
max-depth = 10

# terrain-adaptation: How the structure sits on terrain.
terrain-adaptation = "beard_thin"

# biome-filter: Where this structure can generate.
biome-filter = [
    "sanctuary_meadow", "sanctuary_farmland",
    "verdant_plains", "verdant_rolling_hills",
]

# pool-references: Map connector names to template pools.
# When the assembler encounters a connector with name "building_side",
# it looks up pieces from the "my_village/houses" pool.
[structures.my_village.pool-references]
street_end = "my_village/streets"
building_side = "my_village/houses"
```

### Jigsaw Placement Keys Reference

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `type` | string | required | Must be `"jigsaw"` |
| `start-pool` | string | required | Pool ID for the first piece |
| `spacing` | int | required | Grid spacing in chunks |
| `separation` | int | required | Minimum distance in chunks |
| `salt` | int | required | Unique random salt |
| `max-pieces` | int | `12` | Maximum pieces per instance |
| `max-depth` | int | `6` | Maximum assembly depth |
| `terrain-adaptation` | string | `"beard_thin"` | Terrain blending mode |
| `biome-filter` | array | `[]` | Biome/zone filter |
| `pool-references` | table | `{}` | Maps connector names to pool IDs |
| `start-piece` | string | none | Specific first piece (optional) |

## Procedural Structures

Procedural structures are generated algorithmically at runtime. They do not use schematics or template pools. Instead, they define parameters that control an algorithm (e.g., mineshaft corridors, dungeon rooms).

Procedural structures are generally used for things that need infinite variation and do not have a fixed visual design.

```toml
[structures.custom_mineshaft]
type = "procedural"

# chance: Per-chunk probability of generating.
# 0.004 = about 1 in 250 chunks.
chance = 0.004

# y-range: Vertical range for generation.
y-range = [-40, 50]

# Parameters specific to mineshafts:
# corridor-length: Min and max corridor segment length.
corridor-length = [5, 15]

# branch-chance: Probability of a corridor branching at each segment.
branch-chance = 0.4

# room-chance: Probability of a room at a branch point.
room-chance = 0.3
```

For dungeons (mob spawner rooms):

```toml
[structures.dungeon]
type = "procedural"

# attempts: Number of placement attempts per chunk.
# Each attempt picks a random position and checks if it is valid.
attempts = 8

# y-range: Height range for dungeons.
y-range = [-56, 56]
```

### Procedural Placement Keys Reference

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `type` | string | required | Must be `"procedural"` |
| `chance` | float | varies | Per-chunk probability |
| `attempts` | int | varies | Placement attempts per chunk |
| `y-range` | array | required | `[min-y, max-y]` generation range |

Additional parameters depend on the specific procedural structure type. Check the comments in the default profile's `structures.toml` for all available options.

## Strongholds

Strongholds have special placement rules because they use a ring-based system centered on world spawn:

```toml
[structures.stronghold]
type = "jigsaw"

# count: Total number of strongholds in the world.
count = 128

# distance: Distance from spawn to the first ring (in chunks).
distance = 32

# spread: Ring spread factor. Higher = more rings, closer together.
spread = 3
```

## Biome Filters

The `biome-filter` array accepts both individual biome IDs and zone names:

```toml
# Individual biome IDs:
biome-filter = ["sanctuary_meadow", "verdant_plains"]

# Zone names (matches ALL biomes in that zone):
biome-filter = ["scorched"]

# Mix of both:
biome-filter = ["scorched", "frostfang_alpine_meadow"]

# Empty = generate everywhere:
biome-filter = []
```

## Testing Structures

After adding or modifying a structure, reload the profile:

```
/strata reload myworld
```

Then explore newly generated terrain. Structures generate on a grid determined by `spacing`, so you may need to travel some distance.

**Tips for finding structures quickly:**

- Lower `spacing` temporarily (e.g., set it to 4) to make structures very frequent while testing. Reset it to the intended value when you are done.
- Set `separation` to 1 temporarily so structures can generate right next to each other.
- Use `/strata locate <structure_id>` to find the nearest instance (if this command is available in your Strata version).

**Common issues:**

- **Structure does not appear:** Check the biome filter. The structure only generates in listed biomes/zones. Also check that `spacing` and `separation` values are reasonable.
- **Schematic loads wrong:** Make sure the .schem file is in the correct path and is a valid WorldEdit schematic.
- **Jigsaw pieces do not connect:** Verify that connector names match between pieces. A connector named `"street_end"` only connects to another `"street_end"` connector facing the opposite direction.
- **Structure clips into terrain:** Change `terrain-adaptation` to `"beard_thin"` or `"beard_box"` to fill gaps underneath.

## Salt Values

Every structure needs a unique `salt` value. If two structures share the same salt, they compete for the same grid positions and one will crowd out the other. Pick any large integer and make sure it is not used by another structure in your profile.

Convention from the default profiles: use 8-digit numbers in the 10000000-99999999 range. The exact value does not matter as long as it is unique.
