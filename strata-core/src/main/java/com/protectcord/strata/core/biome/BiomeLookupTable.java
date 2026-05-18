package com.protectcord.strata.core.biome;

import com.protectcord.strata.api.biome.Biome;
import com.protectcord.strata.api.biome.ClimateParameters;

import java.util.List;

/**
 * KD-tree based biome lookup table for fast nearest-neighbor search
 * in 5D climate parameter space (temperature, humidity, continentalness,
 * erosion, weirdness).
 */
public final class BiomeLookupTable {

    private final KDNode root;

    private BiomeLookupTable(KDNode root) {
        this.root = root;
    }

    /**
     * Builds a KD-tree from the given biome list.
     */
    public static BiomeLookupTable build(List<Biome> biomes) {
        if (biomes.isEmpty()) throw new IllegalArgumentException("No biomes to build lookup table");
        KDNode root = buildNode(biomes, 0);
        return new BiomeLookupTable(root);
    }

    /**
     * Finds the nearest biome for the given climate parameters.
     */
    public Biome lookup(ClimateParameters target) {
        BestMatch best = new BestMatch();
        search(root, target, 0, best);
        return best.biome;
    }

    private static KDNode buildNode(List<Biome> biomes, int depth) {
        if (biomes.isEmpty()) return null;
        if (biomes.size() == 1) return new KDNode(biomes.getFirst(), null, null);

        int axis = depth % 5;
        List<Biome> sorted = biomes.stream()
                .sorted((a, b) -> Double.compare(axisValue(a.climate(), axis), axisValue(b.climate(), axis)))
                .toList();

        int mid = sorted.size() / 2;
        return new KDNode(
                sorted.get(mid),
                buildNode(sorted.subList(0, mid), depth + 1),
                buildNode(sorted.subList(mid + 1, sorted.size()), depth + 1)
        );
    }

    private void search(KDNode node, ClimateParameters target, int depth, BestMatch best) {
        if (node == null) return;

        double dist = target.distanceSquared(node.biome.climate());
        if (dist < best.distSq) {
            best.distSq = dist;
            best.biome = node.biome;
        }

        int axis = depth % 5;
        double diff = axisValue(target, axis) - axisValue(node.biome.climate(), axis);

        KDNode first = diff < 0 ? node.left : node.right;
        KDNode second = diff < 0 ? node.right : node.left;

        search(first, target, depth + 1, best);

        if (diff * diff < best.distSq) {
            search(second, target, depth + 1, best);
        }
    }

    private static double axisValue(ClimateParameters p, int axis) {
        return switch (axis) {
            case 0 -> p.temperature();
            case 1 -> p.humidity();
            case 2 -> p.continentalness();
            case 3 -> p.erosion();
            case 4 -> p.weirdness();
            default -> throw new AssertionError();
        };
    }

    private record KDNode(Biome biome, KDNode left, KDNode right) {}

    private static final class BestMatch {
        Biome biome;
        double distSq = Double.MAX_VALUE;
    }
}
