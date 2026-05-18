package com.protectcord.strata.noise.composite;

import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.api.noise.NoiseFunction;
import com.protectcord.strata.noise.NoiseFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CompositeNoiseBuilder Tests")
class CompositeNoiseBuilderTest {

    private static final NamespacedKey KEY = NamespacedKey.strata("test_noise");
    private static final double EPSILON = 1e-12;

    @Test
    @DisplayName("abs() converts negative values to positive")
    void absConvertsNegativeToPositive() {
        NoiseFunction source = NoiseFactory.constant(KEY, -0.7);
        NoiseFunction result = CompositeNoiseBuilder.wrap(source).abs().build(KEY);
        assertEquals(0.7, result.sample(0, 0), EPSILON);
        assertEquals(0.7, result.sample(0, 0, 0), EPSILON);
    }

    @Test
    @DisplayName("abs() leaves positive values unchanged")
    void absLeavesPositiveUnchanged() {
        NoiseFunction source = NoiseFactory.constant(KEY, 0.5);
        NoiseFunction result = CompositeNoiseBuilder.wrap(source).abs().build(KEY);
        assertEquals(0.5, result.sample(0, 0), EPSILON);
    }

    @Test
    @DisplayName("abs() converts zero to zero")
    void absOfZero() {
        NoiseFunction source = NoiseFactory.constant(KEY, 0.0);
        NoiseFunction result = CompositeNoiseBuilder.wrap(source).abs().build(KEY);
        assertEquals(0.0, result.sample(0, 0), EPSILON);
    }

    @Test
    @DisplayName("negate() flips the sign of positive values")
    void negateFlipsPositive() {
        NoiseFunction source = NoiseFactory.constant(KEY, 0.3);
        NoiseFunction result = CompositeNoiseBuilder.wrap(source).negate().build(KEY);
        assertEquals(-0.3, result.sample(0, 0), EPSILON);
    }

    @Test
    @DisplayName("negate() flips the sign of negative values")
    void negateFlipsNegative() {
        NoiseFunction source = NoiseFactory.constant(KEY, -0.8);
        NoiseFunction result = CompositeNoiseBuilder.wrap(source).negate().build(KEY);
        assertEquals(0.8, result.sample(0, 0), EPSILON);
    }

    @Test
    @DisplayName("square() squares the value")
    void squareSquaresValue() {
        NoiseFunction source = NoiseFactory.constant(KEY, 0.5);
        NoiseFunction result = CompositeNoiseBuilder.wrap(source).square().build(KEY);
        assertEquals(0.25, result.sample(0, 0), EPSILON);
    }

    @Test
    @DisplayName("square() of negative value yields positive result")
    void squareOfNegativeIsPositive() {
        NoiseFunction source = NoiseFactory.constant(KEY, -0.4);
        NoiseFunction result = CompositeNoiseBuilder.wrap(source).square().build(KEY);
        assertEquals(0.16, result.sample(0, 0), EPSILON);
    }

    @Test
    @DisplayName("add() adds a constant value")
    void addAddsConstant() {
        NoiseFunction source = NoiseFactory.constant(KEY, 0.3);
        NoiseFunction result = CompositeNoiseBuilder.wrap(source).add(0.5).build(KEY);
        assertEquals(0.8, result.sample(0, 0), EPSILON);
    }

    @Test
    @DisplayName("add() with negative value subtracts")
    void addWithNegative() {
        NoiseFunction source = NoiseFactory.constant(KEY, 0.5);
        NoiseFunction result = CompositeNoiseBuilder.wrap(source).add(-0.2).build(KEY);
        assertEquals(0.3, result.sample(0, 0), EPSILON);
    }

    @Test
    @DisplayName("multiply() scales the value")
    void multiplyScalesValue() {
        NoiseFunction source = NoiseFactory.constant(KEY, 0.4);
        NoiseFunction result = CompositeNoiseBuilder.wrap(source).multiply(2.0).build(KEY);
        assertEquals(0.8, result.sample(0, 0), EPSILON);
    }

