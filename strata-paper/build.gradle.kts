plugins {
    id("strata.paper-conventions")
}

description = "Strata Paper Plugin - PaperMC integration and entry point"

dependencies {
    // Core modules
    implementation(project(":strata-api"))
    implementation(project(":strata-noise"))
    implementation(project(":strata-config"))
    implementation(project(":strata-core"))
    implementation(project(":strata-migrate"))
    implementation(project(":strata-starter"))

    // NMS common + all version adapters
    implementation(project(":strata-nms:common"))
    implementation(project(":strata-nms:v1_8_R3"))
    implementation(project(":strata-nms:v1_12_R1"))
    implementation(project(":strata-nms:v1_13_R2"))
    implementation(project(":strata-nms:v1_16_R3"))
    implementation(project(":strata-nms:v1_17_R1"))
    implementation(project(":strata-nms:v1_18_R2"))
    implementation(project(":strata-nms:v1_19_R3"))
    implementation(project(":strata-nms:v1_20_R4"))
    implementation(project(":strata-nms:v26_1"))

    // Paper API (provided at runtime by the server)
    compileOnly(libs.paper.api)

    // Shaded into the fat jar
    implementation(libs.bstats.bukkit)
    implementation(libs.adventure.api)
    implementation(libs.adventure.minimessage)

    // Testing
    testImplementation(libs.bundles.junit)
    testImplementation(libs.bundles.mockito)
}

// Resource filtering — inject build-time properties into plugin.yml
tasks.processResources {
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand("version" to project.version, "name" to project.name)
    }
}
