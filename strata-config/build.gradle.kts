plugins {
    id("strata.library-conventions")
}

description = "Strata Configuration System - TOML parsing, validation, hot-reload"

dependencies {
    api(project(":strata-api"))
    implementation(project(":strata-noise"))

    implementation(libs.night.config.toml)
    implementation(libs.bundles.jackson)

    testImplementation(libs.bundles.junit)
}
