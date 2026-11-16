package com.arthou.arthousreactions.client;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SneakReleaseLatchTest {
    @Test void selectingWithShiftWaitsForReleaseBeforeTheNextCrouchStops() {
        var latch = new SneakReleaseLatch();
        latch.reset(true);
        assertTrue(latch.isWaiting(), "Suppress vanilla input and the crouch pose during the initial hold");
        assertFalse(latch.shouldStop(false, true), "GUI Shift can precede the key mapping state");
        assertFalse(latch.shouldStop(true, true));
        assertFalse(latch.shouldStop(true, false));
        assertTrue(latch.isWaiting());
        assertFalse(latch.shouldStop(false, false));
        assertFalse(latch.isWaiting(), "Release restores normal input without cancelling the dance");
        for (int tick = 0; tick < 100; tick++) assertFalse(latch.shouldStop(false, false));
        assertTrue(latch.shouldStop(false, true), "A new physical Shift press cancels immediately");
    }

    @Test void selectingWithoutShiftDoesNotSuppressCrouching() {
        var latch = new SneakReleaseLatch();
        latch.reset(false);
        assertFalse(latch.isWaiting());
        assertFalse(latch.shouldStop(false, false));
        assertTrue(latch.shouldStop(true, false), "The player's bound sneak key still stops Scuba");
        latch.reset(true);
        latch.reset(false);
        assertFalse(latch.isWaiting(), "Changing emotes clears the initial hold");
    }
}
