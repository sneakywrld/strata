# Contributing to Strata

Thank you for your interest in contributing to Strata, a Minecraft world generation plugin for PaperMC. This guide covers what you need to know to get started, make changes, and submit them for review.

---

## Getting Started

1. **Clone the repository**

   ```
   git clone https://github.com/sneakywrld/strata.git
   cd strata
   ```

2. **Build the project**

   ```
   ./gradlew build
   ```

3. **Run the tests**

   ```
   ./gradlew test
   ```

You will need Java 21 installed. The Gradle wrapper handles everything else.

---

## Code Style

- **Java 21** -- use modern language features where they improve clarity (records, sealed interfaces, pattern matching, etc.).
- **Follow existing patterns.** When in doubt, look at how surrounding code is written and match it.
- **No comments on self-documenting code.** If the code is clear on its own, a comment restating it adds noise. Reserve comments for explaining *why*, not *what*.
- **Meaningful names.** Variable, method, and class names should describe their purpose precisely. Prefer `biomeTemperatureThreshold` over `temp` or `val`.

---

## Project Structure

Strata is split into several modules. Understanding where your change belongs is important.

### `strata-api`

Public interfaces and contracts. Every other module depends on this, so changes here have wide-reaching consequences. If you want to modify or add an API surface, **open an issue first** to discuss the design before writing code.

### `strata-noise`

Noise generation algorithms and mathematical utilities. This is math-heavy code. Any new noise algorithm must include unit tests that verify:
- Output stays within the documented range.
- Output is deterministic for a given seed and coordinates.

### `strata-config`

TOML configuration parsing and validation. All parsing code must handle malformed input gracefully -- never let a bad config crash the server. Return clear error messages that tell the user what went wrong and where.

### `strata-core`

The main world generation engine. This includes pipeline stages, the biome system, water placement, and carvers. Most contributions will land here. If you are unsure where something belongs, it probably belongs in core.

### `strata-nms`

Version-specific NMS (net.minecraft.server) code. This module bridges Strata to a specific Minecraft server version. Any changes here require testing on the target Minecraft version -- compilation alone is not sufficient.

### `strata-paper`

PaperMC plugin integration: lifecycle, commands, event hooks. Changes in this module need a running PaperMC server to verify. Automated unit tests are not enough for plugin-level behavior.

### `strata-migrate`

Converter for importing Terra world generation packs into Strata's format. Testing changes here requires sample Terra packs. If you do not have any, ask in the issue tracker and we will provide some.

### `strata-starter`

Bundled world generation profiles shipped with the plugin. These are TOML configuration files. Every key in every TOML file must have an explanatory comment above it (see the Config Files section below).

---

## Pull Requests

- **One feature or fix per PR.** Do not bundle unrelated changes.
- **Use a descriptive title.** The title should summarize the change in a single line (e.g., "Add Voronoi noise generator with distance metrics" rather than "New noise stuff").
- **Reference issues.** If your PR addresses an open issue, include `Closes #123` or `Fixes #123` in the description.
- **Tests are required for new code.** PRs that add functionality without corresponding tests will be sent back for revision.
- **Keep the diff focused.** Avoid reformatting files you did not otherwise change.

---

## Testing

- **Framework:** JUnit 5 with Mockito for mocking where needed in core tests.
- **Performance tests:** Tag them with `@Tag("benchmark")` so they can be run separately from the main test suite.
- **Determinism:** World generation must be deterministic for a given seed. Tests that verify generation output should assert exact values, not ranges.
- **Run the full suite before submitting:**

  ```
  ./gradlew test
  ```

---

## Config Files

Strata uses TOML for all configuration. Every TOML key must have a comment directly above it that includes:

1. **What the key does** -- a plain-English explanation.
2. **Valid values** -- the type and any constraints (e.g., "Integer, 0-255" or "One of: SMOOTH, LINEAR, CUBIC").
3. **Default value** -- what the system uses if the key is absent.

Example:

```toml
# Controls the overall height multiplier for terrain generation.
# Valid values: Decimal, 0.1 to 10.0
# Default: 1.0
height-scale = 1.0
```

This is not optional. Undocumented config keys make the plugin harder to use.

---

## Reporting Issues

Use GitHub Issues. Include the following information:

- **Minecraft version** (e.g., 1.8.8, 1.21.4)
- **Strata version** (e.g., 26.1.2)
- **Server log** -- the relevant section, or the full log if you are unsure what is relevant
- **Steps to reproduce** -- what you did, what you expected, and what happened instead
- **Configuration** -- if the issue relates to world generation output, include the TOML config you are using

The more detail you provide, the faster we can diagnose the problem.

---

## Questions

If something in this guide is unclear or you need help getting started, open a discussion on the repository. We are happy to help.
