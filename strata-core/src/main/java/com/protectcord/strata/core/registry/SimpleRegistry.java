package com.protectcord.strata.core.registry;

import com.protectcord.strata.api.core.Keyed;
import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.api.registry.Registry;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe implementation of {@link Registry}.
 */
public final class SimpleRegistry<T extends Keyed> implements Registry<T> {

    private final Map<NamespacedKey, T> entries = new ConcurrentHashMap<>();
    private final String name;

    public SimpleRegistry(String name) {
        this.name = name;
    }

    @Override
    public void register(T entry) {
        NamespacedKey key = entry.key();
        if (entries.putIfAbsent(key, entry) != null) {
            throw new IllegalArgumentException(
                    "Duplicate " + name + " registration: " + key);
        }
    }

    @Override
    public Optional<T> get(NamespacedKey key) {
        return Optional.ofNullable(entries.get(key));
    }

    @Override
    public boolean contains(NamespacedKey key) {
        return entries.containsKey(key);
    }

    @Override
    public Collection<T> entries() {
        return Collections.unmodifiableCollection(entries.values());
    }

    @Override
    public Collection<NamespacedKey> keys() {
        return Collections.unmodifiableSet(entries.keySet());
    }

    @Override
    public void clear() {
        entries.clear();
    }

    @Override
    public String toString() {
        return "Registry[" + name + ", size=" + entries.size() + "]";
    }
}
