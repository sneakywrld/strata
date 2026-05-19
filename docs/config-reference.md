# Config Reference

Complete reference of all TOML configuration keys in Strata, organized by config file. Every key listed here is also documented with inline comments in the default profile files.

---

## profile.toml

Profile metadata and world settings. This is the only required file in a profile.

### Root Keys

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `name` | string | required | Display name for the profile. |
| `description` | string | `""` | Short description of the profile. |
| `author` | string | `""` | Profile author name. |
| `version` | string | `"1.0.0"` | Profile version string. |
| `environment` | string | `"NORMAL"` | Dimension type: `"NORMAL"`, `"NETHER"`, or `"THE_END"`. |
| `extends` | string | none | Parent profile ID to inherit from. Only overridden keys take effect. |

### `[world]`

| Key | Type | Default | Range | Description |
|-----|------|---------|-------|-------------|
| `min-y` | int | `-64` | -2048 to 0 | Minimum world Y-level. |
| `max-y` | int | `320` | 128 to 2048 | Maximum world Y-level. |
| `sea-level` | int | `63` | 0 to 256 | Sea level Y-coordinate. |
| `ceiling` | bool | `false` | | Enable bedrock ceiling (nether-style). |
| `ceiling-y` | int | `128` | 64 to 2048 | Y-level of the bedrock ceiling. |

### Section References

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `[terrain].config-dir` | string | `"terrain"` | Directory containing terrain TOML files. |
| `[noise].config-dir` | string | `"noise"` | Directory containing noise function TOML files. |
| `[carvers].config-dir` | string | `"carvers"` | Directory containing carver TOML files. |
| `[features].config-dir` | string | `"features"` | Directory containing feature TOML files. |
| `[structures].config-dir` | string | `"structures"` | Directory containing structure TOML files. |
| `[entities].config-dir` | string | `"entities"` | Directory containing entity spawning TOML files. |
| `[biomes].config-dir` | string | `"biomes"` | Directory containing biome TOML files. |
| `[biomes].total-count` | int | auto | Expected total biome count (for validation). |

---

## terrain/density.toml

Controls 3D terrain density. Positive density = solid block; negative = air.

### `[density]`

| Key | Type | Default | Range | Description |
|-----|------|---------|-------|-------------|
| `base-offset` | float | `0.0` | -1.0 to 1.0 | Shifts the density curve up (more land) or down (more air). |
| `height-factor` | float | `1.0` | 0.1 to 5.0 | How strongly Y-level affects density. Higher = sharper solid-to-air transition. |
| `y-scale` | float | `1.0` | 0.25 to 4.0 | Vertical compression. >1.0 = shorter hills, <1.0 = taller features. |
| `variation-scale` | float | `0.15` | 0.0 to 1.0 | 3D noise influence. 0.0 = pure heightmap, 1.0 = heavy overhangs. |
| `variation-noise` | string | `"strata:terrain_3d"` | | Noise function reference for 3D variation. |
| `gradient-start-y` | int | `-64` | | Y-level where density starts transitioning from solid. |
| `gradient-end-y` | int | `128` | | Y-level where density reaches minimum (air). |
| `gradient-curve` | string | `"smooth"` | | Gradient shape: `"linear"`, `"smooth"`, or `"steep"`. |
| `continental-weight` | float | `0.8` | 0.0 to 2.0 | How strongly continentalness affects base height. |
| `continental-noise` | string | `"strata:continentalness"` | | Noise function reference for continental shape. |
| `erosion-weight` | float | `0.6` | 0.0 to 2.0 | How strongly erosion smooths terrain. |
| `erosion-noise` | string | `"strata:erosion"` | | Noise function reference for erosion. |

---

## terrain/splines.toml

Maps climate parameters to terrain height using cubic splines.

### Spline Definition

Each spline is a `[splines.<name>]` table:

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `description` | string | `""` | Human-readable description. |
| `points` | array | required | List of `[input, output]` control point pairs. |
| `apply-when` | table | none | Conditional activation (optional). |

### `apply-when` Conditions

| Key | Type | Description |
|-----|------|-------------|
| `min-continentalness` | float | Minimum continentalness value for spline to apply. |
| `max-continentalness` | float | Maximum continentalness value. |
| `min-erosion` | float | Minimum erosion value. |
| `max-erosion` | float | Maximum erosion value. |

