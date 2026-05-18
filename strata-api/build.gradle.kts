plugins {
    id("strata.library-conventions")
}

description = "Strata World Generation API - Public developer interfaces"

// API module has ZERO dependencies. This is what third-party developers depend on.
// It contains only interfaces, records, enums, annotations, and event definitions.
