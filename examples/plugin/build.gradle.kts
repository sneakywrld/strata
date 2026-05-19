plugins {
    java
}

group = "com.example"
version = "1.0.0"
description = "Example plugin demonstrating Strata API usage"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    // Replace with the actual Strata repository when available.
    // For local development, use mavenLocal() and install strata-api first.
    mavenLocal()
}

dependencies {
    // PaperMC API — provided at runtime by the server
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")

    // Strata API — provided at runtime by the Strata plugin
    compileOnly("com.protectcord.strata:strata-api:1.0.0-SNAPSHOT")
}

tasks.processResources {
    // Replace ${version} tokens in plugin.yml
    filesMatching("plugin.yml") {
        expand("version" to project.version)
    }
}
