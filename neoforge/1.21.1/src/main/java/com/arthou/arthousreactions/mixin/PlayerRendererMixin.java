package com.arthou.arthousreactions.mixin;

import com.arthou.arthousreactions.client.ReactionClientBootstrap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin {
    @Inject(method = "setModelProperties", at = @At("TAIL"))
    private void arthousreactions$ignoreInitialCrouchPose(AbstractClientPlayer player, CallbackInfo ci) {
        if (player == Minecraft.getInstance().player && ReactionClientBootstrap.suppressInitialScubaSneak()) {
            ((PlayerRenderer) (Object) this).getModel().crouching = false;
        }
    }

    @Inject(method = "getRenderOffset(Lnet/minecraft/client/player/AbstractClientPlayer;F)Lnet/minecraft/world/phys/Vec3;",
        at = @At("HEAD"), cancellable = true)
    private void arthousreactions$ignoreInitialCrouchOffset(AbstractClientPlayer player, float partialTick,
                                                          CallbackInfoReturnable<Vec3> cir) {
        if (player == Minecraft.getInstance().player && ReactionClientBootstrap.suppressInitialScubaSneak()) {
            cir.setReturnValue(Vec3.ZERO);
        }
    }
}
