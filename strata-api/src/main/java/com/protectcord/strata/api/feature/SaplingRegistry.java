package com.protectcord.strata.api.feature;

import java.util.Collection;
import java.util.Optional;

/**
 * Registry for {@link SaplingRule} definitions controlling sapling growth behavior.
 *
 * <p>Unlike standard {@link com.protectcord.strata.api.registry.Registry} which stores
 * {@link com.protectcord.strata.api.core.Keyed} objects, sapling rules are keyed by their
 * {@link SaplingRule#saplingType() sapling type} string.</p>
 *
 * @since 1.0.0
 * @see SaplingRule
 */
public interface SaplingRegistry {

    /**
     * Registers a sapling growth rule.
     *
     * @param rule the rule to register
     * @throws IllegalArgumentException if a rule for the same sapling type is already registered
     * @throws NullPointerException     if {@code rule} is {@code null}
     */
    void register(SaplingRule rule);

    /**
     * Retrieves the sapling rule for the given sapling type.
     *
     * @param saplingType the sapling type identifier
     * @return an {@link Optional} containing the rule if present, or empty
     */
    Optional<SaplingRule> getRuleFor(String saplingType);

    /**
     * Returns an unmodifiable view of all registered sapling rules.
     *
     * @return all registered rules, never {@code null}
     */
    Collection<SaplingRule> getAllRules();
}
