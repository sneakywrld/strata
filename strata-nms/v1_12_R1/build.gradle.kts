plugins {
    id("strata.nms-conventions")
}

description = "Strata NMS Adapter - Minecraft 1.12.2 (v1_12_R1)"

dependencies {
    compileOnly(project(":strata-nms:common"))
    compileOnly(project(":strata-api"))
    compileOnly(project(":strata-core"))
}
