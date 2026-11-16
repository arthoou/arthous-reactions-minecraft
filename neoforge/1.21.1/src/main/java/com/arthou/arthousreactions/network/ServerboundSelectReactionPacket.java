package com.arthou.arthousreactions.network;

import com.arthou.arthousreactions.ArthousReactions;
import com.arthou.arthousreactions.reaction.ReactionType;
import com.arthou.arthousreactions.sound.ReactionSoundEvents;
import com.arthou.arthousreactions.sound.ClapSoundScheduler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashSet;

public record ServerboundSelectReactionPacket(int reactionIndex, boolean byeSound, boolean showCat) implements CustomPacketPayload {
    public static final int CANCEL = -1;
    public static final Type<ServerboundSelectReactionPacket> TYPE = new Type<>(ArthousReactions.id("select_reaction"));
    public static final StreamCodec<FriendlyByteBuf, ServerboundSelectReactionPacket> STREAM_CODEC = StreamCodec.of(
        (buffer, packet) -> {
            buffer.writeVarInt(packet.reactionIndex);
            buffer.writeByte((packet.byeSound ? 1 : 0) | (packet.showCat && packet.reactionIndex == ReactionType.SCUBA.index() ? 2 : 0));
        },
        buffer -> {
            int index = buffer.readVarInt();
            int flags = buffer.readUnsignedByte();
            return new ServerboundSelectReactionPacket(index, (flags & 1) != 0,
                index == ReactionType.SCUBA.index() && (flags & 2) != 0);
        }
    );

    public ServerboundSelectReactionPacket(int reactionIndex, boolean byeSound) {
        this(reactionIndex, byeSound, false);
    }

    @Override
    public Type<ServerboundSelectReactionPacket> type() {
        return TYPE;
    }

    public static void handle(ServerboundSelectReactionPacket packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }

        if (packet.reactionIndex == CANCEL) {
            ClapSoundScheduler.cancel(player.getUUID());
            broadcast(player, new ClientboundPlayReactionPacket(player.getUUID(), CANCEL, 0));
            return;
        }

        ReactionType reaction = ReactionType.byIndex(packet.reactionIndex);
        if (reaction == null) {
            return;
        }
        ClapSoundScheduler.cancel(player.getUUID());

        int seed = player.getRandom().nextInt();
        if (reaction == ReactionType.SCUBA) {
            // Reuse the existing alternate-sound byte: right click enables music.
            seed = ScubaOptions.encode(seed, packet.byeSound, packet.showCat);
        }
        ClientboundPlayReactionPacket playPacket = new ClientboundPlayReactionPacket(player.getUUID(), reaction.index(), seed);
        broadcast(player, playPacket);
        // Scuba music follows the client animation, with its exact pitch and loop.
        if (reaction == ReactionType.EMOTE_CLAP) {
            ClapSoundScheduler.schedule(player);
        } else if (reaction != ReactionType.SCUBA) {
            player.level().playSound(null, player.getX(), player.getY() + player.getBbHeight() * 0.7D, player.getZ(), packet.byeSound && reaction == ReactionType.EMOTE_HI ? ReactionSoundEvents.bye() : ReactionSoundEvents.forReaction(reaction), SoundSource.PLAYERS, ReactionSoundEvents.volumeForReaction(reaction), 0.96F + player.level().random.nextFloat() * 0.08F);
        }
    }

    private static void broadcast(ServerPlayer player, ClientboundPlayReactionPacket playPacket) {
        // Optional channels allow clients without this mod. Keep the original
        // tracking audience, but send custom packets only to subscribed clients.
        var viewers = new HashSet<>(player.serverLevel().getChunkSource().chunkMap.getPlayersWatching(player));
        viewers.add(player);
        for (ServerPlayer viewer : viewers) {
            if (viewer.connection.hasChannel(ClientboundPlayReactionPacket.TYPE)) {
                PacketDistributor.sendToPlayer(viewer, playPacket);
            }
        }
    }
}
