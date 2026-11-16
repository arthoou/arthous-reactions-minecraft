package com.arthou.arthousreactions.client;

import com.arthou.arthousreactions.reaction.ClapTiming;
import com.arthou.arthousreactions.reaction.ReactionType;
import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.core.util.Vec3f;
import org.joml.Matrix3f;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PolishedGesturesTest {
    private static final ReactionType[] UPDATED = {ReactionType.EMOTE_CLAP, ReactionType.EMOTE_FEAR,
        ReactionType.EMOTE_WOAH, ReactionType.EMOTE_67, ReactionType.EMOTE_LAUGH};

    @Test void allUpdatedGesturesUseBendsAndFinishAtRestAtTheirDeclaredDurations() {
        for (ReactionType type : UPDATED) {
            var data = BentArmGestures.data(type);
            assertTrue(ReactionAnimations.handles(type));
            assertFalse(data.isInfinite());
            assertEquals(new ActiveReaction(type, 0, 0).durationMs(), data.endTick * 50L);
            var pose = sample(type, data.endTick * 50L);
            for (String bone : new String[]{"torso", "head", "rightArm", "leftArm"}) {
                var r = transform(pose, bone, TransformType.ROTATION);
                assertEquals(0, r.getX(), .0001, type + " / " + bone);
                assertEquals(0, r.getY(), .0001, type + " / " + bone);
                assertEquals(0, r.getZ(), .0001, type + " / " + bone);
                assertEquals(0, transform(pose, bone, TransformType.BEND).getY(), .0001);
            }
            assertTrue(data.getPart("rightArm").bend.getKeyFrames().stream().anyMatch(frame -> Math.abs(frame.value) > .4F));
        }
    }

    @Test void sevenClapContactsMatchTheUnmodifiedRecordingAfterPreparation() {
        assertEquals(4, ClapTiming.PREPARATION_TICKS);
        assertEquals(7, ClapTiming.count());
        assertEquals(.24F, ClapTiming.soundImpact(0), .0001);
        for (int i = 0; i < ClapTiming.count(); i++) {
            float contact = ClapTiming.poseImpact(i);
            assertTrue(Math.abs(contact - ClapTiming.soundImpact(i)) <= .0251F);
            var closed = sample(ReactionType.EMOTE_CLAP, Math.round(contact * 1000));
            float distance = wrist(closed, "rightArm").distance(wrist(closed, "leftArm"));
            assertTrue(distance > 3.2F && distance < 4.1F, "Palms must meet without crossing: " + distance);
            for (long offset = 10; offset <= 30; offset += 10) {
                var held = sample(ReactionType.EMOTE_CLAP, Math.round(contact * 1000) + offset);
                assertTrue(wrist(held, "rightArm").distance(wrist(held, "leftArm")) < 4.1F,
                    "Contact must remain visible between render frames");
            }
            var open = sample(ReactionType.EMOTE_CLAP, Math.round((contact + .14F) * 1000));
            assertTrue(wrist(open, "rightArm").distance(wrist(open, "leftArm")) > distance + 2.0F);
        }
        var rest = sample(ReactionType.EMOTE_CLAP, 0);
        assertEquals(0, transform(rest, "rightArm", TransformType.ROTATION).getX());
        assertFalse(new ActiveReaction(ReactionType.EMOTE_CLAP, 0, 0).isExpired(2300));
        assertTrue(new ActiveReaction(ReactionType.EMOTE_CLAP, 0, 0).isExpired(2301));
    }

    @Test void laughHasVisibleFaceAndTwoHandsAtDifferentHeightsOnTheAbdomen() {
        for (long ms = 250; ms <= 1100; ms += 50) {
            var pose = sample(ReactionType.EMOTE_LAUGH, ms);
            Vector3f lower = wrist(pose, "rightArm"), upper = wrist(pose, "leftArm");
            assertTrue(lower.y > 6 && lower.y < 10, lower.toString());
            assertTrue(upper.y > 3 && upper.y < 7, upper.toString());
            assertTrue(lower.y > upper.y + 1.0F);
            assertTrue(lower.z < -3.8F && upper.z < -4.8F, lower + " / " + upper);
            assertTrue(Math.abs(lower.x) < 3 && Math.abs(upper.x) < 3);
            assertTrue(transform(pose, "torso", TransformType.ROTATION).getX() > .1F);
        }
    }

    @Test void laughTorsoPositionRemainsContinuousBetweenFramesAndAcrossTickBoundaries() {
        var playback = ReactionAnimations.previewAnimation(ReactionType.EMOTE_LAUGH, 0);
        int tick = 0;
        Vec3f previous = Vec3f.ZERO;
        for (long ms = 0; ms <= 1450; ms++) {
            while (tick < ms / 50) { playback.tick(); tick++; }
            playback.setupAnim(ms % 50 / 50.0F);
            var actual = transform(playback, "torso", TransformType.POSITION);
            float distance = new Vector3f(actual.getX() - previous.getX(), actual.getY() - previous.getY(),
                actual.getZ() - previous.getZ()).length();
            assertTrue(distance < .03F, "Torso jumped by " + distance + " pixels at " + ms + " ms");
            previous = actual;
        }
    }

    @Test void laughTorsoUsesSmallSlowPulsesInsteadOfRapidJerks() {
        float previous = transform(sample(ReactionType.EMOTE_LAUGH, 250), "torso", TransformType.ROTATION).getX();
        float min = previous, max = previous;
        for (long ms = 260; ms <= 1100; ms += 10) {
            float pitch = transform(sample(ReactionType.EMOTE_LAUGH, ms), "torso", TransformType.ROTATION).getX();
            assertTrue(Math.abs(pitch - previous) <= .0018F, "Torso turns too fast at " + ms + " ms");
            min = Math.min(min, pitch); max = Math.max(max, pitch);
            previous = pitch;
        }
        assertTrue(max - min > .004F, "Retain a visible breathing impulse");
        assertTrue(max - min < .018F, "Keep torso pulses below one degree");
    }

    @Test void angelTorsoHasNoAuthoredTranslationRotationOrBendAtAnyRenderFraction() {
        for (long ms = 0; ms <= 2200; ms += 10) {
            var pose = sample(ReactionType.EMOTE_ANGEL, ms);
            for (TransformType type : new TransformType[]{TransformType.POSITION, TransformType.ROTATION, TransformType.BEND}) {
                var v = transform(pose, "torso", type);
                assertEquals(0, v.getX(), .000001);
                assertEquals(0, v.getY(), .000001);
                assertEquals(0, v.getZ(), .000001);
            }
        }
    }

    @Test void woahKeepsTorsoStillWhileItsArmsRespondAtSlightlyDifferentTimes() {
        var reaction = sample(ReactionType.EMOTE_WOAH, 250);
        var settled = sample(ReactionType.EMOTE_WOAH, 1050);
        assertEquals(0, transform(reaction, "torso", TransformType.ROTATION).getX());
        assertEquals(0, transform(settled, "torso", TransformType.ROTATION).getX());
        assertTrue(Math.abs(transform(settled, "rightArm", TransformType.ROTATION).getX())
            < Math.abs(transform(reaction, "rightArm", TransformType.ROTATION).getX()));
        var reaching = sample(ReactionType.EMOTE_WOAH, 100);
        assertNotEquals(transform(reaching, "rightArm", TransformType.ROTATION).getX(),
            transform(reaching, "leftArm", TransformType.ROTATION).getX(), .01);
    }

    @Test void sixSevenUsesElbowMotionInsteadOfWideShoulderSwings() {
        float minShoulder = 10, maxShoulder = -10, minBend = 10, maxBend = -10;
        boolean asymmetric = false;
        for (long ms = 300; ms <= 2000; ms += 50) {
            var pose = sample(ReactionType.EMOTE_67, ms);
            float shoulder = transform(pose, "rightArm", TransformType.ROTATION).getX();
            float bend = transform(pose, "rightArm", TransformType.BEND).getY();
            minShoulder = Math.min(minShoulder, shoulder); maxShoulder = Math.max(maxShoulder, shoulder);
            minBend = Math.min(minBend, bend); maxBend = Math.max(maxBend, bend);
            float other = transform(pose, "leftArm", TransformType.BEND).getY();
            if (Math.abs(bend + other + 2.02F) > .15F) asymmetric = true;
        }
        assertTrue(maxShoulder - minShoulder < .42F);
        assertTrue(maxBend - minBend > .65F);
        assertTrue(asymmetric);
    }

    @Test void fearKeepsHandsInFrontOfTheFaceAndAvoidsTheOldRapidHeadShaking() {
        float minYaw = 10, maxYaw = -10;
        for (long ms = 200; ms <= 950; ms += 10) {
            var pose = sample(ReactionType.EMOTE_FEAR, ms);
            for (String arm : new String[]{"rightArm", "leftArm"}) {
                Vector3f hand = wrist(pose, arm);
                assertTrue(hand.z < -5.4F, hand.toString());
            }
            float yaw = transform(pose, "head", TransformType.ROTATION).getY();
            minYaw = Math.min(minYaw, yaw); maxYaw = Math.max(maxYaw, yaw);
        }
        assertTrue(maxYaw - minYaw < .12F);
    }

    private static KeyframeAnimationPlayer sample(ReactionType type, long ms) {
        var player = new KeyframeAnimationPlayer(BentArmGestures.data(type));
        for (long tick = 0; tick < ms / 50; tick++) player.tick();
        player.setupAnim(ms % 50 / 50.0F);
        return player;
    }

    private static Vec3f transform(KeyframeAnimationPlayer pose, String bone, TransformType type) {
        return pose.get3DTransform(bone, type, 0, Vec3f.ZERO);
    }

    private static Vector3f wrist(KeyframeAnimationPlayer pose, String bone) {
        var rotation = transform(pose, bone, TransformType.ROTATION);
        var position = pose.get3DTransform(bone, TransformType.POSITION, 0,
            new Vec3f(bone.equals("rightArm") ? -5 : 5, 2, 0));
        var point = new Vector3f(0, 6, 0).rotateX(transform(pose, bone, TransformType.BEND).getY()).add(0, 4, 0);
        return new Matrix3f().rotationZYX(rotation.getZ(), rotation.getY(), rotation.getX())
            .transform(point).add(position.getX(), position.getY(), position.getZ());
    }
}
