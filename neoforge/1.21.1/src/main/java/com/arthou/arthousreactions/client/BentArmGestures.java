package com.arthou.arthousreactions.client;

import com.arthou.arthousreactions.reaction.ReactionType;
import dev.kosmx.playerAnim.core.data.AnimationFormat;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.util.Ease;

/** Shoulder motion and elbow motion are authored separately, in Minecraft ticks. */
final class BentArmGestures {
    private static final Arm REST = new Arm(0.0F, 0.0F, 0.0F, 0.0F);
    private static final KeyframeAnimation CRY = cry();

    private BentArmGestures() {
    }

    static KeyframeAnimation data(ReactionType reaction) {
        return switch (reaction) {
            case EMOTE_SAD -> CRY;
            case EMOTE_LAUGH, EMOTE_CLAP, EMOTE_FEAR, EMOTE_WOAH, EMOTE_67 -> PolishedGestures.data(reaction);
            case EMOTE_HEARTS, EMOTE_ANGEL -> ExpressiveGestures.data(reaction);
            default -> throw new IllegalArgumentException("No bent-arm gesture for " + reaction);
        };
    }

    private static KeyframeAnimation cry() {
        var builder = builder("Cry", 47);
        pose(builder, 0, 0.0F, REST, REST);
        // A relaxed reach precedes the fold; the wrists settle in front of the
        // cheeks, with unequal hand heights instead of two vertical forearms.
        pose(builder, 6, 0.06F,
            new Arm(-1.18F, -0.12F, -0.03F, 0.0F), new Arm(-1.12F, 0.14F, 0.02F, 0.0F));
        pose(builder, 14, 0.20F,
            new Arm(-1.46F, -0.52F, -0.05F, -1.50F), new Arm(-1.36F, 0.50F, 0.05F, -1.60F));
        pose(builder, 21, 0.23F,
            new Arm(-1.44F, -0.53F, -0.06F, -1.52F), new Arm(-1.35F, 0.49F, 0.05F, -1.59F));
        pose(builder, 28, 0.18F,
            new Arm(-1.47F, -0.51F, -0.04F, -1.48F), new Arm(-1.38F, 0.51F, 0.04F, -1.62F));
        pose(builder, 35, 0.22F,
            new Arm(-1.45F, -0.52F, -0.05F, -1.51F), new Arm(-1.36F, 0.50F, 0.05F, -1.60F));
        pose(builder, 40, 0.19F,
            new Arm(-1.44F, -0.51F, -0.05F, -1.49F), new Arm(-1.35F, 0.50F, 0.05F, -1.59F));
        pose(builder, 47, 0.0F, REST, REST);
        return builder.build();
    }

    private static KeyframeAnimation.AnimationBuilder builder(String name, int endTick) {
        var builder = new KeyframeAnimation.AnimationBuilder(AnimationFormat.BINARY);
        builder.name = name;
        builder.beginTick = 0;
        builder.endTick = endTick;
        builder.stopTick = endTick + 3;
        builder.isLooped = false;
        return builder;
    }

    private static void pose(KeyframeAnimation.AnimationBuilder builder, int tick, float headPitch, Arm right, Arm left) {
        key(builder.head.pitch, tick, headPitch);
        key(builder.torso.pitch, tick, 0.0F);
        arm(builder.rightArm, tick, right);
        arm(builder.leftArm, tick, left);
    }

    private static void arm(KeyframeAnimation.StateCollection part, int tick, Arm pose) {
        // Keep the hand surface in front of the face instead of inside its mesh.
        key(part.z, tick, -Math.max(0.0F, -pose.pitch()) * 0.7F);
        key(part.pitch, tick, pose.pitch());
        key(part.yaw, tick, pose.yaw());
        key(part.roll, tick, pose.roll());
        key(part.bendDirection, tick, 0.0F);
        key(part.bend, tick, pose.bend());
    }

    private static void key(KeyframeAnimation.StateCollection.State channel, int tick, float value) {
        channel.setEnabled(true);
        channel.addKeyFrame(tick, value, Ease.INOUTSINE);
    }

    private record Arm(float pitch, float yaw, float roll, float bend) {
    }
}
