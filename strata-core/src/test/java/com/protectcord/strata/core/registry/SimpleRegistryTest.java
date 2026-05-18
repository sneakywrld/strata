package com.protectcord.strata.core.registry;

import com.protectcord.strata.api.core.Keyed;
import com.protectcord.strata.api.core.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link SimpleRegistry} thread-safe registry implementation.
 */
@MockitoSettings(strictness = Strictness.LENIENT)
class SimpleRegistryTest {

    private SimpleRegistry<Keyed> registry;

    @BeforeEach
    void setUp() {
        registry = new SimpleRegistry<>("test");
    }

    // ------------------------------------------------------------------ helpers

    /**
     * Creates a mock Keyed entry with the given key.
     */
    private Keyed mockEntry(String namespace, String key) {
        Keyed entry = mock(Keyed.class, namespace + ":" + key);
        when(entry.key()).thenReturn(NamespacedKey.of(namespace, key));
        return entry;
    }

    /**
     * Creates a mock Keyed entry under the strata namespace.
     */
    private Keyed mockStrataEntry(String key) {
        return mockEntry("strata", key);
    }

    // ------------------------------------------------ register and get

    @Test
    void register_singleEntry_canBeRetrieved() {
        Keyed entry = mockStrataEntry("plains");
        registry.register(entry);

        Optional<Keyed> result = registry.get(NamespacedKey.strata("plains"));
        assertTrue(result.isPresent(), "Registered entry should be present");
        assertSame(entry, result.get());
    }

    @Test
    void register_multipleEntries_allRetrievable() {
        Keyed plains = mockStrataEntry("plains");
        Keyed desert = mockStrataEntry("desert");
        Keyed ocean = mockStrataEntry("ocean");

        registry.register(plains);
        registry.register(desert);
        registry.register(ocean);

        assertSame(plains, registry.get(NamespacedKey.strata("plains")).orElse(null));
        assertSame(desert, registry.get(NamespacedKey.strata("desert")).orElse(null));
        assertSame(ocean, registry.get(NamespacedKey.strata("ocean")).orElse(null));
    }

    @Test
    void get_unregisteredKey_returnsEmpty() {
        Optional<Keyed> result = registry.get(NamespacedKey.strata("nonexistent"));
        assertTrue(result.isEmpty(), "Unregistered key should return empty Optional");
    }

    // ------------------------------------------------ duplicate registration

    @Test
    void register_duplicateKey_throwsIllegalArgumentException() {
        Keyed first = mockStrataEntry("plains");
        Keyed second = mockStrataEntry("plains");

        registry.register(first);
        assertThrows(IllegalArgumentException.class, () -> registry.register(second),
                "Registering a duplicate key should throw");
    }

    @Test
    void register_duplicateKey_preservesOriginal() {
        Keyed first = mockStrataEntry("plains");
        Keyed second = mockStrataEntry("plains");

        registry.register(first);
        try {
            registry.register(second);
        } catch (IllegalArgumentException ignored) {
            // expected
        }

        assertSame(first, registry.get(NamespacedKey.strata("plains")).orElse(null),
                "Original entry should still be present after failed duplicate registration");
    }

    // ------------------------------------------------ contains

    @Test
    void contains_registeredKey_returnsTrue() {
        registry.register(mockStrataEntry("plains"));
        assertTrue(registry.contains(NamespacedKey.strata("plains")));
    }

    @Test
    void contains_unregisteredKey_returnsFalse() {
        assertFalse(registry.contains(NamespacedKey.strata("nonexistent")));
    }

    // ------------------------------------------------ entries()

    @Test
    void entries_returnsAllRegisteredValues() {
        Keyed plains = mockStrataEntry("plains");
        Keyed desert = mockStrataEntry("desert");

        registry.register(plains);
        registry.register(desert);

        Collection<Keyed> entries = registry.entries();
        assertEquals(2, entries.size());
        assertTrue(entries.contains(plains));
        assertTrue(entries.contains(desert));
    }

    @Test
    void entries_emptyRegistry_returnsEmptyCollection() {
        assertTrue(registry.entries().isEmpty());
    }

    @Test
    void entries_returnsUnmodifiableView() {
        registry.register(mockStrataEntry("plains"));

        Collection<Keyed> entries = registry.entries();
        assertThrows(UnsupportedOperationException.class,
                () -> entries.add(mockStrataEntry("desert")),
                "entries() collection should be unmodifiable");
    }

    // ------------------------------------------------ keys()

    @Test
    void keys_returnsAllRegisteredKeys() {
        registry.register(mockStrataEntry("plains"));
        registry.register(mockStrataEntry("desert"));

        Collection<NamespacedKey> keys = registry.keys();
        assertEquals(2, keys.size());
        assertTrue(keys.contains(NamespacedKey.strata("plains")));
        assertTrue(keys.contains(NamespacedKey.strata("desert")));
    }

    @Test
    void keys_emptyRegistry_returnsEmptyCollection() {
        assertTrue(registry.keys().isEmpty());
    }

