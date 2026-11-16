package com.arthou.arthousreactions.client;

import com.arthou.arthousreactions.reaction.ReactionType;
import net.minecraft.client.model.PlayerModel;

/** Copy the final model pose; Bendy's copyFrom extension transfers the actual deformation. */
final class SkinLayerSync {
    private SkinLayerSync() { }

    static void synchronize(PlayerModel<?> model, ReactionType reaction) {
        if (reaction == null) return;
        OptionalSkinLayers.bindModel(model);
        model.hat.copyFrom(model.head);
        model.rightSleeve.copyFrom(model.rightArm);
        model.leftSleeve.copyFrom(model.leftArm);
        model.rightPants.copyFrom(model.rightLeg);
        model.leftPants.copyFrom(model.leftLeg);
        model.jacket.copyFrom(model.body);
    }
}
