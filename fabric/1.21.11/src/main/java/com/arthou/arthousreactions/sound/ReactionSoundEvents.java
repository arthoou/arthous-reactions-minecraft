package com.arthou.arthousreactions.sound;

import com.arthou.arthousreactions.ArthousReactions;
import com.arthou.arthousreactions.reaction.ReactionType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public final class ReactionSoundEvents {
    public static final SoundEvent REACTION_POP = sound("reaction_pop");
    public static final SoundEvent CHEER_POP = sound("cheer_pop");
    public static final SoundEvent ANGRY_ROBLOX = sound("angry_roblox");
    public static final SoundEvent POUT_STOMP = sound("pout_stomp");
    public static final SoundEvent PIZZA_HI = sound("pizza_hi");
    public static final SoundEvent PIZZA_SAD = sound("pizza_sad");
    public static final SoundEvent PIZZA_LAUGH = sound("pizza_laugh");
    public static final SoundEvent PIZZA_FEAR = sound("pizza_fear");
    public static final SoundEvent PIZZA_WOAH = sound("pizza_woah");
    public static final SoundEvent PIZZA_EW = sound("pizza_ew");
    public static final SoundEvent PIZZA_CLAP = sound("pizza_clap");
    public static final SoundEvent PIZZA_67 = sound("pizza_67");
    public static final SoundEvent PIZZA_BYE = sound("pizza_bye");
    public static final SoundEvent PIZZA_VINE_BOOM = sound("pizza_vine_boom");

    private ReactionSoundEvents() {
    }

    public static void register() {
        register("reaction_pop", REACTION_POP);
        register("cheer_pop", CHEER_POP);
        register("angry_roblox", ANGRY_ROBLOX);
        register("pout_stomp", POUT_STOMP);
        register("pizza_hi", PIZZA_HI);
        register("pizza_sad", PIZZA_SAD);
        register("pizza_laugh", PIZZA_LAUGH);
        register("pizza_fear", PIZZA_FEAR);
        register("pizza_woah", PIZZA_WOAH);
        register("pizza_ew", PIZZA_EW);
        register("pizza_clap", PIZZA_CLAP);
        register("pizza_67", PIZZA_67);
        register("pizza_bye", PIZZA_BYE);
        register("pizza_vine_boom", PIZZA_VINE_BOOM);
    }

    public static SoundEvent forReaction(ReactionType reaction) {
        if (reaction == ReactionType.CHEER) {
            return CHEER_POP;
        }
        if (reaction == ReactionType.ANGRY) {
            return ANGRY_ROBLOX;
        }
        if (reaction == ReactionType.POUT) {
            return POUT_STOMP;
        }
        return switch (reaction) {
            case PIZZA_HI -> PIZZA_HI;
            case PIZZA_SAD -> PIZZA_SAD;
            case PIZZA_LAUGH -> PIZZA_LAUGH;
            case PIZZA_FEAR -> PIZZA_FEAR;
            case PIZZA_WOAH -> PIZZA_WOAH;
            case PIZZA_EW -> PIZZA_EW;
            case PIZZA_CLAP -> PIZZA_CLAP;
            case PIZZA_67 -> PIZZA_67;
            case PIZZA_VINE_BOOM -> PIZZA_VINE_BOOM;
            default -> REACTION_POP;
        };
    }

    public static float volumeForReaction(ReactionType reaction) {
        if (reaction == ReactionType.PIZZA_67) {
            return 0.45F;
        }
        return reaction == ReactionType.PIZZA_VINE_BOOM ? 0.38F : 0.85F;
    }

    public static SoundEvent bye() {
        return PIZZA_BYE;
    }

    private static SoundEvent sound(String name) {
        Identifier id = ArthousReactions.id(name);
        return SoundEvent.createVariableRangeEvent(id);
    }

    private static void register(String name, SoundEvent soundEvent) {
        Registry.register(BuiltInRegistries.SOUND_EVENT, ArthousReactions.id(name), soundEvent);
    }
}
