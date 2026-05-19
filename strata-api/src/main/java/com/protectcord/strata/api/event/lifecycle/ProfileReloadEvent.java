package com.protectcord.strata.api.event.lifecycle;

import com.protectcord.strata.api.event.StrataEvent;

/**
 * Fired when a world profile is reloaded at runtime.
 *
 * <p>This is a read-only notification event; it cannot be cancelled. Plugins can use it
 * to invalidate caches or re-read profile-dependent configuration.</p>
 *
 * @since 1.0.0
 * @see com.protectcord.strata.api.event.EventBus
 */
public class ProfileReloadEvent implements StrataEvent {

    private final String profileName;

    /**
     * Constructs a new profile reload event.
     *
     * @param profileName the name of the profile that was reloaded
     */
    public ProfileReloadEvent(String profileName) {
        this.profileName = profileName;
    }

    /**
     * Returns the name of the profile that was reloaded.
     *
     * @return the profile name, never {@code null}
     */
    public String profileName() { return profileName; }
}
