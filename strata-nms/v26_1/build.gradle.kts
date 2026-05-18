plugins {
    id("strata.nms-conventions")
}

description = "Strata NMS Adapter - Minecraft 26.1 (latest)"

dependencies {
    compileOnly(project(":strata-nms:common"))
    compileOnly(project(":strata-api"))
    compileOnly(project(":strata-core"))
}
