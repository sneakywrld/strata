# Composing Noise Functions

Noise functions are the mathematical foundation of all procedural generation in Strata. Every terrain shape, biome boundary, cave system, river path, and ore vein is driven by noise. This guide explains each noise type, how to configure them in TOML, and how to compose them into complex behaviors.

## Where Noise Is Defined

Noise functions live in `noise/functions.toml` inside your profile directory. Each function has a unique namespaced ID (e.g., `strata:continentalness`) and can be referenced by name from terrain, biome, water, cave, and feature configs.

## Noise Types

Strata supports six noise algorithms, plus three composition types.

### Simplex

The workhorse noise. Produces smooth, organic-looking gradients. Computationally efficient and free of directional artifacts.

**Best for:** Climate parameters, continental shape, vegetation density, zone edges.

```toml
[functions."strata:my_simplex"]
type = "simplex"
frequency = 0.001
seed-offset = 50
[functions."strata:my_simplex".fractal]
octaves = 4
lacunarity = 2.0
gain = 0.5
```

Output range: **-1.0 to 1.0**

### Perlin

Classic gradient noise. Slightly more grid-aligned than simplex (you may notice faint axis-parallel patterns at very low frequencies), but well understood and predictable.

**Best for:** Cave tunnels (spaghetti and noodle caves), terrain detail.

```toml
[functions."strata:my_perlin"]
type = "perlin"
frequency = 0.03
seed-offset = 60
[functions."strata:my_perlin".fractal]
octaves = 2
lacunarity = 2.0
gain = 0.5
```

Output range: **-1.0 to 1.0**

### OpenSimplex2

An improved version of simplex noise with better isotropy (no directional bias). Slightly more expensive to compute but produces higher quality results in 3D.

**Best for:** 3D terrain density (overhangs, arches), cheese caves, aquifer boundaries.

```toml
[functions."strata:my_open_simplex"]
type = "open_simplex_2"
frequency = 0.015
seed-offset = 100
[functions."strata:my_open_simplex".fractal]
octaves = 3
lacunarity = 2.5
gain = 0.4
```

Output range: **-1.0 to 1.0**

### Cellular (Voronoi)

Divides space into irregular cells (like a Voronoi diagram). Each cell has a center point, and the noise value at any position depends on the distance to the nearest cell center. Produces organic, cell-like patterns.

**Best for:** Crystal formations, cracked terrain, hexagonal patterns, rock textures.

```toml
[functions."strata:my_cellular"]
type = "cellular"
frequency = 0.02
seed-offset = 70
# distance-function controls how distance is measured.
# Options: "euclidean" (round cells), "manhattan" (diamond cells),
#          "hybrid" (mix of both)
distance-function = "euclidean"
# return-type controls what value is returned.
# Options: "cell_value" (random per cell), "distance" (distance to nearest),
#          "distance2" (distance to second nearest),
#          "distance2_sub" (distance2 - distance, creates edges)
return-type = "distance"
```

Output range: varies by return type. `cell_value` returns **-1.0 to 1.0**. Distance-based types return **0.0 to 1.0+** (normalize with a clamp if needed).

### Value

Simple interpolated random values on a grid. Less visually interesting than simplex/perlin but extremely fast. The output is blocky at low frequencies.

**Best for:** Quick random variation where visual quality is not critical (ore scatter, spawn chance noise).

```toml
[functions."strata:my_value"]
type = "value"
frequency = 0.06
seed-offset = 400
```

Output range: **-1.0 to 1.0**

### Ridged Multifractal

A fractal noise where each octave's output is inverted from its absolute value, creating sharp ridges and creases. Produces mountain-like ridgelines naturally.

**Best for:** Mountain ridges, sharp peaks, terrain fractures, cliff lines.

```toml
[functions."strata:my_ridge"]
type = "ridged_multi"
frequency = 0.002
seed-offset = 110
[functions."strata:my_ridge".fractal]
octaves = 5
lacunarity = 2.1
gain = 0.5
```

Output range: **-1.0 to 1.0** (peaks at 1.0 along ridges, valleys near -1.0)

## Core Parameters

These parameters apply to all noise types.

### frequency

How "zoomed in" the noise is. Lower frequency = larger features, higher frequency = finer detail.

| Frequency | Approximate wavelength | Use case |
|-----------|----------------------|----------|
| 0.0003 | ~3000 blocks | Continental shape |
| 0.001 | ~1000 blocks | Biome-scale climate |
| 0.005 | ~200 blocks | Regional terrain detail |
| 0.02 | ~50 blocks | Cave tunnels |
| 0.06 | ~17 blocks | Ore scatter, surface patches |

### seed-offset

An integer added to the world seed for this specific noise function. This ensures that different noise functions produce uncorrelated patterns even if they use the same algorithm and frequency. Each noise function should have a unique seed-offset.

### Fractal Settings

