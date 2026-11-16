package com.arthou.arthousreactions.client;

import com.arthou.arthousreactions.reaction.ReactionType;
import dev.kosmx.playerAnim.impl.animation.IBendHelper;
import net.minecraft.client.model.geom.ModelPart;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Consumer;

/** Torso motion belongs only to Laugh; renderer reuse must not retain its pose. */
final class TorsoStability {
    private final Set<ModelPart> owned = Collections.newSetFromMap(new WeakHashMap<>());
    private final Consumer<ModelPart> clearBend;

    TorsoStability() { this(part -> IBendHelper.INSTANCE.bend(part, null)); }

    TorsoStability(Consumer<ModelPart> clearBend) { this.clearBend = clearBend; }

    static boolean allowsMotion(ReactionType reaction) { return reaction == ReactionType.EMOTE_LAUGH; }

    void prepare(ModelPart body, ModelPart jacket, ReactionType reaction) {
        boolean previouslyOwned = owned.remove(body);
        if (reaction == null && !previouslyOwned) return;
        reset(body);
        reset(jacket);
        if (reaction != null) owned.add(body);
    }

    void finish(ModelPart body, ModelPart jacket, ReactionType reaction) {
        if (reaction == null) return; // Vanilla crouching and other ordinary poses remain authoritative.
        if (!allowsMotion(reaction)) reset(body);
        clearBend.accept(jacket);
        jacket.copyFrom(body);
    }

    private void reset(ModelPart part) {
        part.resetPose();
        clearBend.accept(part);
    }
}
