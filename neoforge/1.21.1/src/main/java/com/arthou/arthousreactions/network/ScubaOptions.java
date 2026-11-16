package com.arthou.arthousreactions.network;

/** Scuba reserves two seed bits for its per-selection music and cat options. */
public final class ScubaOptions {
    private static final int CAT = 1 << 30;
    private static final int RANDOM_BITS = CAT - 1;
    private ScubaOptions() {
    }

    // Preserve the existing 21-byte playback packet and the old reactions' seeds.
    public static int encode(int randomSeed, boolean withMusic, boolean withCat) {
        return (randomSeed & RANDOM_BITS) | (withMusic ? Integer.MIN_VALUE : 0) | (withCat ? CAT : 0);
    }

    public static boolean musicEnabled(int seed) {
        return seed < 0;
    }

    public static boolean catEnabled(int seed) {
        return (seed & CAT) != 0;
    }
}