    @Test
    @DisplayName("multiply() by zero yields zero")
    void multiplyByZero() {
        NoiseFunction source = NoiseFactory.constant(KEY, 0.9);
        NoiseFunction result = CompositeNoiseBuilder.wrap(source).multiply(0.0).build(KEY);
        assertEquals(0.0, result.sample(0, 0), EPSILON);
    }

    @Test
    @DisplayName("clamp() constrains value to [min, max]")
    void clampConstrainsToRange() {
        NoiseFunction source = NoiseFactory.constant(KEY, 0.8);
        NoiseFunction result = CompositeNoiseBuilder.wrap(source).clamp(0, 0.5).build(KEY);
        assertEquals(0.5, result.sample(0, 0), EPSILON);
    }

    @Test
    @DisplayName("clamp() clamps negative values to min")
    void clampClampsNegativeToMin() {
        NoiseFunction source = NoiseFactory.constant(KEY, -0.5);
        NoiseFunction result = CompositeNoiseBuilder.wrap(source).clamp(0, 1).build(KEY);
        assertEquals(0.0, result.sample(0, 0), EPSILON);
    }

    @Test
    @DisplayName("clamp() leaves value within range unchanged")
    void clampLeavesWithinRangeUnchanged() {
        NoiseFunction source = NoiseFactory.constant(KEY, 0.3);
        NoiseFunction result = CompositeNoiseBuilder.wrap(source).clamp(0, 0.5).build(KEY);
        assertEquals(0.3, result.sample(0, 0), EPSILON);
    }

    @Test
    @DisplayName("remap() maps [-1, 1] to [0, 100]")
    void remapMapsRange() {
        NoiseFunction source = NoiseFactory.constant(KEY, 0.0);
        NoiseFunction result = CompositeNoiseBuilder.wrap(source)
                .remap(-1, 1, 0, 100).build(KEY);
        assertEquals(50.0, result.sample(0, 0), EPSILON);
    }

    @Test
    @DisplayName("remap() maps min boundary correctly")
    void remapMinBoundary() {
        NoiseFunction source = NoiseFactory.constant(KEY, -1.0);
        NoiseFunction result = CompositeNoiseBuilder.wrap(source)
                .remap(-1, 1, 0, 100).build(KEY);
        assertEquals(0.0, result.sample(0, 0), EPSILON);
    }

    @Test
    @DisplayName("remap() maps max boundary correctly")
    void remapMaxBoundary() {
        NoiseFunction source = NoiseFactory.constant(KEY, 1.0);
        NoiseFunction result = CompositeNoiseBuilder.wrap(source)
                .remap(-1, 1, 0, 100).build(KEY);
        assertEquals(100.0, result.sample(0, 0), EPSILON);
    }

    @Test
    @DisplayName("remap() clamps values outside source range")
    void remapClampsOutOfRange() {
        NoiseFunction source = NoiseFactory.constant(KEY, 2.0);
        NoiseFunction result = CompositeNoiseBuilder.wrap(source)
                .remap(-1, 1, 0, 100).build(KEY);
        assertEquals(100.0, result.sample(0, 0), EPSILON);
    }

    @Test
    @DisplayName("invert() returns 1.0 - value")
    void invertReturns1MinusValue() {
        NoiseFunction source = NoiseFactory.constant(KEY, 0.3);
        NoiseFunction result = CompositeNoiseBuilder.wrap(source).invert().build(KEY);
        assertEquals(0.7, result.sample(0, 0), EPSILON);
    }

    @Test
    @DisplayName("invert() of zero is one")
    void invertOfZero() {
        NoiseFunction source = NoiseFactory.constant(KEY, 0.0);
        NoiseFunction result = CompositeNoiseBuilder.wrap(source).invert().build(KEY);
        assertEquals(1.0, result.sample(0, 0), EPSILON);
    }

    @Test
    @DisplayName("invert() of one is zero")
    void invertOfOne() {
        NoiseFunction source = NoiseFactory.constant(KEY, 1.0);
        NoiseFunction result = CompositeNoiseBuilder.wrap(source).invert().build(KEY);
        assertEquals(0.0, result.sample(0, 0), EPSILON);
    }

