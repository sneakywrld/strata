plugins {
    id("strata.nms-conventions")
}

description = "Strata NMS Adapter - Minecraft 1.19.4 (v1_19_R3)"

dependencies {
    compileOnly(project(":strata-nms:common"))
    compileOnly(project(":strata-api"))
    compileOnly(project(":strata-core"))
}
