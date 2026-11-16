package com.arthou.arthousreactions.client;

import com.arthou.arthousreactions.ArthousReactions;
import com.arthou.arthousreactions.network.ScubaOptions;
import com.arthou.arthousreactions.reaction.ReactionType;
import com.arthou.arthousreactions.sound.ReactionSoundEvents;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.core.data.AnimationBinary;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Player Animator owns these reactions so arm poses can also bend at the elbow. */
public final class ReactionAnimations {
    private static final ResourceLocation LAYER = ArthousReactions.id("reactions");
    private static final KeyframeAnimation DATA = load();
    private static final Map<UUID, Playback> PLAYBACKS = new HashMap<>();

    private ReactionAnimations() {
    }

    public static void register() {
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(LAYER, 1000,
            player -> new ModifierLayer<KeyframeAnimationPlayer>());
    }

    public static KeyframeAnimation data() {
        return DATA;
    }

    public static boolean handles(ReactionType reaction) {
        return reaction == ReactionType.SCUBA || reaction == ReactionType.EMOTE_SAD || reaction == ReactionType.EMOTE_LAUGH
            || reaction == ReactionType.EMOTE_HEARTS || reaction == ReactionType.EMOTE_ANGEL
            || reaction == ReactionType.EMOTE_CLAP || reaction == ReactionType.EMOTE_FEAR
            || reaction == ReactionType.EMOTE_WOAH || reaction == ReactionType.EMOTE_67;
    }

    static KeyframeAnimationPlayer configurePreview(AbstractClientPlayer player, ReactionType reaction) {
        return configurePreview(player, reaction, 0L);
    }

    static KeyframeAnimationPlayer configurePreview(AbstractClientPlayer player, ReactionType reaction, long tick) {
        ModifierLayer<KeyframeAnimationPlayer> layer = layer(player);
        if (layer == null) return null;
        KeyframeAnimationPlayer animation = previewAnimation(reaction, tick);
        layer.setAnimation(animation);
        return animation; // No sound, network message or live PLAYBACKS entry.
    }

    static KeyframeAnimationPlayer previewAnimation(ReactionType reaction, long elapsedTicks) {
        if (!handles(reaction)) return null;
        KeyframeAnimation data = reaction == ReactionType.SCUBA ? DATA : BentArmGestures.data(reaction);
        long tick = Math.max(0L, elapsedTicks);
        KeyframeAnimationPlayer animation;
        if (data.isInfinite() && tick > data.endTick) {
            // Enter the loop normally, then seek within it. Work stays bounded after a long pause.
            animation = new ReactionAnimationPlayer(data, data.endTick, reaction);
            animation.tick();
            long remaining = (tick - data.endTick - 1L) % (data.endTick - data.returnToTick + 1L);
            for (long index = 0; index < remaining; index++) animation.tick();
        } else {
            animation = new ReactionAnimationPlayer(data, (int) Math.min(tick, data.stopTick), reaction);
            if (!data.isInfinite() && tick > data.stopTick) animation.tick();
        }
        return animation;
    }

    public static void play(AbstractClientPlayer player, ReactionType reaction, int seed) {
        stop(player.getUUID());
        ModifierLayer<KeyframeAnimationPlayer> layer = layer(player);
        if (layer == null) {
            return;
        }
        KeyframeAnimation data = reaction == ReactionType.SCUBA ? DATA : BentArmGestures.data(reaction);
        layer.setAnimation(new ReactionAnimationPlayer(data, 0, reaction));
        LoopingScubaSound sound = reaction == ReactionType.SCUBA && ScubaOptions.musicEnabled(seed)
            ? new LoopingScubaSound(player, seed) : null;
        PLAYBACKS.put(player.getUUID(), new Playback(layer, sound));
        if (sound != null) {
            Minecraft.getInstance().getSoundManager().play(sound);
        }
    }

    public static void stop(UUID playerId) {
        Playback playback = PLAYBACKS.remove(playerId);
        if (playback != null) {
            playback.layer().setAnimation(null);
            if (playback.sound() != null) {
                playback.sound().finish();
                Minecraft.getInstance().getSoundManager().stop(playback.sound());
            }
        }
    }

    public static void stopAll() {
        for (UUID playerId : PLAYBACKS.keySet().toArray(UUID[]::new)) {
            stop(playerId);
        }
    }

    @SuppressWarnings("unchecked")
    private static ModifierLayer<KeyframeAnimationPlayer> layer(AbstractClientPlayer player) {
        return (ModifierLayer<KeyframeAnimationPlayer>) PlayerAnimationAccess.getPlayerAssociatedData(player).get(LAYER);
    }

    private static KeyframeAnimation load() {
        try (InputStream input = ReactionAnimations.class.getResourceAsStream(
                "/assets/arthousreactions/animations/scuba.emotecraft")) {
            if (input == null) {
                throw new IOException("Missing scuba animation");
            }
            return read(input);
        } catch (IOException exception) {
            throw new UncheckedIOException("Cannot read scuba animation", exception);
        }
    }

    // Emotecraft container v8: each module has its own ID, version and length.
    // Only module 0 is animation data; embedded icons and descriptions are data.
    static KeyframeAnimation read(InputStream input) throws IOException {
        DataInputStream stream = new DataInputStream(input);
        if (stream.readInt() != 8) {
            throw new IOException("Unsupported Emotecraft container version");
        }
        stream.readUnsignedByte(); // File purpose.
        int modules = stream.readUnsignedByte();
        for (int index = 0; index < modules; index++) {
            int id = stream.readUnsignedByte();
            int version = stream.readUnsignedByte();
            int length = stream.readInt();
            if (length < 4 || length > 1_048_576) {
                throw new IOException("Invalid Emotecraft module length");
            }
            byte[] bytes = stream.readNBytes(length);
            if (bytes.length != length) {
                throw new IOException("Truncated Emotecraft module");
            }
            if (id == 0) {
                ByteBuffer buffer = ByteBuffer.wrap(bytes);
                buffer.getInt(); // Starting playback tick stored by Emotecraft.
                return AnimationBinary.read(buffer, version);
            }
        }
        throw new IOException("Emotecraft file has no animation module");
    }

    private static final class LoopingScubaSound extends EntityBoundSoundInstance {
        private LoopingScubaSound(AbstractClientPlayer player, int seed) {
            super(ReactionSoundEvents.SCUBA.get(), SoundSource.PLAYERS, 0.85F, 1.0F, player, seed);
            this.looping = true;
            this.delay = 0;
        }

        private void finish() {
            stop(); // Also clears looping, so stopping cannot schedule another pass.
        }
    }

    private record Playback(ModifierLayer<KeyframeAnimationPlayer> layer, LoopingScubaSound sound) {
    }
}
