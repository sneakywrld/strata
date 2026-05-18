package com.protectcord.strata.nms;

import java.util.ServiceLoader;
import java.util.logging.Logger;

/**
 * Detects the current Minecraft server version and loads the appropriate NMS adapter
 * via ServiceLoader.
 */
public final class VersionDetector {

    private static final Logger LOGGER = Logger.getLogger("Strata");

    private VersionDetector() {}

    /**
     * Detects the server version and returns the matching NMS adapter.
     *
     * @return the NMS adapter for the current server version
     * @throws UnsupportedOperationException if no adapter is found
     */
    public static NMSAdapter detect() {
        String serverVersion = detectServerVersion();
        LOGGER.info("Detected server version: " + serverVersion);

        ServiceLoader<NMSAdapter> loader = ServiceLoader.load(NMSAdapter.class);

        for (NMSAdapter adapter : loader) {
            if (isCompatible(adapter.version(), serverVersion)) {
                LOGGER.info("Using NMS adapter: " + adapter.version().name()
                        + " (MC " + adapter.version().minecraftVersion() + ")");
                return adapter;
            }
        }

        throw new UnsupportedOperationException(
                "No NMS adapter found for server version: " + serverVersion
                        + ". Supported versions: 1.8.8, 1.12.2, 1.13.2, 1.16.5, "
                        + "1.17.1, 1.18.2, 1.19.4, 1.20.6, 26.1");
    }

    private static String detectServerVersion() {
        try {
            // Try Bukkit API first
            Class<?> bukkit = Class.forName("org.bukkit.Bukkit");
            Object server = bukkit.getMethod("getServer").invoke(null);
            String version = (String) server.getClass().getMethod("getVersion").invoke(server);
            return version;
        } catch (Exception e) {
            // Try NMS package name detection
            try {
                Package[] packages = Package.getPackages();
                for (Package pkg : packages) {
                    String name = pkg.getName();
                    if (name.startsWith("net.minecraft.server.v")) {
                        return name.substring("net.minecraft.server.".length());
                    }
                }
            } catch (Exception ignored) {}
        }
        return "unknown";
    }

    private static boolean isCompatible(NMSVersion adapterVersion, String serverVersion) {
        return serverVersion.contains(adapterVersion.minecraftVersion())
                || serverVersion.contains(adapterVersion.name().toLowerCase());
    }
}
