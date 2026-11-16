package com.arthou.arthousreactions.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ReactionNetwork {
    private static final String PROTOCOL = "3";

    private ReactionNetwork() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        // Paper advertises these play channels through minecraft:register rather
        // than the NeoForge handshake. Keep the protocol for existing mod servers.
        PayloadRegistrar registrar = event.registrar(PROTOCOL).optional();
        registrar.playToServer(ServerboundSelectReactionPacket.TYPE, ServerboundSelectReactionPacket.STREAM_CODEC, ServerboundSelectReactionPacket::handle);
        registrar.playToClient(ClientboundPlayReactionPacket.TYPE, ClientboundPlayReactionPacket.STREAM_CODEC, ClientboundPlayReactionPacket::handle);
    }
}
