package com.arthou.arthousreactions.client;

import com.arthou.arthousreactions.reaction.ReactionType;
import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.core.util.Vec3f;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ReactionAnimationPlayerTest {
    private static final Vec3f DIRTY_FALLBACK = new Vec3f(.3F, -.4F, .2F);

    @Test void actualPlaybackHasAStableTorsoForEveryNonLaughAnimatedEmoteAndRenderFraction() {
        for (ReactionType reaction : ReactionType.values()) {
            if (!ReactionAnimations.handles(reaction) || reaction == ReactionType.EMOTE_LAUGH) continue;
            var player = ReactionAnimations.previewAnimation(reaction, 0);
            long duration = reaction == ReactionType.SCUBA ? 5000 : new ActiveReaction(reaction, 0, 0).durationMs();
            int tick = 0;
            for (long ms = 0; ms <= duration; ms += 10) {
                while (tick < ms / 50) { player.tick(); tick++; }
                player.setupAnim(ms % 50 / 50.0F);
                for (TransformType type : new TransformType[]{TransformType.POSITION, TransformType.ROTATION, TransformType.BEND}) {
                    assertVector(Vec3f.ZERO, player.get3DTransform("torso", type, 0, DIRTY_FALLBACK));
                }
                assertVector(new Vec3f(1, 1, 1), player.get3DTransform("torso", TransformType.SCALE, 0, DIRTY_FALLBACK));
                assertVector(Vec3f.ZERO, player.get3DTransform("body", TransformType.BEND, 0, DIRTY_FALLBACK));
            }
        }
    }

    @Test void laughKeepsTheFullAuthoredTorsoMotion() {
        var original = new KeyframeAnimationPlayer(BentArmGestures.data(ReactionType.EMOTE_LAUGH));
        var playback = ReactionAnimations.previewAnimation(ReactionType.EMOTE_LAUGH, 0);
        boolean moved = false;
        for (int tick = 0; tick <= 29; tick++) {
            original.setupAnim(.35F); playback.setupAnim(.35F);
            for (TransformType type : new TransformType[]{TransformType.POSITION, TransformType.ROTATION, TransformType.BEND, TransformType.SCALE}) {
                var expected = original.get3DTransform("torso", type, 0, DIRTY_FALLBACK);
                assertVector(expected, playback.get3DTransform("torso", type, 0, DIRTY_FALLBACK));
                if (type == TransformType.ROTATION && Math.abs(expected.getX()) > .1F) moved = true;
            }
            original.tick(); playback.tick();
        }
        assertTrue(moved);
    }

    @Test void scubaRootChoreographyAndAllLimbTracksStayIntact() {
        var original = new KeyframeAnimationPlayer(ReactionAnimations.data());
        var playback = ReactionAnimations.previewAnimation(ReactionType.SCUBA, 0);
        for (int tick = 0; tick < 180; tick++) {
            original.setupAnim(.6F); playback.setupAnim(.6F);
            for (String part : new String[]{"body", "head", "rightArm", "leftArm", "rightLeg", "leftLeg"}) {
                for (TransformType type : new TransformType[]{TransformType.POSITION, TransformType.ROTATION, TransformType.SCALE, TransformType.BEND}) {
                    if (part.equals("body") && type == TransformType.BEND) continue;
                    assertVector(original.get3DTransform(part, type, 0, DIRTY_FALLBACK),
                        playback.get3DTransform(part, type, 0, DIRTY_FALLBACK));
                }
            }
            original.tick(); playback.tick();
        }
    }

    private static void assertVector(Vec3f expected, Vec3f actual) {
        assertEquals(expected.getX(), actual.getX(), .000001);
        assertEquals(expected.getY(), actual.getY(), .000001);
        assertEquals(expected.getZ(), actual.getZ(), .000001);
    }
}
