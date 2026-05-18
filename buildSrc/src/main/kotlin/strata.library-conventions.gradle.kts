plugins {
    id("strata.base-conventions")
    `java-library`
    `maven-publish`
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            pom {
                name.set(project.name)
                description.set("Strata World Generation Platform - ${project.name}")
                url.set("https://github.com/sneakywrld/strata")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("sneakywrld")
                        name.set("SneakyWrld")
                    }
                }
            }
        }
    }
}