---

## terrain/continents.toml

Controls large-scale land/ocean distribution.

### `[continents]`

| Key | Type | Default | Range | Description |
|-----|------|---------|-------|-------------|
| `land-ratio` | float | `0.55` | 0.1 to 0.9 | Fraction of world that is land. |
| `coastal-width` | int | `200` | 50 to 500 | Width of coastal transition (blocks). |
| `continent-scale` | int | `8000` | 2000 to 50000 | Approximate continent diameter (blocks). |
| `sub-continent-scale` | int | `3000` | 500 to 10000 | Peninsulas, bays, large islands (blocks). |
| `detail-scale` | int | `500` | 100 to 2000 | Fine coastal features (blocks). |
| `ocean-floor-depth` | int | `40` | 10 to 100 | Average ocean floor depth below sea level. |
| `trench-depth` | int | `30` | 0 to 60 | Max depth of ocean trenches below average floor. |
| `shelf-width` | int | `300` | 50 to 1000 | Continental shelf width (blocks). |
| `island-frequency` | float | `0.3` | 0.0 to 1.0 | How often islands appear in oceans. |
| `island-size-range` | array | `[50, 400]` | | `[min, max]` island diameter (blocks). |
| `volcanic-island-chance` | float | `0.15` | 0.0 to 1.0 | Fraction of volcanic islands. |
| `primary-noise` | string | `"strata:continentalness"` | | Main continental shape noise reference. |
| `sub-noise` | string | `"strata:sub_continentalness"` | | Sub-continental detail noise reference. |
| `detail-noise` | string | `"strata:coastal_detail"` | | Fine coastal detail noise reference. |

---

## noise/functions.toml

Named noise function definitions. Each function is a `[functions."<id>"]` table.

### Common Keys (all noise types)

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `type` | string | required | Noise algorithm: `"simplex"`, `"perlin"`, `"open_simplex_2"`, `"cellular"`, `"value"`, `"ridged_multi"`, `"composite"`, `"domain_warp"`. |
| `frequency` | float | required | Base sampling frequency. Lower = larger features. |
| `seed-offset` | int | `0` | Added to world seed for decorrelation. |

### Fractal Settings (`[functions."<id>".fractal]`)

| Key | Type | Default | Range | Description |
|-----|------|---------|-------|-------------|
| `octaves` | int | `1` | 1 to 8 | Number of noise layers stacked. |
| `lacunarity` | float | `2.0` | 1.5 to 4.0 | Frequency multiplier between octaves. |
| `gain` | float | `0.5` | 0.1 to 0.9 | Amplitude multiplier between octaves (persistence). |

### Cellular-Specific Keys

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `distance-function` | string | `"euclidean"` | Distance metric: `"euclidean"`, `"manhattan"`, `"hybrid"`. |
| `return-type` | string | `"distance"` | Return value: `"cell_value"`, `"distance"`, `"distance2"`, `"distance2_sub"`. |

### Composite-Specific Keys

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `source` | string | required | Source noise function ID. |
| `operations` | array | required | List of operation tables (see [Noise Function Guide](noise-function-guide.md)). |

### Domain Warp-Specific Keys

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `source` | string | required | Noise function whose coordinates are distorted. |
| `warp-noise` | string | required | Noise function used for distortion. |
| `warp-amplitude` | float | required | Displacement distance (blocks). |
| `warp-frequency` | float | required | Frequency of the warp noise. |

---

## surface/rules.toml

Surface block placement rules. Rules are evaluated in order; the first match wins.

### Rule Entry (`[[rules]]`)

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `name` | string | `""` | Human-readable rule name (for debugging). |

### Conditions (`[rules.conditions]`)

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `depth` | int | none | Exact depth from surface (0 = topmost block). |
| `min-depth` | int | none | Minimum depth. |
| `max-depth` | int | none | Maximum depth. |
| `min-y` | int | none | Minimum Y-level. |
| `max-y` | int | none | Maximum Y-level. |
| `underwater` | bool | `false` | Block is below sea level with water above. |
| `min-slope` | float | none | Minimum terrain slope (degrees). |
| `max-slope` | float | none | Maximum terrain slope (degrees). |
| `coastal-proximity` | int | none | Within this many blocks of ocean/river. |
| `noise` | string | none | Noise function reference for variation. |
| `noise-threshold` | float | none | Noise value threshold (rule applies when noise > threshold). |

