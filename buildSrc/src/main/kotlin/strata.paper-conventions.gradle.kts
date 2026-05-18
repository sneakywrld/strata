plugins {
    id("strata.base-conventions")
    id("com.gradleup.shadow")
}

tasks.shadowJar {
    archiveClassifier.set("")
    mergeServiceFiles()

    // Relocate bStats to avoid conflicts with other plugins
    relocate("org.bstats", "com.protectcord.strata.lib.bstats")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

// Process resources - inject version into plugin.yml
tasks.processResources {
    val props = mapOf(
        "version" to project.version,
        "name" to "Strata",
        "author" to "SneakyWrld",
    )
    inputs.properties(props)
    filesMatching(listOf("plugin.yml", "paper-plugin.yml")) {
        expand(props)
    }
}
