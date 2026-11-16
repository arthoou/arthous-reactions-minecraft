package com.arthou.arthousreactions.client;

import com.arthou.arthousreactions.reaction.ReactionType;
import dev.kosmx.playerAnim.core.data.AnimationFormat;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.util.Ease;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** Approved Hearts pose and the original Angel sequence, with articulated limbs. */
final class ExpressiveGestures {
    private static final KeyframeAnimation HEARTS = create(false);
    private static final KeyframeAnimation ANGEL = create(true);

    private ExpressiveGestures() { }

    static KeyframeAnimation data(ReactionType reaction) {
        return switch (reaction) {
            case EMOTE_HEARTS -> HEARTS;
            case EMOTE_ANGEL -> ANGEL;
            default -> throw new IllegalArgumentException("No expressive gesture for " + reaction);
        };
    }

    private static KeyframeAnimation create(boolean angel) {
        var builder = new KeyframeAnimation.AnimationBuilder(AnimationFormat.BINARY);
        builder.name = angel ? "Angel" : "Hearts";
        builder.beginTick = 0;
        builder.endTick = angel ? 44 : 37;
        builder.stopTick = builder.endTick + 3;
        builder.isLooped = false;
        for (int tick = 0; tick <= builder.endTick; tick++) {
            float seconds = tick / 20.0F;
            if (angel) angel(builder, tick, seconds);
            else hearts(builder, tick, seconds);
        }
        return builder.build();
    }

    private static void hearts(KeyframeAnimation.AnimationBuilder b, int tick, float seconds) {
        float amount = seconds < 0.5F ? AngelMotion.smooth(seconds / 0.5F)
            : 1.0F - AngelMotion.smooth((seconds - 1.30F) / 0.55F);
        float breath = (float) Math.sin(seconds * Math.PI * 2.0) * 0.6F * amount;
        Quaternionf upper = rotation(0, 0, 0);
        part(b.torso, tick, new Vector3f(0, 0, 0), upper, 0.0F);
        upperPart(b.head, tick, 0, 0, upper, rotation(7 * amount, -4 * amount, -7 * amount), 0);
        upperPart(b.rightArm, tick, -5, 2, upper, rotation(-30 * amount + breath, -27 * amount, 0), -55 * amount - breath);
        upperPart(b.leftArm, tick, 5, 2, upper, rotation(-30 * amount + breath, 27 * amount, 0), -55 * amount - breath);
        // The support leg stays planted while the right knee folds behind the player.
        part(b.rightLeg, tick, new Vector3f(-1.9F, 12, 0), rotation(18 * amount, 0, 10 * amount), 82 * amount);
        part(b.leftLeg, tick, new Vector3f(1.9F, 12, 0), rotation(0, 0, 0), 0);
        torsoPosition(b.torso, tick, upper);
    }

    private static void angel(KeyframeAnimation.AnimationBuilder b, int tick, float seconds) {
        float enter = AngelMotion.smooth(seconds / 0.20F);
        float exit = 1.0F - AngelMotion.smooth((seconds - 1.92F) / 0.28F);
        float amount = enter * exit;
        float fold = AngelMotion.smooth((seconds - 0.14F) / 0.42F) * exit;
        float floaty = (float) Math.sin(seconds * 5.0F) * amount;
        // Keep the approved arms/head and wing motion, with a steady torso.
        Quaternionf upper = rotation(0, 0, 0);
        part(b.torso, tick, new Vector3f(0, 0, 0), upper, 0);
        torsoPosition(b.torso, tick, upper);
        // Retain the lifted, gently floating head and the original 2.2 second sequence.
        upperPart(b.head, tick, 0, 0, upper, rotation((-0.08F * amount + floaty * 0.05F) * 180.0F / (float) Math.PI, 0, 0), 0);
        float shoulder = -0.36F * amount * 180.0F / (float) Math.PI;
        float yaw = (0.14F * amount + 0.39F * fold) * 180.0F / (float) Math.PI;
        upperPart(b.rightArm, tick, -5, 2, upper, rotation(shoulder, -yaw, 0), -1.98F * fold * 180.0F / (float) Math.PI);
        upperPart(b.leftArm, tick, 5, 2, upper, rotation(shoulder, yaw, 0), -1.98F * fold * 180.0F / (float) Math.PI);
    }

    static Quaternionf rotation(float pitch, float yaw, float roll) {
        float r = (float) Math.PI / 180.0F;
        return new Quaternionf().rotationZYX(roll * r, yaw * r, pitch * r);
    }

    static void torsoPosition(KeyframeAnimation.StateCollection torso, int tick, Quaternionf upper) {
        Vector3f position = new Vector3f(0, -12, 0).rotate(upper).add(0, 12, 0);
        position(torso, tick, position);
    }

    static void upperPart(KeyframeAnimation.StateCollection part, int tick, float x, float y,
                                  Quaternionf upper, Quaternionf local, float bendDegrees) {
        Vector3f position = new Vector3f(x, y - 12.0F, 0).rotate(upper).add(0, 12, 0);
        part(part, tick, position, new Quaternionf(upper).mul(local), bendDegrees);
    }

    static void part(KeyframeAnimation.StateCollection part, int tick, Vector3f position,
                             Quaternionf rotation, float bendDegrees) {
        position(part, tick, position);
        Vector3f angles = rotation.getEulerAnglesZYX(new Vector3f());
        key(part.pitch, tick, angles.x);
        key(part.yaw, tick, angles.y);
        key(part.roll, tick, angles.z);
        if (part.bend != null) {
            key(part.bendDirection, tick, 0.0F);
            key(part.bend, tick, bendDegrees * (float) Math.PI / 180.0F);
        }
    }

    private static void position(KeyframeAnimation.StateCollection part, int tick, Vector3f position) {
        key(part.x, tick, position.x);
        key(part.y, tick, position.y);
        key(part.z, tick, position.z);
    }

    private static void key(KeyframeAnimation.StateCollection.State channel, int tick, float value) {
        channel.setEnabled(true);
        // The dense samples already describe a quintic ease, including zero velocity at the ends.
        channel.addKeyFrame(tick, value, Ease.LINEAR);
    }
}
