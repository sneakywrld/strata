plugins {
    id("strata.nms-conventions")
}

description = "Strata NMS Adapter - Minecraft 1.20.6 (v1_20_R4)"

dependencies {
    compileOnly(project(":strata-nms:common"))
    compileOnly(project(":strata-api"))
    compileOnly(project(":strata-core"))
}
