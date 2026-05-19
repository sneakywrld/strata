package com.protectcord.strata.paper.command;

import com.protectcord.strata.migrate.MigrationValidator;
import com.protectcord.strata.migrate.converter.ProfileAssembler;
import com.protectcord.strata.migrate.report.MigrationReport;
import com.protectcord.strata.migrate.terra.TerraPackData;
import com.protectcord.strata.migrate.terra.TerraPackParser;
import com.protectcord.strata.paper.StrataPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Handles the /strata migrate subcommand.
 * Parses a Terra config pack directory and converts it to a Strata profile,
 * outputting into the plugin's profiles directory.
 *
 * <p>Usage: {@code /strata migrate <terra-pack-dir> [profile-name]}</p>
 */
public final class MigrateCommand {

    private final StrataPlugin plugin;

    public MigrateCommand(StrataPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Executes the migration command.
     *
     * @param sender the command sender
     * @param args   command arguments (args[0] is "migrate", args[1] is terra-pack-dir, args[2] is optional profile name)
     * @return true always (command handled)
     */
    public boolean execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("strata.admin")) {
            sender.sendMessage(Component.text("No permission.", NamedTextColor.RED));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /strata migrate <terra-pack-dir> [profile-name]",
                    NamedTextColor.RED));
            sender.sendMessage(Component.text("  <terra-pack-dir>  Path to the Terra config pack directory",
                    NamedTextColor.GRAY));
            sender.sendMessage(Component.text("  [profile-name]    Name for the generated Strata profile (default: converted-pack)",
                    NamedTextColor.GRAY));
            sender.sendMessage(Component.text("", NamedTextColor.GRAY));
            sender.sendMessage(Component.text("  Example: /strata migrate plugins/Terra/packs/DEFAULT my-world",
                    NamedTextColor.YELLOW));
            return true;
        }

        String terraPackPath = args[1];
        String profileName = args.length > 2 ? args[2] : "converted-pack";
        Path packDir = Path.of(terraPackPath);
        Path profilesDir = plugin.getDataFolder().toPath().resolve("profiles");
        Path outputDir = profilesDir.resolve(profileName);

        // Validate input
        if (!Files.isDirectory(packDir)) {
            sender.sendMessage(Component.text("Terra pack directory not found: " + terraPackPath,
                    NamedTextColor.RED));
            sender.sendMessage(Component.text("Provide the full path to the Terra config pack directory.",
                    NamedTextColor.GRAY));
            return true;
        }

        if (!Files.isRegularFile(packDir.resolve("pack.yml"))) {
            sender.sendMessage(Component.text("No pack.yml found in " + terraPackPath,
                    NamedTextColor.RED));
            sender.sendMessage(Component.text("This doesn't appear to be a valid Terra config pack.",
                    NamedTextColor.GRAY));
            return true;
        }

        if (Files.isDirectory(outputDir) && Files.isRegularFile(outputDir.resolve("profile.toml"))) {
            sender.sendMessage(Component.text("A profile named '" + profileName + "' already exists.",
                    NamedTextColor.RED));
            sender.sendMessage(Component.text("Choose a different name or delete the existing profile first.",
                    NamedTextColor.GRAY));
            return true;
        }

        // Run migration asynchronously to avoid blocking the main thread
        sender.sendMessage(Component.text("Starting Terra migration...", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("  Source: " + packDir.toAbsolutePath(), NamedTextColor.GRAY));
        sender.sendMessage(Component.text("  Output: " + outputDir.toAbsolutePath(), NamedTextColor.GRAY));
        sender.sendMessage(Component.text("  Profile: " + profileName, NamedTextColor.GRAY));

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                // Phase 1: Parse Terra pack
                notifySync(sender, Component.text("Parsing Terra config pack...", NamedTextColor.YELLOW));
                TerraPackParser parser = new TerraPackParser(packDir);
                TerraPackData packData = parser.parse();

                notifySync(sender, Component.text("Found " + packData.biomes().size() + " biomes, "
                        + packData.noises().size() + " noise configs, "
                        + packData.palettes().size() + " palettes",
                        NamedTextColor.GRAY));

                // Phase 2: Convert to Strata profile
                notifySync(sender, Component.text("Converting to Strata format...", NamedTextColor.YELLOW));
                ProfileAssembler assembler = new ProfileAssembler();
                MigrationReport report = assembler.assemble(packData, outputDir, profileName);

                // Phase 3: Validate output
                notifySync(sender, Component.text("Validating generated profile...", NamedTextColor.YELLOW));
                MigrationValidator validator = new MigrationValidator();
                MigrationValidator.ValidationResult validation = validator.validate(outputDir);

                // Report results back on main thread
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    sendMigrationReport(sender, report, validation, outputDir);

                    // Offer to reload profiles if migration succeeded
                    if (!report.hasErrors()) {
                        sender.sendMessage(Component.text("Run /strata reload to load the new profile.",
                                NamedTextColor.GREEN));
                    }
                });

            } catch (IOException e) {
                notifySync(sender, Component.text("Migration failed: " + e.getMessage(),
                        NamedTextColor.RED));
                plugin.getLogger().severe("Terra migration failed: " + e.getMessage());
            } catch (Exception e) {
                notifySync(sender, Component.text("Unexpected error during migration: " + e.getMessage(),
                        NamedTextColor.RED));
                plugin.getLogger().severe("Terra migration error: " + e.getMessage());
                e.printStackTrace();
            }
        });

        return true;
    }

    /**
     * Sends a message to the sender on the main server thread.
     */
    private void notifySync(CommandSender sender, Component message) {
        plugin.getServer().getScheduler().runTask(plugin, () -> sender.sendMessage(message));
    }

    /**
     * Formats and sends the migration report to the command sender.
     */
    private void sendMigrationReport(CommandSender sender, MigrationReport report,
                                     MigrationValidator.ValidationResult validation,
                                     Path outputDir) {
        sender.sendMessage(Component.text(""));
        sender.sendMessage(Component.text("=== Migration Report ===", NamedTextColor.GOLD));

        // Conversion summary
        sender.sendMessage(Component.text("Converted: " + report.converted().size() + " items",
                NamedTextColor.GREEN));

        if (!report.approximated().isEmpty()) {
            sender.sendMessage(Component.text("Approximated: " + report.approximated().size()
                    + " items (manual review recommended)", NamedTextColor.YELLOW));
            for (String item : report.approximated()) {
                sender.sendMessage(Component.text("  ~ " + item, NamedTextColor.YELLOW));
            }
        }

        if (!report.unsupported().isEmpty()) {
            sender.sendMessage(Component.text("Unsupported: " + report.unsupported().size()
                    + " items (manual conversion needed)", NamedTextColor.RED));
            for (String item : report.unsupported()) {
                sender.sendMessage(Component.text("  ! " + item, NamedTextColor.RED));
            }
        }

        if (!report.warnings().isEmpty()) {
            sender.sendMessage(Component.text("Warnings: " + report.warnings().size(), NamedTextColor.YELLOW));
            for (String warning : report.warnings()) {
                sender.sendMessage(Component.text("  W " + warning, NamedTextColor.YELLOW));
            }
        }

        if (report.hasErrors()) {
            sender.sendMessage(Component.text("Errors: " + report.errors().size(), NamedTextColor.RED));
            for (String error : report.errors()) {
                sender.sendMessage(Component.text("  E " + error, NamedTextColor.RED));
            }
        }

        // Validation results
        if (!validation.isValid()) {
            sender.sendMessage(Component.text(""));
            sender.sendMessage(Component.text("Post-migration validation found issues:",
                    NamedTextColor.RED));
            for (String error : validation.errors()) {
                sender.sendMessage(Component.text("  E " + error, NamedTextColor.RED));
            }
        }

        for (String warning : validation.warnings()) {
            sender.sendMessage(Component.text("  W " + warning, NamedTextColor.YELLOW));
        }

        sender.sendMessage(Component.text(""));
        sender.sendMessage(Component.text("Full report: " + outputDir.resolve("MIGRATION_REPORT.txt"),
                NamedTextColor.GRAY));

        if (!report.hasErrors() && validation.isValid()) {
            sender.sendMessage(Component.text("Migration completed successfully!", NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.text("Migration completed with issues. Review the report above.",
                    NamedTextColor.YELLOW));
        }
    }
}
