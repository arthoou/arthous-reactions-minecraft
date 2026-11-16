package com.arthou.arthousreactions.client;

import com.arthou.arthousreactions.network.ScubaOptions;
import com.arthou.arthousreactions.reaction.ReactionType;

/** Independent, silent preview clock; finite gestures return to rest before repeating. */
final class PreviewTimeline {
    private ActiveReaction reaction;

    void reset() { reaction = null; }

    boolean update(ReactionType selected, boolean cat, long now) {
        boolean restart = reaction == null || reaction.type() != selected
            || (selected != ReactionType.SCUBA && now - reaction.startedAtMs() > reaction.durationMs() + 300L);
        int seed = selected == ReactionType.SCUBA ? ScubaOptions.encode(0, false, cat) : 0;
        if (restart) reaction = new ActiveReaction(selected, now, seed);
        else if (reaction.burstSeed() != seed) reaction = new ActiveReaction(selected, reaction.startedAtMs(), seed);
        return restart;
    }

    ActiveReaction visibleReaction(long now) {
        return reaction != null && !reaction.isExpired(now) ? reaction : null;
    }

    long tick(long now) {
        return Math.max(0L, now - reaction.startedAtMs()) / 50L;
    }

    float fraction(long now) {
        return Math.max(0L, now - reaction.startedAtMs()) % 50L / 50.0F;
    }
}
