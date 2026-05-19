package com.protectcord.strata.paper.listener;

import com.protectcord.strata.config.registry.ConfigRegistry;
import org.bukkit.TreeType;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.StructureGrowEvent;

public final class SaplingGrowthListener implements Listener {

    private final ConfigRegistry configRegistry;

    public SaplingGrowthListener(ConfigRegistry configRegistry) {
        this.configRegistry = configRegistry;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onSaplingGrow(StructureGrowEvent event) {
        Block sapling = event.getLocation().getBlock();
        TreeType treeType = event.getSpecies();

        org.bukkit.block.Biome biome = sapling.getBiome();
        String biomeKey = biome.getKey().toString();

        var saplingRules = configRegistry.getSaplingRulesForBiome(biomeKey);
        if (saplingRules == null) {
            return;
        }

        String saplingType = treeTypeToSaplingKey(treeType);
        if (saplingType == null) {
            return;
        }

        if (saplingRules.isDenied(saplingType)) {
            event.setCancelled(true);
            return;
        }

        if (!saplingRules.isAllowed(saplingType)) {
            event.setCancelled(true);
        }
    }

    private String treeTypeToSaplingKey(TreeType type) {
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
