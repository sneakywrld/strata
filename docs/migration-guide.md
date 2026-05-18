# Migrating from Terra to Strata

## Overview

Strata includes a built-in migration tool that converts Terra YAML configuration packs into Strata TOML profiles. The converter handles the bulk of the translation work, but because Strata has its own architecture and design philosophy, some manual adjustments will be needed after conversion.

There are two ways to run the migration:

- **In-game command** -- requires a running server with the Strata plugin installed.
- **Standalone CLI** -- runs outside the server, useful for batch conversion or CI pipelines.

The migration tool is not a 1:1 clone of Terra's config format. It produces a best-effort conversion and generates a detailed report so you know exactly what was translated, what was approximated, and what requires manual work.

---

## Step-by-Step Migration

### 1. Locate Your Terra Pack

Terra packs are typically stored at:

```
plugins/Terra/packs/<pack-name>/
```

Make sure the pack directory contains the full set of YAML files (biomes, noise, palettes, flora, structures, etc.). Incomplete packs will still convert, but the report will flag missing references.

### 2. Run the Migration

**In-game command:**

```
/strata migrate plugins/Terra/packs/my-pack converted-pack
```

**Standalone CLI:**

```
java -jar strata-migrate.jar plugins/Terra/packs/my-pack output/converted-pack
```

Both methods produce the same output: a complete Strata profile directory and a migration report.

### 3. Review the Migration Report

The migration report is generated in the output directory as `migration-report.txt`. It lists three categories:

- **Successfully converted** -- Items that translated cleanly with no issues.
- **Approximated conversions** -- Items that were converted but may not behave identically to the original. These should be reviewed and tested.
- **Unsupported features** -- Items that could not be converted automatically and must be recreated manually in Strata's format.

Read this report carefully before testing. It is the single most important output of the migration process.

### 4. Test the Converted Profile

Load the converted profile on a test world:

```
/strata create testworld converted-pack
```

Fly around at multiple coordinates and biomes. Compare the output against your original Terra world to identify areas that need tuning.

### 5. Tune and Adjust

Open the generated biome TOML files in the profile directory and adjust as needed:

- Noise parameters (frequency, amplitude, octaves)
- Surface configurations (block layers, depth thresholds)
- Feature placement (density, spread, conditions)
- Ore distribution (Strata defaults to intentionally scarcer ores than Terra)

Iterate between editing configs and testing in-game until the result matches your expectations.

---

## Feature Comparison

The following table maps Terra features to their Strata equivalents.

| Terra Feature    | Strata Equivalent    | Notes                                                        |
| ---------------- | -------------------- | ------------------------------------------------------------ |
| Biome YAML       | Biome TOML           | Climate parameters are auto-mapped during conversion.        |
| noise-equation   | Noise functions TOML | Expression syntax is translated to a composable noise graph. |
| Block palettes   | Surface rules TOML   | Layer-based palettes become depth-based surface rules.       |
| Flora/trees      | Feature TOML         | Placement rules differ; review density and spread settings.  |
| Structures       | Structure TOML       | Both jigsaw and schematic-based structures are supported.    |
| Carvers          | Carver TOML          | Similar cave types are available.                            |
| TerraScript      | Manual recreation    | TerraScript is too different to auto-convert; the converter flags these. |
| Pack inheritance  | Profile `extends`    | Similar concept; parent profiles are referenced by name.     |
| Addons           | Strata API plugins   | Different registration API; addon logic must be rewritten.   |

---

## What Gets Converted Automatically

The following elements are translated directly by the migration tool and typically require little or no manual adjustment:

- **Biome definitions** -- Climate parameters, vanilla biome mapping, and basic biome metadata.
- **Noise configurations** -- Sampler type, frequency, octaves, and lacunarity settings.
- **Block palettes** -- Converted to Strata's depth-based surface rules with equivalent block layers.
- **Basic structure definitions** -- Schematic references, bounding boxes, and simple placement conditions.

---

## What Needs Manual Review

These elements are converted on a best-effort basis. The migration report flags each instance so you know where to look.

- **Noise equations** -- Terra uses an expression-based syntax (`noise-equation`) that is translated into Strata's composable noise graph. The approximation is usually close but may produce slightly different terrain shapes. Compare visually and adjust node parameters as needed.
- **Complex flora definitions** -- Terra's weighted random placement translates to Strata's feature system, but density and spread behavior may differ. Check flower fields, tall grass distribution, and custom vegetation.
- **TerraScript structures** -- These are flagged in the report and cannot be auto-converted. Recreate them as schematics or as Strata's declarative structure TOML format.

---

## Known Limitations

- **noise-equation expressions** -- Terra's expression evaluator does not have a direct equivalent in Strata. The converter creates noise graph approximations that produce similar but not identical output.
- **TerraScript** -- Cannot be auto-converted. The migration report lists all affected structures so you can prioritize manual recreation.
- **Biome provider system** -- Terra's noise-based biome selection maps to Strata's 5D climate system. This is a fundamentally different approach, and some loss of fidelity in biome placement is expected. Fine-tune climate parameters after conversion.
- **Custom addon configurations** -- Addon configs are plugin-specific and are not converted. If your Terra pack relies on addon behavior, you will need to reimplement that logic using the Strata API.

---

## After Migration

Once the initial conversion is complete and loaded on a test world, work through the following checklist:

1. **Review each biome TOML** to verify that terrain shape, surface blocks, and feature placement look correct.
2. **Test at multiple coordinates** -- teleport to different biomes and check transitions, ocean floors, mountain peaks, and cave systems.
3. **Adjust noise frequencies** if terrain scale feels different from the original. Strata and Terra use different default frequency ranges, so a global scaling factor may be needed.
4. **Fine-tune ore distribution** -- Strata defaults to intentionally scarcer ores than Terra. If your pack expects higher ore density, increase the `count` and `spread` values in ore feature TOMLs.
5. **Check the migration report one more time** for any approximated or unsupported items you may have missed during initial review.

Migration is an iterative process. Expect to make several rounds of adjustments before the converted profile matches your original pack's intent.
