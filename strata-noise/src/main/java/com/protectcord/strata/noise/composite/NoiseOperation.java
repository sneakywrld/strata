package com.protectcord.strata.noise.composite;

/**
 * Operations that can be applied to noise values within a {@link NoiseGraph}.
 *
 * <p>Each operation has a defined number of input operands:
 * <ul>
 *   <li><b>Unary (1):</b> ABS, NEGATE</li>
 *   <li><b>Binary (2):</b> ADD, MULTIPLY, MIN, MAX, POWER, LERP</li>
 *   <li><b>Ternary (3):</b> CLAMP, THRESHOLD</li>
 *   <li><b>Variadic:</b> SPLINE (1 input + control points set externally)</li>
 * </ul>
 */
public enum NoiseOperation {

    ABS(1),
    NEGATE(1),
    ADD(2),
    MULTIPLY(2),
    CLAMP(3),
    SPLINE(1),
    LERP(3),
    MIN(2),
    MAX(2),
    POWER(2),
    THRESHOLD(3);

    private final int operandCount;

    NoiseOperation(int operandCount) {
        this.operandCount = operandCount;
    }

    /**
     * Returns the number of input operands this operation expects.
     *
     * <p>For CLAMP, the inputs are (value, min, max).
     * For LERP, the inputs are (a, b, t).
     * For THRESHOLD, the inputs are (value, threshold, falloff).
     * For POWER, the inputs are (base, exponent).</p>
     */
    public int operandCount() {
        return operandCount;
    }

    public boolean isUnary() {
        return operandCount == 1;
    }

    public boolean isBinary() {
        return operandCount == 2;
    }
}
