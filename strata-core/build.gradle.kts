plugins {
    id("strata.library-conventions")
}

description = "Strata Core Engine - World generation pipeline, all stages, platform-agnostic"

dependencies {
    api(project(":strata-api"))
    implementation(project(":strata-noise"))
    implementation(project(":strata-config"))

    implementation(libs.caffeine)

    testImplementation(libs.bundles.junit)
    testImplementation(libs.bundles.mockito)
}
