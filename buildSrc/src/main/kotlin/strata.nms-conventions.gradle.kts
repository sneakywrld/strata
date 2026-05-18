plugins {
    id("strata.base-conventions")
}

// NMS adapter modules compile against version-specific CraftBukkit.
// Each adapter's build.gradle.kts sets its own Java target and NMS dependencies.
// This convention provides the shared configuration.
