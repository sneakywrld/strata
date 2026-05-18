package com.protectcord.strata.api.registry;

import com.protectcord.strata.api.core.Keyed;
import com.protectcord.strata.api.core.NamespacedKey;

import java.util.Collection;
import java.util.Optional;

/**
 * A typed registry that maps {@link NamespacedKey} identifiers to {@link Keyed} objects.
 *
 * <p>Registries are the central lookup mechanism for all Strata content: biomes, noise functions,
 * surface rules, structures, features, carvers, and block palettes. Each content type has its own
 * dedicated registry accessible through {@link com.protectcord.strata.api.core.StrataAPI StrataAPI}.</p>
 *
 * <p>Third-party plugins register custom content by calling {@link #register(Keyed)} and look up
 * existing entries via {@link #get(NamespacedKey)}.</p>
 *
 * <p>Example:</p>
 * <pre>{@code
 * Registry<Biome> biomes = api.biomeRegistry();
 * biomes.register(myCustomBiome);
 * Optional<Biome> plains = biomes.get(NamespacedKey.strata("plains"));
 * }</pre>
 *
 * @param <T> the type of objects stored in this registry; must implement {@link Keyed}
 * @since 1.0.0
 * @see Keyed
 * @see NamespacedKey
 */
public interface Registry<T extends Keyed> {

    /**
     * Registers an object in this registry.
     *
     * <p>The entry's {@link Keyed#key() key} must not already be present in the registry.
     * If a duplicate key is detected, an exception is thrown.</p>
     *
     * @param entry the object to register
     * @throws IllegalArgumentException if an entry with the same key is already registered
     * @throws NullPointerException     if {@code entry} is {@code null}
     */
    void register(T entry);

    /**
     * Retrieves an entry by its namespaced key.
     *
     * @param key the key to look up
     * @return an {@link Optional} containing the entry if present, or empty if not found
     * @throws NullPointerException if {@code key} is {@code null}
     */
    Optional<T> get(NamespacedKey key);

    /**
     * Returns {@code true} if an entry with the given key is registered.
     *
     * @param key the key to check
     * @return {@code true} if the key is present in this registry
     * @throws NullPointerException if {@code key} is {@code null}
     */
    boolean contains(NamespacedKey key);

    /**
     * Returns an unmodifiable view of all registered entries.
     *
     * <p>The returned collection reflects the current state of the registry and should not
     * be cached across hot-reloads.</p>
     *
     * @return all registered entries, never {@code null}
     */
    Collection<T> entries();

    /**
     * Returns an unmodifiable view of all registered keys.
     *
     * @return all registered keys, never {@code null}
     */
    Collection<NamespacedKey> keys();

    /**
     * Removes all entries from this registry.
     *
     * <p><b>Internal use only.</b> This method is called during configuration hot-reload
     * to allow re-registration of content. Third-party plugins should not call this directly.</p>
     */
    void clear();
}
