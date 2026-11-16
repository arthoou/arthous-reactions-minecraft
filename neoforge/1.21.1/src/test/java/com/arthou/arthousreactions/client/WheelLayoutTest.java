package com.arthou.arthousreactions.client;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WheelLayoutTest {
    @Test void defaultCompactModeCentersOnlyTheWheelAtEveryWindowSizeAndOpeningFrame() {
        var motion = new WheelMotion(15, 12, 0, 1, false);
        for (int[] viewport : new int[][]{{320, 240}, {640, 360}, {1280, 720}, {240, 400}}) {
            for (long now : new long[]{0, 45, 120, 210, 1000}) {
                float preview = motion.previewVisibility(false, now);
                assertEquals(0, preview);
                var layout = WheelLayout.at(viewport[0], viewport[1], motion, now, preview);
                assertEquals(viewport[0] / 2.0F, layout.screenX(WheelLayout.WHEEL_X), .0001);
                assertEquals(viewport[1] / 2.0F, layout.screenY(WheelLayout.WHEEL_Y), .0001);
                assertEquals(136, layout.localX(layout.screenX(WheelLayout.WHEEL_X + 136)) - WheelLayout.WHEEL_X, .0001);
            }
        }
    }

    @Test void previewModeSwitchCanReverseSmoothlyAndRestoreTheSavedModeWithoutAJump() {
        var motion = new WheelMotion(15, 12, 0, 0, false);
        assertEquals(0, motion.previewVisibility(true, 1000));
        float opening = motion.previewVisibility(true, 1090);
        assertTrue(opening > 0 && opening < 1);
        assertEquals(opening, motion.previewVisibility(false, 1090));
        for (long now : new long[]{1090, 1130, 1200, 1270}) {
            float progress = motion.previewVisibility(false, now);
            var layout = WheelLayout.at(480, 270, motion, now, progress);
            assertEquals(WheelLayout.WHEEL_X, layout.localX(layout.screenX(WheelLayout.WHEEL_X)), .0001);
            assertTrue(layout.screenX(WheelLayout.WHEEL_X) <= 240.0001);
        }
        assertEquals(0, motion.previewVisibility(false, 1270));
        var restored = new WheelMotion(15, 12, 2000, 1, true);
        assertEquals(1, restored.previewVisibility(true, 2000));
        assertEquals(1, restored.page());
    }

    @Test void wheelAndPortraitStayInsideSmallAndWideViewportsThroughoutOpening() {
        var motion = new WheelMotion(15, 12, 0L);
        for (int[] viewport : new int[][]{{320, 240}, {640, 360}, {1280, 720}, {240, 400}}) {
            for (long now : new long[]{0L, 40L, 105L, 210L, 1000L}) {
                var layout = WheelLayout.at(viewport[0], viewport[1], motion, now);
                assertTrue(layout.scale() > 0.0F && layout.scale() <= 1.0F);
                assertTrue(layout.left() >= 11.99F && layout.top() >= 11.99F);
                assertTrue(layout.screenX(WheelLayout.WIDTH) <= viewport[0] - 11.99F);
                assertTrue(layout.screenY(WheelLayout.HEIGHT) <= viewport[1] - 11.99F);
                assertEquals(viewport[0] / 2.0F, layout.screenX(WheelLayout.WIDTH / 2.0F), 0.0001F);
                assertEquals(viewport[1] / 2.0F, layout.screenY(WheelLayout.HEIGHT / 2.0F), 0.0001F);
            }
        }
    }

    @Test void hitTestingAndPortraitScissorUseTheSameTransformDuringMotion() {
        var motion = new WheelMotion(15, 12, 0L);
        motion.navigate(1, 0L);
        for (long now : new long[]{0L, 45L, 120L, 210L}) {
            var layout = WheelLayout.at(480, 270, motion, now);
            float scale = motion.pageScale(motion.page(), now);
            float slotX = WheelLayout.WHEEL_X + 136.0F * scale;
            float slotY = WheelLayout.WHEEL_Y;
            assertEquals(136.0, (layout.localX(layout.screenX(slotX)) - WheelLayout.WHEEL_X) / scale, 0.0001);
            assertEquals(slotY, layout.localY(layout.screenY(slotY)), 0.0001);
            for (float x : new float[]{WheelLayout.PREVIEW_X, WheelLayout.PREVIEW_X + WheelLayout.PREVIEW_WIDTH}) {
                assertEquals(x, layout.localX(layout.screenX(x)), 0.0001);
            }
            for (float y : new float[]{WheelLayout.PREVIEW_Y, WheelLayout.PREVIEW_Y + WheelLayout.PREVIEW_HEIGHT}) {
                assertEquals(y, layout.localY(layout.screenY(y)), 0.0001);
            }
        }
    }
}
