package com.arthou.arthousreactions.client;

/** Time-based GUI motion. Retargeting preserves the current value, including mid-flight reversals. */
final class WheelMotion {
    static final long OPEN_MS = 210L;
    static final long HOVER_MS = 150L;
    static final long PAGE_MS = 180L;
    private final long openedAt;
    private final Value[] pages;
    private final Value[] items;
    private final Value preview;
    private final Value[] arrows = {new Value(0.0F), new Value(0.0F)};
    private int page;

    WheelMotion(int itemCount, int pageSize, long now) {
        this(itemCount, pageSize, now, 0);
    }

    WheelMotion(int itemCount, int pageSize, long now, int initialPage) {
        this(itemCount, pageSize, now, initialPage, false);
    }

    WheelMotion(int itemCount, int pageSize, long now, int initialPage, boolean previewEnabled) {
        openedAt = now;
        preview = new Value(previewEnabled ? 1.0F : 0.0F);
        pages = new Value[Math.max(1, (itemCount + pageSize - 1) / pageSize)];
        page = Math.clamp(initialPage, 0, pages.length - 1);
        for (int index = 0; index < pages.length; index++) pages[index] = new Value(index == page ? 1.0F : 0.0F);
        items = new Value[itemCount];
        for (int index = 0; index < items.length; index++) items[index] = new Value(0.0F);
    }

    float opacity(long now) {
        return easeOut((now - openedAt) / (float) OPEN_MS);
    }

    float rootScale(int width, int height, long now) {
        float fit = Math.min(1.0F, Math.max(1.0F, Math.min(width, height) - 24.0F) / 352.0F);
        return fit * openingScale(now);
    }

    float openingScale(long now) { return 0.78F + 0.22F * opacity(now); }

    float previewVisibility(boolean enabled, long now) {
        preview.target(enabled ? 1.0F : 0.0F, now, PAGE_MS);
        return preview.value(now);
    }

    static double inverse(double position, double center, float scale) {
        return center + (position - center) / scale;
    }

    int page() {
        return page;
    }

    int pageCount() {
        return pages.length;
    }

    void navigate(int delta, long now) {
        int next = Math.floorMod(page + delta, pages.length);
        if (next == page) return;
        page = next;
        // Equal durations preserve a total opacity of one even when navigation reverses.
        for (int index = 0; index < pages.length; index++) pages[index].target(index == page ? 1.0F : 0.0F, now, PAGE_MS);
    }

    float pageOpacity(int index, long now) {
        return pages[index].value(now);
    }

    float pageScale(int index, long now) {
        return 0.985F + 0.015F * pageOpacity(index, now);
    }

    float hover(int index, boolean hovered, long now) {
        items[index].target(hovered ? 1.0F : 0.0F, now, HOVER_MS);
        return items[index].value(now);
    }

    float arrow(int index, boolean hovered, long now) {
        arrows[index].target(hovered ? 1.0F : 0.0F, now, HOVER_MS);
        return arrows[index].value(now);
    }

    static float easeOut(float progress) {
        float t = Math.clamp(progress, 0.0F, 1.0F);
        float remaining = 1.0F - t;
        return 1.0F - remaining * remaining * remaining;
    }

    static int mix(int first, int second, float progress) {
        int result = 0;
        for (int shift = 0; shift <= 24; shift += 8) {
            int a = (first >>> shift) & 255;
            int b = (second >>> shift) & 255;
            result |= Math.round(a + (b - a) * progress) << shift;
        }
        return result;
    }

    private static final class Value {
        private float from;
        private float to;
        private long startedAt;
        private long duration = 1L;

        private Value(float value) {
            from = to = value;
        }

        private float value(long now) {
            return from + (to - from) * easeOut((now - startedAt) / (float) duration);
        }

        private void target(float target, long now, long durationMs) {
            if (to == target) return;
            from = value(now);
            to = target;
            startedAt = now;
            duration = durationMs;
        }
    }
}
