package com.protectcord.strata.core.feature;

import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.api.feature.SaplingRegistry;
import com.protectcord.strata.api.feature.SaplingRule;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Biome-aware sapling growth control. Determines whether a sapling can grow in
 * a given biome, computes growth rate multipliers, and resolves tree variant
 * overrides based on registered {@link SaplingRule} definitions.
 */
public final class SaplingBiomeHandler {

    private static final double DEFAULT_GROWTH_RATE = 1.0;

    private static final Map<String, Set<String>> DEFAULT_BIOME_AFFINITY = Map.of(
            "oak", Set.of("minecraft:plains", "minecraft:forest", "minecraft:meadow",
                    "minecraft:sunflower_plains", "minecraft:river"),
            "birch", Set.of("minecraft:birch_forest", "minecraft:old_growth_birch_forest",
                    "minecraft:forest", "minecraft:meadow"),
            "spruce", Set.of("minecraft:taiga", "minecraft:old_growth_spruce_taiga",
                    "minecraft:snowy_taiga", "minecraft:snowy_plains"),
            "jungle", Set.of("minecraft:jungle", "minecraft:bamboo_jungle",
                    "minecraft:sparse_jungle"),
            "dark_oak", Set.of("minecraft:dark_forest"),
            "acacia", Set.of("minecraft:savanna", "minecraft:savanna_plateau")
    );

    private static final Map<String, Double> NATIVE_BIOME_GROWTH_BONUS = Map.of(
            "oak", 1.5,
            "birch", 1.4,
            "spruce", 1.3,
            "jungle", 1.6,
            "dark_oak", 1.2,
            "acacia", 1.3
    );

    private final SaplingRegistry registry;

    public SaplingBiomeHandler(SaplingRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    public boolean canGrowInBiome(String saplingType, String biomeKey) {
        Objects.requireNonNull(saplingType, "saplingType");
        Objects.requireNonNull(biomeKey, "biomeKey");

        Optional<SaplingRule> ruleOpt = registry.getRuleFor(saplingType);
        if (ruleOpt.isPresent()) {
            SaplingRule rule = ruleOpt.get();
            NamespacedKey biomeNsk = NamespacedKey.parse(biomeKey);

            if (!rule.deniedBiomes().isEmpty() && rule.deniedBiomes().contains(biomeNsk)) {
                return false;
            }

            if (!rule.allowedBiomes().isEmpty()) {
                return rule.allowedBiomes().contains(biomeNsk);
            }

            return true;
        }

        return true;
    }

    public double getGrowthRateMultiplier(String saplingType, String biomeKey) {
        Objects.requireNonNull(saplingType, "saplingType");
        Objects.requireNonNull(biomeKey, "biomeKey");

        Optional<SaplingRule> ruleOpt = registry.getRuleFor(saplingType);
        if (ruleOpt.isPresent()) {
            SaplingRule rule = ruleOpt.get();
            double baseMultiplier = rule.growthRateMultiplier();

            NamespacedKey biomeNsk = NamespacedKey.parse(biomeKey);
            if (isNativeBiome(saplingType, biomeKey)) {
                double nativeBonus = NATIVE_BIOME_GROWTH_BONUS.getOrDefault(saplingType, 1.0);
                return baseMultiplier * nativeBonus;
            }

            if (!rule.deniedBiomes().isEmpty() && rule.deniedBiomes().contains(biomeNsk)) {
                return 0.0;
            }

            return baseMultiplier;
        }

        if (isNativeBiome(saplingType, biomeKey)) {
            return NATIVE_BIOME_GROWTH_BONUS.getOrDefault(saplingType, DEFAULT_GROWTH_RATE);
        }

        return DEFAULT_GROWTH_RATE;
    }

    public String getTreeVariant(String saplingType, String biomeKey) {
        Objects.requireNonNull(saplingType, "saplingType");
        Objects.requireNonNull(biomeKey, "biomeKey");

        Optional<SaplingRule> ruleOpt = registry.getRuleFor(saplingType);
        if (ruleOpt.isPresent()) {
            SaplingRule rule = ruleOpt.get();
            NamespacedKey biomeNsk = NamespacedKey.parse(biomeKey);
            String override = rule.treeVariantOverride().get(biomeNsk);
            if (override != null) {
                return override;
            }
        }

        return resolveDefaultVariant(saplingType, biomeKey);
    }

    private String resolveDefaultVariant(String saplingType, String biomeKey) {
        return switch (saplingType) {
            case "oak" -> {
                if (biomeKey.contains("swamp")) yield "swamp_oak";
                if (biomeKey.contains("forest")) yield "fancy_oak";
                yield "oak";
            }
            case "spruce" -> {
                if (biomeKey.contains("old_growth")) yield "mega_spruce";
                if (biomeKey.contains("snowy")) yield "snowy_spruce";
                yield "spruce";
            }
            case "birch" -> {
                if (biomeKey.contains("old_growth")) yield "tall_birch";
                yield "birch";
            }
            case "jungle" -> {
                if (biomeKey.contains("sparse")) yield "small_jungle";
                yield "mega_jungle";
            }
            default -> saplingType;
        };
    }

    private boolean isNativeBiome(String saplingType, String biomeKey) {
        Set<String> nativeBiomes = DEFAULT_BIOME_AFFINITY.get(saplingType);
        return nativeBiomes != null && nativeBiomes.contains(biomeKey);
    }
}
