package com.arthou.arthousreactions.mixin;

import com.arthou.arthousreactions.client.ReactionClientBootstrap;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerModel.class)
public abstract class PlayerModelMixin {
    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V", at = @At("TAIL"))
    private void arthousreactions$applyReactionPose(AvatarRenderState state, CallbackInfo callbackInfo) {
        ReactionClientBootstrap.applyReactionPoseForEntity(state.id, (PlayerModel) (Object) this);
    }
}
