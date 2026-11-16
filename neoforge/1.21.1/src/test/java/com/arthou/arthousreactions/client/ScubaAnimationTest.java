package com.arthou.arthousreactions.client;

import com.arthou.arthousreactions.reaction.ReactionType;
import com.google.gson.JsonParser;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class ScubaAnimationTest {
    @Test void suppliedDanceRetainsItsLoopAndArmAndLegBends() {
        var data = ReactionAnimations.data();
        assertTrue(data.isInfinite());
        assertEquals(12, data.beginTick);
        assertEquals(50, data.endTick);
        assertEquals(12, data.returnToTick);
        for (String part : new String[]{"rightArm", "leftArm", "rightLeg", "leftLeg"}) {
            var bone = data.getPart(part);
            assertNotNull(bone);
            assertEquals(50, bone.pitch.getKeyFrames().size());
            assertEquals(50, bone.bend.getKeyFrames().size());
            assertTrue(bone.bend.getKeyFrames().stream().anyMatch(frame -> Math.abs(frame.value) > 0.5F));
        }
        var player = new KeyframeAnimationPlayer(data);
        for (int i = 0; i < 220; i++) player.tick();
        assertTrue(player.isActive(), "The supplied loop must keep dancing for the full song");
        assertTrue(player.isLoopStarted());
        assertTrue(player.getTick() >= data.returnToTick && player.getTick() <= data.endTick);
    }

    @Test void allGifFramesStayInsideTheTextureAndRepeat() throws IOException {
        var type = ReactionType.SCUBA;
        var atlas = ImageIO.read(getClass().getResourceAsStream("/assets/arthousreactions/textures/reactions/scuba.png"));
        assertEquals(type.textureWidth(), atlas.getWidth());
        assertEquals(type.textureHeight(), atlas.getHeight());
        var coordinates = new HashSet<String>();
        for (int frame = 0; frame < 106; frame++) {
            long time = frame * 100L;
            int x = type.frameX(time);
            int y = type.frameY(time);
            assertTrue(x >= 0 && x + type.frameSize() <= atlas.getWidth());
            assertTrue(y >= 0 && y + type.frameSize() <= atlas.getHeight());
            assertTrue(coordinates.add(x + ":" + y), "Every GIF frame must have its own atlas region");
        }
        assertEquals(type.frameX(0), type.frameX(10_600));
        assertEquals(type.frameY(0), type.frameY(10_600));
        assertEquals(0, type.frameX(-100));
        assertEquals(0, type.frameY(-100));
    }

    @Test void previewSeekMatchesContinuousPlaybackIncludingTheLoopBoundary() {
        var continuous = new KeyframeAnimationPlayer(ReactionAnimations.data());
        for (int tick = 0; tick <= 1000; tick++) {
            var sought = ReactionAnimations.previewAnimation(ReactionType.SCUBA, tick);
            assertEquals(continuous.getTick(), sought.getTick(), "tick " + tick);
            assertEquals(continuous.isLoopStarted(), sought.isLoopStarted(), "loop at " + tick);
            assertTrue(sought.isActive());
            continuous.tick();
        }
        var afterLongPause = ReactionAnimations.previewAnimation(ReactionType.SCUBA, 1_000_000_000L);
        assertTrue(afterLongPause.isActive());
        assertTrue(afterLongPause.isLoopStarted());
        assertTrue(afterLongPause.getTick() >= 12 && afterLongPause.getTick() <= 50);
    }

    @Test void finitePreviewSeekingStillReturnsToRest() {
        for (ReactionType type : new ReactionType[]{ReactionType.EMOTE_SAD, ReactionType.EMOTE_LAUGH}) {
            assertTrue(ReactionAnimations.previewAnimation(type, 0L).isActive());
            assertFalse(ReactionAnimations.previewAnimation(type, 1000L).isActive());
        }
        assertNull(ReactionAnimations.previewAnimation(ReactionType.EMOTE_HI, 0L));
    }

    @Test void scubaKeepsLoopingBeyondTheSongAndNeverFadesTheCat() {
        var active = new ActiveReaction(ReactionType.SCUBA, 0L, 123);
        for (long elapsed : new long[]{10_218L, 60_000L, 3_600_000L, 2_592_000_000L, Long.MAX_VALUE}) {
            assertFalse(active.isExpired(elapsed));
            assertEquals(0.0F, active.normalizedAge(elapsed));
        }
    }

    @Test void finiteGesturesStillFinishAtTheirOriginalTimes() {
        var cry = new ActiveReaction(ReactionType.EMOTE_SAD, 0L, 123);
        var laugh = new ActiveReaction(ReactionType.EMOTE_LAUGH, 0L, 123);
        assertFalse(cry.isExpired(2350L));
        assertTrue(cry.isExpired(2351L));
        assertFalse(laugh.isExpired(1450L));
        assertTrue(laugh.isExpired(1451L));
    }

    @Test void everyRenamedIconAndSoundStillResolves() throws IOException {
        for (ReactionType type : ReactionType.values()) {
            assertNotNull(getClass().getResource("/assets/arthousreactions/" + type.texture().getPath()));
            if (type != ReactionType.SCUBA) {
                assertEquals(64, type.textureWidth());
                assertEquals(64, type.textureHeight());
                assertEquals(0, type.frameX(123_456));
                assertEquals(0, type.frameY(123_456));
            }
        }
        try (var reader = new InputStreamReader(getClass().getResourceAsStream(
                "/assets/arthousreactions/sounds.json"), StandardCharsets.UTF_8)) {
            var sounds = JsonParser.parseReader(reader).getAsJsonObject();
            assertTrue(sounds.has("scuba"));
            for (var entry : sounds.entrySet()) {
                for (var sound : entry.getValue().getAsJsonObject().getAsJsonArray("sounds")) {
                    String path = sound.getAsJsonObject().get("name").getAsString().split(":", 2)[1];
                    assertNotNull(getClass().getResource("/assets/arthousreactions/sounds/" + path + ".ogg"), path);
                }
            }
        }
        for (String locale : new String[]{"en_us", "pt_br"}) {
            try (var reader = new InputStreamReader(getClass().getResourceAsStream(
                    "/assets/arthousreactions/lang/" + locale + ".json"), StandardCharsets.UTF_8)) {
                var language = JsonParser.parseReader(reader).getAsJsonObject();
                for (ReactionType type : ReactionType.values()) assertTrue(language.has(type.translationKey()));
            }
        }
    }

    @Test void malformedEmoteDoesNotBecomeAnUnrelatedAnimation() {
        assertThrows(IOException.class, () -> ReactionAnimations.read(new ByteArrayInputStream(new byte[6])));
    }
}
