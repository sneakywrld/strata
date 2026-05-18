package com.protectcord.strata.nms;

/**
 * Enum of supported NMS version groups.
 */
public enum NMSVersion {
    V1_8_R3("1.8.8", 8),
    V1_12_R1("1.12.2", 8),
    V1_13_R2("1.13.2", 11),
    V1_16_R3("1.16.5", 16),
    V1_17_R1("1.17.1", 16),
    V1_18_R2("1.18.2", 17),
    V1_19_R3("1.19.4", 17),
    V1_20_R4("1.20.6", 21),
    V26_1("26.1", 25);

    private final String minecraftVersion;
    private final int javaVersion;

    NMSVersion(String minecraftVersion, int javaVersion) {
        this.minecraftVersion = minecraftVersion;
        this.javaVersion = javaVersion;
    }

    public String minecraftVersion() { return minecraftVersion; }
    public int javaVersion() { return javaVersion; }
}
