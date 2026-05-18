package com.protectcord.strata.migrate.cli;

import com.protectcord.strata.migrate.converter.ProfileAssembler;
import com.protectcord.strata.migrate.report.MigrationReport;
import com.protectcord.strata.migrate.terra.TerraPackData;
import com.protectcord.strata.migrate.terra.TerraPackParser;

import java.nio.file.Path;

/**
 * Standalone CLI for Terra → Strata migration.
 * Can be run outside of a Minecraft server for offline conversion.
 *
 * <p>Usage: {@code java -jar strata-migrate.jar <terra-pack-dir> <output-dir> [profile-name]}</p>
 */
public final class MigrateCLI {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Strata Terra Migration Tool");
            System.out.println("Usage: java -jar strata-migrate.jar <terra-pack-dir> <output-dir> [profile-name]");
            System.out.println();
            System.out.println("  <terra-pack-dir>  Path to the Terra config pack directory");
            System.out.println("  <output-dir>      Output directory for the Strata profile");
            System.out.println("  [profile-name]    Optional name for the generated profile (default: converted-pack)");
            System.exit(1);
            return;
        }

        Path packDir = Path.of(args[0]);
        Path outputDir = Path.of(args[1]);
        String profileName = args.length > 2 ? args[2] : "converted-pack";

        try {
            System.out.println("Parsing Terra pack at: " + packDir);
            TerraPackParser parser = new TerraPackParser(packDir);
            TerraPackData data = parser.parse();

            System.out.println("Converting to Strata profile: " + profileName);
            ProfileAssembler assembler = new ProfileAssembler();
            MigrationReport report = assembler.assemble(data, outputDir, profileName);

            System.out.println();
            System.out.println(report.toText());

            if (report.hasErrors()) {
                System.exit(2);
            }
        } catch (Exception e) {
            System.err.println("Migration failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(3);
        }
    }
}
