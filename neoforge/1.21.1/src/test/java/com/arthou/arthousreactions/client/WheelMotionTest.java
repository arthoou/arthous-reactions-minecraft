package com.arthou.arthousreactions.client;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WheelMotionTest {
    @Test void openingHasAFixedCenterAndFinishesInTwoHundredTenMilliseconds() {
        var motion = new WheelMotion(15, 12, 1000L);
        assertEquals(0.0F, motion.opacity(1000L));
        assertEquals(0.78F, motion.rootScale(1200, 800, 1000L), 0.0001F);
        assertTrue(motion.opacity(1105L) > 0.5F, "Ease-out must respond promptly");
        assertEquals(1.0F, motion.opacity(1210L));
        assertEquals(1.0F, motion.rootScale(1200, 800, 1210L));
        assertEquals(600.0, WheelMotion.inverse(600.0, 600.0, motion.rootScale(1200, 800, 1075L)));
    }

    @Test void hoverCanReverseWithoutJumpingOrOvershooting() {
        var motion = new WheelMotion(15, 12, 0L);
        assertEquals(0.0F, motion.hover(0, true, 0L));
        float growing = motion.hover(0, true, 75L);
        assertTrue(growing > 0.0F && growing < 1.0F);
        assertEquals(growing, motion.hover(0, false, 75L));
        float shrinking = motion.hover(0, false, 150L);
        assertTrue(shrinking < growing && shrinking > 0.0F);
        assertEquals(shrinking, motion.hover(0, true, 150L));
        assertEquals(1.0F, motion.hover(0, true, 300L));
        motion.hover(0, false, 450L);
        assertEquals(0.0F, motion.hover(0, false, 450L + WheelMotion.HOVER_MS));
    }

    @Test void rapidNavigationRetainsVisibleContentAndContinuousOpacity() {
        var motion = new WheelMotion(36, 12, 0L);
        for (long now = 0L; now < 2000L; now += 17L) {
            float[] before = new float[motion.pageCount()];
            for (int p = 0; p < before.length; p++) before[p] = motion.pageOpacity(p, now);
            motion.navigate(now % 2 == 0 ? 1 : -1, now);
            float sum = 0.0F;
            for (int p = 0; p < before.length; p++) {
                float value = motion.pageOpacity(p, now);
                assertEquals(before[p], value, 0.0001F);
                assertTrue(value >= -0.0001F && value <= 1.0001F);
                sum += value;
            }
            assertEquals(1.0F, sum, 0.0001F, "Cross-fades must never leave a blank wheel");
        }
    }

    @Test void mouseCoordinatesStayAlignedDuringOpeningAndPageTransitions() {
        for (int[] viewport : new int[][]{{320, 240}, {640, 360}, {1280, 720}}) {
            var motion = new WheelMotion(15, 12, 0L);
            motion.navigate(1, 0L);
            for (long now : new long[]{0L, 40L, 105L, 210L, 1000L}) {
                float root = motion.rootScale(viewport[0], viewport[1], now);
                float content = root * motion.pageScale(motion.page(), now);
                double center = viewport[0] / 2;
                double physical = center + 136.0 * content;
                assertEquals(center + 136.0, WheelMotion.inverse(physical, center, content), 0.00001);
                if (now >= WheelMotion.OPEN_MS) assertTrue(root * 352.0F <= Math.min(viewport[0], viewport[1]) - 23.99F);
            }
        }
    }

    @Test void renderingFrequencyDoesNotChangeMotionDuration() {
        var fast = new WheelMotion(15, 12, 0L);
        var slow = new WheelMotion(15, 12, 0L);
        fast.hover(0, true, 0L);
        slow.hover(0, true, 0L);
        for (long now = 1L; now < 100L; now++) fast.hover(0, true, now);
        assertEquals(fast.hover(0, true, 100L), slow.hover(0, true, 100L));
        assertEquals(fast.opacity(100L), slow.opacity(100L));
    }

    @Test void onePageAndBackNavigationRemainUsableImmediately() {
        var single = new WheelMotion(3, 12, 0L);
        single.navigate(-1, 0L);
        assertEquals(0, single.page());
        assertEquals(1.0F, single.pageOpacity(0, 0L));
        var two = new WheelMotion(15, 12, 0L);
        two.navigate(1, 0L);
        assertEquals(1, two.page());
        two.navigate(-1, 20L);
        assertEquals(0, two.page());
        assertEquals(1.0F, two.pageOpacity(0, 200L));
    }
}
