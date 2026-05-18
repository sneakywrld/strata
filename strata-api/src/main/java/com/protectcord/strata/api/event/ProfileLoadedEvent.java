package com.protectcord.strata.api.event;

import com.protectcord.strata.api.core.NamespacedKey;

/**
 * Fired when a world profile is loaded or reloaded.
 */
public class ProfileLoadedEvent implements StrataEvent {

    private final NamespacedKey profileKey;
    private final boolean reload;

    public ProfileLoadedEvent(NamespacedKey profileKey, boolean reload) {
        this.profileKey = profileKey;
        this.reload = reload;
    }

    public NamespacedKey profileKey() { return profileKey; }
    public boolean isReload() { return reload; }
}
