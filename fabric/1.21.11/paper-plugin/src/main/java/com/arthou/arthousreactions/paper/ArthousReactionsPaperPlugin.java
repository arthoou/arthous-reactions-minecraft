package com.arthou.arthousreactions.paper;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.UUID;

public final class ArthousReactionsPaperPlugin extends JavaPlugin implements PluginMessageListener {
    private static final String SELECT_CHANNEL = "arthousreactions:select_reaction";
    private static final String PLAY_CHANNEL = "arthousreactions:play_reaction";
    private static final double BROADCAST_DISTANCE_SQUARED = 128.0D * 128.0D;

    private final Random random = new Random();

    @Override
    public void onEnable() {
        getServer().getMessenger().registerIncomingPluginChannel(this, SELECT_CHANNEL, this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, PLAY_CHANNEL);
        getLogger().info("Arthous Reactions Paper bridge enabled.");
    }

    @Override
    public void onDisable() {
        getServer().getMessenger().unregisterIncomingPluginChannel(this, SELECT_CHANNEL, this);
        getServer().getMessenger().unregisterOutgoingPluginChannel(this, PLAY_CHANNEL);
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!SELECT_CHANNEL.equals(channel)) {
            return;
        }

        SelectReaction selectReaction = readSelectReaction(message);
        if (selectReaction == null || selectReaction.reactionIndex < 0 || selectReaction.reactionIndex > 64) {
            return;
        }

        byte[] payload = writePlayReaction(player.getUniqueId(), selectReaction.reactionIndex, random.nextInt(), selectReaction.byeSound);
        broadcast(player, payload);
    }

    private void broadcast(Player source, byte[] payload) {
        World world = source.getWorld();
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (target.getWorld() != world) {
                continue;
            }

            if (target.getLocation().distanceSquared(source.getLocation()) <= BROADCAST_DISTANCE_SQUARED) {
                target.sendPluginMessage(this, PLAY_CHANNEL, payload);
            }
        }
    }

    private static SelectReaction readSelectReaction(byte[] message) {
        try {
            Cursor cursor = new Cursor(message);
            int reactionIndex = cursor.readVarInt();
            boolean byeSound = cursor.readBoolean();
            return new SelectReaction(reactionIndex, byeSound);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private static byte[] writePlayReaction(UUID playerId, int reactionIndex, int burstSeed, boolean byeSound) {
        ByteArrayOutputStream output = new ByteArrayOutputStream(32);
        byte[] uuid = ByteBuffer.allocate(16).putLong(playerId.getMostSignificantBits()).putLong(playerId.getLeastSignificantBits()).array();
        output.writeBytes(uuid);
        writeVarInt(output, reactionIndex);
        output.write((burstSeed >>> 24) & 255);
        output.write((burstSeed >>> 16) & 255);
        output.write((burstSeed >>> 8) & 255);
        output.write(burstSeed & 255);
        output.write(byeSound ? 1 : 0);
        return output.toByteArray();
    }

    private static void writeVarInt(ByteArrayOutputStream output, int value) {
        while ((value & -128) != 0) {
            output.write(value & 127 | 128);
            value >>>= 7;
        }
        output.write(value);
    }

    private record SelectReaction(int reactionIndex, boolean byeSound) {
    }

    private static final class Cursor {
        private final byte[] bytes;
        private int index;

        private Cursor(byte[] bytes) {
            this.bytes = bytes;
        }

        private int readVarInt() {
            int value = 0;
            int position = 0;
            byte currentByte;
            do {
                if (position >= 32 || index >= bytes.length) {
                    throw new IllegalArgumentException("Invalid VarInt");
                }
                currentByte = bytes[index++];
                value |= (currentByte & 127) << position;
                position += 7;
            } while ((currentByte & 128) == 128);
            return value;
        }

        private boolean readBoolean() {
            if (index >= bytes.length) {
                throw new IllegalArgumentException("Missing boolean");
            }
            return bytes[index++] != 0;
        }
    }
}
