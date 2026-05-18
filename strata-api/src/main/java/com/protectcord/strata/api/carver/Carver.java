package com.protectcord.strata.api.carver;

import com.protectcord.strata.api.core.Keyed;

/**
 * A world carver that removes blocks to create caves, ravines, and other subterranean voids.
 *
 * <p>Carvers execute during the
 * {@link com.protectcord.strata.api.pipeline.GenerationStage#CARVING CARVING} pipeline stage,
 * after terrain shaping and surface building but before feature decoration. Each carver
 * operates on a chunk-by-chunk basis, using a {@link CarvingMask} to track which positions
 * have been carved and prevent conflicts between multiple carvers.</p>
 *
 * <p>Carvers are registered in the carver registry
 * ({@link com.protectcord.strata.api.core.StrataAPI#carverRegistry()}) and referenced by key
 * in {@link com.protectcord.strata.api.biome.Biome#carvers()}.</p>
 *
 * @since 1.0.0
 * @see CarverType
 * @see CarverContext
 * @see CarvingMask
 */
public interface Carver extends Keyed {

    /**
     * Returns the type of this carver (e.g., cheese cave, spaghetti cave, ravine).
     *
     * @return the {@link CarverType}, never {@code null}
     */
    CarverType type();

    /**
     * Carves blocks within the given context, recording carved positions in the mask.
     *
     * <p>Implementations should use {@link CarverContext#blocks()} to remove blocks
     * (replace with air or fluid) and call {@link CarvingMask#set(int, int, int)} for
     * each carved position so that later carvers and surface rules can detect carved regions.</p>
     *
     * @param context the carving context providing block access, chunk coordinate, seed, and Y bounds
     * @param mask    the carving mask to record all carved positions
     * @return {@code true} if at least one block was carved, {@code false} otherwise
     */
    boolean carve(CarverContext context, CarvingMask mask);

    /**
     * Returns the probability (0.0 to 1.0) that this carver attempts to carve in any given chunk.
     *
     * <p>A value of {@code 1.0} means the carver runs in every chunk; {@code 0.1} means roughly
     * one in ten chunks.</p>
     *
     * @return the per-chunk carving probability, in the range [0.0, 1.0]
     */
    double probability();
}
