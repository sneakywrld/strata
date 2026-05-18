plugins {
    java
}

group = "com.protectcord.strata"
version = rootProject.version

java {
    toolchain {
        // TODO: Upgrade to 25 once JDK 25 is installed / auto-provisionable
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-Xlint:all", "-Xlint:-processing"))
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
