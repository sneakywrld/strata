plugins {
    id("strata.nms-conventions")
}

description = "Strata NMS Adapter - Minecraft 1.8.8 (v1_8_R3)"

dependencies {
    compileOnly(project(":strata-nms:common"))
    compileOnly(project(":strata-api"))
    compileOnly(project(":strata-core"))
}
