package com.arthou.arthousreactions.client;

import com.arthou.arthousreactions.reaction.ReactionType;
import net.minecraft.client.model.geom.ModelPart;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TorsoStabilityTest {
    @Test void laughIsTheOnlyReactionAllowedToAnimateTheTorso() {
        for (ReactionType type : ReactionType.values()) {
            assertEquals(type == ReactionType.EMOTE_LAUGH, TorsoStability.allowsMotion(type), type.toString());
        }
        assertFalse(TorsoStability.allowsMotion(null));
    }

    @Test void allNonLaughReactionsNeutralizeBodyAndSynchronizeTheJacket() {
        var cleared = new ArrayList<ModelPart>();
        var stability = new TorsoStability(cleared::add);
        var body = part(); var jacket = part();
        for (ReactionType reaction : ReactionType.values()) {
            if (reaction == ReactionType.EMOTE_LAUGH) continue;
            stability.prepare(body, jacket, reaction);
            contaminate(body);
            contaminate(jacket);
            cleared.clear();
            stability.finish(body, jacket, reaction);
            assertNeutral(body);
            assertNeutral(jacket);
            assertTrue(cleared.contains(body));
            assertTrue(cleared.contains(jacket));
        }
    }

    @Test void switchingFromLaughToAnotherPlayerOrPreviewCannotRetainTorsoState() {
        var cleared = new ArrayList<ModelPart>();
        var stability = new TorsoStability(cleared::add);
        var body = part(); var jacket = part();
        stability.prepare(body, jacket, ReactionType.EMOTE_LAUGH);
        contaminate(body);
        stability.finish(body, jacket, ReactionType.EMOTE_LAUGH);
        assertEquals(.3F, body.xRot);
        assertEquals(body.xRot, jacket.xRot);
        assertEquals(body.xScale, jacket.xScale);
        cleared.clear();
        // A renderer is shared by world players and the detached GUI preview.
        stability.prepare(body, jacket, ReactionType.EMOTE_ANGEL);
        assertNeutral(body); assertNeutral(jacket);
        assertTrue(cleared.contains(body)); assertTrue(cleared.contains(jacket));
        contaminate(body);
        stability.finish(body, jacket, ReactionType.EMOTE_ANGEL);
        assertNeutral(body); assertNeutral(jacket);
    }

    @Test void leavingAnEmoteCleansItsStateBeforeVanillaAndThenPreservesNormalCrouching() {
        var stability = new TorsoStability(part -> { });
        var body = part(); var jacket = part();
        stability.prepare(body, jacket, ReactionType.EMOTE_LAUGH);
        contaminate(body);
        stability.prepare(body, jacket, null);
        assertNeutral(body);
        body.xRot = .5F; body.y = 3.2F; // Ordinary vanilla crouching is applied next.
        jacket.copyFrom(body);
        stability.finish(body, jacket, null);
        assertEquals(.5F, body.xRot); assertEquals(3.2F, body.y);
        stability.prepare(body, jacket, null);
        assertEquals(.5F, body.xRot, "No continuous reset outside this mod's emotes");
        assertEquals(3.2F, body.y);
    }

    private static ModelPart part() { return new ModelPart(List.of(), Map.of()); }

    private static void contaminate(ModelPart part) {
        part.x = 1; part.y = 2; part.z = 3;
        part.xRot = .3F; part.yRot = -.2F; part.zRot = .1F;
        part.xScale = 1.2F; part.yScale = .8F; part.zScale = 1.1F;
    }

    private static void assertNeutral(ModelPart part) {
        assertEquals(0, part.x); assertEquals(0, part.y); assertEquals(0, part.z);
        assertEquals(0, part.xRot); assertEquals(0, part.yRot); assertEquals(0, part.zRot);
        assertEquals(1, part.xScale); assertEquals(1, part.yScale); assertEquals(1, part.zScale);
    }
}
