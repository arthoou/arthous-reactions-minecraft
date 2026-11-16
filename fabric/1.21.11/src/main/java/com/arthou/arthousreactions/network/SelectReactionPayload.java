package com.arthou.arthousreactions.network;

import com.arthou.arthousreactions.ArthousReactions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SelectReactionPayload(int reactionIndex, boolean byeSound) implements CustomPacketPayload {
    public static final Type<SelectReactionPayload> TYPE = new Type<>(ArthousReactions.id("select_reaction"));
    public static final StreamCodec<FriendlyByteBuf, SelectReactionPayload> CODEC = StreamCodec.of(
        (buffer, payload) -> {
            buffer.writeVarInt(payload.reactionIndex);
            buffer.writeBoolean(payload.byeSound);
        },
        buffer -> new SelectReactionPayload(buffer.readVarInt(), buffer.readBoolean())
    );

    @Override
    public Type<SelectReactionPayload> type() {
        return TYPE;
    }
}