    @Test
    void keys_returnsUnmodifiableView() {
        registry.register(mockStrataEntry("plains"));

        Collection<NamespacedKey> keys = registry.keys();
        assertThrows(UnsupportedOperationException.class,
                () -> keys.add(NamespacedKey.strata("desert")),
                "keys() collection should be unmodifiable");
    }

    // ------------------------------------------------ clear

    @Test
    void clear_removesAllEntries() {
        registry.register(mockStrataEntry("plains"));
        registry.register(mockStrataEntry("desert"));
        registry.register(mockStrataEntry("ocean"));

        registry.clear();

        assertTrue(registry.entries().isEmpty(), "entries() should be empty after clear");
        assertTrue(registry.keys().isEmpty(), "keys() should be empty after clear");
        assertFalse(registry.contains(NamespacedKey.strata("plains")));
        assertFalse(registry.contains(NamespacedKey.strata("desert")));
        assertFalse(registry.contains(NamespacedKey.strata("ocean")));
    }

    @Test
    void clear_emptyRegistry_doesNotThrow() {
        assertDoesNotThrow(() -> registry.clear());
    }

    @Test
    void clear_allowsReRegistration() {
        Keyed first = mockStrataEntry("plains");
        registry.register(first);

        registry.clear();

        Keyed second = mockStrataEntry("plains");
        assertDoesNotThrow(() -> registry.register(second),
                "Should be able to register the same key after clear");
        assertSame(second, registry.get(NamespacedKey.strata("plains")).orElse(null));
    }

    // ------------------------------------------------ different namespaces

    @Test
    void register_differentNamespaces_treatedAsDistinctKeys() {
        Keyed strataPlains = mockEntry("strata", "plains");
        Keyed customPlains = mockEntry("myplugin", "plains");

        registry.register(strataPlains);
        registry.register(customPlains);

        assertEquals(2, registry.entries().size());
        assertSame(strataPlains, registry.get(NamespacedKey.strata("plains")).orElse(null));
        assertSame(customPlains, registry.get(NamespacedKey.of("myplugin", "plains")).orElse(null));
    }

    // ------------------------------------------------ toString

    @Test
    void toString_containsRegistryNameAndSize() {
        registry.register(mockStrataEntry("plains"));
        registry.register(mockStrataEntry("desert"));

        String str = registry.toString();
        assertTrue(str.contains("test"), "toString should contain registry name");
        assertTrue(str.contains("2"), "toString should contain current size");
    }

    @Test
    void toString_emptyRegistry_showsZeroSize() {
        String str = registry.toString();
        assertTrue(str.contains("0"), "toString should show size 0 for empty registry");
    }

    // ------------------------------------------------ thread safety

    @Test
    void concurrentRegistrations_noDuplicatesOrLostEntries() throws InterruptedException {
        int threadCount = 8;
        int entriesPerThread = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    for (int i = 0; i < entriesPerThread; i++) {
                        // Each thread uses a unique namespace to avoid collisions
                        String key = "entry_" + i;
                        Keyed entry = mock(Keyed.class);
                        when(entry.key()).thenReturn(
                                NamespacedKey.of("thread" + threadId, key));
                        try {
                            registry.register(entry);
                        } catch (Exception e) {
                            failureCount.incrementAndGet();
                        }
                    }
                } catch (InterruptedException ignored) {
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // release all threads
        assertTrue(doneLatch.await(10, TimeUnit.SECONDS), "Threads should complete in time");
        executor.shutdown();

        assertEquals(0, failureCount.get(), "No registrations should fail with unique keys");
        assertEquals(threadCount * entriesPerThread, registry.entries().size(),
                "All entries from all threads should be present");
    }

    @Test
    void concurrentRegistrations_duplicateKey_exactlyOneSucceeds() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int t = 0; t < threadCount; t++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    Keyed entry = mock(Keyed.class);
                    when(entry.key()).thenReturn(NamespacedKey.strata("contested_key"));
                    try {
                        registry.register(entry);
                        successCount.incrementAndGet();
                    } catch (IllegalArgumentException e) {
                        failureCount.incrementAndGet();
                    }
                } catch (InterruptedException ignored) {
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        assertTrue(doneLatch.await(10, TimeUnit.SECONDS));
        executor.shutdown();

        assertEquals(1, successCount.get(),
                "Exactly one thread should successfully register the key");
        assertEquals(threadCount - 1, failureCount.get(),
                "All other threads should get IllegalArgumentException");
        assertEquals(1, registry.entries().size());
    }

    // ------------------------------------------------ large registry

    @Test
    void largeRegistry_manyEntries_allRetrievable() {
        int count = 500;
        for (int i = 0; i < count; i++) {
            Keyed entry = mock(Keyed.class);
            when(entry.key()).thenReturn(NamespacedKey.strata("entry_" + i));
            registry.register(entry);
        }

        assertEquals(count, registry.entries().size());
        assertEquals(count, registry.keys().size());

        for (int i = 0; i < count; i++) {
            assertTrue(registry.contains(NamespacedKey.strata("entry_" + i)),
                    "Entry " + i + " should be present");
            assertTrue(registry.get(NamespacedKey.strata("entry_" + i)).isPresent(),
                    "Entry " + i + " should be retrievable");
        }
    }
}
