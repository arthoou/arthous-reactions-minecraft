package com.arthou.arthousreactions.client;

import com.arthou.arthousreactions.ArthousReactions;
import com.arthou.arthousreactions.reaction.ReactionType;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

/** Runs inside the player renderer's pose stack, after the torso has been animated. */
final class AngelWingsLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final ResourceLocation TEXTURE = ArthousReactions.id("textures/effect/angel_wings.png");

    AngelWingsLayer(PlayerRenderer renderer) { super(renderer); }

    @Override
    public void render(PoseStack stack, MultiBufferSource buffers, int light, AbstractClientPlayer player,
                       float limbSwing, float limbAmount, float partialTick, float age, float headYaw, float headPitch) {
        ActiveReaction reaction = ReactionClientBootstrap.reactionFor(player);
        if (reaction == null || reaction.type() != ReactionType.EMOTE_ANGEL || player.isInvisible()) return;
        float seconds = reaction.ageSeconds(Util.getMillis());
        int alpha = Math.round(255.0F * AngelMotion.fade(seconds));
        if (alpha <= 0) return;
        stack.pushPose();
        try {
            getParentModel().body.translateAndRotate(stack);
            AngelWingModel.instance().render(stack, buffers.getBuffer(RenderType.entityTranslucent(TEXTURE)),
                light, alpha, seconds);
        } finally { stack.popPose(); }
    }
}
