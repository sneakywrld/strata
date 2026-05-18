plugins {
    id("strata.nms-conventions")
}

description = "Strata NMS Adapter - Minecraft 1.17.1 (v1_17_R1)"

dependencies {
    compileOnly(project(":strata-nms:common"))
    compileOnly(project(":strata-api"))
    compileOnly(project(":strata-core"))
}
