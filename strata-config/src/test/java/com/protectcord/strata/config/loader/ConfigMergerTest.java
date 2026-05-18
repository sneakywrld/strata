package com.protectcord.strata.config.loader;

import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.config.model.ProfileConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConfigMergerTest {

    private ConfigMerger merger;

    @BeforeEach
    void setUp() {
        merger = new ConfigMerger();
    }

    private TomlReader dummyReader() {
        return TomlReader.fromString("placeholder = true");
    }

    private ProfileConfig profile(String name, String extendsFrom) {
        return new ProfileConfig(
                NamespacedKey.strata(name),
                name,
                "desc",
                "author",
                extendsFrom,
                Path.of("/tmp/" + name),
                dummyReader()
        );
    }

    private Map<NamespacedKey, ProfileConfig> mapOf(ProfileConfig... configs) {
        Map<NamespacedKey, ProfileConfig> map = new LinkedHashMap<>();
        for (ProfileConfig c : configs) {
            map.put(c.key(), c);
        }
        return map;
    }

    // ---------------------------------------------------------------
    // Single profile (no inheritance)
    // ---------------------------------------------------------------
    @Nested
    class SingleProfile {

        @Test
        void singleProfileIsReturnedAsIs() {
            ProfileConfig root = profile("root", null);
            List<ProfileConfig> sorted = merger.resolveInheritance(mapOf(root));

            assertEquals(1, sorted.size());
            assertEquals(root, sorted.get(0));
        }
    }

    // ---------------------------------------------------------------
    // Parent before child ordering
    // ---------------------------------------------------------------
    @Nested
    class ParentChildOrdering {

        @Test
        void parentComesBeforeChild() {
            ProfileConfig parent = profile("parent", null);
            ProfileConfig child = profile("child", "parent");

            // Insert child first to prove ordering is by dependency, not insertion
            List<ProfileConfig> sorted = merger.resolveInheritance(mapOf(child, parent));

            assertEquals(2, sorted.size());
            assertEquals(parent, sorted.get(0));
            assertEquals(child, sorted.get(1));
        }

        @Test
        void threeGenerationChain() {
            ProfileConfig grandparent = profile("grandparent", null);
            ProfileConfig parent = profile("parent", "grandparent");
            ProfileConfig child = profile("child", "parent");

            // Insert in reverse order
            List<ProfileConfig> sorted = merger.resolveInheritance(
                    mapOf(child, parent, grandparent));

            assertEquals(3, sorted.size());
            assertEquals(grandparent, sorted.get(0));
            assertEquals(parent, sorted.get(1));
            assertEquals(child, sorted.get(2));
        }

        @Test
        void deepChainResolvesCorrectly() {
            ProfileConfig a = profile("a", null);
            ProfileConfig b = profile("b", "a");
            ProfileConfig c = profile("c", "b");
            ProfileConfig d = profile("d", "c");

            List<ProfileConfig> sorted = merger.resolveInheritance(mapOf(d, c, b, a));

            assertEquals(4, sorted.size());
            // a must come first, d must come last
            assertEquals(NamespacedKey.strata("a"), sorted.get(0).key());
            assertEquals(NamespacedKey.strata("d"), sorted.get(3).key());

            // Each profile's parent appears before it
            for (int i = 0; i < sorted.size(); i++) {
                String ext = sorted.get(i).extendsFrom();
                if (ext != null) {
                    NamespacedKey parentKey = NamespacedKey.strata(ext);
                    int parentIndex = -1;
                    for (int j = 0; j < sorted.size(); j++) {
                        if (sorted.get(j).key().equals(parentKey)) {
                            parentIndex = j;
                            break;
                        }
                    }
                    assertTrue(parentIndex < i,
                            "Parent " + ext + " should come before " + sorted.get(i).key());
                }
            }
        }
    }

    // ---------------------------------------------------------------
    // Multiple root profiles (no inheritance between them)
    // ---------------------------------------------------------------
    @Nested
    class MultipleRoots {

        @Test
        void multipleIndependentProfilesAllReturned() {
            ProfileConfig a = profile("alpha", null);
            ProfileConfig b = profile("beta", null);
            ProfileConfig c = profile("gamma", null);

            List<ProfileConfig> sorted = merger.resolveInheritance(mapOf(a, b, c));

            assertEquals(3, sorted.size());
            // All three should be present (order among siblings is unspecified but all must appear)
            assertTrue(sorted.contains(a));
            assertTrue(sorted.contains(b));
            assertTrue(sorted.contains(c));
        }

        @Test
        void independentTreesResolveSeparately() {
            // Tree 1: root1 -> child1
            ProfileConfig root1 = profile("root1", null);
            ProfileConfig child1 = profile("child1", "root1");

            // Tree 2: root2 -> child2
            ProfileConfig root2 = profile("root2", null);
            ProfileConfig child2 = profile("child2", "root2");

            List<ProfileConfig> sorted = merger.resolveInheritance(
                    mapOf(child2, child1, root1, root2));

            assertEquals(4, sorted.size());

            // Each child must come after its parent
            int root1Idx = sorted.indexOf(root1);
            int child1Idx = sorted.indexOf(child1);
            int root2Idx = sorted.indexOf(root2);
            int child2Idx = sorted.indexOf(child2);

            assertTrue(root1Idx < child1Idx, "root1 should come before child1");
            assertTrue(root2Idx < child2Idx, "root2 should come before child2");
        }
    }

    // ---------------------------------------------------------------
    // Circular inheritance detection
    // ---------------------------------------------------------------
    @Nested
    class CircularInheritance {

        @Test
        void directCircularInheritanceThrows() {
            // A extends B, B extends A
            ProfileConfig a = profile("a", "b");
            ProfileConfig b = profile("b", "a");

            assertThrows(IllegalStateException.class,
                    () -> merger.resolveInheritance(mapOf(a, b)));
        }

        @Test
        void indirectCircularInheritanceThrows() {
            // A -> B -> C -> A
            ProfileConfig a = profile("a", "c");
            ProfileConfig b = profile("b", "a");
            ProfileConfig c = profile("c", "b");

            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> merger.resolveInheritance(mapOf(a, b, c)));
            assertTrue(ex.getMessage().contains("Circular"));
        }

        @Test
        void selfReferencingProfileThrows() {
            // A extends A
            ProfileConfig a = profile("a", "a");

            assertThrows(IllegalStateException.class,
                    () -> merger.resolveInheritance(mapOf(a)));
        }
    }

    // ---------------------------------------------------------------
    // Empty input
    // ---------------------------------------------------------------
    @Nested
    class EmptyInput {

        @Test
        void emptyMapReturnsEmptyList() {
            List<ProfileConfig> sorted = merger.resolveInheritance(Map.of());
            assertNotNull(sorted);
            assertTrue(sorted.isEmpty());
        }
    }

    // ---------------------------------------------------------------
    // Profiles extending non-existent parents
    // ---------------------------------------------------------------
    @Nested
    class MissingParent {

        @Test
        void profileExtendingMissingParentCausesDeadlock() {
            // "child" extends "ghost" which is not in the map.
            // The algorithm will never resolve "child" because "ghost" is never resolved.
            // This is effectively a circular dependency situation.
            ProfileConfig child = profile("child", "ghost");

            assertThrows(IllegalStateException.class,
                    () -> merger.resolveInheritance(mapOf(child)));
        }
    }

    // ---------------------------------------------------------------
    // Values from parent vs child (raw config overlay)
    // ---------------------------------------------------------------
    @Nested
    class ValuePreservation {

        @Test
        void parentAndChildRetainTheirOwnRawConfigs() {
            // The resolveInheritance method only sorts -- it doesn't merge values.
            // Each ProfileConfig in the result retains its own rawConfig.
            TomlReader parentReader = TomlReader.fromString("""
                    base-value = "from-parent"
                    shared = "parent-version"
                    """);
            TomlReader childReader = TomlReader.fromString("""
                    child-value = "from-child"
                    shared = "child-version"
                    """);

            ProfileConfig parent = new ProfileConfig(
                    NamespacedKey.strata("parent"), "Parent", "desc", "auth",
                    null, Path.of("/tmp/parent"), parentReader
            );
            ProfileConfig child = new ProfileConfig(
                    NamespacedKey.strata("child"), "Child", "desc", "auth",
                    "parent", Path.of("/tmp/child"), childReader
            );

            List<ProfileConfig> sorted = merger.resolveInheritance(mapOf(child, parent));

            // Parent's raw config is intact
            ProfileConfig resolvedParent = sorted.get(0);
            assertEquals("from-parent", resolvedParent.rawConfig().getString("base-value"));
            assertEquals("parent-version", resolvedParent.rawConfig().getString("shared"));

            // Child's raw config is intact (child overrides parent value for "shared")
            ProfileConfig resolvedChild = sorted.get(1);
            assertEquals("from-child", resolvedChild.rawConfig().getString("child-value"));
            assertEquals("child-version", resolvedChild.rawConfig().getString("shared"));

            // Parent does not have child-only values
            assertNull(resolvedParent.rawConfig().getString("child-value"));
            // Child does not have parent-only values
            assertNull(resolvedChild.rawConfig().getString("base-value"));
        }
    }
}
