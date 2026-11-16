package com.arthou.arthousreactions.sound;

import com.arthou.arthousreactions.reaction.ClapTiming;
import com.arthou.arthousreactions.reaction.ReactionType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Keeps the original world sound audible to all clients, after Clap's preparation. */
public final class ClapSoundScheduler {
    private static final Map<UUID, Integer> PENDING = new HashMap<>();

    private ClapSoundScheduler() { }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(ClapSoundScheduler::tick);
        NeoForge.EVENT_BUS.addListener(ClapSoundScheduler::stopped);
    }

    public static void schedule(ServerPlayer player) {
        PENDING.put(player.getUUID(), player.server.getTickCount() + ClapTiming.PREPARATION_TICKS);
    }

    public static void cancel(UUID player) { PENDING.remove(player); }

    private static void tick(ServerTickEvent.Post event) {
        var iterator = PENDING.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            if (event.getServer().getTickCount() < entry.getValue()) continue;
            iterator.remove();
            ServerPlayer player = event.getServer().getPlayerList().getPlayer(entry.getKey());
            if (player == null || !player.isAlive() || player.isShiftKeyDown()) continue;
            player.level().playSound(null, player.getX(), player.getY() + player.getBbHeight() * 0.7D,
                player.getZ(), ReactionSoundEvents.EMOTE_CLAP.get(), SoundSource.PLAYERS,
                ReactionSoundEvents.volumeForReaction(ReactionType.EMOTE_CLAP), 1.0F);
        }
    }

    private static void stopped(ServerStoppedEvent event) { PENDING.clear(); }
}
