package com.arthou.arthousreactions.client;

import com.arthou.arthousreactions.reaction.ReactionType;
import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.core.util.Vec3f;
import org.joml.Matrix3f;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BentArmGesturesTest {
    @Test void cryReachesOutThenCoversTheFaceWithoutPuttingHandsInsideTheHead() {
        var reaching = sample(ReactionType.EMOTE_SAD, 6);
        assertTrue(Math.abs(reaching.get3DTransform("rightArm", TransformType.ROTATION, 0.0F, Vec3f.ZERO).getX()) > 0.9F);
        assertEquals(0.0F, elbow(reaching, "rightArm"), 0.01F);
        assertEquals(0.0F, elbow(reaching, "leftArm"), 0.01F);
        for (int tick = 14; tick <= 40; tick++) {
            var crying = sample(ReactionType.EMOTE_SAD, tick);
            for (String arm : new String[]{"rightArm", "leftArm"}) {
                assertTrue(elbow(crying, arm) > 1.3F && elbow(crying, arm) < 1.99F);
                Vector3f hand = wrist(crying, arm);
                assertTrue(hand.z < -4.7F && hand.z > -8.0F, "Hand must stay in front of the face: " + hand);
                assertTrue(hand.y > -5.5F && hand.y < -0.7F, "Hand must cover the cheek: " + hand);
                assertTrue(Math.abs(hand.x) < 4.8F, "Hand must not float beside the head: " + hand);
            }
        }
    }

    @Test void bothGesturesAreFiniteAndReturnToRest() {
        for (ReactionType type : new ReactionType[]{ReactionType.EMOTE_SAD, ReactionType.EMOTE_LAUGH}) {
            var data = BentArmGestures.data(type);
            var resting = sample(type, data.endTick);
            assertFalse(resting.isInfinite());
            assertEquals(0.0F, elbow(resting, "rightArm"), 0.01F);
            assertEquals(0.0F, elbow(resting, "leftArm"), 0.01F);
        }
    }

    private static KeyframeAnimationPlayer sample(ReactionType reaction, int tick) {
        var player = new KeyframeAnimationPlayer(BentArmGestures.data(reaction));
        for (int index = 0; index < tick; index++) player.tick();
        player.setupAnim(0.0F);
        return player;
    }

    private static float elbow(KeyframeAnimationPlayer player, String arm) {
        return Math.abs(player.get3DTransform(arm, TransformType.BEND, 0.0F, Vec3f.ZERO).getY());
    }

    // Vanilla's arm spans -2..10 from its shoulder pivot; Bendy's joint is at 4.
    // This checks the center of the hand against the actual authored transforms.
    private static Vector3f wrist(KeyframeAnimationPlayer player, String arm) {
        Vec3f rotation = player.get3DTransform(arm, TransformType.ROTATION, 0.0F, Vec3f.ZERO);
        Vec3f bend = player.get3DTransform(arm, TransformType.BEND, 0.0F, Vec3f.ZERO);
        Vec3f position = player.get3DTransform(arm, TransformType.POSITION, 0.0F,
            new Vec3f(arm.equals("rightArm") ? -5.0F : 5.0F, 2.0F, 0.0F));
        Vector3f point = new Vector3f(0.0F, 6.0F, 0.0F).rotateX(bend.getY()).add(0.0F, 4.0F, 0.0F);
        return new Matrix3f().rotationZYX(rotation.getZ(), rotation.getY(), rotation.getX())
            .transform(point).add(position.getX(), position.getY(), position.getZ());
    }
}
