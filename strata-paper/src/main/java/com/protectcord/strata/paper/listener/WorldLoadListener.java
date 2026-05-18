package com.protectcord.strata.paper.listener;

import com.protectcord.strata.paper.StrataPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

import java.util.logging.Logger;

/**
 * Listener for world load events to initialize Strata engines for managed worlds.
 */
public final class WorldLoadListener implements Listener {

    private static final Logger LOGGER = Logger.getLogger("Strata");

    private final StrataPlugin plugin;

    public WorldLoadListener(StrataPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        String worldName = event.getWorld().getName();
        LOGGER.fine("World loaded: " + worldName);
    }
}