### Output (`[rules.output]`)

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `block` | string | required | Block ID to place (e.g., `"minecraft:grass_block"`). |

---

## carvers/caves.toml

Cave carver configuration. Each carver type is a `[carvers.<type>]` table.

### Cheese Caves (`[carvers.cheese]`)

| Key | Type | Default | Range | Description |
|-----|------|---------|-------|-------------|
| `enabled` | bool | `true` | | Enable cheese cave generation. |
| `noise` | string | `"strata:cheese_cave"` | | 3D noise function for chamber shapes. |
| `threshold` | float | `-0.5` | -1.0 to 0.0 | Noise threshold. Lower = larger chambers. |
| `y-range` | array | `[-56, 40]` | | `[min-y, max-y]` generation range. |
| `max-height` | int | `30` | 5 to 60 | Maximum chamber vertical extent (blocks). |
| `floor-level` | int | `-10` | | Y-level below which floors may contain water/lava. |
| `aquifer-interaction` | bool | `true` | | Whether caves interact with aquifer system. |

### Spaghetti Caves (`[carvers.spaghetti]`)

| Key | Type | Default | Range | Description |
|-----|------|---------|-------|-------------|
| `enabled` | bool | `true` | | Enable spaghetti tunnel generation. |
| `noise` | string | `"strata:spaghetti_cave"` | | Noise function for tunnel paths. |
| `threshold` | float | `-0.6` | -1.0 to 0.0 | Noise threshold. Lower = more tunnels. |
| `y-range` | array | `[-56, 50]` | | `[min-y, max-y]` generation range. |
| `width-range` | array | `[2, 5]` | | `[min, max]` tunnel width (blocks). |
| `vertical-stretch` | float | `0.8` | | Vertical stretch factor. >1.0 = taller tunnels. |

### Noodle Caves (`[carvers.noodle]`)

| Key | Type | Default | Range | Description |
|-----|------|---------|-------|-------------|
| `enabled` | bool | `true` | | Enable noodle crevice generation. |
| `noise` | string | `"strata:noodle_cave"` | | Noise function for crevice placement. |
| `threshold` | float | `-0.65` | -1.0 to 0.0 | Noise threshold. Lower = more crevices. |
| `y-range` | array | `[-56, 30]` | | `[min-y, max-y]` generation range. |
| `width-range` | array | `[1, 2]` | | `[min, max]` crevice width (blocks). |
| `vertical-stretch` | float | `2.0` | | Vertical stretch factor (tall and thin). |

---

## carvers/ravines.toml

### `[carvers.ravine]`

| Key | Type | Default | Range | Description |
|-----|------|---------|-------|-------------|
| `enabled` | bool | `true` | | Enable ravine generation. |
| `chance` | float | `0.02` | 0.0 to 0.2 | Per-chunk probability. |
| `y-range` | array | `[10, 72]` | | `[min-y, max-y]` generation range. |
| `max-depth` | int | `40` | 10 to 80 | Maximum carve depth below start Y. |
| `width-range` | array | `[2, 8]` | | `[min, max]` top width (blocks). V-shaped. |
| `length-range` | array | `[50, 200]` | | `[min, max]` length (blocks). |
| `taper` | float | `1.5` | 1.0 to 3.0 | V-shape steepness. 1.0 = straight walls. |
| `branch-chance` | float | `0.1` | 0.0 to 0.5 | Probability of branching. |
| `water-level` | int | `10` | | Y-level where ravines fill with water. -64 = disable. |
| `lava-level` | int | `-20` | | Y-level where ravines fill with lava. |

---

## water/rivers.toml

### `[rivers]`

