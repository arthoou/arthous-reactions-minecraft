package com.arthou.arthousreactions.mixin;

import com.arthou.arthousreactions.client.ReactionClientBootstrap;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin extends Input {
    @Inject(method = "tick(ZF)V", at = @At("TAIL"))
    private void arthousreactions$ignoreInitialScubaSneak(boolean slow, float speed, CallbackInfo ci) {
        if (ReactionClientBootstrap.suppressInitialScubaSneak()) {
            this.shiftKeyDown = false;
        }
    }
}