Fractal (multi-octave) noise layers multiple copies of the base noise at increasing frequencies and decreasing amplitudes. This adds fine detail on top of broad shapes.

```toml
[functions."strata:example".fractal]
octaves = 4
lacunarity = 2.0
gain = 0.5
```

**octaves** (int, 1 to 8): Number of noise layers. Each octave adds detail at a finer scale. More octaves = richer detail but more computation.

| Octaves | Effect |
|---------|--------|
| 1 | Smooth, blobby shapes |
| 3 | Moderate detail, good for most uses |
| 5 | Rich detail, good for terrain and climate |
| 8 | Very fine detail, expensive |

**lacunarity** (float, 1.5 to 4.0): Frequency multiplier between octaves. Standard is 2.0, meaning each octave is twice as fine as the previous. Higher values skip to finer detail faster.

**gain** (float, 0.1 to 0.9): Amplitude multiplier between octaves. Also called "persistence" in some noise literature.

- 0.5 (standard): Each octave contributes half the amplitude of the previous. Balanced detail.
- 0.3 (low gain): Fine detail is very subtle. Smooth, broad shapes dominate.
- 0.7 (high gain): Fine detail is pronounced. Rough, textured surfaces.

**Relationship between gain and visual character:**

```
gain = 0.3  →  Smooth rolling hills (broad shapes dominate)
gain = 0.5  →  Natural terrain (balanced)
gain = 0.7  →  Rocky, rough terrain (detail is strong)
```

If you omit the `[fractal]` section entirely, the noise function uses a single octave (no fractal layering).

## Composing Noise

### Composite Noise

Composite noise takes the output of a source noise function and transforms it through a chain of mathematical operations. This lets you shape noise without writing code.

```toml
[functions."strata:mountain_shape"]
type = "composite"
source = "strata:ridge"
operations = [
    { op = "abs" },
    { op = "multiply", value = 2.5 },
    { op = "clamp", min = 0.0, max = 1.0 },
]
```

Available operations:

| Operation | Parameters | Formula | Use case |
|-----------|-----------|---------|----------|
| `abs` | none | abs(x) | Turn valleys into ridges |
| `negate` | none | -x | Invert high/low |
| `square` | none | x * x | Emphasize extremes |
| `cube` | none | x * x * x | Preserve sign, emphasize extremes |
| `sqrt` | none | sqrt(x) | Flatten extremes (input should be >= 0) |
| `add` | `value` | x + value | Shift output range |
| `multiply` | `value` | x * value | Scale output range |
| `min` | `value` | min(x, value) | Cap at a ceiling |
| `max` | `value` | max(x, value) | Enforce a floor |
| `clamp` | `min`, `max` | clamp(x, min, max) | Constrain range |
| `lerp` | `a`, `b` | lerp(a, b, x) | Map [0,1] to [a,b] |
| `power` | `value` | x ^ value | Exponential shaping |
| `invert` | none | 1.0 / x | Reciprocal (avoid zero!) |
| `spline` | `points` | cubic spline | Arbitrary remapping |
| `terrace` | `points` | stepped spline | Plateau/mesa shapes |

### Spline Remapping

Splines let you remap noise values through arbitrary curves. Provide control points as `[input, output]` pairs. Strata interpolates smoothly between them using cubic splines.

```toml
[functions."strata:continent_shaped"]
type = "composite"
source = "strata:continentalness"
operations = [
    { op = "spline", points = [
        [-1.0, -50.0],
        [-0.2,  -5.0],
        [ 0.0,   0.0],
        [ 0.3,  15.0],
        [ 1.0,  80.0],
    ]},
]
```

This takes raw continentalness noise (-1 to 1) and maps it to terrain height offsets (-50 to 80 blocks). The curve is not linear; it bends smoothly through each control point.

**Terrace** works similarly but creates flat plateaus between points instead of smooth curves, which is useful for mesa and plateau terrain.

### Adding Two Noises Together

To combine two noise functions, define both individually and then create a composite that adds them:

```toml
# Base continental noise
[functions."strata:continental_base"]
type = "simplex"
frequency = 0.0003
seed-offset = 0
[functions."strata:continental_base".fractal]
octaves = 5
lacunarity = 2.0
gain = 0.45

# Fine coastal detail
[functions."strata:coastal_detail"]
type = "simplex"
frequency = 0.004
seed-offset = 2
[functions."strata:coastal_detail".fractal]
octaves = 3
lacunarity = 2.0
gain = 0.6

# Combined: broad continents + fine coastline detail
[functions."strata:combined_coast"]
type = "composite"
source = "strata:continental_base"
operations = [
    { op = "add_noise", source = "strata:coastal_detail", weight = 0.15 },
]
```

The `add_noise` operation samples a second noise function and blends it with the source. The `weight` parameter controls how strongly the second noise affects the result.

### Multiplying Noises

Use multiplication to "mask" one noise with another. For example, applying a large-scale vegetation density noise as a mask for small-scale flower placement:

```toml
[functions."strata:masked_flowers"]
type = "composite"
source = "strata:vegetation_density"
operations = [
    { op = "clamp", min = 0.0, max = 1.0 },
    { op = "multiply_noise", source = "strata:surface_noise" },
]
```

When the vegetation density noise is low (0.0), the multiplication zeros out the surface noise. When density is high (1.0), the surface noise passes through at full strength.

## Domain Warping

Domain warping distorts the input coordinates of a noise function using another noise function before sampling. This creates twisted, organic-looking patterns from regular noise.

```toml
[functions."strata:warped_terrain"]
type = "domain_warp"
# source: The noise function whose coordinates will be distorted.
source = "strata:terrain_3d"
# warp-noise: The noise function used to distort coordinates.
warp-noise = "strata:river_meander"
# warp-amplitude: How far coordinates are displaced (in blocks).
# Higher values = more extreme distortion.
warp-amplitude = 40.0
# warp-frequency: Frequency of the warp noise itself.
# Lower = broad sweeping warps. Higher = tight squiggles.
warp-frequency = 0.003
```

**What domain warping does visually:**

- Low amplitude (5-15): Subtle organic irregularity. Makes straight lines wavy.
- Medium amplitude (20-50): Noticeable distortion. Creates twisted terrain features.
- High amplitude (60-100+): Extreme distortion. Creates surreal, alien landscapes.

**Example: warped cave walls**

Without domain warping, cave walls follow the underlying noise pattern directly, which can look too smooth or regular. Adding a warp makes walls feel more natural:

```toml
[functions."strata:organic_caves"]
type = "domain_warp"
source = "strata:cheese_cave"
warp-noise = "strata:surface_variation"
warp-amplitude = 12.0
warp-frequency = 0.008
```

## Practical Recipes

### Recipe: Continent with Islands

```toml
# Broad continental shape
[functions."strata:continents"]
type = "simplex"
frequency = 0.0003
seed-offset = 0
[functions."strata:continents".fractal]
octaves = 5
lacunarity = 2.0
gain = 0.45

# Island spots (cellular gives natural island clusters)
[functions."strata:island_spots"]
type = "cellular"
frequency = 0.001
seed-offset = 5
distance-function = "euclidean"
return-type = "distance"

# Combine: continental base + island bumps in ocean areas
[functions."strata:land_with_islands"]
type = "composite"
source = "strata:continents"
operations = [
    { op = "add_noise", source = "strata:island_spots", weight = 0.2 },
    { op = "spline", points = [
        [-1.0, -40.0],
        [-0.3, -10.0],
        [-0.1,  -2.0],
        [ 0.0,   3.0],
        [ 0.5,  30.0],
        [ 1.0,  60.0],
    ]},
]
```

### Recipe: Ridged Mountains

```toml
[functions."strata:mountain_ridges"]
type = "ridged_multi"
frequency = 0.002
seed-offset = 110
[functions."strata:mountain_ridges".fractal]
octaves = 5
lacunarity = 2.1
gain = 0.5

[functions."strata:shaped_mountains"]
type = "composite"
source = "strata:mountain_ridges"
operations = [
    { op = "abs" },
    { op = "power", value = 1.5 },
    { op = "multiply", value = 80.0 },
    { op = "clamp", min = 0.0, max = 120.0 },
]
```

### Recipe: Patchy Surface Variation

```toml
[functions."strata:patchy_surface"]
type = "simplex"
frequency = 0.08
seed-offset = 420

# Use for surface rules: when this noise > 0, place coarse dirt instead of grass
# Reference in surface/rules.toml:
#   noise = "strata:patchy_surface"
#   noise-threshold = 0.0
```

## Testing Noise Changes

After editing `noise/functions.toml`, reload the profile:

```
/strata reload myworld
```

Noise changes only affect **newly generated chunks**. To see the effect, teleport to an unexplored area:

```
/tp @s myworld 10000 100 10000
```

If terrain looks wrong after a noise change, check the server logs for configuration errors. Common mistakes:

- **Referencing a noise ID that does not exist.** All noise IDs are case-sensitive. Double-check the exact key.
- **Frequency too high or too low.** A frequency of 0.0003 looks continental. A frequency of 0.3 is block-scale static. Start with values from the default profile and adjust gradually.
- **Missing fractal section.** Without fractal settings, noise uses a single octave (very smooth and blobby). If your terrain looks unnaturally smooth, add fractal octaves.

## Performance Notes

- Each fractal octave roughly doubles the computation for that noise function. Going from 3 octaves to 6 doubles the cost.
- Cellular noise is more expensive than simplex. Use it selectively, not as your primary terrain noise.
- Domain warping adds the cost of sampling the warp noise on top of the source noise.
- Composite operations (abs, clamp, multiply, etc.) are essentially free compared to the noise sampling itself.
- The noise cache (`noise-cache-size` in `strata.toml`) stores recently computed values. Larger cache = less recomputation but more memory.
