package com.arthou.arthousreactions.reaction;

/** The original recording's seven contacts, preceded by a short arm preparation. */
public final class ClapTiming {
    public static final int PREPARATION_TICKS = 4;
    public static final int DURATION_MS = 2300;
    private static final float[] IMPACTS = {0.04F, 0.305F, 0.6F, 0.91F, 1.195F, 1.49F, 1.78F};

    private ClapTiming() { }

    public static int count() { return IMPACTS.length; }

    public static float soundImpact(int index) { return PREPARATION_TICKS / 20.0F + IMPACTS[index]; }

    public static float poseImpact(int index) { return Math.round(soundImpact(index) * 20.0F) / 20.0F; }
}
