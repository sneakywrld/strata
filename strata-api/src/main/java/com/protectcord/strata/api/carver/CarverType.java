package com.protectcord.strata.api.carver;

/**
 * Built-in carver algorithm types supported by the Strata carving system.
 *
 * <p>Each constant corresponds to a distinct cave/ravine generation algorithm. The type
 * is specified in TOML configuration or programmatically via {@link Carver#type()}.
 * Use {@link #CUSTOM} for plugin-defined carver algorithms.</p>
 *
 * @since 1.0.0
 * @see Carver
 */
public enum CarverType {
    /** Swiss cheese cave system (large open chambers). */
    CHEESE_CAVE,

    /** Spaghetti caves (long winding tunnels). */
    SPAGHETTI_CAVE,

    /** Noodle caves (thin connecting tunnels). */
    NOODLE_CAVE,

    /** Classic ravine/canyon carver. */
    RAVINE,

    /** Underwater cave system. */
    UNDERWATER_CAVE,

    /** Lava tube cave. */
    LAVA_TUBE,

    /** Nether-style open caverns. */
    NETHER_CAVERN,

    /** Custom carver type (for plugins). */
    CUSTOM
}
