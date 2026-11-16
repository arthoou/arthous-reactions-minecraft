package com.arthou.arthousreactions.client;

import com.arthou.arthousreactions.reaction.ReactionType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PreviewScopeTest {
    @Test void renderingIsRestrictedToTheDetachedEntityEvenForEqualObjects() {
        var scope = new PreviewScope<String>();
        var detached = new String("same profile");
        var worldPlayer = new String("same profile");
        var reaction = new ActiveReaction(ReactionType.SCUBA, 1L, 0);
        scope.render(detached, reaction, () -> {
            assertTrue(scope.contains(detached));
            assertFalse(scope.contains(worldPlayer));
            assertSame(reaction, scope.reaction());
        });
        assertFalse(scope.contains(detached));
        assertNull(scope.reaction());
    }

    @Test void nestedRenderAndExceptionsRestoreThePreviousScope() {
        var scope = new PreviewScope<Object>();
        var outer = new Object();
        var inner = new Object();
        var reaction = new ActiveReaction(ReactionType.EMOTE_SAD, 1L, 0);
        scope.render(outer, reaction, () -> {
            assertThrows(IllegalStateException.class, () -> scope.render(inner, null, () -> {
                assertTrue(scope.contains(inner));
                assertFalse(scope.contains(outer));
                assertNull(scope.reaction(), "Resting previews must not fall back to live state");
                throw new IllegalStateException("renderer failed");
            }));
            assertTrue(scope.contains(outer));
            assertSame(reaction, scope.reaction());
        });
        assertFalse(scope.contains(outer));
        assertFalse(scope.contains(inner));
    }
}
