package com.arthou.arthousreactions.network;

import com.arthou.arthousreactions.ArthousReactions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.UUID;

public record PlayReactionPayload(UUID playerId, int reactionIndex, int burstSeed, boolean byeSound) implements CustomPacketPayload {
    public static final Type<PlayReactionPayload> TYPE = new Type<>(ArthousReactions.id("play_reaction"));
    public static final StreamCodec<FriendlyByteBuf, PlayReactionPayload> CODEC = StreamCodec.of(
        (buffer, payload) -> {
            buffer.writeUUID(payload.playerId);
            buffer.writeVarInt(payload.reactionIndex);
            buffer.writeInt(payload.burstSeed);
            buffer.writeBoolean(payload.byeSound);
        },
        buffer -> new PlayReactionPayload(buffer.readUUID(), buffer.readVarInt(), buffer.readInt(), buffer.readBoolean())
    );

    @Override
    public Type<PlayReactionPayload> type() {
        return TYPE;
    }
}
