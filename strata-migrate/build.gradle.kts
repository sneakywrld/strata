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
