package com.protectcord.strata.api.event.lifecycle;

import com.protectcord.strata.api.event.StrataEvent;

/**
 * Fired when a Strata configuration file is reloaded at runtime.
 *
 * <p>This is a read-only notification event; it cannot be cancelled. Plugins can use it
 * to react to configuration changes and refresh dependent state.</p>
 *
 * @since 1.0.0
 * @see com.protectcord.strata.api.event.EventBus
 */
public class ConfigReloadEvent implements StrataEvent {

    private final String configPath;

    /**
     * Constructs a new config reload event.
     *
     * @param configPath the path of the configuration file that was reloaded
     */
    public ConfigReloadEvent(String configPath) {
        this.configPath = configPath;
    }

    /**
     * Returns the path of the configuration file that was reloaded.
     *
     * @return the config path, never {@code null}
     */
    public String configPath() { return configPath; }
}
