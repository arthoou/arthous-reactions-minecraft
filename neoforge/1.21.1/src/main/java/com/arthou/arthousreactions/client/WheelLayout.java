package com.arthou.arthousreactions.client;

/** Logical coordinates shared by drawing, scissoring and hit testing. */
record WheelLayout(float scale, float left, float top) {
    static final int WIDTH = 640;
    static final int HEIGHT = 384;
    static final int WHEEL_X = 188;
    static final int WHEEL_Y = 192;
    static final int COMPACT_WIDTH = WHEEL_X * 2;
    static final int PREVIEW_X = 390;
    static final int PREVIEW_Y = 8;
    static final int PREVIEW_WIDTH = 238;
    static final int PREVIEW_HEIGHT = 278;

    static WheelLayout at(int width, int height, WheelMotion motion, long now) {
        return at(width, height, motion, now, 1.0F);
    }

    static WheelLayout at(int width, int height, WheelMotion motion, long now, float previewVisibility) {
        float contentWidth = COMPACT_WIDTH + (WIDTH - COMPACT_WIDTH) * Math.clamp(previewVisibility, 0.0F, 1.0F);
        float fit = Math.min(1.0F, Math.min(Math.max(1, width - 24) / contentWidth,
            Math.max(1, height - 24) / (float) HEIGHT));
        float scale = fit * motion.openingScale(now);
        return new WheelLayout(scale, (width - contentWidth * scale) / 2.0F, (height - HEIGHT * scale) / 2.0F);
    }

    double localX(double screenX) { return (screenX - left) / scale; }
    double localY(double screenY) { return (screenY - top) / scale; }
    float screenX(float x) { return left + x * scale; }
    float screenY(float y) { return top + y * scale; }
}
