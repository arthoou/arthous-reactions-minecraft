package com.arthou.arthousreactions.client;

import com.arthou.arthousreactions.reaction.ReactionType;
import com.arthou.arthousreactions.reaction.ClapTiming;
import net.minecraft.util.Mth;

public record ActiveReaction(ReactionType type, long startedAtMs, int burstSeed) {
    public boolean isExpired(long now) {
        return type != ReactionType.SCUBA && now - startedAtMs > lifetimeMs();
    }

    public float normalizedAge(long now) {
        if (type == ReactionType.SCUBA) {
            return 0.0F;
        }
        return Mth.clamp((now - startedAtMs) / (float) lifetimeMs(), 0.0F, 1.0F);
    }

    public float ageSeconds(long now) {
        return Math.max(0.0F, (now - startedAtMs) / 1000.0F);
    }

    long durationMs() { return lifetimeMs(); }

    private long lifetimeMs() {
        return switch (type) {
            case CHEER -> 2840L;
            case ANGRY -> 920L;
            case POUT -> 1720L;
            case EMOTE_HI -> 1750L;
            case EMOTE_SAD -> 2350L;
            case EMOTE_LAUGH -> 1450L;
            case EMOTE_CLAP -> ClapTiming.DURATION_MS;
            case EMOTE_FEAR -> 1250L;
            case EMOTE_WOAH -> 1150L;
            case EMOTE_EW -> 1050L;
            case EMOTE_ANGEL -> 2200L;
            case EMOTE_HEARTS -> 1850L;
            case EMOTE_67 -> 2400L;
            case EMOTE_VINE_BOOM -> 1200L;
            case SCUBA -> Long.MAX_VALUE;
        };
    }
}
