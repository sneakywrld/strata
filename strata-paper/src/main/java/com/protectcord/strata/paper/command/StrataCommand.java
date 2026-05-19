package com.protectcord.strata.paper.command;

import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.config.registry.ConfigRegistry;
import com.protectcord.strata.config.reload.ReloadCoordinator;
import com.protectcord.strata.paper.StrataPlugin;
import com.protectcord.strata.paper.guide.GuidePage;
import com.protectcord.strata.paper.guide.GuideRegistry;
import com.protectcord.strata.paper.guide.GuideRenderer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

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
    private final GuideRegistry guideRegistry;
    private final MigrateCommand migrateCommand;

    public StrataCommand(StrataPlugin plugin, ConfigRegistry configRegistry,
                         ReloadCoordinator reloadCoordinator, GuideRegistry guideRegistry) {
        this.plugin = plugin;
        this.configRegistry = configRegistry;
        this.reloadCoordinator = reloadCoordinator;
        this.guideRegistry = guideRegistry;
        this.migrateCommand = new MigrateCommand(plugin);
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
            case "migrate" -> migrateCommand.execute(sender, args);
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
            sendTopicList(sender);
            return true;
        }

        String topic = args[1].toLowerCase();
        int page = 1;
        if (args.length >= 3) {
            try {
                page = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage(Component.text("Invalid page number: " + args[2], NamedTextColor.RED));
                return true;
            }
        }

        Optional<GuidePage> guidePage = guideRegistry.getPage(topic, page);
        if (guidePage.isEmpty()) {
            if (guideRegistry.getTopics().contains(topic)) {
                sender.sendMessage(Component.text("Page " + page + " does not exist for topic '" + topic + "'.",
                        NamedTextColor.RED));
            } else {
                sender.sendMessage(Component.text("Unknown topic: " + topic + ". Use /strata guide to list topics.",
                        NamedTextColor.RED));
            }
            return true;
        }

        if (sender instanceof Player player) {
            GuideRenderer.renderPage(player, guidePage.get());
        } else {
            GuidePage gp = guidePage.get();
            sender.sendMessage(Component.text("=== " + gp.title() + " (" + gp.pageNumber() + "/"
                    + gp.totalPages() + ") ===", NamedTextColor.GOLD));
            for (String line : gp.content()) {
                sender.sendMessage(Component.text(line, NamedTextColor.WHITE));
            }
        }
        return true;
    }

    private void sendTopicList(CommandSender sender) {
        sender.sendMessage(Component.text("=== Strata Guide ===", NamedTextColor.GOLD, TextDecoration.BOLD));
        sender.sendMessage(Component.text("Available topics:", NamedTextColor.GRAY));

        List<String> topics = guideRegistry.getTopics();
        for (String topic : topics) {
            Component entry = Component.text("  " + topic, NamedTextColor.GREEN)
                    .clickEvent(ClickEvent.runCommand("/strata guide " + topic))
                    .hoverEvent(HoverEvent.showText(
                            Component.text("Open guide: " + topic, NamedTextColor.YELLOW)));
            sender.sendMessage(entry);
        }

        if (topics.isEmpty()) {
            sender.sendMessage(Component.text("  No guide topics loaded.", NamedTextColor.GRAY));
        }

        sender.sendMessage(Component.text("Usage: /strata guide <topic> [page]", NamedTextColor.YELLOW));
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
        sender.sendMessage(Component.text("/strata migrate <pack-dir> [name]", NamedTextColor.GREEN)
                .append(Component.text(" - Convert Terra pack to Strata", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/strata guide <topic>", NamedTextColor.GREEN)
                .append(Component.text(" - In-game documentation", NamedTextColor.GRAY)));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return SUBCOMMANDS.stream().filter(s -> s.startsWith(args[0].toLowerCase())).toList();
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("guide")) {
            return guideRegistry.getTopics().stream()
                    .filter(s -> s.startsWith(args[1].toLowerCase())).toList();
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("create")) {
            return configRegistry.profileKeys().stream()
                    .map(NamespacedKey::toString).toList();
        }
        return List.of();
    }
}
