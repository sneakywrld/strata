plugins {
    id("strata.nms-conventions")
}

description = "Strata NMS Common - Abstraction interfaces for version-specific adapters"

dependencies {
    compileOnly(project(":strata-api"))
    compileOnly(project(":strata-core"))
}