| Key | Type | Default | Range | Description |
|-----|------|---------|-------|-------------|
| `enabled` | bool | `true` | | Enable river generation. |
| `flow-threshold` | int | `150` | 50 to 500 | Minimum accumulated flow. Lower = more rivers. |
| `min-width` | int | `3` | 1 to 10 | Narrowest stream width (blocks). |
| `max-width` | int | `20` | 5 to 50 | Widest river width (blocks). |
| `width-growth-rate` | float | `0.3` | 0.1 to 1.0 | How quickly rivers widen with flow. |
| `min-depth` | int | `2` | 1 to 5 | Shallowest depth (blocks). |
| `max-depth` | int | `6` | 3 to 15 | Deepest depth (blocks). |
| `meander-amplitude` | int | `30` | 0 to 100 | Curve displacement from direct path (blocks). |
| `meander-noise` | string | `"strata:river_meander"` | | Noise function for curves. |
| `bank-noise` | string | `"strata:river_bank"` | | Noise function for bank variation. |
| `bank-variation` | float | `2.0` | 0.0 to 5.0 | Bank irregularity (blocks). |
| `shallow-bed` | string | `"minecraft:sand"` | | Block for shallow riverbeds. |
| `deep-bed` | string | `"minecraft:gravel"` | | Block for deep riverbeds. |
| `clay-patches` | bool | `true` | | Place clay patches in riverbeds. |
| `clay-frequency` | float | `0.3` | 0.0 to 1.0 | How often clay patches appear. |
| `sugar-cane` | bool | `true` | | Place sugar cane on banks. |
| `lily-pads` | bool | `true` | | Place lily pads on slow sections. |
| `lily-pad-density` | float | `0.15` | 0.0 to 1.0 | Density of lily pads. |
| `seagrass` | bool | `true` | | Place seagrass in deeper sections. |

---

## water/oceans.toml

### `[oceans]`

| Key | Type | Default | Range | Description |
|-----|------|---------|-------|-------------|
| `enabled` | bool | `true` | | Enable ocean generation. |
| `base-depth` | int | `40` | 10 to 100 | Average ocean floor depth below sea level. |
| `shelf-depth` | int | `12` | 5 to 30 | Continental shelf depth (blocks). |
| `shelf-width` | int | `300` | 50 to 1000 | Shelf distance from coast (blocks). |
| `slope-gradient` | float | `0.4` | 0.1 to 1.0 | Steepness of the continental slope. |
| `floor-noise` | string | `"strata:ocean_floor"` | | Noise for floor variation. |
| `floor-variation` | int | `8` | 0 to 30 | Floor depth variation (blocks). |
| `ridge-enabled` | bool | `true` | | Generate mid-ocean ridges. |
| `ridge-height` | int | `20` | 5 to 50 | Ridge height above floor (blocks). |
| `ridge-frequency` | float | `0.0005` | 0.0001 to 0.005 | Ridge spacing. |
| `trench-enabled` | bool | `true` | | Generate deep ocean trenches. |
| `trench-depth` | int | `30` | 10 to 60 | Trench depth below floor (blocks). |
| `trench-frequency` | float | `0.0003` | 0.0001 to 0.003 | Trench spacing. |
| `magma-vents` | bool | `true` | | Place magma vents. |
| `vent-frequency` | float | `0.05` | 0.0 to 1.0 | Vent placement frequency. |
| `coral-enabled` | bool | `true` | | Enable coral reefs. |
| `coral-min-depth` | int | `3` | 1 to 10 | Minimum water depth for coral. |
| `coral-max-depth` | int | `20` | 5 to 40 | Maximum water depth for coral. |
| `coral-density` | float | `0.6` | 0.0 to 1.0 | Coral cluster density. |
| `kelp-enabled` | bool | `true` | | Enable kelp forests. |
| `kelp-min-depth` | int | `5` | 3 to 15 | Minimum water depth for kelp. |
| `kelp-max-height` | int | `20` | 5 to 40 | Maximum kelp stalk height. |
| `kelp-density` | float | `0.4` | 0.0 to 1.0 | Kelp forest density. |
| `seagrass-enabled` | bool | `true` | | Enable seagrass. |
| `seagrass-density` | float | `0.3` | 0.0 to 1.0 | Seagrass coverage density. |

---

## water/lakes.toml

### `[lakes]`

