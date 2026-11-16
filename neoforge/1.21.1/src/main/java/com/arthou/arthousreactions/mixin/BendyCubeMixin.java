package com.arthou.arthousreactions.mixin;

import com.arthou.arthousreactions.client.ReactionBendRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ModelPart.Cube.class, priority = 2000)
public abstract class BendyCubeMixin {
    @Inject(method = "compile(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V",
        at = @At("HEAD"), cancellable = true)
    private void arthousreactions$drawBendBeforeCachedCube(PoseStack.Pose pose, VertexConsumer consumer,
                                                         int light, int overlay, int color, CallbackInfo ci) {
        if (ReactionBendRenderer.draw(this, pose, consumer, light, overlay, color)) ci.cancel();
    }
}
