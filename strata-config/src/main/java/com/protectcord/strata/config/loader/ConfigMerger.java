package com.protectcord.strata.config.loader;

import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.config.model.ProfileConfig;

import java.util.*;
import java.util.logging.Logger;

/**
 * Merges profile configurations using the {@code extends} inheritance chain.
 * Child profiles inherit all settings from the parent, overriding only what they redefine.
 */
public final class ConfigMerger {

    private static final Logger LOGGER = Logger.getLogger("Strata");

    /**
     * Resolves inheritance and returns profiles in dependency order.
     * Parent profiles come before their children.
     *
     * @param profiles all loaded profiles
     * @return profiles sorted so parents come first
     * @throws IllegalStateException if a circular inheritance chain is detected
     */
    public List<ProfileConfig> resolveInheritance(Map<NamespacedKey, ProfileConfig> profiles) {
        // Topological sort based on extends chains
        Map<NamespacedKey, ProfileConfig> remaining = new LinkedHashMap<>(profiles);
        List<ProfileConfig> sorted = new ArrayList<>();
        Set<NamespacedKey> resolved = new HashSet<>();

        while (!remaining.isEmpty()) {
            boolean progress = false;
            var iter = remaining.entrySet().iterator();

            while (iter.hasNext()) {
                var entry = iter.next();
                ProfileConfig config = entry.getValue();
                String parent = config.extendsFrom();

                if (parent == null || resolved.contains(NamespacedKey.strata(parent))) {
                    sorted.add(config);
                    resolved.add(entry.getKey());
                    iter.remove();
                    progress = true;
                }
            }

            if (!progress) {
                throw new IllegalStateException(
                        "Circular profile inheritance detected among: " + remaining.keySet());
            }
        }

        return sorted;
    }
}
