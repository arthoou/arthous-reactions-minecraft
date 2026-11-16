package com.arthou.arthousreactions.client;

/** The Shift used to select the cat must be released before crouching can stop it. */
final class SneakReleaseLatch {
    private boolean waiting;

    void reset(boolean selectedWithShift) {
        waiting = selectedWithShift;
    }

    boolean isWaiting() {
        return waiting;
    }

    boolean shouldStop(boolean sneakDown, boolean shiftModifierDown) {
        if (waiting) {
            if (!sneakDown && !shiftModifierDown) waiting = false;
            return false;
        }
        return sneakDown || shiftModifierDown;
    }
}
