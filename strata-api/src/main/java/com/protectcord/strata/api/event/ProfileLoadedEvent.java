package com.protectcord.strata.api.event;

import com.protectcord.strata.api.core.NamespacedKey;

/**
 * Fired when a {@link com.protectcord.strata.api.world.WorldProfile} is loaded or hot-reloaded.
 *
 * <p>This is a read-only notification event. Plugins can use it to invalidate caches,
 * re-read profile-dependent configuration, or log reload activity.</p>
 *
 * @since 1.0.0
 * @see com.protectcord.strata.api.world.WorldProfile
 * @see EventBus
 */
public class ProfileLoadedEvent implements StrataEvent {

    private final NamespacedKey profileKey;
    private final boolean reload;

    /**
     * Constructs a new profile-loaded event.
     *
     * @param profileKey the key of the profile that was loaded
     * @param reload     {@code true} if this is a hot-reload, {@code false} for initial load
     */
    public ProfileLoadedEvent(NamespacedKey profileKey, boolean reload) {
        this.profileKey = profileKey;
        this.reload = reload;
    }

    /**
     * Returns the key of the profile that was loaded.
     *
     * @return the profile key, never {@code null}
     */
    public NamespacedKey profileKey() { return profileKey; }

    /**
     * Returns whether this is a hot-reload of an already-loaded profile.
     *
     * @return {@code true} if the profile was reloaded, {@code false} if it was loaded for the first time
     */
    public boolean isReload() { return reload; }
}
