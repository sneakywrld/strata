package com.protectcord.strata.noise.composite;

import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.api.noise.NoiseFunction;
import com.protectcord.strata.noise.math.NoiseMath;
import com.protectcord.strata.noise.math.SplineInterpolator;

import java.util.*;

/**
 * A directed acyclic graph of noise nodes compiled from configuration.
 *
 * <p>Each node is either a <em>source</em> (a concrete {@link NoiseFunction}) or an
 * <em>operation</em> ({@link NoiseOperation} applied to named input nodes).
 * The graph is evaluated in topological order so that every node's inputs
 * are resolved before it is evaluated.</p>
 *
 * <p>Example:
 * <pre>
 * NoiseGraph graph = new NoiseGraph();
 * graph.addSource("base", perlinNoise);
 * graph.addSource("mask", simplexNoise);
 * graph.addOperation("abs_base", NoiseOperation.ABS, "base");
 * graph.addOperation("blended", NoiseOperation.MULTIPLY, "abs_base", "mask");
 * NoiseFunction compiled = graph.compile(NamespacedKey.strata("terrain"));
 * </pre>
 */
public final class NoiseGraph {

    private final Map<String, Node> nodes = new LinkedHashMap<>();
    private String outputNode;

    public void addSource(String name, NoiseFunction source) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(source, "source");
        if (nodes.containsKey(name)) {
            throw new IllegalArgumentException("Duplicate node name: " + name);
        }
        nodes.put(name, new SourceNode(source));
    }

    public void addOperation(String name, NoiseOperation op, String... inputNames) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(op, "op");
        if (nodes.containsKey(name)) {
            throw new IllegalArgumentException("Duplicate node name: " + name);
        }
        if (op != NoiseOperation.SPLINE && inputNames.length != op.operandCount()) {
            throw new IllegalArgumentException(
                    op + " expects " + op.operandCount() + " inputs, got " + inputNames.length);
        }
        for (String input : inputNames) {
            if (!nodes.containsKey(input)) {
                throw new IllegalArgumentException("Unknown input node: " + input);
            }
        }
        nodes.put(name, new OperationNode(op, List.of(inputNames)));
    }

    public void addSplineOperation(String name, String inputName,
                                   double[] xs, double[] ys) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(inputName, "inputName");
        if (nodes.containsKey(name)) {
            throw new IllegalArgumentException("Duplicate node name: " + name);
        }
        if (!nodes.containsKey(inputName)) {
            throw new IllegalArgumentException("Unknown input node: " + inputName);
        }
        SplineInterpolator spline = new SplineInterpolator(xs, ys);
        nodes.put(name, new SplineNode(inputName, spline));
    }

    public void setOutput(String name) {
        if (!nodes.containsKey(name)) {
            throw new IllegalArgumentException("Unknown output node: " + name);
        }
        this.outputNode = name;
    }

    public double evaluate(double x, double z) {
        List<String> order = topologicalSort();
        Map<String, Double> values = new HashMap<>();
        for (String name : order) {
            values.put(name, nodes.get(name).evaluate2D(x, z, values));
        }
        return values.get(resolveOutput());
    }

    public double evaluate(double x, double y, double z) {
        List<String> order = topologicalSort();
        Map<String, Double> values = new HashMap<>();
        for (String name : order) {
            values.put(name, nodes.get(name).evaluate3D(x, y, z, values));
        }
        return values.get(resolveOutput());
    }

    public NoiseFunction compile(NamespacedKey key) {
        List<String> order = topologicalSort();
        String output = resolveOutput();

        return new NoiseFunction() {
            @Override
            public NamespacedKey key() { return key; }

            @Override
            public double sample(double x, double z) {
                Map<String, Double> values = new HashMap<>();
                for (String name : order) {
                    values.put(name, nodes.get(name).evaluate2D(x, z, values));
                }
                return values.get(output);
            }

            @Override
            public double sample(double x, double y, double z) {
                Map<String, Double> values = new HashMap<>();
                for (String name : order) {
                    values.put(name, nodes.get(name).evaluate3D(x, y, z, values));
                }
                return values.get(output);
            }

            @Override
            public double minValue() { return -1.0; }

            @Override
            public double maxValue() { return 1.0; }
        };
    }

    private String resolveOutput() {
        if (outputNode != null) {
            return outputNode;
        }
        String last = null;
        for (String name : nodes.keySet()) {
            last = name;
        }
        if (last == null) {
            throw new IllegalStateException("NoiseGraph has no nodes");
        }
        return last;
    }

    private List<String> topologicalSort() {
        Map<String, Integer> inDegree = new LinkedHashMap<>();
        for (String name : nodes.keySet()) {
            inDegree.put(name, 0);
        }
        for (Node node : nodes.values()) {
            for (String dep : node.dependencies()) {
                inDegree.merge(dep, 0, Integer::sum);
            }
        }
        for (Map.Entry<String, Node> entry : nodes.entrySet()) {
            for (String dep : entry.getValue().dependencies()) {
                inDegree.merge(entry.getKey(), 1, (a, b) -> a + 1);
            }
        }

        Deque<String> queue = new ArrayDeque<>();
        Map<String, Integer> actualInDegree = new LinkedHashMap<>();
        for (String name : nodes.keySet()) {
            actualInDegree.put(name, 0);
        }
        for (Map.Entry<String, Node> entry : nodes.entrySet()) {
            for (String dep : entry.getValue().dependencies()) {
                actualInDegree.merge(entry.getKey(), 1, Integer::sum);
            }
        }

        for (Map.Entry<String, Integer> entry : actualInDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.add(entry.getKey());
            }
        }

        List<String> sorted = new ArrayList<>();
        while (!queue.isEmpty()) {
            String current = queue.poll();
            sorted.add(current);
            for (Map.Entry<String, Node> entry : nodes.entrySet()) {
                if (entry.getValue().dependencies().contains(current)) {
                    int newDegree = actualInDegree.merge(entry.getKey(), -1, Integer::sum);
                    if (newDegree == 0) {
                        queue.add(entry.getKey());
                    }
                }
            }
        }

        if (sorted.size() != nodes.size()) {
            throw new IllegalStateException("Cycle detected in noise graph");
        }
        return sorted;
    }

    private sealed interface Node {
        double evaluate2D(double x, double z, Map<String, Double> resolved);
        double evaluate3D(double x, double y, double z, Map<String, Double> resolved);
        List<String> dependencies();
    }

    private record SourceNode(NoiseFunction source) implements Node {
        @Override
        public double evaluate2D(double x, double z, Map<String, Double> resolved) {
            return source.sample(x, z);
        }

        @Override
        public double evaluate3D(double x, double y, double z, Map<String, Double> resolved) {
            return source.sample(x, y, z);
        }

        @Override
        public List<String> dependencies() {
            return List.of();
        }
    }

    private record OperationNode(NoiseOperation op, List<String> inputs) implements Node {
        @Override
        public double evaluate2D(double x, double z, Map<String, Double> resolved) {
            return applyOp(resolved);
        }

        @Override
        public double evaluate3D(double x, double y, double z, Map<String, Double> resolved) {
            return applyOp(resolved);
        }

        @Override
        public List<String> dependencies() {
            return inputs;
        }

        private double applyOp(Map<String, Double> resolved) {
            return switch (op) {
                case ABS -> Math.abs(resolved.get(inputs.get(0)));
                case NEGATE -> -resolved.get(inputs.get(0));
                case ADD -> resolved.get(inputs.get(0)) + resolved.get(inputs.get(1));
                case MULTIPLY -> resolved.get(inputs.get(0)) * resolved.get(inputs.get(1));
                case MIN -> Math.min(resolved.get(inputs.get(0)), resolved.get(inputs.get(1)));
                case MAX -> Math.max(resolved.get(inputs.get(0)), resolved.get(inputs.get(1)));
                case POWER -> {
                    double base = resolved.get(inputs.get(0));
                    double exp = resolved.get(inputs.get(1));
                    yield Math.signum(base) * Math.pow(Math.abs(base), exp);
                }
                case CLAMP -> NoiseMath.clamp(
                        resolved.get(inputs.get(0)),
                        resolved.get(inputs.get(1)),
                        resolved.get(inputs.get(2)));
                case LERP -> NoiseMath.lerp(
                        resolved.get(inputs.get(0)),
                        resolved.get(inputs.get(1)),
                        resolved.get(inputs.get(2)));
                case THRESHOLD -> {
                    double value = resolved.get(inputs.get(0));
                    double threshold = resolved.get(inputs.get(1));
                    double falloff = resolved.get(inputs.get(2));
                    if (falloff <= 0) {
                        yield value >= threshold ? 1.0 : 0.0;
                    }
                    yield NoiseMath.clamp((value - threshold + falloff) / (2.0 * falloff), 0.0, 1.0);
                }
                case SPLINE -> throw new IllegalStateException("SPLINE handled by SplineNode");
            };
        }
    }

    private record SplineNode(String inputName, SplineInterpolator spline) implements Node {
        @Override
        public double evaluate2D(double x, double z, Map<String, Double> resolved) {
            return spline.evaluate(resolved.get(inputName));
        }

        @Override
        public double evaluate3D(double x, double y, double z, Map<String, Double> resolved) {
            return spline.evaluate(resolved.get(inputName));
        }

        @Override
        public List<String> dependencies() {
            return List.of(inputName);
        }
    }
}
