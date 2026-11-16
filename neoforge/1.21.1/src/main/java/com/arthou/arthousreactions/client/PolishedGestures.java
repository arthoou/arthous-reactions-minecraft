package com.arthou.arthousreactions.client;

import com.arthou.arthousreactions.reaction.ClapTiming;
import com.arthou.arthousreactions.reaction.ReactionType;
import dev.kosmx.playerAnim.core.data.AnimationFormat;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import static com.arthou.arthousreactions.client.ExpressiveGestures.*;

/** Articulated, finite gestures. Upper-body motion shares one coherent shoulder/torso frame. */
final class PolishedGestures {
    private static final KeyframeAnimation CLAP = create(ReactionType.EMOTE_CLAP);
    private static final KeyframeAnimation FEAR = create(ReactionType.EMOTE_FEAR);
    private static final KeyframeAnimation WOAH = create(ReactionType.EMOTE_WOAH);
    private static final KeyframeAnimation SIX_SEVEN = create(ReactionType.EMOTE_67);
    private static final KeyframeAnimation LAUGH = create(ReactionType.EMOTE_LAUGH);

    private PolishedGestures() { }

    static KeyframeAnimation data(ReactionType type) {
        return switch (type) {
            case EMOTE_CLAP -> CLAP;
            case EMOTE_FEAR -> FEAR;
            case EMOTE_WOAH -> WOAH;
            case EMOTE_67 -> SIX_SEVEN;
            case EMOTE_LAUGH -> LAUGH;
            default -> throw new IllegalArgumentException("No polished gesture for " + type);
        };
    }

    private static KeyframeAnimation create(ReactionType type) {
        var b = new KeyframeAnimation.AnimationBuilder(AnimationFormat.BINARY);
        b.name = type.name();
        b.beginTick = 0;
        b.endTick = (int) (new ActiveReaction(type, 0, 0).durationMs() / 50L);
        b.stopTick = b.endTick + 3;
        b.isLooped = false;
        for (int tick = 0; tick <= b.endTick; tick++) {
            float seconds = tick / 20.0F;
            switch (type) {
                case EMOTE_CLAP -> clap(b, tick, seconds);
                case EMOTE_FEAR -> fear(b, tick, seconds);
                case EMOTE_WOAH -> woah(b, tick, seconds);
                case EMOTE_67 -> sixSeven(b, tick, seconds);
                case EMOTE_LAUGH -> laugh(b, tick, seconds);
                default -> throw new IllegalArgumentException();
            }
        }
        return b.build();
    }

    private static void clap(KeyframeAnimation.AnimationBuilder b, int tick, float t) {
        float amount = envelope(t, .20F, 2.04F, 2.30F);
        float contact = 0;
        for (int i = 0; i < ClapTiming.count(); i++) {
            float peak = ClapTiming.poseImpact(i);
            float close = t <= peak ? smooth((t - peak + .09F) / .09F)
                : 1 - smooth((t - peak - .035F) / .13F);
            contact = Math.max(contact, close);
        }
        float yaw = (float) Math.toDegrees(.10F + .28F * contact) * amount;
        float pitch = (float) Math.toDegrees(-.57F - .08F * contact) * amount;
        float bend = (float) Math.toDegrees(-.92F + .12F * contact) * amount;
        float response = bump(t, .52F, .14F, .20F) * .5F + bump(t, 1.43F, .13F, .20F) * .4F;
        upper(b, tick, rotation(0, 0, 0),
            rotation(4 * amount + .8F * response, 0, 0),
            new Arm(pitch, -yaw, 0, bend), new Arm(pitch, yaw, 0, bend), 0, 0);
    }

    private static void fear(KeyframeAnimation.AnimationBuilder b, int tick, float t) {
        float a = envelope(t, .18F, .98F, 1.25F);
        float startle = bump(t, .20F, .07F, .14F) + .55F * bump(t, .56F, .09F, .15F)
            + .30F * bump(t, .89F, .07F, .14F);
        float glance = (-3 + 5 * smooth((t - .45F) / .35F)) * a;
        upper(b, tick, rotation(0, 0, 0),
            rotation(9 * a + 1.5F * startle, glance, -2 * a),
            new Arm((-52 - 1.5F * startle) * a, 5 * a, 4 * a, -78 * a),
            new Arm((-46 - startle) * a, -6 * a, -3 * a, -86 * a), .42F * startle, .30F * startle);
    }

