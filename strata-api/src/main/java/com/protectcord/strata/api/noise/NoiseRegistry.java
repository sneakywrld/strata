package com.protectcord.strata.api.noise;

import com.protectcord.strata.api.registry.Registry;

/**
 * Registry for {@link NoiseFunction} instances.
 *
 * <p>Accessible via {@link com.protectcord.strata.api.core.StrataAPI#noiseRegistry()}.</p>
 *
 * @since 1.0.0
 * @see NoiseFunction
 * @see Registry
 */
public interface NoiseRegistry extends Registry<NoiseFunction> {
}
