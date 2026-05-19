package com.protectcord.strata.nms.legacy;

import com.protectcord.strata.api.block.StrataBlockState;

import java.util.HashMap;
import java.util.Map;

public final class LegacyBlockTable {

    private final Map<String, Integer> nameToId = new HashMap<>();
    private final Map<Integer, String> idToName = new HashMap<>();

    public LegacyBlockTable() {
        registerDefaults();
    }

    public int getNumericId(String namespacedKey) {
        return nameToId.getOrDefault(namespacedKey, 0);
    }

    public String getNamespacedKey(int numericId) {
        return idToName.getOrDefault(numericId, "minecraft:air");
    }

    public int getNumericId(StrataBlockState state) {
        return getNumericId(state.blockId().toString());
    }

    private void registerDefaults() {
        register(0, "minecraft:air");
        register(1, "minecraft:stone");
        register(2, "minecraft:grass_block");
        register(3, "minecraft:dirt");
        register(4, "minecraft:cobblestone");
        register(5, "minecraft:oak_planks");
        register(7, "minecraft:bedrock");
        register(8, "minecraft:water");
        register(9, "minecraft:water");
        register(10, "minecraft:lava");
        register(11, "minecraft:lava");
        register(12, "minecraft:sand");
        register(13, "minecraft:gravel");
        register(14, "minecraft:gold_ore");
        register(15, "minecraft:iron_ore");
        register(16, "minecraft:coal_ore");
        register(17, "minecraft:oak_log");
        register(18, "minecraft:oak_leaves");
        register(24, "minecraft:sandstone");
        register(31, "minecraft:short_grass");
        register(32, "minecraft:dead_bush");
        register(37, "minecraft:dandelion");
        register(38, "minecraft:poppy");
        register(44, "minecraft:stone_slab");
        register(46, "minecraft:tnt");
        register(48, "minecraft:mossy_cobblestone");
        register(49, "minecraft:obsidian");
        register(56, "minecraft:diamond_ore");
        register(73, "minecraft:redstone_ore");
        register(78, "minecraft:snow");
        register(79, "minecraft:ice");
        register(80, "minecraft:snow_block");
        register(81, "minecraft:cactus");
        register(82, "minecraft:clay");
        register(83, "minecraft:sugar_cane");
        register(86, "minecraft:pumpkin");
        register(87, "minecraft:netherrack");
        register(88, "minecraft:soul_sand");
        register(89, "minecraft:glowstone");
        register(98, "minecraft:stone_bricks");
        register(110, "minecraft:mycelium");
        register(121, "minecraft:end_stone");
        register(129, "minecraft:emerald_ore");
        register(153, "minecraft:nether_quartz_ore");
        register(162, "minecraft:acacia_log");
        register(174, "minecraft:packed_ice");
    }

    private void register(int id, String name) {
        nameToId.put(name, id);
        idToName.put(id, name);
    }
}