    private static void woah(KeyframeAnimation.AnimationBuilder b, int tick, float t) {
        float a = envelope(Math.max(0, t - .02F), .22F, .77F, 1.13F);
        float left = envelope(Math.max(0, t - .055F), .22F, .735F, 1.095F);
        float head = envelope(Math.max(0, t - .05F), .23F, .75F, 1.10F);
        float settle = smooth((t - .40F) / .35F);
        upper(b, tick, rotation(0, 0, 0),
            rotation((-13 + 4 * settle) * head, 1.5F * head, 0),
            new Arm(-48 * a, 5 * a, 13 * a, -28 * a),
            new Arm(-44 * left, -6 * left, -10 * left, -34 * left), .20F * a, .15F * left);
    }

    private static void sixSeven(KeyframeAnimation.AnimationBuilder b, int tick, float t) {
        float a = envelope(t, .22F, 2.10F, 2.40F);
        float right = (float) Math.sin(t * 7.0F);
        float left = -(float) Math.sin((t - .13F) * 7.0F);
        float sway = (float) Math.sin(t * 3.5F);
        upper(b, tick, rotation(0, 0, 0),
            rotation((6 + 2.5F * (float) Math.sin((t - .05F) * 7)) * a, 3 * sway * a, -sway * a),
            new Arm((-26 + 10 * right) * a, -8 * a, 5 * a, (-58 - 24 * right) * a),
            new Arm((-26 + 9 * left) * a, 8 * a, -5 * a, (-58 - 22 * left) * a), 0, 0);
    }

    private static void laugh(KeyframeAnimation.AnimationBuilder b, int tick, float t) {
        float a = envelope(t, .18F, 1.16F, 1.45F);
        float beat = bump(t, .45F, .20F, .24F) + .75F * bump(t, .95F, .20F, .24F);
        float nod = bump(t, .51F, .22F, .24F) + .70F * bump(t, 1.01F, .22F, .24F);
        upper(b, tick, rotation((7 + .65F * beat) * a, -2 * a, -2.5F * a),
            rotation((-16 + 1.2F * nod) * a, 2 * a, 2 * a),
            new Arm((15 + beat) * a, -40 * a, -4 * a, (-95 - beat) * a),
            new Arm((5 + .6F * beat) * a, 37 * a, 3 * a, (-105 - .6F * beat) * a),
            .12F * beat * a, .09F * beat * a);
    }

    private static void upper(KeyframeAnimation.AnimationBuilder b, int tick, Quaternionf upper,
                              Quaternionf head, Arm right, Arm left, float rightLift, float leftLift) {
        // One position per tick: duplicate zero/pivot keys caused a sawtooth
        // toward zero followed by a jump back at every 50 ms boundary.
        Vector3f pivot = new Vector3f(0, -12, 0).rotate(upper).add(0, 12, 0);
        part(b.torso, tick, pivot, upper, 0);
        upperPart(b.head, tick, 0, 0, upper, head, 0);
        upperPart(b.rightArm, tick, -5, 2 - rightLift, upper, rotation(right.pitch, right.yaw, right.roll), right.bend);
        upperPart(b.leftArm, tick, 5, 2 - leftLift, upper, rotation(left.pitch, left.yaw, left.roll), left.bend);
    }

    private static float envelope(float t, float enter, float exit, float end) {
        return smooth(t / enter) * (1 - smooth((t - exit) / (end - exit)));
    }

    private static float bump(float t, float peak, float attack, float release) {
        return t <= peak ? smooth((t - peak + attack) / attack) : 1 - smooth((t - peak) / release);
    }

    private static float smooth(float value) { return AngelMotion.smooth(value); }
    private record Arm(float pitch, float yaw, float roll, float bend) { }
}
