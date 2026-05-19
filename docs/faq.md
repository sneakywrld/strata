# Frequently Asked Questions

## General

**Do I need to configure anything to start?**

No. Install the jar, start the server, run `/strata create myworld elysium`. The Elysium profile works out of the box with 191 biomes and full world generation.

**Does Strata replace vanilla world generation?**

Only for worlds created through Strata. Your existing worlds are untouched. Strata worlds use their own chunk generator.

**Can I use Strata with other world generation plugins?**

Strata manages its own worlds. Other plugins can manage other worlds on the same server without conflict.

**Which Minecraft versions are supported?**

1.8.8 through 26.1.2. Strata detects your server version automatically and loads the appropriate adapter.

## Profiles

**How do I create a custom profile?**

Copy an existing profile directory and modify the TOML files. See the [Profile Creation Guide](profile-creation-guide.md).

**Can I change a world's profile after creation?**

Not directly. Create a new world with the desired profile. Existing chunks keep their original generation.

**How does profile inheritance work?**

Add `extends = "elysium"` to your profile.toml. Your profile inherits all settings from Elysium. You only need to specify the values you want to change.

## Configuration

**What format does Strata use for configs?**

TOML. Not YAML. Every key in every config file has a comment explaining what it does, valid values, and the default.

**How do I change ore rates?**

Edit `features/ores.toml` in your profile, or set the `ore-scarcity-multiplier` in `profile.toml` for a global adjustment. `/strata reload` to apply.

**Can I hot-reload configs?**

Yes. Edit any TOML file and Strata reloads automatically (if hot-reload is enabled in `strata.toml`). Or run `/strata reload`. Changes apply to newly generated chunks only.

## Terra Migration

**Can I import my Terra config pack?**

Yes. Run `/strata migrate <path-to-terra-pack> myprofile`. Strata converts Terra's YAML configs to Strata TOML. A migration report tells you what was converted and what needs manual review.

**Is the migration 100% accurate?**

Most configs convert well. Some Terra-specific features (TerraScript, custom samplers) may need manual adjustment. The migration report details exactly what needs attention.

## Performance

**How fast is chunk generation?**

Target is under 50ms per chunk. Actual speed depends on profile complexity and hardware. Use `/strata pregen` to pre-generate chunks for the best player experience.

**How much memory does Strata use?**

Base overhead is small (~50MB). Cache sizes in `strata.toml` control memory usage. Default settings work well for servers with 4GB+ allocated to the JVM.

**Does Strata support async chunk generation?**

Yes. The generation pipeline is fully thread-safe. Paper's async chunk generation is supported on 1.18+.

## Compatibility

**Does Strata work with MythicMobs?**

Yes. If MythicMobs is installed, Strata zones can reference MythicMobs spawn tables for custom mob spawning. Without MythicMobs, zones use vanilla mob tables and work perfectly.

**Does Strata work with Folia?**

Strata's generation is thread-safe by design, making it compatible with Folia's regionized threading model.

**Does Strata work with Purpur?**

Yes. Purpur is Paper-compatible and fully supported.
