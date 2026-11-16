package com.arthou.arthousreactions.client;

import com.arthou.arthousreactions.network.ScubaOptions;
import com.arthou.arthousreactions.reaction.ReactionType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PreviewTimelineTest {
    @Test void enablingThePreviewAgainStartsFromTheBeginningWithoutReusingTheHiddenClock() {
        var timeline = new PreviewTimeline();
        timeline.update(ReactionType.SCUBA, true, 1000L);
        timeline.reset();
        assertNull(timeline.visibleReaction(4000L));
        assertTrue(timeline.update(ReactionType.SCUBA, false, 5000L));
        assertEquals(0, timeline.tick(5000L));
        assertEquals(0, timeline.fraction(5000L));
        assertFalse(ScubaOptions.catEnabled(timeline.visibleReaction(5000L).burstSeed()));
    }

    @Test void finiteGesturesRestBeforeRepeatingAndChangingSelectionStartsImmediately() {
        var timeline = new PreviewTimeline();
        assertTrue(timeline.update(ReactionType.EMOTE_SAD, false, 1000L));
        assertFalse(timeline.update(ReactionType.EMOTE_SAD, false, 3350L));
        assertNotNull(timeline.visibleReaction(3350L));
        assertNull(timeline.visibleReaction(3351L));
        assertFalse(timeline.update(ReactionType.EMOTE_SAD, false, 3650L));
        assertTrue(timeline.update(ReactionType.EMOTE_SAD, false, 3651L));
        assertEquals(3651L, timeline.visibleReaction(3651L).startedAtMs());
        assertTrue(timeline.update(ReactionType.EMOTE_LAUGH, false, 3700L));
        assertEquals(ReactionType.EMOTE_LAUGH, timeline.visibleReaction(3700L).type());
        assertEquals(0L, timeline.tick(3700L));
    }

    @Test void scubaCatCanChangeWithoutRestartingTheDanceAndIsAlwaysSilent() {
        var timeline = new PreviewTimeline();
        assertTrue(timeline.update(ReactionType.SCUBA, false, 1000L));
        assertFalse(ScubaOptions.catEnabled(timeline.visibleReaction(1000L).burstSeed()));
        assertFalse(timeline.update(ReactionType.SCUBA, true, 2500L));
        var cat = timeline.visibleReaction(2500L);
        assertTrue(ScubaOptions.catEnabled(cat.burstSeed()));
        assertFalse(ScubaOptions.musicEnabled(cat.burstSeed()));
        assertEquals(1000L, cat.startedAtMs());
        assertFalse(timeline.update(ReactionType.SCUBA, false, 3_601_000L));
        assertNotNull(timeline.visibleReaction(3_601_000L));
        assertFalse(ScubaOptions.catEnabled(timeline.visibleReaction(3_601_000L).burstSeed()));
        assertFalse(ScubaOptions.musicEnabled(timeline.visibleReaction(3_601_000L).burstSeed()));
        assertEquals(72_000L, timeline.tick(3_601_000L));
    }

    @Test void clockInterpolatesBetweenTicksIndependentlyOfRenderFrequency() {
        var timeline = new PreviewTimeline();
        timeline.update(ReactionType.SCUBA, false, 1000L);
        assertEquals(1L, timeline.tick(1085L));
        assertEquals(0.7F, timeline.fraction(1085L), 0.0001F);
        assertEquals(2L, timeline.tick(1100L));
        assertEquals(0.0F, timeline.fraction(1100L));
        assertEquals(0L, timeline.tick(999L));
        assertEquals(0.0F, timeline.fraction(999L));
    }
}
