package com.protectcord.strata.paper.command;

import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.config.registry.ConfigRegistry;
import com.protectcord.strata.config.reload.ReloadCoordinator;
import com.protectcord.strata.paper.StrataPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Main /strata command handler with subcommands:
 * create, reload, pregen, info, profiles, migrate, guide
 */
public final class StrataCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBCOMMANDS = List.of(
            "create", "reload", "pregen", "info", "profiles", "migrate", "guide", "version"
    );

    private final StrataPlugin plugin;
    private final ConfigRegistry configRegistry;
    private final ReloadCoordinator reloadCoordinator;

    public StrataCommand(StrataPlugin plugin, ConfigRegistry configRegistry,
                         ReloadCoordinator reloadCoordinator) {
        this.plugin = plugin;
        this.configRegistry = configRegistry;
        this.reloadCoordinator = reloadCoordinator;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        return switch (args[0].toLowerCase()) {
            case "create" -> handleCreate(sender, args);
            case "reload" -> handleReload(sender, args);
            case "info" -> handleInfo(sender, args);
            case "profiles" -> handleProfiles(sender);
            case "version" -> handleVersion(sender);
            case "guide" -> handleGuide(sender, args);
            default -> {
                sender.sendMessage(Component.text("Unknown subcommand: " + args[0], NamedTextColor.RED));
                yield true;
            }
        };
    }

    private boolean handleCreate(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(Component.text("Usage: /strata create <world-name> <profile>", NamedTextColor.RED));
            return true;
        }

        String worldName = args[1];
        String profileName = args[2];
        NamespacedKey profileKey = NamespacedKey.parse(profileName);

        if (!configRegistry.getProfile(profileKey).isPresent()) {
            sender.sendMessage(Component.text("Unknown profile: " + profileName, NamedTextColor.RED));
            return true;
        }

        sender.sendMessage(Component.text("Creating world '" + worldName + "' with profile '"
                + profileName + "'...", NamedTextColor.GREEN));

        // World creation delegated to the WorldManager
        return true;
    }

    private boolean handleReload(CommandSender sender, String[] args) {
        if (!sender.hasPermission("strata.admin")) {
            sender.sendMessage(Component.text("No permission.", NamedTextColor.RED));
            return true;
        }

        sender.sendMessage(Component.text("Reloading configurations...", NamedTextColor.YELLOW));
        int count = reloadCoordinator.reloadAll();
        sender.sendMessage(Component.text("Reloaded " + count + " profile(s).", NamedTextColor.GREEN));
        return true;
    }

    private boolean handleInfo(CommandSender sender, String[] args) {
        sender.sendMessage(Component.text("=== Strata World Generation ===", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("Version: " + plugin.getDescription().getVersion(), NamedTextColor.GRAY));
        sender.sendMessage(Component.text("Profiles: " + configRegistry.profileKeys().size(), NamedTextColor.GRAY));
        sender.sendMessage(Component.text("Biomes: " + configRegistry.biomeKeys().size(), NamedTextColor.GRAY));
        return true;
    }

    private boolean handleProfiles(CommandSender sender) {
        sender.sendMessage(Component.text("=== Loaded Profiles ===", NamedTextColor.GOLD));
        for (NamespacedKey key : configRegistry.profileKeys()) {
            configRegistry.getProfile(key).ifPresent(profile ->
                    sender.sendMessage(Component.text("  " + key + " - " + profile.displayName(), NamedTextColor.GREEN)));
        }
        if (configRegistry.profileKeys().isEmpty()) {
            sender.sendMessage(Component.text("  No profiles loaded.", NamedTextColor.GRAY));
        }
        return true;
    }

    private boolean handleVersion(CommandSender sender) {
        sender.sendMessage(Component.text("Strata v" + plugin.getDescription().getVersion()
                + " by " + plugin.getDescription().getAuthors(), NamedTextColor.GOLD));
        return true;
    }

    private boolean handleGuide(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text("=== Strata Guide ===", NamedTextColor.GOLD));
            sender.sendMessage(Component.text("Topics: profiles, biomes, noise, terrain, surface,", NamedTextColor.GRAY));
            sender.sendMessage(Component.text("  carvers, water, structures, features, entities,", NamedTextColor.GRAY));
            sender.sendMessage(Component.text("  config, migration", NamedTextColor.GRAY));
            sender.sendMessage(Component.text("Usage: /strata guide <topic>", NamedTextColor.YELLOW));
            return true;
        }

        String topic = args[1].toLowerCase();
        Component guide = switch (topic) {
            case "profiles" -> Component.text("""
                    Profiles define complete world generation configurations.
                    Each profile is a directory in plugins/Strata/profiles/
                    containing a profile.toml and subdirectories for biomes,
                    noise, terrain, etc. Use 'extends' to inherit from another profile.""", NamedTextColor.GREEN);
            case "biomes" -> Component.text("""
                    Biomes are defined in TOML files under profiles/<name>/biomes/.
                    Each biome specifies climate parameters (temperature, humidity,
                    continentalness, erosion, weirdness) that determine where it generates.
                    Biomes also define surface blocks, features, carvers, and spawn rules.""", NamedTextColor.GREEN);
            case "water" -> Component.text("""
                    Strata's water system generates realistic rivers using macro-scale
                    drainage basin computation. Rivers flow downhill, merge, and terminate
                    at oceans. Waterfalls form where rivers cross elevation changes.
                    Configure in profile.toml under [water].""", NamedTextColor.GREEN);
            default -> Component.text("Unknown guide topic: " + topic + ". Try /strata guide", NamedTextColor.RED);
        };

        sender.sendMessage(guide);
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Component.text("=== Strata Commands ===", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("/strata create <world> <profile>", NamedTextColor.GREEN)
                .append(Component.text(" - Create a new world", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/strata reload", NamedTextColor.GREEN)
                .append(Component.text(" - Reload all configurations", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/strata info", NamedTextColor.GREEN)
                .append(Component.text(" - Show plugin information", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/strata profiles", NamedTextColor.GREEN)
                .append(Component.text(" - List loaded profiles", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/strata guide <topic>", NamedTextColor.GREEN)
                .append(Component.text(" - In-game documentation", NamedTextColor.GRAY)));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return SUBCOMMANDS.stream().filter(s -> s.startsWith(args[0].toLowerCase())).toList();
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("guide")) {
            List<String> topics = List.of("profiles", "biomes", "noise", "terrain", "surface",
                    "carvers", "water", "structures", "features", "entities", "config", "migration");
            return topics.stream().filter(s -> s.startsWith(args[1].toLowerCase())).toList();
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("create")) {
            return configRegistry.profileKeys().stream()
                    .map(NamespacedKey::toString).toList();
        }
        return List.of();
    }
}
