package com.protectcord.strata.nms.legacy;

import java.util.HashMap;
import java.util.Map;

public final class BlockSubstitution {

    private static final Map<String, String> SUBSTITUTIONS = new HashMap<>();

    static {
        // 1.13+ blocks → closest pre-1.13 equivalent
        SUBSTITUTIONS.put("minecraft:grass_block", "minecraft:grass");
        SUBSTITUTIONS.put("minecraft:deepslate", "minecraft:stone");
        SUBSTITUTIONS.put("minecraft:deepslate_diamond_ore", "minecraft:diamond_ore");
        SUBSTITUTIONS.put("minecraft:deepslate_iron_ore", "minecraft:iron_ore");
        SUBSTITUTIONS.put("minecraft:deepslate_gold_ore", "minecraft:gold_ore");
        SUBSTITUTIONS.put("minecraft:deepslate_coal_ore", "minecraft:coal_ore");
        SUBSTITUTIONS.put("minecraft:deepslate_copper_ore", "minecraft:iron_ore");
        SUBSTITUTIONS.put("minecraft:deepslate_redstone_ore", "minecraft:redstone_ore");
        SUBSTITUTIONS.put("minecraft:deepslate_lapis_ore", "minecraft:lapis_ore");
        SUBSTITUTIONS.put("minecraft:deepslate_emerald_ore", "minecraft:emerald_ore");
        SUBSTITUTIONS.put("minecraft:copper_ore", "minecraft:iron_ore");
        SUBSTITUTIONS.put("minecraft:raw_copper_block", "minecraft:iron_block");
        SUBSTITUTIONS.put("minecraft:raw_iron_block", "minecraft:iron_block");
        SUBSTITUTIONS.put("minecraft:raw_gold_block", "minecraft:gold_block");
        SUBSTITUTIONS.put("minecraft:tuff", "minecraft:stone");
        SUBSTITUTIONS.put("minecraft:calcite", "minecraft:stone");
        SUBSTITUTIONS.put("minecraft:amethyst_block", "minecraft:purple_stained_glass");
        SUBSTITUTIONS.put("minecraft:budding_amethyst", "minecraft:purple_stained_glass");
        SUBSTITUTIONS.put("minecraft:moss_block", "minecraft:grass_block");
        SUBSTITUTIONS.put("minecraft:mud", "minecraft:dirt");
        SUBSTITUTIONS.put("minecraft:muddy_mangrove_roots", "minecraft:dirt");
        SUBSTITUTIONS.put("minecraft:mangrove_roots", "minecraft:oak_log");
        SUBSTITUTIONS.put("minecraft:mangrove_log", "minecraft:oak_log");
        SUBSTITUTIONS.put("minecraft:cherry_log", "minecraft:birch_log");
        SUBSTITUTIONS.put("minecraft:cherry_leaves", "minecraft:birch_leaves");
        SUBSTITUTIONS.put("minecraft:sculk", "minecraft:black_wool");
        SUBSTITUTIONS.put("minecraft:sculk_sensor", "minecraft:black_wool");
        SUBSTITUTIONS.put("minecraft:sculk_catalyst", "minecraft:black_wool");
        SUBSTITUTIONS.put("minecraft:sculk_shrieker", "minecraft:black_wool");
        SUBSTITUTIONS.put("minecraft:sculk_vein", "minecraft:air");
        SUBSTITUTIONS.put("minecraft:pointed_dripstone", "minecraft:cobblestone");
        SUBSTITUTIONS.put("minecraft:dripstone_block", "minecraft:stone");
        SUBSTITUTIONS.put("minecraft:glow_lichen", "minecraft:vine");
        SUBSTITUTIONS.put("minecraft:cave_air", "minecraft:air");
        SUBSTITUTIONS.put("minecraft:powder_snow", "minecraft:snow_block");
        SUBSTITUTIONS.put("minecraft:azalea_leaves", "minecraft:oak_leaves");
        SUBSTITUTIONS.put("minecraft:flowering_azalea_leaves", "minecraft:oak_leaves");
        SUBSTITUTIONS.put("minecraft:rooted_dirt", "minecraft:dirt");
        SUBSTITUTIONS.put("minecraft:hanging_roots", "minecraft:air");
        SUBSTITUTIONS.put("minecraft:small_dripleaf", "minecraft:fern");
        SUBSTITUTIONS.put("minecraft:big_dripleaf", "minecraft:lily_pad");
        SUBSTITUTIONS.put("minecraft:spore_blossom", "minecraft:air");
        SUBSTITUTIONS.put("minecraft:blackstone", "minecraft:stone");
        SUBSTITUTIONS.put("minecraft:basalt", "minecraft:stone");
        SUBSTITUTIONS.put("minecraft:smooth_basalt", "minecraft:stone");
        SUBSTITUTIONS.put("minecraft:crimson_nylium", "minecraft:netherrack");
        SUBSTITUTIONS.put("minecraft:warped_nylium", "minecraft:netherrack");
        SUBSTITUTIONS.put("minecraft:crimson_stem", "minecraft:nether_brick");
        SUBSTITUTIONS.put("minecraft:warped_stem", "minecraft:nether_brick");
        SUBSTITUTIONS.put("minecraft:shroomlight", "minecraft:glowstone");
        SUBSTITUTIONS.put("minecraft:crying_obsidian", "minecraft:obsidian");
    }

    private BlockSubstitution() {}

    public static String substitute(String modernBlock) {
        return SUBSTITUTIONS.getOrDefault(modernBlock, modernBlock);
    }

    public static boolean hasSubstitute(String modernBlock) {
        return SUBSTITUTIONS.containsKey(modernBlock);
    }
}
