package com.protectcord.strata.paper.metrics;

import com.protectcord.strata.paper.StrataPlugin;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;

public final class MetricsCharts {

    private MetricsCharts() {}

    public static void register(Metrics metrics, StrataPlugin plugin) {
        metrics.addCustomChart(new SimplePie("active_worlds",
                () -> String.valueOf(Bukkit.getWorlds().stream()
                        .filter(w -> w.getGenerator() != null
                                && w.getGenerator().getClass().getName()
                                .startsWith("com.protectcord.strata"))
                        .count())));

        metrics.addCustomChart(new SimplePie("profiles_in_use",
                () -> String.valueOf(plugin.configRegistry().profileKeys().size())));

        metrics.addCustomChart(new SimplePie("total_biomes",
                () -> String.valueOf(plugin.configRegistry().biomeKeys().size())));

        metrics.addCustomChart(new SimplePie("minecraft_version",
                () -> Bukkit.getVersion()));

        metrics.addCustomChart(new SimplePie("java_version",
                () -> System.getProperty("java.version")));

        metrics.addCustomChart(new SimplePie("nms_adapter",
                () -> {
                    String version = Bukkit.getServer().getBukkitVersion();
                    int dash = version.indexOf('-');
                    return dash > 0 ? version.substring(0, dash) : version;
                }));
    }
}
