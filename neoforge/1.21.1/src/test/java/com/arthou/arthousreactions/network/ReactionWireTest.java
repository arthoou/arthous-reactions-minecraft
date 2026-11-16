package com.arthou.arthousreactions.network;

import com.arthou.arthousreactions.reaction.ReactionType;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

import java.util.HexFormat;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ReactionWireTest {
    private static final UUID PLAYER = UUID.fromString("00112233-4455-6677-8899-aabbccddeeff");

    @Test void currentPaperScubaAndCancellationGoldenPacketsUseTheRealClientCodec() {
        String[] packets = {"00112233445566778899aabbccddeeff0ed2345678",
            "00112233445566778899aabbccddeeffffffffff0f00000000"};
        for (String hex : packets) {
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.wrappedBuffer(HexFormat.of().parseHex(hex)));
            try {
                var packet = ClientboundPlayReactionPacket.STREAM_CODEC.decode(buffer);
                assertEquals(PLAYER, packet.playerId());
                if (packet.reactionIndex() == ReactionType.SCUBA.index()) {
                    assertTrue(ScubaOptions.catEnabled(packet.burstSeed()));
                    assertTrue(ScubaOptions.musicEnabled(packet.burstSeed()));
                    assertEquals(0xd2345678, packet.burstSeed());
                } else {
                    assertEquals(ServerboundSelectReactionPacket.CANCEL, packet.reactionIndex());
                    assertEquals(0, packet.burstSeed());
                }
                assertFalse(buffer.isReadable());
            } finally { buffer.release(); }
        }
    }

    @Test void paperSelectRequestHasOnlyIndexAndByeFlag() {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            var packet = new ServerboundSelectReactionPacket(ReactionType.EMOTE_HI.index(), true);
            ServerboundSelectReactionPacket.STREAM_CODEC.encode(buffer, packet);
            assertEquals(2, buffer.readableBytes());
            assertEquals(3, buffer.getUnsignedByte(0));
            assertEquals(1, buffer.getUnsignedByte(1));
            assertEquals(packet, ServerboundSelectReactionPacket.STREAM_CODEC.decode(buffer));
            assertFalse(buffer.isReadable());
        } finally { buffer.release(); }
    }

    @Test void clientReadsPaperGoldenPacketAndExistingNeoForgeServerPacket() {
        byte[] wire = HexFormat.of().parseHex("00112233445566778899aabbccddeeff0d89abcdef");
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.wrappedBuffer(wire));
        try {
            var packet = ClientboundPlayReactionPacket.STREAM_CODEC.decode(buffer);
            assertEquals(PLAYER, packet.playerId());
            assertEquals(ReactionType.EMOTE_VINE_BOOM.index(), packet.reactionIndex());
            assertEquals(0x89abcdef, packet.burstSeed());
            assertFalse(buffer.isReadable(), "Paper must not append the newer client's bye flag");
        } finally { buffer.release(); }
    }

    @Test void serverPlayEncoderPreservesTheOriginalTwentyOneByteFormat() {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            ClientboundPlayReactionPacket.STREAM_CODEC.encode(buffer,
                    new ClientboundPlayReactionPacket(PLAYER, 13, 0x89abcdef));
            byte[] wire = new byte[buffer.readableBytes()];
            buffer.readBytes(wire);
            assertEquals("00112233445566778899aabbccddeeff0d89abcdef", HexFormat.of().formatHex(wire));
        } finally { buffer.release(); }
    }

    @Test void everySelectableReactionKeepsItsPublishedIndex() {
        String[] names = {"CHEER", "POUT", "ANGRY", "EMOTE_HI", "EMOTE_CLAP", "EMOTE_WOAH",
                "EMOTE_LAUGH", "EMOTE_SAD", "EMOTE_FEAR", "EMOTE_EW", "EMOTE_HEARTS", "EMOTE_ANGEL",
                "EMOTE_67", "EMOTE_VINE_BOOM", "SCUBA"};
        assertEquals(names.length, ReactionType.values().length);
        for (int index = 0; index < names.length; index++) {
            assertEquals(names[index], ReactionType.byIndex(index).name());
            assertEquals(index, ReactionType.valueOf(names[index]).index());
        }
        assertNull(ReactionType.byIndex(-1));
        assertNull(ReactionType.byIndex(names.length));
    }

    @Test void scubaUsesNewIndexWithoutChangingTheWireFormat() {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            var packet = new ServerboundSelectReactionPacket(ReactionType.SCUBA.index(), false);
            ServerboundSelectReactionPacket.STREAM_CODEC.encode(buffer, packet);
            assertEquals(14, buffer.readUnsignedByte());
            assertFalse(buffer.readBoolean());
            assertFalse(buffer.isReadable());
        } finally { buffer.release(); }
    }

    @Test void scubaClickModesAndCatModifierUseTheExistingSelectionByte() {
        for (boolean music : new boolean[]{false, true}) {
            for (boolean cat : new boolean[]{false, true}) {
                FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
                try {
                    var request = new ServerboundSelectReactionPacket(ReactionType.SCUBA.index(), music, cat);
                    ServerboundSelectReactionPacket.STREAM_CODEC.encode(buffer, request);
                    assertEquals(2, buffer.readableBytes());
                    assertEquals(14, buffer.getUnsignedByte(0));
                    assertEquals((music ? 1 : 0) | (cat ? 2 : 0), buffer.getUnsignedByte(1));
                    assertEquals(request, ServerboundSelectReactionPacket.STREAM_CODEC.decode(buffer));
                } finally { buffer.release(); }
            }
        }
    }

    @Test void bothScubaOptionsReachEveryViewerWithoutExtendingThePlaybackPacket() {
        for (int seed : new int[]{0, -1, Integer.MIN_VALUE, Integer.MAX_VALUE, 0x12345678}) {
            for (boolean music : new boolean[]{false, true}) {
                for (boolean cat : new boolean[]{false, true}) {
                    FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
                    try {
                        int encoded = ScubaOptions.encode(seed, music, cat);
                        ClientboundPlayReactionPacket.STREAM_CODEC.encode(buffer,
                            new ClientboundPlayReactionPacket(PLAYER, ReactionType.SCUBA.index(), encoded));
                        assertEquals(21, buffer.readableBytes());
                        var received = ClientboundPlayReactionPacket.STREAM_CODEC.decode(buffer);
                        assertEquals(music, ScubaOptions.musicEnabled(received.burstSeed()));
                        assertEquals(cat, ScubaOptions.catEnabled(received.burstSeed()));
                        assertEquals(seed & 0x3fffffff, received.burstSeed() & 0x3fffffff);
                        assertFalse(buffer.isReadable());
                    } finally { buffer.release(); }
                }
            }
        }
    }

    @Test void ownerCancellationHasAnUnambiguousRoundTrip() {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            var request = new ServerboundSelectReactionPacket(ServerboundSelectReactionPacket.CANCEL, false);
            ServerboundSelectReactionPacket.STREAM_CODEC.encode(buffer, request);
            assertEquals(request, ServerboundSelectReactionPacket.STREAM_CODEC.decode(buffer));
            assertFalse(buffer.isReadable());
            var reply = new ClientboundPlayReactionPacket(PLAYER, ServerboundSelectReactionPacket.CANCEL, 0);
            ClientboundPlayReactionPacket.STREAM_CODEC.encode(buffer, reply);
            assertEquals(reply, ClientboundPlayReactionPacket.STREAM_CODEC.decode(buffer));
            assertFalse(buffer.isReadable());
        } finally { buffer.release(); }
    }
}
