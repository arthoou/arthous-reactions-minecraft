package com.arthou.arthousreactions.client;

import com.arthou.arthousreactions.reaction.ReactionType;
import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.core.util.Vec3f;
import org.joml.Matrix3f;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ExpressiveGesturesTest {
    @Test void approvedHeartsKeepsHandsTogetherInFrontAndRightFootRaisedBehind() {
        for (int tick = 10; tick <= 26; tick++) {
            var player = sample(ReactionType.EMOTE_HEARTS, tick);
            Vector3f right = endpoint(player, "rightArm", 4, 6);
            Vector3f left = endpoint(player, "leftArm", 4, 6);
            assertTrue(right.z < -6 && left.z < -6, right + " / " + left);
            assertTrue(right.distance(left) < 4.0F, right + " / " + left);
            assertTrue(right.y > 4 && right.y < 9);
            Vector3f foot = endpoint(player, "rightLeg", 6, 6);
            Vector3f support = endpoint(player, "leftLeg", 6, 6);
            assertTrue(foot.z > 6.0F, "Right foot stays behind the body: " + foot);
            assertTrue(foot.y < support.y - 5.0F, "Right foot is lifted: " + foot);
            assertTrue(foot.x < -2.0F, "Right knee opens slightly to the side: " + foot);
            assertEquals(24.0F, support.y, 0.001F);
            assertEquals(0.0F, support.z, 0.001F);
        }
    }

    @Test void angelPalmsNearlyMeetInFrontOfUpperChestWithBentElbows() {
        for (int tick = 12; tick <= 37; tick++) {
            var player = sample(ReactionType.EMOTE_ANGEL, tick);
            Vector3f right = endpoint(player, "rightArm", 4, 6);
            Vector3f left = endpoint(player, "leftArm", 4, 6);
            assertTrue(right.distance(left) > 3.5F && right.distance(left) < 4.8F, right + " / " + left);
            for (Vector3f hand : new Vector3f[]{right, left}) {
                assertTrue(hand.z < -4.6F && hand.z > -7.0F, "Hands clear the torso by more than half a pixel: " + hand);
                assertTrue(hand.y > 0 && hand.y < 3.3F, "Palms are in front of the upper chest: " + hand);
            }
            assertTrue(Math.abs(transform(player, "rightArm", TransformType.BEND).getY()) > 1.8F);
        }
    }

    @Test void bothGesturesKeepTheirDurationsAndFinishAtRestWithoutLooping() {
        for (ReactionType type : new ReactionType[]{ReactionType.EMOTE_HEARTS, ReactionType.EMOTE_ANGEL}) {
            var data = ExpressiveGestures.data(type);
            assertEquals(type == ReactionType.EMOTE_HEARTS ? 37 : 44, data.endTick);
            assertFalse(data.isInfinite());
            var player = sample(type, data.endTick);
            for (String bone : new String[]{"torso", "head", "rightArm", "leftArm", "rightLeg", "leftLeg"}) {
                var rotation = transform(player, bone, TransformType.ROTATION);
                assertEquals(0, rotation.getX(), 0.0001);
                assertEquals(0, rotation.getY(), 0.0001);
                assertEquals(0, rotation.getZ(), 0.0001);
                assertEquals(0, transform(player, bone, TransformType.BEND).getY(), 0.0001);
            }
            assertTrue(ReactionAnimations.handles(type));
        }
    }

    private static KeyframeAnimationPlayer sample(ReactionType type, int tick) {
        var player = new KeyframeAnimationPlayer(ExpressiveGestures.data(type));
        for (int i = 0; i < tick; i++) player.tick();
        player.setupAnim(0);
        return player;
    }

    private static Vec3f transform(KeyframeAnimationPlayer player, String part, TransformType type) {
        return player.get3DTransform(part, type, 0, Vec3f.ZERO);
    }

    private static Vector3f endpoint(KeyframeAnimationPlayer player, String bone, float joint, float lower) {
        var rotation = transform(player, bone, TransformType.ROTATION);
        var bend = transform(player, bone, TransformType.BEND);
        Vec3f fallback = new Vec3f(bone.startsWith("right") ? -5 : 5, bone.endsWith("Leg") ? 12 : 2, 0);
        var position = player.get3DTransform(bone, TransformType.POSITION, 0, fallback);
        Vector3f point = new Vector3f(0, lower, 0).rotateX(bend.getY()).add(0, joint, 0);
        return new Matrix3f().rotationZYX(rotation.getZ(), rotation.getY(), rotation.getX())
            .transform(point).add(position.getX(), position.getY(), position.getZ());
    }
}
