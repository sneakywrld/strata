plugins {
    id("strata.library-conventions")
}

description = "Strata Migration - Terra config pack importer and converter"

dependencies {
    implementation(project(":strata-api"))
    implementation(project(":strata-config"))

    // YAML parsing for reading Terra config packs
    implementation(libs.snakeyaml)

    // JSON for intermediate conversion
    implementation(libs.bundles.jackson)

    testImplementation(libs.bundles.junit)
}

// Configure the jar task so strata-migrate can run as a standalone CLI tool.
// Usage: java -jar strata-migrate.jar <terra-pack-dir> <output-dir> [profile-name]
tasks.jar {
    manifest {
        attributes("Main-Class" to "com.protectcord.strata.migrate.cli.MigrateCLI")
    }
}
