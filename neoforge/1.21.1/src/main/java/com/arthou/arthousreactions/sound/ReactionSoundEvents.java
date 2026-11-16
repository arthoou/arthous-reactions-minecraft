package com.arthou.arthousreactions.sound;

import com.arthou.arthousreactions.ArthousReactions;
import com.arthou.arthousreactions.reaction.ReactionType;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ReactionSoundEvents {
    private static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, ArthousReactions.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> REACTION_POP = SOUND_EVENTS.register("reaction_pop", () -> SoundEvent.createVariableRangeEvent(ArthousReactions.id("reaction_pop")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CHEER_POP = SOUND_EVENTS.register("cheer_pop", () -> SoundEvent.createVariableRangeEvent(ArthousReactions.id("cheer_pop")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ANGRY_REACTION = SOUND_EVENTS.register("angry_reaction", () -> SoundEvent.createVariableRangeEvent(ArthousReactions.id("angry_reaction")));
    public static final DeferredHolder<SoundEvent, SoundEvent> POUT_STOMP = SOUND_EVENTS.register("pout_stomp", () -> SoundEvent.createVariableRangeEvent(ArthousReactions.id("pout_stomp")));
    public static final DeferredHolder<SoundEvent, SoundEvent> EMOTE_HI = registerSound("emote_hi");
    public static final DeferredHolder<SoundEvent, SoundEvent> EMOTE_SAD = registerSound("emote_sad");
    public static final DeferredHolder<SoundEvent, SoundEvent> EMOTE_LAUGH = registerSound("emote_laugh");
    public static final DeferredHolder<SoundEvent, SoundEvent> EMOTE_FEAR = registerSound("emote_fear");
    public static final DeferredHolder<SoundEvent, SoundEvent> EMOTE_WOAH = registerSound("emote_woah");
    public static final DeferredHolder<SoundEvent, SoundEvent> EMOTE_EW = registerSound("emote_ew");
    public static final DeferredHolder<SoundEvent, SoundEvent> EMOTE_CLAP = registerSound("emote_clap");
    public static final DeferredHolder<SoundEvent, SoundEvent> EMOTE_67 = registerSound("emote_67");
    public static final DeferredHolder<SoundEvent, SoundEvent> EMOTE_BYE = registerSound("emote_bye");
    public static final DeferredHolder<SoundEvent, SoundEvent> EMOTE_VINE_BOOM = registerSound("emote_vine_boom");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCUBA = registerSound("scuba");

    private ReactionSoundEvents() {
    }

    public static void register(IEventBus modBus) {
        SOUND_EVENTS.register(modBus);
    }

    public static SoundEvent forReaction(ReactionType reaction) {
        if (reaction == ReactionType.CHEER) {
            return CHEER_POP.get();
        }
        if (reaction == ReactionType.ANGRY) {
            return ANGRY_REACTION.get();
        }
        if (reaction == ReactionType.POUT) {
            return POUT_STOMP.get();
        }
        return switch (reaction) {
            case EMOTE_HI -> EMOTE_HI.get();
            case EMOTE_SAD -> EMOTE_SAD.get();
            case EMOTE_LAUGH -> EMOTE_LAUGH.get();
            case EMOTE_FEAR -> EMOTE_FEAR.get();
            case EMOTE_WOAH -> EMOTE_WOAH.get();
            case EMOTE_EW -> EMOTE_EW.get();
            case EMOTE_CLAP -> EMOTE_CLAP.get();
            case EMOTE_67 -> EMOTE_67.get();
            case EMOTE_VINE_BOOM -> EMOTE_VINE_BOOM.get();
            case SCUBA -> SCUBA.get();
            default -> REACTION_POP.get();
        };
    }

    public static float volumeForReaction(ReactionType reaction) {
        return reaction == ReactionType.EMOTE_VINE_BOOM ? 0.38F : 0.85F;
    }

    public static SoundEvent bye() {
        return EMOTE_BYE.get();
    }

    private static DeferredHolder<SoundEvent, SoundEvent> registerSound(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(ArthousReactions.id(name)));
    }
}
