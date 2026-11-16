package com.arthou.arthousreactions.client;

/** Timing shared by the real wings and the exported motion preview, in seconds. */
final class AngelMotion {
    private AngelMotion() { }

    static float smooth(float value) {
        float t = Math.clamp(value, 0.0F, 1.0F);
        return t * t * t * (t * (t * 6.0F - 15.0F) + 10.0F);
    }

    static float fade(float seconds) {
        return smooth((seconds - 0.14F) / 0.48F) * (1.0F - smooth((seconds - 1.82F) / 0.38F));
    }

    static float opening(float seconds) {
        return smooth((seconds - 0.20F) / 0.56F) * (1.0F - smooth((seconds - 1.86F) / 0.34F));
    }

    static float wingAngle(float seconds, boolean second) {
        float side = second ? -1.0F : 1.0F;
        float open = opening(seconds);
        float flutter = (float) Math.sin(seconds * 4.0F) * 1.8F * open;
        return side * (-25.0F * (1.0F - open) + flutter);
    }

    static float tipAngle(float seconds, boolean second) {
        float side = second ? -1.0F : 1.0F;
        float delayed = opening(seconds - 0.10F);
        return side * (-5.0F * (1.0F - delayed)
            + (float) Math.sin((seconds - 0.10F) * 4.0F) * 2.5F * delayed);
    }
}
