package com.arthou.arthousreactions.network;

import com.arthou.arthousreactions.client.ReactionClientBootstrap;
import com.arthou.arthousreactions.reaction.ReactionType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public final class ReactionNetworking {
    private ReactionNetworking() {
    }

    public static void registerServer() {
        PayloadTypeRegistry.playC2S().register(SelectReactionPayload.TYPE, SelectReactionPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(PlayReactionPayload.TYPE, PlayReactionPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(SelectReactionPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> handleServerSelection(player, payload));
        });
    }

    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(PlayReactionPayload.TYPE, (payload, context) ->
            context.client().execute(() -> ReactionClientBootstrap.playReaction(payload.playerId(), ReactionType.byIndex(payload.reactionIndex()), payload.burstSeed(), payload.byeSound()))
        );
    }

    public static void sendToServer(ReactionType reaction, boolean byeSound) {
        if (ClientPlayNetworking.canSend(SelectReactionPayload.TYPE)) {
            ClientPlayNetworking.send(new SelectReactionPayload(reaction.index(), byeSound));
        } else {
            ReactionClientBootstrap.playLocalReaction(reaction, byeSound);
        }
    }

    private static void handleServerSelection(ServerPlayer player, SelectReactionPayload payload) {
        ReactionType reaction = ReactionType.byIndex(payload.reactionIndex());
        if (reaction == null) {
            return;
        }

        PlayReactionPayload playPayload = new PlayReactionPayload(player.getUUID(), reaction.index(), player.getRandom().nextInt(), payload.byeSound());
        for (ServerPlayer trackingPlayer : PlayerLookup.tracking(player)) {
            ServerPlayNetworking.send(trackingPlayer, playPayload);
        }
        ServerPlayNetworking.send(player, playPayload);
    }
}
