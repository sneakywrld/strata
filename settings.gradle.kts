rootProject.name = "strata"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
    }
}

include(
    "strata-api",
    "strata-noise",
    "strata-config",
    "strata-core",
    "strata-nms:common",
    "strata-nms:v1_8_R3",
    "strata-nms:v1_12_R1",
    "strata-nms:v1_13_R2",
    "strata-nms:v1_16_R3",
    "strata-nms:v1_17_R1",
    "strata-nms:v1_18_R2",
    "strata-nms:v1_19_R3",
    "strata-nms:v1_20_R4",
    "strata-nms:v26_1",
    "strata-paper",
    "strata-migrate",
    "strata-starter",
)
