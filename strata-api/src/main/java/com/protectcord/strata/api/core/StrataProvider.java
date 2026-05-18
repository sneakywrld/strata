package com.protectcord.strata.api.core;

/**
 * Static accessor for the {@link StrataAPI} singleton.
 *
 * <p>The Strata plugin sets the instance at startup via {@link #set(StrataAPI)};
 * third-party plugins retrieve it via {@link #get()} to interact with the generation system.
 * Before calling {@link #get()}, plugins should verify availability with {@link #isAvailable()}
 * or handle the potential {@link IllegalStateException}.</p>
 *
 * <p>Typical third-party plugin usage:</p>
 * <pre>{@code
 * if (StrataProvider.isAvailable()) {
 *     StrataAPI api = StrataProvider.get();
 *     api.biomeRegistry().register(myCustomBiome);
 * }
 * }</pre>
 *
 * @since 1.0.0
 * @see StrataAPI
 */
public final class StrataProvider {

    private static StrataAPI instance;

    private StrataProvider() {}

    /**
     * Returns the active {@link StrataAPI} instance.
     *
     * @return the initialized API instance, never {@code null}
     * @throws IllegalStateException if the Strata plugin has not been initialized yet
     */
    public static StrataAPI get() {
        if (instance == null) {
            throw new IllegalStateException(
                    "StrataAPI has not been initialized. Is the Strata plugin loaded?");
        }
        return instance;
    }

    /**
     * Sets the {@link StrataAPI} instance. Called once by the Strata plugin at startup.
     *
     * <p><b>Internal use only.</b> This method is not intended for third-party plugins.
     * Calling it after initialization will throw an exception.</p>
     *
     * @param api the API implementation to register
     * @throws IllegalStateException if the API has already been initialized
     */
    public static void set(StrataAPI api) {
        if (instance != null) {
            throw new IllegalStateException("StrataAPI has already been initialized");
        }
        instance = api;
    }

    /**
     * Returns {@code true} if the API has been initialized and is ready for use.
     *
     * <p>Third-party plugins should call this before {@link #get()} during early
     * lifecycle phases (e.g., {@code onLoad()}) when initialization order is uncertain.</p>
     *
     * @return {@code true} if {@link #get()} will succeed without throwing
     */
    public static boolean isAvailable() {
        return instance != null;
    }
}
