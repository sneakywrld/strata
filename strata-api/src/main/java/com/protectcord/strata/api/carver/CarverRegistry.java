package com.protectcord.strata.api.carver;

import com.protectcord.strata.api.registry.Registry;

/**
 * Registry for {@link Carver} definitions.
 *
 * <p>Accessible via {@link com.protectcord.strata.api.core.StrataAPI#carverRegistry()}.</p>
 *
 * @since 1.0.0
 * @see Carver
 * @see Registry
 */
public interface CarverRegistry extends Registry<Carver> {
}
