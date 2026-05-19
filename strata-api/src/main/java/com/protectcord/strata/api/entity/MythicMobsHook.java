package com.protectcord.strata.api.entity;

import java.util.List;
import java.util.Optional;

/**
 * Optional integration hook for MythicMobs plugin.
 *
 * <p>Implementations of this interface bridge Strata's spawn system with MythicMobs,
 * allowing spawn tables and zone-based spawn configurations to reference MythicMobs
 * mob definitions. If MythicMobs is not installed, {@link #isAvailable()} returns
 * {@code false} and all other methods return empty/no-op results.</p>
 *
 * @since 1.0.0
 * @see SpawnRule#mythicMobId()
 * @see SpawnZone
 */
public interface MythicMobsHook {

    /**
     * Returns whether the MythicMobs plugin is installed and active.
     *
     * @return {@code true} if MythicMobs integration is available
     */
    boolean isAvailable();

    /**
     * Retrieves the spawn rules defined in a MythicMobs spawn table.
     *
     * @param tableName the MythicMobs spawn table name
     * @return an {@link Optional} containing the list of spawn rules, or empty if the table
     *         does not exist or MythicMobs is unavailable
     */
    Optional<List<SpawnRule>> getSpawnTable(String tableName);

    /**
     * Registers a mapping between a Strata spawn zone and a MythicMobs spawn table.
     *
     * <p>When entities spawn within the specified zone, the MythicMobs table is consulted
     * for mob selection.</p>
     *
     * @param zoneName   the Strata spawn zone name
     * @param mythicTable the MythicMobs spawn table name
     * @throws IllegalStateException if MythicMobs is not available
     */
    void registerZoneTable(String zoneName, String mythicTable);
}
