package com.protectcord.strata.paper.world;

import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.api.world.StrataWorld;
import com.protectcord.strata.api.world.WorldProfile;
import org.bukkit.Bukkit;

/**
 * Paper-backed implementation of {@link StrataWorld}.
 */
public final class PaperStrataWorld implements StrataWorld {

    private final String name;
    private final NamespacedKey profileKey;
    private final WorldProfile profile;
    private final long seed;

    public PaperStrataWorld(String name, NamespacedKey profileKey, WorldProfile profile, long seed) {
        this.name = name;
        this.profileKey = profileKey;
        this.profile = profile;
        this.seed = seed;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public NamespacedKey profileKey() {
        return profileKey;
    }

    @Override
    public WorldProfile profile() {
        return profile;
    }

    @Override
    public long seed() {
        return seed;
    }

    @Override
    public boolean isLoaded() {
        return Bukkit.getWorld(name) != null;
    }
}
