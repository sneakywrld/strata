package com.protectcord.strata.api.biome;

/**
 * Ambient sound and particle settings for a biome.
 *
 * <p>All string fields are nullable. A {@code null} value indicates the biome does not
 * define that particular ambient property and the client falls back to defaults.</p>
 *
 * @param particle       the ambient particle identifier, or {@code null} for none
 * @param ambientSound   the looping ambient sound identifier, or {@code null} for none
 * @param moodSound      the mood sound identifier (plays periodically in dark areas), or {@code null}
 * @param moodTickDelay  the minimum tick delay between mood sound plays
 * @param additionsSound the additions sound identifier (random ambient sounds), or {@code null}
 * @since 1.0.0
 */
public record BiomeAmbience(
        String particle,
        String ambientSound,
        String moodSound,
        int moodTickDelay,
        String additionsSound
) {

    public static BiomeAmbience none() {
        return new BiomeAmbience(null, null, null, 6000, null);
    }
}
