package com.arthou.arthousreactions.client;

import com.arthou.arthousreactions.reaction.ReactionType;
import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.util.Vec3f;

/** Applies the torso rule before Player Animator or Bendy sees any pose channels. */
final class ReactionAnimationPlayer extends KeyframeAnimationPlayer {
    private static final Vec3f UNIT_SCALE = new Vec3f(1, 1, 1);
    private final ReactionType reaction;

    ReactionAnimationPlayer(KeyframeAnimation data, int tick, ReactionType reaction) {
        super(data, tick);
        this.reaction = reaction;
    }

    @Override
    public Vec3f get3DTransform(String part, TransformType type, float tickDelta, Vec3f value0) {
        if (!TorsoStability.allowsMotion(reaction)) {
            if (part.equals("torso")) return type == TransformType.SCALE ? UNIT_SCALE : Vec3f.ZERO;
            // Player Animator adds global body bending to torso bending. Keep
            // root dance movement, but prevent that additional torso deformation.
            if (part.equals("body") && type == TransformType.BEND) return Vec3f.ZERO;
        }
        return super.get3DTransform(part, type, tickDelta, value0);
    }
}
