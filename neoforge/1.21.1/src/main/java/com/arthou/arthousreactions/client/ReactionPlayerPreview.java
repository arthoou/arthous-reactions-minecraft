package com.arthou.arthousreactions.client;

import com.arthou.arthousreactions.reaction.ReactionType;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.PlayerModelPart;
import org.joml.Quaternionf;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/** A detached player, with the owner's appearance and its own silent animation layer. */
final class ReactionPlayerPreview {
    private static final EquipmentSlot[] EQUIPMENT = {EquipmentSlot.HEAD, EquipmentSlot.CHEST,
        EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND};
    private final PreviewTimeline timeline = new PreviewTimeline();
    private PreviewPlayer player;
    private KeyframeAnimationPlayer animation;
    private long tick;

    void render(GuiGraphics graphics, WheelLayout layout, ReactionType selected, boolean cat, long now) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) return;
        boolean newPlayer = player == null || player.owner != minecraft.player || player.clientLevel != minecraft.level;
        if (newPlayer) player = new PreviewPlayer(minecraft.level, minecraft.player);
        boolean restart = timeline.update(selected, cat, now);
        if (restart || newPlayer) {
            animation = ReactionAnimations.configurePreview(player, selected);
            tick = 0L;
            for (EquipmentSlot slot : EQUIPMENT) player.setItemSlot(slot, minecraft.player.getItemBySlot(slot).copy());
        }
        long targetTick = timeline.tick(now);
        if (animation != null && targetTick - tick > 20L) {
            animation = ReactionAnimations.configurePreview(player, selected, targetTick);
            tick = targetTick;
        }
        if (animation != null) while (tick < targetTick) { animation.tick(); tick++; }
        player.tickCount = (int) Math.min(Integer.MAX_VALUE, targetTick);
        player.yBodyRot = player.yBodyRotO = 200.0F;
        player.yHeadRot = player.yHeadRotO = 200.0F;
        player.setYRot(200.0F);
        player.yRotO = 200.0F;
        player.setXRot(0.0F);
        player.xRotO = 0.0F;

        graphics.flush();
        graphics.enableScissor((int) Math.floor(layout.screenX(WheelLayout.PREVIEW_X)),
            (int) Math.floor(layout.screenY(WheelLayout.PREVIEW_Y)),
            (int) Math.ceil(layout.screenX(WheelLayout.PREVIEW_X + WheelLayout.PREVIEW_WIDTH)),
            (int) Math.ceil(layout.screenY(WheelLayout.PREVIEW_Y + WheelLayout.PREVIEW_HEIGHT)));
        try {
            ReactionClientBootstrap.renderPreview(player, timeline.visibleReaction(now),
                () -> drawPlayer(graphics, timeline.fraction(now)));
        } finally {
            graphics.flush();
            graphics.disableScissor();
        }
    }

    private void drawPlayer(GuiGraphics graphics, float partialTick) {
        var dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        Quaternionf camera = new Quaternionf(dispatcher.cameraOrientation());
        graphics.pose().pushPose();
        try {
            graphics.pose().translate(WheelLayout.PREVIEW_X + WheelLayout.PREVIEW_WIDTH / 2.0F,
                WheelLayout.PREVIEW_Y + WheelLayout.PREVIEW_HEIGHT / 2.0F + 12.0F, 50.0F);
            graphics.pose().scale(110.0F, 110.0F, -110.0F);
            graphics.pose().translate(0.0F, player.getBbHeight() / 2.0F + 0.12F, 0.0F);
            graphics.pose().mulPose(new Quaternionf().rotateZ((float) Math.PI));
            Lighting.setupForEntityInInventory();
            dispatcher.overrideCameraOrientation(new Quaternionf().rotateY((float) Math.PI));
            dispatcher.setRenderShadow(false);
            RenderSystem.runAsFancy(() -> dispatcher.render(player, 0.0, 0.0, 0.0, 0.0F,
                partialTick, graphics.pose(), graphics.bufferSource(), 15728880));
            graphics.flush();
        } finally {
            dispatcher.setRenderShadow(true);
            dispatcher.overrideCameraOrientation(camera);
            graphics.pose().popPose();
            Lighting.setupFor3DItems();
        }
    }

    void close() {
        if (player != null) ReactionAnimations.configurePreview(player, ReactionType.EMOTE_HI);
        player = null;
        animation = null;
        timeline.reset();
    }

    private static final class PreviewPlayer extends RemotePlayer {
        private final AbstractClientPlayer owner;

        private PreviewPlayer(ClientLevel level, AbstractClientPlayer owner) {
            super(level, new GameProfile(UUID.nameUUIDFromBytes(("arthousreactions:preview:" + owner.getUUID())
                .getBytes(StandardCharsets.UTF_8)), owner.getGameProfile().getName()));
            this.owner = owner;
            // Keep cape interpolation independent of the world's spawn coordinates.
            this.moveTo(0.0, 0.0, 0.0, 200.0F, 0.0F);
        }

        @Override public PlayerSkin getSkin() { return owner.getSkin(); }
        @Override public boolean isModelPartShown(PlayerModelPart part) { return owner.isModelPartShown(part); }
        @Override public boolean isSpectator() { return false; }
        @Override public boolean isCrouching() { return false; }
    }
}
