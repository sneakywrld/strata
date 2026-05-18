plugins {
    id("strata.nms-conventions")
}

description = "Strata NMS Adapter - Minecraft 1.16.5 (v1_16_R3)"

dependencies {
    compileOnly(project(":strata-nms:common"))
    compileOnly(project(":strata-api"))
    compileOnly(project(":strata-core"))
}