| Key | Type | Default | Range | Description |
|-----|------|---------|-------|-------------|
| `enabled` | bool | `true` | | Enable lake generation. |
| `min-size` | int | `5` | 2 to 20 | Minimum lake radius (blocks). |
| `max-size` | int | `40` | 10 to 100 | Maximum lake radius (blocks). |
| `min-depth` | int | `3` | 1 to 10 | Minimum lake depth (blocks). |
| `max-depth` | int | `8` | 3 to 30 | Maximum lake depth (blocks). |
| `frequency` | int | `2` | 0 to 10 | Lakes per 256x256 region. |
| `bed-material` | string | `"minecraft:clay"` | | Block lining the lake bottom. |
| `shore-material` | string | `"minecraft:sand"` | | Block around the lake shore. |
| `lily-pads` | bool | `true` | | Place lily pads on the surface. |
| `lily-pad-density` | float | `0.25` | 0.0 to 1.0 | Lily pad density. |
| `reeds` | bool | `true` | | Place sugar cane at edges. |
| `dripleaf` | bool | `false` | | Place dripleaf in shallows. |

---

## water/aquifers.toml

### `[aquifers]`

| Key | Type | Default | Range | Description |
|-----|------|---------|-------|-------------|
| `enabled` | bool | `true` | | Enable underground aquifers. |
| `noise` | string | `"strata:aquifer"` | | 3D noise for aquifer boundaries. |
| `barrier-noise` | string | `"strata:terrain_3d"` | | Noise for wall thickness (anti-leak). |
| `water-level-range` | array | `[0, 50]` | | `[min, max]` Y-levels for water surfaces. |
| `water-threshold` | float | `0.3` | 0.0 to 1.0 | Noise threshold for water aquifers. |
| `lava-enabled` | bool | `true` | | Enable lava-filled aquifers. |
| `lava-threshold-y` | int | `-20` | -64 to 0 | Y-level below which aquifers contain lava. |
| `lava-threshold` | float | `0.4` | 0.0 to 1.0 | Noise threshold for lava aquifers. |
| `drip-particles` | bool | `true` | | Enable drip particles above aquifers. |
| `glow-lichen` | bool | `true` | | Place glow lichen near water surfaces. |
| `glow-lichen-density` | float | `0.15` | 0.0 to 1.0 | Glow lichen density. |

---

## Biome TOML Files

Each biome is a separate `.toml` file in the `biomes/` directory tree. See the [Biome Creation Guide](biome-creation-guide.md) for a walkthrough.

### `[biome]`

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `id` | string | required | Unique namespaced identifier (e.g., `"strata:sanctuary_meadow"`). |
| `display-name` | string | required | Human-readable name. |
| `category` | string | `"PLAINS"` | Biome category. Values: `NONE`, `PLAINS`, `FOREST`, `TAIGA`, `DESERT`, `SWAMP`, `SAVANNA`, `BADLANDS`, `JUNGLE`, `MOUNTAIN`, `OCEAN`, `RIVER`, `BEACH`, `CAVE`, `MUSHROOM`, `NETHER`, `END`. |
| `zone` | string | required | Zone key from `zones/zones.toml`. |

### `[biome.climate]`

| Key | Type | Default | Range | Description |
|-----|------|---------|-------|-------------|
| `temperature` | float | required | -1.0 to 1.0 | Hot/cold axis. |
| `humidity` | float | required | -1.0 to 1.0 | Wet/dry axis. |
| `continentalness` | float | required | -1.0 to 1.0 | Ocean/inland axis. |
| `erosion` | float | required | -1.0 to 1.0 | Mountain/flat axis. |
| `weirdness` | float | required | -1.0 to 1.0 | Normal/mutated axis. |

### `[biome.terrain]`

| Key | Type | Default | Range | Description |
|-----|------|---------|-------|-------------|
| `base-height` | float | `4.0` | -64.0 to 256.0 | Y offset from sea level. |
| `height-variation` | float | `2.5` | 0.0 to 30.0 | Terrain undulation. |

### `[biome.surface]`

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `top-block` | string | `"minecraft:grass_block"` | Top surface block. |
| `filler-block` | string | `"minecraft:dirt"` | Sub-surface block. |
| `filler-depth` | int | `3` | Filler thickness. Range: 1-8. |
| `underwater-block` | string | `"minecraft:gravel"` | Block under water bodies. |

### `[biome.features]`

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `trees` | table | `{}` | `types` (string array) + `density` (float 0-30+). |
| `grass-density` | float | `0.0` | Grass patches per chunk (0-20). |
| `flower-density` | float | `0.0` | Flower patches per chunk (0-15). |
| `flower-types` | array | `[]` | Flower keys from `vegetation.toml`. |
| `pumpkin-chance` | float | `0.0` | Per-chunk pumpkin probability (0-1). |