    @Test
    @DisplayName("Chained operations apply in correct order: abs then negate")
    void chainAbsThenNegate() {
        NoiseFunction source = NoiseFactory.constant(KEY, -0.5);
        NoiseFunction result = CompositeNoiseBuilder.wrap(source)
                .abs()
                .negate()
                .build(KEY);
        assertEquals(-0.5, result.sample(0, 0), EPSILON);
    }

    @Test
    @DisplayName("Chained operations apply in correct order: negate then abs")
    void chainNegateThenAbs() {
        NoiseFunction source = NoiseFactory.constant(KEY, -0.5);
        NoiseFunction result = CompositeNoiseBuilder.wrap(source)
                .negate()
                .abs()
                .build(KEY);
        assertEquals(0.5, result.sample(0, 0), EPSILON);
    }

    @Test
    @DisplayName("Order matters: multiply then add differs from add then multiply")
    void orderMattersMultiplyAddVsAddMultiply() {
        NoiseFunction source = NoiseFactory.constant(KEY, 0.5);

        NoiseFunction mulThenAdd = CompositeNoiseBuilder.wrap(source)
                .multiply(2.0)
                .add(0.1)
                .build(KEY);

        NoiseFunction addThenMul = CompositeNoiseBuilder.wrap(source)
                .add(0.1)
                .multiply(2.0)
                .build(KEY);

        double resultMulAdd = mulThenAdd.sample(0, 0);
        double resultAddMul = addThenMul.sample(0, 0);

        assertEquals(1.1, resultMulAdd, EPSILON);
        assertEquals(1.2, resultAddMul, EPSILON);
        assertNotEquals(resultMulAdd, resultAddMul, EPSILON);
    }

    @Test
    @DisplayName("Complex chain: abs -> multiply -> add -> clamp")
    void complexChain() {
        NoiseFunction source = NoiseFactory.constant(KEY, -0.6);
        NoiseFunction result = CompositeNoiseBuilder.wrap(source)
                .abs()
                .multiply(2.0)
                .add(-0.5)
                .clamp(0, 1)
                .build(KEY);
        // -0.6 -> abs -> 0.6 -> *2 -> 1.2 -> +(-0.5) -> 0.7 -> clamp(0,1) -> 0.7
        assertEquals(0.7, result.sample(0, 0), EPSILON);
    }

    @Test
    @DisplayName("Chain result works for 3D sampling too")
    void chainWorksFor3D() {
        NoiseFunction source = NoiseFactory.constant(KEY, 0.4);
        NoiseFunction result = CompositeNoiseBuilder.wrap(source)
                .multiply(2.0)
                .add(0.1)
                .build(KEY);
        assertEquals(0.9, result.sample(0, 0, 0), EPSILON);
    }

    @Test
    @DisplayName("Built noise has the assigned key")
    void builtNoiseHasAssignedKey() {
        NamespacedKey resultKey = NamespacedKey.strata("result_noise");
        NoiseFunction source = NoiseFactory.constant(KEY, 0.5);
        NoiseFunction result = CompositeNoiseBuilder.wrap(source).abs().build(resultKey);
        assertEquals(resultKey, result.key());
    }

    @Test
    @DisplayName("No-op builder (no operations) passes through source value")
    void noOpBuilderPassesThrough() {
        NoiseFunction source = NoiseFactory.constant(KEY, 0.42);
        NoiseFunction result = CompositeNoiseBuilder.wrap(source).build(KEY);
        assertEquals(0.42, result.sample(5.0, 10.0), EPSILON);
        assertEquals(0.42, result.sample(5.0, 10.0, 15.0), EPSILON);
    }

    @Test
    @DisplayName("square() then invert() produces expected result")
    void squareThenInvert() {
        NoiseFunction source = NoiseFactory.constant(KEY, 0.5);
        NoiseFunction result = CompositeNoiseBuilder.wrap(source)
                .square()
                .invert()
                .build(KEY);
        // 0.5 -> square -> 0.25 -> invert -> 0.75
        assertEquals(0.75, result.sample(0, 0), EPSILON);
    }
}
