package com.arthou.arthousreactions.mixin;

import com.arthou.arthousreactions.client.ReactionClientBootstrap;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {
    @Shadow
    protected M model;

    @WrapMethod(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V")
    private void arthousreactions$keepBentGeometry(T entity, float yaw, float partialTick, PoseStack poses,
                                                  MultiBufferSource buffers, int light, Operation<Void> original) {
        ReactionClientBootstrap.renderWithBendCompatibility(entity,
            () -> original.call(entity, yaw, partialTick, poses, buffers, light));
    }

    @Inject(
        method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/EntityModel;setupAnim(Lnet/minecraft/world/entity/Entity;FFFFF)V")
    )
    private void arthousreactions$prepareTorso(T entity, float entityYaw, float partialTick, PoseStack poseStack,
                                              MultiBufferSource bufferSource, int packedLight, CallbackInfo ci) {
        if (entity instanceof AbstractClientPlayer player && this.model instanceof PlayerModel<?> rawModel) {
            @SuppressWarnings("unchecked")
            PlayerModel<AbstractClientPlayer> playerModel = (PlayerModel<AbstractClientPlayer>) rawModel;
            ReactionClientBootstrap.prepareReactionModel(player, playerModel);
        }
    }

    @Inject(method = "getRenderType(Lnet/minecraft/world/entity/LivingEntity;ZZZ)Lnet/minecraft/client/renderer/RenderType;",
        at = @At("HEAD"), cancellable = true)
    private void arthousreactions$fadePreview(T entity, boolean visible, boolean translucent, boolean glowing,
                                             CallbackInfoReturnable<RenderType> cir) {
        if (ReactionClientBootstrap.isPreviewEntity(entity)) {
            @SuppressWarnings("unchecked")
            LivingEntityRenderer<T, M> renderer = (LivingEntityRenderer<T, M>) (Object) this;
            cir.setReturnValue(RenderType.entityTranslucent(renderer.getTextureLocation(entity)));
        }
    }

    @Inject(
        method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/model/EntityModel;setupAnim(Lnet/minecraft/world/entity/Entity;FFFFF)V",
            shift = At.Shift.AFTER
        )
    )
    private void arthousreactions$applyCheerPoseBeforeDraw(
        T entity,
        float entityYaw,
        float partialTick,
        PoseStack poseStack,
        MultiBufferSource bufferSource,
        int packedLight,
        CallbackInfo ci
    ) {
        if (entity instanceof AbstractClientPlayer player && this.model instanceof PlayerModel<?> rawModel) {
            @SuppressWarnings("unchecked")
            PlayerModel<AbstractClientPlayer> playerModel = (PlayerModel<AbstractClientPlayer>) rawModel;
            ReactionClientBootstrap.applyCheerPoseToModel(player, playerModel);
            ReactionClientBootstrap.finishReactionModel(player, playerModel);
        }
    }
}
