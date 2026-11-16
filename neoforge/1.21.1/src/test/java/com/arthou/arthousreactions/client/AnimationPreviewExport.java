package com.arthou.arthousreactions.client;

import com.arthou.arthousreactions.reaction.ReactionType;
import com.google.gson.Gson;
import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.core.util.Vec3f;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/** Exports the actual Java animation samples; no game instance or renderer is started. */
public final class AnimationPreviewExport {
    public static void main(String[] args) throws Exception {
        var all = new LinkedHashMap<String, Object>();
        for (ReactionType reaction : new ReactionType[]{ReactionType.EMOTE_ANGEL, ReactionType.EMOTE_HEARTS, ReactionType.EMOTE_CLAP, ReactionType.EMOTE_FEAR, ReactionType.EMOTE_WOAH, ReactionType.EMOTE_67, ReactionType.EMOTE_LAUGH}) {
            var data = BentArmGestures.data(reaction);
            var animation = ReactionAnimations.previewAnimation(reaction, 0L);
            var frames = new ArrayList<Object>();
            int tick = 0;
            for (int ms = 0; ms < data.endTick * 50 + 350; ms += 40) {
                while (tick < ms / 50) { animation.tick(); tick++; }
                float fraction = ms % 50 / 50.0F;
                animation.setupAnim(fraction);
                var parts = new LinkedHashMap<String, Object>();
                for (String bone : new String[]{"torso", "head", "rightArm", "leftArm", "rightLeg", "leftLeg"}) {
                    boolean leg = bone.endsWith("Leg");
                    Vec3f fallback = bone.endsWith("Arm") ? new Vec3f(bone.startsWith("right") ? -5 : 5, 2, 0)
                        : leg ? new Vec3f(bone.startsWith("right") ? -1.9F : 1.9F, 12, 0) : Vec3f.ZERO;
                    var position = animation.get3DTransform(bone, TransformType.POSITION, fraction, fallback);
                    var rotation = animation.get3DTransform(bone, TransformType.ROTATION, fraction, Vec3f.ZERO);
                    var bend = animation.get3DTransform(bone, TransformType.BEND, fraction, Vec3f.ZERO);
                    parts.put(bone, Map.of("position", vector(position), "rotation", vector(rotation), "bend", bend.getY()));
                }
                float seconds = ms / 1000.0F;
                frames.add(Map.of("ms", ms, "parts", parts, "wings", Map.of("alpha", AngelMotion.fade(seconds),
                    "wing_a", AngelMotion.wingAngle(seconds, false), "wing_b", AngelMotion.wingAngle(seconds, true),
                    "tip_a", AngelMotion.tipAngle(seconds, false), "tip_b", AngelMotion.tipAngle(seconds, true))));
            }
            all.put(reaction.name().substring("EMOTE_".length()).toLowerCase(java.util.Locale.ROOT), frames);
        }
        Path destination = Path.of(args[0]);
        Files.createDirectories(destination.getParent());
        Files.writeString(destination, "window.ANIMATION_FRAMES=" + new Gson().toJson(all) + ";\n");
        System.out.println("Exported actual animation frames to " + destination);
    }

    private static float[] vector(Vec3f value) { return new float[]{value.getX(), value.getY(), value.getZ()}; }
}
