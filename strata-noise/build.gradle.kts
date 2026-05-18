plugins {
    id("strata.library-conventions")
}

description = "Strata Noise Library - Standalone noise generation algorithms and caching"

dependencies {
    api(project(":strata-api"))

    implementation(libs.caffeine)

    testImplementation(libs.bundles.junit)
}
