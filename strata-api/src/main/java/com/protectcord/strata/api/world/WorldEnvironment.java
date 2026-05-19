package com.protectcord.strata.api.world;

/**
 * The environment type of a Strata-managed world.
 *
 * @since 1.0.0
 * @see StrataWorld
 * @see WorldProfile
 */
public enum WorldEnvironment {

    /** Standard overworld environment with sky, weather, and day-night cycle. */
    NORMAL,

    /** Nether environment with bedrock ceiling and no sky. */
    NETHER,

    /** End environment with void below islands. */
    END,

    /** Plugin-defined custom environment. */
    CUSTOM
}
