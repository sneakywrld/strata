package com.protectcord.strata.api.feature;

import com.protectcord.strata.api.registry.Registry;

/**
 * Registry for {@link Feature} definitions.
 *
 * <p>Accessible via {@link com.protectcord.strata.api.core.StrataAPI#featureRegistry()}.</p>
 *
 * @since 1.0.0
 * @see Feature
 * @see Registry
 */
public interface FeatureRegistry extends Registry<Feature> {
}
