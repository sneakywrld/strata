package com.protectcord.strata.paper.hook;

import com.protectcord.strata.api.entity.MythicMobsHook;
import com.protectcord.strata.api.entity.SpawnRule;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class MythicMobsHookImpl implements MythicMobsHook {

    private final boolean available;
    private final Map<String, String> zoneToTable = new ConcurrentHashMap<>();

    public MythicMobsHookImpl() {
        this.available = Bukkit.getPluginManager().getPlugin("MythicMobs") != null;
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public Optional<List<SpawnRule>> getSpawnTable(String tableName) {
        if (!available) {
            return Optional.empty();
        }
        // MythicMobs integration would query MM's API here.
        // Returns empty until MythicMobs bridge is fully wired.
        return Optional.empty();
    }

    @Override
    public void registerZoneTable(String zoneName, String mythicTable) {
        zoneToTable.put(zoneName, mythicTable);
    }

    public Optional<String> getTableForZone(String zoneName) {
        return Optional.ofNullable(zoneToTable.get(zoneName));
    }
}
