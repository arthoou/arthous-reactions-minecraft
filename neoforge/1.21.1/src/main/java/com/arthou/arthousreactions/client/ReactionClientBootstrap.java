package com.arthou.arthousreactions.client;

import com.arthou.arthousreactions.ArthousReactions;
import com.arthou.arthousreactions.item.ReactionItems;
import com.arthou.arthousreactions.network.ServerboundSelectReactionPacket;
import com.arthou.arthousreactions.network.ScubaOptions;
import com.arthou.arthousreactions.reaction.ReactionType;
import com.arthou.arthousreactions.reaction.ClapTiming;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.Util;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ReactionClientBootstrap {
    private static final ResourceLocation CREATE_CONFETTI = ArthousReactions.id("textures/effect/create_confetti.png");
    private static final ResourceLocation ANGRY_SMOKE = ArthousReactions.id("textures/effect/angry_smoke.png");
    private static final ResourceLocation STOMP_DUST = ArthousReactions.id("textures/effect/stomp_dust.png");
    private static final ResourceLocation HEART_PARTICLE = ArthousReactions.id("textures/effect/heart_particle.png");
    private static final float POUT_POSE_END_SECONDS = 1.72F;
    private static final int[] CONFETTI_COLORS = {0xFFFF2F4F, 0xFFFFD400, 0xFF00E0B8, 0xFF008CFF, 0xFF9C27FF, 0xFFFF7A00, 0xFFFF4FC3};
    private static final float CHEER_POSE_END_SECONDS = 2.84F;
    private static final PoseKeyframe[] CHEER_KEYFRAMES = {
        new PoseKeyframe(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F),
        new PoseKeyframe(0.08F, 0.0F, 0.0F, -0.95F, 0.02F, -0.04F, -0.95F, -0.02F, 0.04F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F),
        new PoseKeyframe(0.18F, 0.0F, 0.0F, -2.35F, 0.08F, -0.12F, -2.35F, -0.08F, 0.12F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F),
        new PoseKeyframe(0.34F, 0.0F, 0.0F, -3.02F, 0.16F, -0.22F, -3.02F, -0.16F, 0.22F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F),
        new PoseKeyframe(0.72F, 0.0F, 0.0F, -2.96F, 0.2F, -0.26F, -3.06F, -0.12F, 0.18F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F),
        new PoseKeyframe(1.12F, 0.0F, 0.0F, -3.06F, 0.12F, -0.18F, -2.96F, -0.2F, 0.26F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F),
        new PoseKeyframe(1.6F, 0.0F, 0.0F, -3.0F, 0.18F, -0.24F, -3.0F, -0.18F, 0.24F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F),
        new PoseKeyframe(2.24F, 0.0F, 0.0F, -3.0F, 0.15F, -0.22F, -3.0F, -0.15F, 0.22F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F),
        new PoseKeyframe(2.62F, 0.0F, 0.0F, -1.35F, 0.04F, -0.08F, -1.35F, -0.04F, 0.08F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F),
        new PoseKeyframe(2.84F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F)
    };
    private static final Map<UUID, ActiveReaction> ACTIVE_REACTIONS = new ConcurrentHashMap<>();
    private static boolean initialized;
    private static KeyMapping openWheelKey;
    private static CameraType previousCameraType;
    private static final SneakReleaseLatch SCUBA_SNEAK = new SneakReleaseLatch();
    private static final PreviewScope<AbstractClientPlayer> PREVIEW = new PreviewScope<>();
    private static final TorsoStability TORSO_STABILITY = new TorsoStability();

    private ReactionClientBootstrap() {
    }

    public static void init(IEventBus modBus) {
        if (initialized) {
            return;
        }

        initialized = true;
        modBus.addListener((net.neoforged.fml.event.lifecycle.FMLClientSetupEvent event) -> event.enqueueWork(
            () -> OptionalSkinLayers.initialize(net.neoforged.fml.ModList.get().isLoaded("skinlayers3d"))));
        ReactionAnimations.register();
        openWheelKey = new KeyMapping("key.arthousreactions.open_wheel", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Z, "key.categories.arthousreactions");
        modBus.addListener(ReactionClientBootstrap::onRegisterKeyMappings);
        modBus.addListener(ReactionClientBootstrap::onAddLayers);

        NeoForge.EVENT_BUS.addListener(ReactionClientBootstrap::onClientTick);
        NeoForge.EVENT_BUS.addListener(ReactionClientBootstrap::onRenderPlayerPost);
        NeoForge.EVENT_BUS.addListener(ReactionClientBootstrap::onClientLeave);
        NeoForge.EVENT_BUS.addListener(RenderNameTagEvent.class, event -> {
            if (PREVIEW.contains(event.getEntity())) event.setCanRender(TriState.FALSE);
        });
    }

    private static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(openWheelKey);
    }

    private static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        for (var skin : event.getSkins()) {
            PlayerRenderer renderer = event.getSkin(skin);
            if (renderer != null) renderer.addLayer(new AngelWingsLayer(renderer));
        }
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        long now = Util.getMillis();
        Minecraft minecraft = Minecraft.getInstance();
        for (var entry : ACTIVE_REACTIONS.entrySet()) {
            Player player = minecraft.level == null ? null : minecraft.level.getPlayerByUUID(entry.getKey());
            boolean stopLoop = entry.getValue().type() == ReactionType.SCUBA
                && (player == null || shouldCancel(player, entry.getValue()));
            if (entry.getValue().isExpired(now) || stopLoop) {
                cancelReaction(entry.getKey(), player == minecraft.player && stopLoop);
            }
        }

        updateCheerCamera(minecraft);
        if (minecraft.player == null || minecraft.level == null || minecraft.screen != null) {
            return;
        }

        ActiveReaction localReaction = ACTIVE_REACTIONS.get(minecraft.player.getUUID());
        if (localReaction != null && shouldCancel(minecraft.player, localReaction)) {
            cancelReaction(minecraft.player);
        }

        while (openWheelKey.consumeClick()) {
            minecraft.setScreen(new ReactionWheelScreen());
        }
    }

    private static void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        Player player = event.getEntity();
        ActiveReaction activeReaction = reactionFor(player);
        if (activeReaction != null) {
            if (shouldCancel(player, activeReaction)) {
                cancelReaction(player);
                return;
            }

            renderBurst(player, event.getRenderer().getModel(), activeReaction, event.getPoseStack(), event.getMultiBufferSource());
        }
    }

    private static void onClientLeave(ClientPlayerNetworkEvent.LoggingOut event) {
        ReactionAnimations.stopAll();
        ACTIVE_REACTIONS.clear();
        SCUBA_SNEAK.reset(false);
        restoreCheerCamera(Minecraft.getInstance());
    }

    public static void playReaction(UUID playerId, ReactionType reaction, int burstSeed) {
        Minecraft.getInstance().execute(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            ReactionAnimations.stop(playerId);
            ACTIVE_REACTIONS.put(playerId, new ActiveReaction(reaction, Util.getMillis(), burstSeed));
            if (minecraft.player != null && playerId.equals(minecraft.player.getUUID())) {
                SCUBA_SNEAK.reset(reaction == ReactionType.SCUBA
                    && (ScubaOptions.catEnabled(burstSeed) || Screen.hasShiftDown() || minecraft.options.keyShift.isDown()));
            }
            if (ReactionAnimations.handles(reaction) && minecraft.level != null
                && minecraft.level.getPlayerByUUID(playerId) instanceof AbstractClientPlayer player) {
                ReactionAnimations.play(player, reaction, burstSeed);
            }
            updateCheerCamera(minecraft);
        });
    }

    public static void sendReaction(ReactionType reaction) {
        sendReaction(reaction, false);
    }

    public static void sendReactionWithAlternateSound(ReactionType reaction) {
        sendReaction(reaction, true);
    }

    private static void sendReaction(ReactionType reaction, boolean byeSound) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || minecraft.getConnection() == null) {
            return;
        }
        if (!minecraft.getConnection().hasChannel(ServerboundSelectReactionPacket.TYPE)) {
            minecraft.player.displayClientMessage(Component.translatable("message.arthousreactions.unsupported_server"), true);
            return;
        }
        PacketDistributor.sendToServer(new ServerboundSelectReactionPacket(reaction.index(), byeSound,
            reaction == ReactionType.SCUBA && Screen.hasShiftDown()));
    }

    public static KeyMapping openWheelKey() {
        return openWheelKey;
    }

    private static void updateCheerCamera(Minecraft minecraft) {
        if (minecraft.player == null) {
            restoreCheerCamera(minecraft);
            return;
        }

        boolean localPlayerReacting = ACTIVE_REACTIONS.containsKey(minecraft.player.getUUID());
        if (localPlayerReacting && minecraft.options.getCameraType().isFirstPerson()) {
            previousCameraType = minecraft.options.getCameraType();
            minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        } else if (!localPlayerReacting) {
            restoreCheerCamera(minecraft);
        }
    }

    private static void restoreCheerCamera(Minecraft minecraft) {
        if (previousCameraType != null) {
            minecraft.options.setCameraType(previousCameraType);
            previousCameraType = null;
        }
    }

    public static void prepareReactionModel(AbstractClientPlayer player, PlayerModel<AbstractClientPlayer> model) {
        ActiveReaction reaction = reactionFor(player);
        TORSO_STABILITY.prepare(model.body, model.jacket, reaction == null ? null : reaction.type());
    }

    public static void finishReactionModel(AbstractClientPlayer player, PlayerModel<AbstractClientPlayer> model) {
        ActiveReaction reaction = reactionFor(player);
        TORSO_STABILITY.finish(model.body, model.jacket, reaction == null ? null : reaction.type());
        if (reaction == null) return;
        SkinLayerSync.synchronize(model, reaction.type());
    }

    public static void renderWithBendCompatibility(LivingEntity entity, Runnable render) {
        boolean active = entity instanceof AbstractClientPlayer player && reactionFor(player) != null;
        ReactionBendRenderer.scoped(active, render);
    }

    public static void applyCheerPoseToModel(AbstractClientPlayer player, PlayerModel<AbstractClientPlayer> model) {
        ActiveReaction activeReaction = reactionFor(player);
        if (activeReaction == null) {
            return;
        }

        if (shouldCancel(player, activeReaction)) {
            cancelReaction(player);
            return;
        }

        if (ReactionAnimations.handles(activeReaction.type())) {
            return; // Player Animator owns these poses and elbow bends.
        }

        ignoreCrouchPose(model);

        if (activeReaction.type() == ReactionType.ANGRY) {
            applyAngryPose(activeReaction, model);
            syncWearLayers(model);
            return;
        }

        if (activeReaction.type() == ReactionType.POUT) {
            applyPoutPose(activeReaction, model);
            syncWearLayers(model);
            return;
        }

        if (isGestureEmote(activeReaction.type())) {
            applyGesturePose(player, activeReaction, model);
            syncWearLayers(model);
            return;
        }

        if (activeReaction.type() != ReactionType.CHEER) {
            return;
        }

        float ageSeconds = activeReaction.ageSeconds(Util.getMillis());
        if (ageSeconds > CHEER_POSE_END_SECONDS) {
            return;
        }

        PoseKeyframe pose = sampleCheerPose(ageSeconds);

        model.rightArm.xRot = pose.rightArmXRot;
        model.rightArm.yRot = pose.rightArmYRot;
        model.rightArm.zRot = pose.rightArmZRot;
        model.leftArm.xRot = pose.leftArmXRot;
        model.leftArm.yRot = pose.leftArmYRot;
        model.leftArm.zRot = pose.leftArmZRot;

        syncWearLayers(model);
    }

    private static void cancelReaction(Player player) {
        cancelReaction(player.getUUID(), true);
    }

    public static void stopReaction(UUID playerId) {
        Minecraft.getInstance().execute(() -> cancelReaction(playerId, false));
    }

    private static void cancelReaction(UUID playerId, boolean notifyServer) {
        ActiveReaction removed = ACTIVE_REACTIONS.remove(playerId);
        if (removed != null) {
            ReactionAnimations.stop(playerId);
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player != null && playerId.equals(minecraft.player.getUUID())) {
                SCUBA_SNEAK.reset(false);
                restoreCheerCamera(minecraft);
                if (notifyServer && removed.type() == ReactionType.SCUBA && minecraft.getConnection() != null
                    && minecraft.getConnection().hasChannel(ServerboundSelectReactionPacket.TYPE)) {
                    PacketDistributor.sendToServer(new ServerboundSelectReactionPacket(ServerboundSelectReactionPacket.CANCEL, false));
                }
            }
        }
    }

    private static boolean shouldCancel(Player player, ActiveReaction reaction) {
        if (PREVIEW.contains(player)) return false;
        if (reaction.type() == ReactionType.SCUBA) {
            // Its owner sends cancellation to every viewer; remote Shift can be
            // the cat-selection modifier arriving on a different network tick.
            return player == Minecraft.getInstance().player
                && SCUBA_SNEAK.shouldStop(Minecraft.getInstance().options.keyShift.isDown(), Screen.hasShiftDown());
        }
        return player.isShiftKeyDown() || player.isCrouching();
    }

    public static boolean suppressInitialScubaSneak() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !SCUBA_SNEAK.isWaiting()) return false;
        ActiveReaction active = ACTIVE_REACTIONS.get(minecraft.player.getUUID());
        return active != null && active.type() == ReactionType.SCUBA;
    }

    static void renderPreview(AbstractClientPlayer player, ActiveReaction reaction, Runnable draw) {
        PREVIEW.render(player, reaction, draw);
    }

    public static boolean isPreviewEntity(Object entity) {
        return PREVIEW.contains(entity);
    }

    static ActiveReaction reactionFor(Player player) {
        return PREVIEW.contains(player) ? PREVIEW.reaction() : ACTIVE_REACTIONS.get(player.getUUID());
    }

    private static void syncWearLayers(PlayerModel<AbstractClientPlayer> model) {
        model.hat.copyFrom(model.head);
        model.jacket.copyFrom(model.body);
        model.rightPants.copyFrom(model.rightLeg);
        model.leftPants.copyFrom(model.leftLeg);
        model.leftSleeve.copyFrom(model.leftArm);
        model.rightSleeve.copyFrom(model.rightArm);
    }

    private static void renderBurst(Player player, PlayerModel<AbstractClientPlayer> model, ActiveReaction activeReaction, PoseStack poseStack, MultiBufferSource bufferSource) {
        Minecraft minecraft = Minecraft.getInstance();
        EntityRenderDispatcher dispatcher = minecraft.getEntityRenderDispatcher();
        float ageSeconds = activeReaction.ageSeconds(Util.getMillis());

        if (activeReaction.type() == ReactionType.CHEER) {
            renderConfetti(activeReaction, ageSeconds, poseStack, bufferSource, dispatcher);
            return;
        }

        if (activeReaction.type() == ReactionType.ANGRY) {
            renderAngrySmoke(activeReaction, ageSeconds, poseStack, bufferSource, dispatcher);
            return;
        }

        if (activeReaction.type() == ReactionType.POUT) {
            renderPoutDust(activeReaction, ageSeconds, poseStack, bufferSource, dispatcher);
            return;
        }

        if (activeReaction.type() == ReactionType.EMOTE_ANGEL) {
            renderAngelEffect(player, model, activeReaction, ageSeconds, poseStack, bufferSource);
            return;
        }

        if (activeReaction.type() == ReactionType.EMOTE_HEARTS) {
            renderHeartBurst(activeReaction, ageSeconds, poseStack, bufferSource, dispatcher);
            return;
        }

        if (isGestureEmote(activeReaction.type())) {
            return;
        }

        if (activeReaction.type().hasEmoji()
            && (activeReaction.type() != ReactionType.SCUBA || ScubaOptions.catEnabled(activeReaction.burstSeed()))) {
            renderEmoji(player, activeReaction, ageSeconds, poseStack, bufferSource, dispatcher);
        }
    }

    private static void renderEmoji(Player player, ActiveReaction activeReaction, float ageSeconds, PoseStack poseStack, MultiBufferSource bufferSource, EntityRenderDispatcher dispatcher) {
        float progress = activeReaction.normalizedAge(Util.getMillis());
        float iconHeight = activeReaction.type() == ReactionType.SCUBA ? 0.30F : 0.52F;
        float yOffset = player.getBbHeight() + iconHeight + Mth.sin(Mth.clamp(progress * 1.2F, 0.0F, 1.0F) * Mth.PI) * 0.28F;
        float alphaFade = progress < 0.78F ? 1.0F : 1.0F - ((progress - 0.78F) / 0.22F);
        int alpha = Math.max(0, Math.round(255.0F * alphaFade * alphaFade));
        if (alpha <= 0) {
            return;
        }

        float xShake = 0.0F;
        float zShake = 0.0F;
        float rotation = 0.0F;
        float scale = 0.46F;
        poseStack.pushPose();
        poseStack.translate(xShake, yOffset, zShake);
        poseStack.mulPose(dispatcher.cameraOrientation());
        poseStack.mulPose(Axis.ZP.rotation(rotation));
        poseStack.scale(-scale, -scale, scale);
        renderIconQuad(poseStack, bufferSource, activeReaction.type(), alpha,
            Math.max(0L, Util.getMillis() - activeReaction.startedAtMs()));
        poseStack.popPose();
    }

    private static void renderAngrySmoke(ActiveReaction activeReaction, float ageSeconds, PoseStack poseStack, MultiBufferSource bufferSource, EntityRenderDispatcher dispatcher) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(ANGRY_SMOKE));
        int light = LightTexture.FULL_BRIGHT;
        int seed = activeReaction.burstSeed();
        float progress = activeReaction.normalizedAge(Util.getMillis());
        float fade = progress < 0.82F ? 1.0F : 1.0F - ((progress - 0.82F) / 0.18F);
        int alphaBase = Math.max(0, Math.round(210.0F * fade));

        for (int i = 0; i < 10; i++) {
            float delay = pseudo(seed, i, 9) * 0.8F;
            float t = (ageSeconds + delay) % 1.1F;
            float side = i % 2 == 0 ? -1.0F : 1.0F;
            float x = side * (0.26F + pseudo(seed, i, 0) * 0.15F) + Mth.sin(ageSeconds * 8.0F + i) * 0.025F;
            float y = 1.46F + t * 0.52F + pseudo(seed, i, 1) * 0.16F;
            float z = (pseudo(seed, i, 2) - 0.5F) * 0.28F;
            float scale = 0.13F + t * 0.08F;
            int alpha = Math.max(0, Math.round(alphaBase * (1.0F - t / 1.1F)));
            if (alpha <= 0) {
                continue;
            }

            poseStack.pushPose();
            poseStack.translate(x, y, z);
            poseStack.mulPose(dispatcher.cameraOrientation());
            poseStack.mulPose(Axis.ZP.rotation(Mth.sin(ageSeconds * 4.0F + i) * 0.18F));
            poseStack.scale(-scale, -scale, scale);
            drawTexturedQuad(poseStack, consumer, light, alpha);
            poseStack.popPose();
        }
    }

    private static void applyPoutPose(ActiveReaction activeReaction, PlayerModel<AbstractClientPlayer> model) {
        float ageSeconds = activeReaction.ageSeconds(Util.getMillis());
        if (ageSeconds > POUT_POSE_END_SECONDS) {
            return;
        }

        float windup = smoothPulse(ageSeconds, 0.04F, 0.22F);
        float lift = smoothPulse(ageSeconds, 0.12F, 0.42F);
        float stomp = smoothPulse(ageSeconds, 0.42F, 0.56F);
        float bounce = smoothPulse(ageSeconds, 0.56F, 0.68F) * (1.0F - smoothPulse(ageSeconds, 0.68F, 0.92F));
        float settle = 1.0F - smoothPulse(ageSeconds, 1.34F, POUT_POSE_END_SECONDS);
        float attitude = windup * settle;
        float tantrumShake = Mth.sin(ageSeconds * 38.0F) * 0.045F * attitude;

        model.head.yRot = 1.08F * attitude + tantrumShake;
        model.head.xRot = -0.5F * attitude;
        model.head.zRot = -0.12F * attitude;
        model.rightLeg.xRot = Mth.lerp(stomp, -1.42F * lift, 0.68F) * settle;
        model.rightLeg.yRot = -0.16F * attitude;
        model.rightLeg.zRot = 0.08F * bounce;
        model.leftLeg.xRot = (-0.14F * attitude) - (0.12F * bounce);
        model.leftLeg.zRot = -0.08F * attitude;
        model.rightArm.xRot = (-0.56F * attitude) + Mth.sin(ageSeconds * 20.0F) * 0.055F * attitude;
        model.leftArm.xRot = (-0.56F * attitude) - Mth.sin(ageSeconds * 20.0F) * 0.055F * attitude;
        model.rightArm.zRot = 0.48F * attitude;
        model.leftArm.zRot = -0.48F * attitude;

        model.hat.copyFrom(model.head);
        model.jacket.copyFrom(model.body);
        model.rightPants.copyFrom(model.rightLeg);
        model.leftPants.copyFrom(model.leftLeg);
        model.leftSleeve.copyFrom(model.leftArm);
        model.rightSleeve.copyFrom(model.rightArm);
    }

    private static void ignoreCrouchPose(PlayerModel<AbstractClientPlayer> model) {
        model.crouching = false;
        model.head.y = 0.0F;
        model.hat.copyFrom(model.head);
        model.body.y = 0.0F;
        model.jacket.copyFrom(model.body);
        model.rightArm.y = 2.0F;
        model.leftArm.y = 2.0F;
        model.rightSleeve.copyFrom(model.rightArm);
        model.leftSleeve.copyFrom(model.leftArm);
        model.rightLeg.y = 12.0F;
        model.leftLeg.y = 12.0F;
        model.rightLeg.z = 0.1F;
        model.leftLeg.z = 0.1F;
        model.rightPants.copyFrom(model.rightLeg);
        model.leftPants.copyFrom(model.leftLeg);
    }

    private static void lookAtNearestPlayer(AbstractClientPlayer player, PlayerModel<AbstractClientPlayer> model, float weight) {
        if (player.clientLevel == null) {
            return;
        }

        AbstractClientPlayer nearestPlayer = null;
        double nearestDistance = 100.0D;
        for (AbstractClientPlayer otherPlayer : player.clientLevel.players()) {
            if (otherPlayer == player || otherPlayer.isInvisible()) {
                continue;
            }

            double distance = player.distanceToSqr(otherPlayer);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestPlayer = otherPlayer;
            }
        }

        if (nearestPlayer == null) {
            return;
        }

        double dx = nearestPlayer.getX() - player.getX();
        double dz = nearestPlayer.getZ() - player.getZ();
        double dy = nearestPlayer.getEyeY() - player.getEyeY();
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        float targetYaw = Mth.wrapDegrees((float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F - player.yBodyRot) * Mth.DEG_TO_RAD;
        float targetPitch = (float) (-Mth.atan2(dy, horizontal)) * Mth.DEG_TO_RAD;

        model.head.yRot = Mth.clamp(targetYaw, -1.15F, 1.15F) * weight;
        model.head.xRot = Mth.clamp(targetPitch, -0.62F, 0.62F) * weight;
    }

    private static void applyAngryPose(ActiveReaction activeReaction, PlayerModel<AbstractClientPlayer> model) {
        float ageSeconds = activeReaction.ageSeconds(Util.getMillis());
        float duration = 0.92F;
        if (ageSeconds > duration) {
            return;
        }

        float in = smoothPulse(ageSeconds, 0.0F, 0.14F);
        float out = 1.0F - smoothPulse(ageSeconds, duration - 0.26F, duration);
        float weight = in * out;
        float shake = Mth.sin(ageSeconds * 22.0F) * weight;

        model.head.xRot = 0.18F * weight;
        model.head.yRot = shake * 0.1F;
        model.rightArm.xRot = -0.32F * weight;
        model.leftArm.xRot = -0.32F * weight;
        model.rightArm.zRot = 0.32F * weight;
        model.leftArm.zRot = -0.32F * weight;

        model.hat.copyFrom(model.head);
        model.leftSleeve.copyFrom(model.leftArm);
        model.rightSleeve.copyFrom(model.rightArm);
    }

    private static void applyGesturePose(AbstractClientPlayer player, ActiveReaction activeReaction, PlayerModel<AbstractClientPlayer> model) {
        float ageSeconds = activeReaction.ageSeconds(Util.getMillis());
        float duration = gesturePoseDuration(activeReaction.type());
        if (ageSeconds > duration) {
            return;
        }

        float in = smoothPulse(ageSeconds, 0.0F, 0.18F);
        float out = 1.0F - smoothPulse(ageSeconds, duration - 0.28F, duration);
        float weight = in * out;

        switch (activeReaction.type()) {
            case EMOTE_HI -> {
                model.rightArm.xRot = -1.75F * weight;
                model.rightArm.zRot = 0.55F * weight + Mth.sin(ageSeconds * 14.0F) * 0.18F * weight;
                model.leftArm.xRot = -0.08F * weight;
                model.head.yRot = Mth.sin(ageSeconds * 5.0F) * 0.16F * weight;
            }
            case EMOTE_EW -> {
                float recoil = smoothPulse(ageSeconds, 0.02F, 0.24F) * out;
                float shiver = Mth.sin(ageSeconds * 18.0F) * 0.025F * recoil;
                model.head.xRot = -0.18F * recoil;
                model.head.yRot = -0.36F * recoil + shiver;
                model.rightArm.xRot = -1.34F * recoil;
                model.rightArm.yRot = -0.18F * recoil;
                model.rightArm.zRot = 0.18F * recoil;
                model.leftArm.xRot = -0.42F * recoil;
                model.leftArm.yRot = 0.34F * recoil;
                model.leftArm.zRot = -0.52F * recoil;
            }
            case EMOTE_VINE_BOOM -> {
                lookAtNearestPlayer(player, model, weight);
            }
            default -> {
            }
        }

        model.hat.copyFrom(model.head);
        model.jacket.copyFrom(model.body);
        model.rightPants.copyFrom(model.rightLeg);
        model.leftPants.copyFrom(model.leftLeg);
        model.leftSleeve.copyFrom(model.leftArm);
        model.rightSleeve.copyFrom(model.rightArm);
    }

    private static void renderPoutDust(ActiveReaction activeReaction, float ageSeconds, PoseStack poseStack, MultiBufferSource bufferSource, EntityRenderDispatcher dispatcher) {
        float impactAge = ageSeconds - 0.52F;
        if (impactAge < 0.0F || impactAge > 0.72F) {
            return;
        }

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(STOMP_DUST));
        int seed = activeReaction.burstSeed();
        float impactProgress = impactAge / 0.72F;
        int alphaBase = Math.max(0, Math.round(220.0F * (1.0F - impactProgress)));

        for (int i = 0; i < 12; i++) {
            float angle = -Mth.HALF_PI + (pseudo(seed, i, 0) - 0.5F) * 1.65F;
            float distance = 0.07F + impactProgress * (0.28F + pseudo(seed, i, 1) * 0.24F);
            float x = -0.18F + Mth.cos(angle) * distance + (pseudo(seed, i, 2) - 0.5F) * 0.08F;
            float y = 0.04F + impactProgress * 0.16F + pseudo(seed, i, 3) * 0.04F;
            float z = -0.42F + Mth.sin(angle) * distance;
            float scale = 0.13F + impactProgress * 0.16F + pseudo(seed, i, 4) * 0.05F;
            int alpha = Math.max(0, Math.round(alphaBase * (0.72F + pseudo(seed, i, 5) * 0.28F)));
            if (alpha <= 0) {
                continue;
            }

            poseStack.pushPose();
            poseStack.translate(x, y, z);
            poseStack.mulPose(dispatcher.cameraOrientation());
            poseStack.mulPose(Axis.ZP.rotation(Mth.sin(ageSeconds * 7.0F + i) * 0.18F));
            poseStack.scale(-scale, -scale, scale);
            drawTexturedQuad(poseStack, consumer, LightTexture.FULL_BRIGHT, alpha);
            poseStack.popPose();
        }
    }

    private static void renderSadTears(Player player, ActiveReaction activeReaction, float ageSeconds, PoseStack poseStack, MultiBufferSource bufferSource, EntityRenderDispatcher dispatcher) {
        float duration = gesturePoseDuration(activeReaction.type());
        if (ageSeconds > duration) {
            return;
        }

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(CREATE_CONFETTI));
        int seed = activeReaction.burstSeed();
        float endFade = 1.0F - Mth.clamp((ageSeconds - (duration - 0.42F)) / 0.42F, 0.0F, 1.0F);

        for (int i = 0; i < 9; i++) {
            float delay = pseudo(seed, i, 10) * 0.62F;
            float t = (ageSeconds + delay) % 0.82F;
            float y = player.getBbHeight() - 0.34F - t * (0.72F + pseudo(seed, i, 1) * 0.16F);
            if (y < 0.58F) {
                continue;
            }

            int alpha = Math.max(0, Math.round(235.0F * (1.0F - t / 0.82F) * endFade));
            if (alpha <= 0) {
                continue;
            }

            for (int sideIndex = 0; sideIndex < 2; sideIndex++) {
                float side = sideIndex == 0 ? -1.0F : 1.0F;
                float x = side * (0.105F + pseudo(seed, i, sideIndex) * 0.018F);
                float z = -0.285F + (pseudo(seed, i, sideIndex + 2) - 0.5F) * 0.025F;
                float size = 0.026F + pseudo(seed, i, sideIndex + 4) * 0.018F;
                poseStack.pushPose();
                poseStack.translate(x, y, z);
                poseStack.mulPose(dispatcher.cameraOrientation());
                poseStack.scale(0.75F, 1.35F, 1.0F);
                drawConfettiQuad(poseStack, consumer, LightTexture.FULL_BRIGHT, 0x5EC8FF, alpha, size);
                poseStack.popPose();
            }
        }
    }

    private static void renderAngelEffect(Player player, PlayerModel<AbstractClientPlayer> model, ActiveReaction activeReaction, float ageSeconds, PoseStack poseStack, MultiBufferSource bufferSource) {
        float fade = effectFade(activeReaction);
        if (fade <= 0.0F) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0D, player.getBbHeight() + 0.48D + Mth.sin(ageSeconds * 4.0F) * 0.012D, 0.0D);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        poseStack.scale(0.42F, 0.42F, 0.42F);
        Minecraft.getInstance().getItemRenderer().renderStatic(new ItemStack(ReactionItems.ANGEL_HALO.get()), ItemDisplayContext.HEAD, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, player.level(), activeReaction.burstSeed());
        poseStack.popPose();
    }

    private static void renderHeartBurst(ActiveReaction activeReaction, float ageSeconds, PoseStack poseStack, MultiBufferSource bufferSource, EntityRenderDispatcher dispatcher) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(HEART_PARTICLE));
        int seed = activeReaction.burstSeed();
        float fade = effectFade(activeReaction);

        for (int i = 0; i < 22; i++) {
            float delay = pseudo(seed, i, 8) * 0.52F;
            float t = (ageSeconds + delay) % 1.32F;
            float side = i % 2 == 0 ? -1.0F : 1.0F;
            float x = side * (0.12F + pseudo(seed, i, 0) * 0.34F) + Mth.sin(ageSeconds * 4.0F + i) * 0.06F;
            float y = 0.92F + t * (0.68F + pseudo(seed, i, 1) * 0.34F);
            float z = (pseudo(seed, i, 2) - 0.5F) * 0.46F;
            float scale = 0.12F + pseudo(seed, i, 3) * 0.065F;
            int alpha = Math.max(0, Math.round(245.0F * (1.0F - t / 1.32F) * fade));
            if (alpha <= 0) {
                continue;
            }

            poseStack.pushPose();
            poseStack.translate(x, y, z);
            poseStack.mulPose(dispatcher.cameraOrientation());
            poseStack.mulPose(Axis.ZP.rotation(Mth.sin(ageSeconds * 3.0F + i) * 0.3F));
            poseStack.scale(-scale, -scale, scale);
            drawTexturedQuad(poseStack, consumer, LightTexture.FULL_BRIGHT, alpha);
            poseStack.popPose();
        }
    }

    private static float effectFade(ActiveReaction activeReaction) {
        float progress = activeReaction.normalizedAge(Util.getMillis());
        return progress < 0.78F ? 1.0F : Mth.clamp(1.0F - ((progress - 0.78F) / 0.22F), 0.0F, 1.0F);
    }

    private static void renderIconQuad(PoseStack poseStack, MultiBufferSource bufferSource, ReactionType reaction, int alpha, long elapsedMs) {
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(reaction.texture()));
        int light = LightTexture.FULL_BRIGHT;
        float u0 = reaction.frameX(elapsedMs) / (float) reaction.textureWidth();
        float v0 = reaction.frameY(elapsedMs) / (float) reaction.textureHeight();
        float u1 = (reaction.frameX(elapsedMs) + reaction.frameSize()) / (float) reaction.textureWidth();
        float v1 = (reaction.frameY(elapsedMs) + reaction.frameSize()) / (float) reaction.textureHeight();

        consumer.addVertex(matrix, -0.5F, 0.5F, 0.0F).setColor(255, 255, 255, alpha).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
        consumer.addVertex(matrix, 0.5F, 0.5F, 0.0F).setColor(255, 255, 255, alpha).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
        consumer.addVertex(matrix, 0.5F, -0.5F, 0.0F).setColor(255, 255, 255, alpha).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
        consumer.addVertex(matrix, -0.5F, -0.5F, 0.0F).setColor(255, 255, 255, alpha).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
    }

    private static void renderConfetti(ActiveReaction activeReaction, float ageSeconds, PoseStack poseStack, MultiBufferSource bufferSource, EntityRenderDispatcher dispatcher) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(CREATE_CONFETTI));
        int light = LightTexture.FULL_BRIGHT;
        int seed = activeReaction.burstSeed();

        for (int i = 0; i < 44; i++) {
            float delay = pseudo(seed, i, 5) * 0.16F;
            float t = ageSeconds - delay;
            if (t < 0.0F) {
                continue;
            }

            float angle = pseudo(seed, i, 0) * Mth.TWO_PI;
            float horizontalSpeed = 0.18F + pseudo(seed, i, 1) * 0.74F;
            float verticalSpeed = 1.58F + pseudo(seed, i, 2) * 0.92F;
            float gravity = 1.85F + pseudo(seed, i, 6) * 0.5F;
            float wobble = Mth.sin((t * 7.0F) + pseudo(seed, i, 7) * Mth.TWO_PI) * 0.05F;
            float x = Mth.cos(angle) * horizontalSpeed * t + Mth.cos(angle + Mth.HALF_PI) * wobble;
            float z = Mth.sin(angle) * horizontalSpeed * t + Mth.sin(angle + Mth.HALF_PI) * wobble;
            float y = 2.02F + verticalSpeed * t - 0.5F * gravity * t * t;
            float size = 0.055F + pseudo(seed, i, 3) * 0.035F;
            if (y <= size) {
                continue;
            }

            int color = CONFETTI_COLORS[Math.floorMod(seed + i, CONFETTI_COLORS.length)];
            int alpha = Math.max(0, Math.round(255.0F * (1.0F - Mth.clamp((ageSeconds - 3.0F) / 0.34F, 0.0F, 1.0F))));

            poseStack.pushPose();
            poseStack.translate(x, y, z);
            poseStack.mulPose(dispatcher.cameraOrientation());
            poseStack.mulPose(Axis.ZP.rotation(t * Mth.TWO_PI * (0.8F + pseudo(seed, i, 4) * 2.4F)));
            drawConfettiQuad(poseStack, consumer, light, color, alpha, size);
            poseStack.popPose();
        }
    }

    private static PoseKeyframe sampleCheerPose(float ageSeconds) {
        for (int i = 0; i < CHEER_KEYFRAMES.length - 1; i++) {
            PoseKeyframe from = CHEER_KEYFRAMES[i];
            PoseKeyframe to = CHEER_KEYFRAMES[i + 1];
            if (ageSeconds <= to.timeSeconds) {
                float delta = (ageSeconds - from.timeSeconds) / (to.timeSeconds - from.timeSeconds);
                float eased = delta * delta * (3.0F - 2.0F * delta);
                return PoseKeyframe.lerp(from, to, Mth.clamp(eased, 0.0F, 1.0F));
            }
        }
        return CHEER_KEYFRAMES[CHEER_KEYFRAMES.length - 1];
    }

    private static float smoothPulse(float value, float start, float end) {
        float progress = Mth.clamp((value - start) / (end - start), 0.0F, 1.0F);
        return progress * progress * (3.0F - 2.0F * progress);
    }

    private static boolean isGestureEmote(ReactionType reaction) {
        return reaction.ordinal() >= ReactionType.EMOTE_HI.ordinal() && reaction.ordinal() <= ReactionType.EMOTE_VINE_BOOM.ordinal();
    }

    private static float gesturePoseDuration(ReactionType reaction) {
        return switch (reaction) {
            case EMOTE_HI -> 1.75F;
            case EMOTE_SAD -> 2.35F;
            case EMOTE_LAUGH -> 1.45F;
            case EMOTE_CLAP -> ClapTiming.DURATION_MS / 1000.0F;
            case EMOTE_FEAR -> 1.25F;
            case EMOTE_WOAH -> 1.15F;
            case EMOTE_EW -> 1.05F;
            case EMOTE_ANGEL -> 2.2F;
            case EMOTE_HEARTS -> 1.85F;
            case EMOTE_67 -> 2.4F;
            case EMOTE_VINE_BOOM -> 1.2F;
            default -> 0.92F;
        };
    }

    private static void drawConfettiQuad(PoseStack poseStack, VertexConsumer consumer, int light, int color, int alpha, float size) {
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        int red = color >> 16 & 255;
        int green = color >> 8 & 255;
        int blue = color & 255;

        consumer.addVertex(matrix, -size, size, 0.0F).setColor(red, green, blue, alpha).setUv(0.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
        consumer.addVertex(matrix, size, size, 0.0F).setColor(red, green, blue, alpha).setUv(1.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
        consumer.addVertex(matrix, size, -size, 0.0F).setColor(red, green, blue, alpha).setUv(1.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
        consumer.addVertex(matrix, -size, -size, 0.0F).setColor(red, green, blue, alpha).setUv(0.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
    }

    private static void drawTexturedQuad(PoseStack poseStack, VertexConsumer consumer, int light, int alpha) {
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        consumer.addVertex(matrix, -0.5F, 0.5F, 0.0F).setColor(255, 255, 255, alpha).setUv(0.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
        consumer.addVertex(matrix, 0.5F, 0.5F, 0.0F).setColor(255, 255, 255, alpha).setUv(1.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
        consumer.addVertex(matrix, 0.5F, -0.5F, 0.0F).setColor(255, 255, 255, alpha).setUv(1.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
        consumer.addVertex(matrix, -0.5F, -0.5F, 0.0F).setColor(255, 255, 255, alpha).setUv(0.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
    }

    private static float pseudo(int seed, int index, int salt) {
        int value = seed ^ (index * 73428767) ^ (salt * 912931);
        value ^= value >>> 16;
        value *= 0x7feb352d;
        value ^= value >>> 15;
        return (value & 0xFFFF) / 65535.0F;
    }

    private record PoseKeyframe(float timeSeconds, float bodyXRot, float headXRot,
                                float rightArmXRot, float rightArmYRot, float rightArmZRot,
                                float leftArmXRot, float leftArmYRot, float leftArmZRot,
                                float rightLegXRot, float leftLegXRot, float rightLegZRot, float leftLegZRot,
                                float rightLegYRot, float leftLegYRot) {
        private static PoseKeyframe lerp(PoseKeyframe from, PoseKeyframe to, float t) {
            return new PoseKeyframe(
                Mth.lerp(t, from.timeSeconds, to.timeSeconds),
                Mth.lerp(t, from.bodyXRot, to.bodyXRot),
                Mth.lerp(t, from.headXRot, to.headXRot),
                Mth.lerp(t, from.rightArmXRot, to.rightArmXRot),
                Mth.lerp(t, from.rightArmYRot, to.rightArmYRot),
                Mth.lerp(t, from.rightArmZRot, to.rightArmZRot),
                Mth.lerp(t, from.leftArmXRot, to.leftArmXRot),
                Mth.lerp(t, from.leftArmYRot, to.leftArmYRot),
                Mth.lerp(t, from.leftArmZRot, to.leftArmZRot),
                Mth.lerp(t, from.rightLegXRot, to.rightLegXRot),
                Mth.lerp(t, from.leftLegXRot, to.leftLegXRot),
                Mth.lerp(t, from.rightLegZRot, to.rightLegZRot),
                Mth.lerp(t, from.leftLegZRot, to.leftLegZRot),
                Mth.lerp(t, from.rightLegYRot, to.rightLegYRot),
                Mth.lerp(t, from.leftLegYRot, to.leftLegYRot)
            );
        }
    }

}