### `[biome.spawning]`

Each mob entry is `mob_name = { weight = N, min-group = N, max-group = N }`.

---

## features/ores.toml

Each ore is an `[[ores]]` array entry.

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `id` | string | required | Unique identifier. |
| `block` | string | required | Ore block ID. |
| `deepslate-block` | string | none | Deepslate variant (auto-used below Y=0). |
| `size` | int | required | Blocks per vein. |
| `count` | float | required | Veins per chunk. Fractional values allowed (e.g., 0.17 = ~1 in 6 chunks). |
| `y-range` | array | required | `[min-y, max-y]` generation range. |
| `distribution` | string | `"uniform"` | Distribution within Y range: `"uniform"`, `"triangle"`, `"trapezoid"`. |
| `discard-chance` | float | `0.0` | Chance to skip exposed surfaces (0.0-1.0). Higher = more buried. |
| `biome-filter` | array | `[]` | Restrict to these biomes/zones. Empty = everywhere. |

---

## features/trees.toml

Each tree type is a `[trees.<name>]` table.

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `type` | string | required | Shape template: `"small_oak"`, `"fancy_oak"`, `"large_oak"`, `"swamp_oak"`, `"small_birch"`, `"tall_birch"`, `"small_spruce"`, `"mega_spruce"`, `"dark_oak"`, `"small_jungle"`, `"mega_jungle"`, `"acacia"`, `"cherry"`, `"mangrove"`, `"azalea"`, `"dead_tree"`. |
| `trunk` | string | required | Trunk block ID. |
| `leaves` | string | none | Leaf block ID (omit for dead trees). |
| `min-height` | int | required | Minimum trunk height. |
| `max-height` | int | required | Maximum trunk height. |
| `bee-nest-chance` | float | `0.0` | Probability of a bee nest (0.0-1.0). |
| `vines` | bool | `false` | Grow vines on trunk/leaves. |
| `cocoa-chance` | float | `0.0` | Probability of cocoa pods (jungle). |
| `roots` | bool | `false` | Generate root blocks (mangrove). |
| `propagule-chance` | float | `0.0` | Chance of hanging propagules (mangrove). |
| `ground-cover` | string | none | Block placed below tree (e.g., podzol, rooted dirt). |
| `flowering-leaves` | string | none | Alternate leaf block (azalea). |
| `flowering-ratio` | float | `0.0` | Ratio of flowering leaves mixed in. |

---

## features/vegetation.toml

### Grass/Fern Types (`[vegetation.<name>]`)

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `block` | string | required | Block ID. |
| `spread` | int | required | Blocks placed per attempt. |
| `chance` | float | required | Probability per valid position (0.0-1.0). |

### Flowers (`[vegetation.flowers.<name>]`)

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `block` | string | required | Block ID. |
| `weight` | int | required | Relative spawn chance. Higher = more common. |

### Special Vegetation (`[vegetation.<name>]`)

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `block` | string | required | Block ID. |
| `max-height` | int | varies | Maximum stalk/vine height. |
| `spread` | int | varies | Blocks per placement. |
| `chance` | float | varies | Per-position probability. |
| `requires-water` | bool | `false` | Must be adjacent to water. |
| `requires-block-below` | string | none | Required block underneath. |
| `placement` | array | `["surface"]` | Where it can generate: `"surface"`, `"cave_floor"`, `"cave_wall"`, `"cave_ceiling"`. |
| `max-length` | int | varies | Maximum hanging length (cave vines). |

---

## features/saplings.toml

### `[saplings]` Global Settings

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `enabled` | bool | `true` | Enable biome-aware sapling rules. |
| `strict-mode` | bool | `false` | If true, denied biomes fully block growth. If false, reduced rate (10%). |

### Per-Sapling (`[saplings.<type>]`)

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `allowed-biomes` | array | `[]` | Biome/zone tags where this sapling grows normally. Empty = everywhere. |
| `denied-biomes` | array | `[]` | Biome/zone tags where growth is restricted. |
| `growth-rate` | float | `1.0` | Multiplier on vanilla growth speed. |

### Biome Overrides (`[saplings.<type>.biome-overrides]`)

