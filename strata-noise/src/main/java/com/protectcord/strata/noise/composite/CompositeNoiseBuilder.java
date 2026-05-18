package com.protectcord.strata.noise.composite;

import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.api.noise.NoiseFunction;
import com.protectcord.strata.noise.math.NoiseMath;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

/**
 * Builder for composing noise functions through chained operations.
 * Each operation transforms the output of the previous step.
 *
 * <p>Example:
 * <pre>
 * NoiseFunction shaped = CompositeNoiseBuilder.wrap(baseNoise)
 *     .abs()
 *     .multiply(2.5)
 *     .clamp(0, 1)
 *     .build(NamespacedKey.strata("mountain_shape"));
 * </pre>
 */
public final class CompositeNoiseBuilder {

    private final NoiseFunction source;
    private final List<DoubleUnaryOperator> ops = new ArrayList<>();

    private CompositeNoiseBuilder(NoiseFunction source) {
        this.source = source;
    }

    public static CompositeNoiseBuilder wrap(NoiseFunction source) {
        return new CompositeNoiseBuilder(source);
    }

    public CompositeNoiseBuilder abs() {
        ops.add(Math::abs);
        return this;
    }

    public CompositeNoiseBuilder negate() {
        ops.add(v -> -v);
        return this;
    }

    public CompositeNoiseBuilder square() {
        ops.add(v -> v * v);
        return this;
    }

    public CompositeNoiseBuilder cube() {
        ops.add(v -> v * v * v);
        return this;
    }

    public CompositeNoiseBuilder sqrt() {
        ops.add(v -> Math.signum(v) * Math.sqrt(Math.abs(v)));
        return this;
    }

    public CompositeNoiseBuilder add(double value) {
        ops.add(v -> v + value);
        return this;
    }

    public CompositeNoiseBuilder multiply(double value) {
        ops.add(v -> v * value);
        return this;
    }

    public CompositeNoiseBuilder clamp(double min, double max) {
        ops.add(v -> NoiseMath.clamp(v, min, max));
        return this;
    }

    public CompositeNoiseBuilder power(double exponent) {
        ops.add(v -> Math.signum(v) * Math.pow(Math.abs(v), exponent));
        return this;
    }

    public CompositeNoiseBuilder invert() {
        ops.add(v -> 1.0 - v);
        return this;
    }

    public CompositeNoiseBuilder remap(double fromMin, double fromMax, double toMin, double toMax) {
        ops.add(v -> {
            double t = (v - fromMin) / (fromMax - fromMin);
            return NoiseMath.lerp(toMin, toMax, NoiseMath.clamp(t, 0, 1));
        });
        return this;
    }

    public CompositeNoiseBuilder custom(DoubleUnaryOperator op) {
        ops.add(op);
        return this;
    }

    public NoiseFunction build(NamespacedKey key) {
        DoubleUnaryOperator chain = DoubleUnaryOperator.identity();
        for (DoubleUnaryOperator op : ops) {
            chain = chain.andThen(op);
        }
        final DoubleUnaryOperator finalChain = chain;

        return new NoiseFunction() {
            @Override
            public NamespacedKey key() { return key; }

            @Override
            public double sample(double x, double z) {
                return finalChain.applyAsDouble(source.sample(x, z));
            }

            @Override
            public double sample(double x, double y, double z) {
                return finalChain.applyAsDouble(source.sample(x, y, z));
            }

            @Override
            public double minValue() { return -1.0; }

            @Override
            public double maxValue() { return 1.0; }
        };
    }
}
