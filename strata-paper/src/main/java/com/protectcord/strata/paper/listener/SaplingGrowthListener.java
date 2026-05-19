package com.protectcord.strata.paper.listener;

import org.bukkit.TreeType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.StructureGrowEvent;

/**
 * Listens for sapling growth events to enforce biome-specific tree rules.
 * Currently a no-op — sapling rules will be enforced once the config
 * model supports per-biome sapling allow/deny lists.
 */
public final class SaplingGrowthListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onSaplingGrow(StructureGrowEvent event) {
        // Sapling rule enforcement will be added when the config model
        // supports per-biome sapling allow/deny lists.
    }

    static String treeTypeToSaplingKey(TreeType type) {
        return switch (type) {
            case TREE, BIG_TREE -> "oak";
            case BIRCH, TALL_BIRCH -> "birch";
            case REDWOOD, TALL_REDWOOD, MEGA_REDWOOD -> "spruce";
            case JUNGLE, SMALL_JUNGLE -> "jungle";
            case ACACIA -> "acacia";
            case DARK_OAK -> "dark_oak";
            case CHERRY -> "cherry";
            case RED_MUSHROOM -> "red_mushroom";
            case BROWN_MUSHROOM -> "brown_mushroom";
            default -> null;
        };
    }
}
