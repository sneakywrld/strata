package com.protectcord.strata.api.structure;

import com.protectcord.strata.api.registry.Registry;

/**
 * Registry for {@link StructureDefinition} entries.
 *
 * <p>Accessible via {@link com.protectcord.strata.api.core.StrataAPI#structureRegistry()}.</p>
 *
 * @since 1.0.0
 * @see StructureDefinition
 * @see Registry
 */
public interface StructureRegistry extends Registry<StructureDefinition> {
}
