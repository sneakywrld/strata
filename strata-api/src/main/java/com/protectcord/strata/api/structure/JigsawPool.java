package com.protectcord.strata.api.structure;

import com.protectcord.strata.api.core.NamespacedKey;

import java.util.List;
import java.util.Optional;

/**
 * A pool of jigsaw structure elements used for assembling jigsaw-type structures.
 *
 * <p>Jigsaw pools define a weighted set of {@link JigsawElement}s that the jigsaw assembler
 * selects from when expanding structure pieces. Each pool has a unique {@link #key()} and
 * an optional {@link #fallback()} pool used when the primary pool is exhausted.</p>
 *
 * @since 1.0.0
 * @see StructureDefinition
 * @see StructureType#JIGSAW
 */
public interface JigsawPool {

    /**
     * Returns the unique key identifying this jigsaw pool.
     *
     * @return the pool key, never {@code null}
     */
    NamespacedKey key();

    /**
     * Returns the list of weighted elements in this pool.
     *
     * @return an unmodifiable list of jigsaw elements, never {@code null}
     */
    List<JigsawElement> elements();

    /**
     * Returns the fallback pool key used when this pool has no valid elements remaining.
     *
     * @return an {@link Optional} containing the fallback pool key, or empty if none
     */
    Optional<NamespacedKey> fallback();

    /**
     * A single element within a jigsaw pool, referencing a structure piece template
     * with a selection weight and optional processor list.
     */
    interface JigsawElement {

        /**
         * Returns the key referencing the structure piece template.
         *
         * @return the element key, never {@code null}
         */
        NamespacedKey key();

        /**
         * Returns the selection weight of this element relative to other elements in the pool.
         *
         * @return the weight (positive integer)
         */
        int weight();

        /**
         * Returns the processor list key applied to this element during placement.
         *
         * @return the processor list key, or {@code null} if no processors are applied
         */
        String processors();
    }
}
