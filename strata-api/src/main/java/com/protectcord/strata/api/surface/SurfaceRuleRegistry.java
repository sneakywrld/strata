package com.protectcord.strata.api.surface;

import com.protectcord.strata.api.registry.Registry;

/**
 * Registry for {@link SurfaceRule} definitions.
 *
 * <p>Accessible via {@link com.protectcord.strata.api.core.StrataAPI#surfaceRuleRegistry()}.</p>
 *
 * @since 1.0.0
 * @see SurfaceRule
 * @see Registry
 */
public interface SurfaceRuleRegistry extends Registry<SurfaceRule> {
}