Each override is a `biome_or_zone = { ... }` table that can contain:

| Key | Type | Description |
|-----|------|-------------|
| `tree-variant` | string | Grow a different tree type in this biome. |
| `growth-rate` | float | Custom growth rate for this biome. |
| `chance` | float | Probability of the variant (0.0-1.0). |

---

## entities/spawning.toml

Each spawn rule is an `[[spawns]]` array entry.

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `mob` | string | required | Entity type (e.g., `"minecraft:cow"`). |
| `weight` | int | required | Relative spawn chance. |
| `min-group` | int | required | Minimum pack size. |
| `max-group` | int | required | Maximum pack size. |
| `category` | string | required | Spawn category: `"PASSIVE"`, `"HOSTILE"`, `"WATER_CREATURE"`, `"WATER_AMBIENT"`, `"AMBIENT"`. |

### Conditions (`[spawns.conditions]`)

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `surface` | bool | `false` | Must spawn on surface (not in caves). |
| `min-light` | int | none | Minimum light level. |
| `max-light` | int | none | Maximum light level. |
| `min-y` | int | none | Minimum Y-level. |
| `max-y` | int | none | Maximum Y-level. |

---

## zones/zones.toml

### `[settings]` Global Zone Settings

| Key | Type | Default | Range | Description |
|-----|------|---------|-------|-------------|
| `edge-noise` | string | `"strata:zone_edge"` | | Noise function for boundary distortion. |
| `edge-variation` | int | `200` | 0 to 500 | Boundary wobble distance (blocks). |
| `transition-width` | int | `100` | 20 to 300 | Blending distance between zones (blocks). |

### Per-Zone (`[zones.<key>]`)

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `display-name` | string | required | Name shown to players on zone entry. |
| `distance-range` | array | required* | `[min, max]` distance from spawn (blocks). *Required for surface zones. |
| `y-range` | array | required* | `[min-y, max-y]` range. *Required for Y-based zones (e.g., abyssal). |
| `difficulty` | float | `1.0` | Difficulty multiplier for mob scaling. |
| `color` | string | `"#FFFFFF"` | Hex color for maps and displays. |
| `biome-dir` | string | required | Subdirectory within `biomes/` for this zone. |
| `passive-density` | float | `1.0` | Passive mob spawn multiplier. |
| `hostile-density` | float | `1.0` | Hostile mob spawn multiplier. |
| `mythicmobs-table` | string | none | Optional MythicMobs table reference. |

---

## structures/structures.toml

See the [Structure Guide](structure-guide.md) for detailed explanations and examples.

### Common Keys (all structure types)

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `type` | string | required | Structure type: `"jigsaw"`, `"schematic"`, `"procedural"`. |
| `biome-filter` | array | `[]` | Biome/zone filter. Empty = everywhere. |

### Grid Placement Keys (jigsaw, schematic)

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `spacing` | int | required | Grid spacing in chunks. |
| `separation` | int | required | Minimum distance between instances in chunks. |
| `salt` | int | required | Unique random salt for grid offset. |

### Jigsaw-Specific Keys

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `max-pieces` | int | `12` | Maximum pieces per instance. |
| `max-depth` | int | `6` | Maximum assembly depth. |
| `terrain-adaptation` | string | `"beard_thin"` | Terrain blending mode. |
| `count` | int | none | Total count (strongholds only). |
| `distance` | int | none | Ring distance from spawn (strongholds only). |
| `spread` | int | none | Ring spread factor (strongholds only). |

### Schematic-Specific Keys

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `schematic` | string | required | Path to .schem file relative to `structures/`. |
| `y-offset` | int | `0` | Vertical placement offset. |
| `rotation` | string | `"random"` | Rotation: `"none"`, `"random_90"`, `"random_180"`, `"random"`. |
| `mirror` | string | `"none"` | Mirror: `"none"`, `"random_x"`, `"random_z"`, `"random"`. |
| `terrain-adaptation` | string | `"beard_thin"` | Terrain blending mode. |
| `chance` | float | `1.0` | Per-attempt placement probability. |

### Procedural-Specific Keys

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `chance` | float | varies | Per-chunk probability. |
| `attempts` | int | varies | Placement attempts per chunk. |
| `y-range` | array | required | `[min-y, max-y]` generation range. |
