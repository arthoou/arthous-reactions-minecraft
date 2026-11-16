package com.arthou.arthousreactions.reaction;

import com.arthou.arthousreactions.ArthousReactions;
import net.minecraft.resources.ResourceLocation;

public enum ReactionType {
    CHEER("cheer"),
    POUT("pout"),
    ANGRY("angry"),
    EMOTE_HI("emote_hi"),
    EMOTE_CLAP("emote_clap"),
    EMOTE_WOAH("emote_woah"),
    EMOTE_LAUGH("emote_laugh"),
    EMOTE_SAD("emote_sad"),
    EMOTE_FEAR("emote_fear"),
    EMOTE_EW("emote_ew"),
    EMOTE_HEARTS("emote_hearts"),
    EMOTE_ANGEL("emote_angel"),
    EMOTE_67("emote_67"),
    EMOTE_VINE_BOOM("emote_vine_boom"),
    SCUBA("scuba", 171, 106, 11, 100);

    public static final int TEXTURE_SIZE = 64;
    private static final ReactionType[] VALUES = values();

    private final String name;
    private final boolean emoji;
    private final ResourceLocation texture;
    private final int frameSize;
    private final int frameCount;
    private final int columns;
    private final int frameTimeMs;

    ReactionType(String name) {
        this(name, true);
    }

    ReactionType(String name, boolean emoji) {
        this(name, emoji, TEXTURE_SIZE, 1, 1, 100);
    }

    ReactionType(String name, int frameSize, int frameCount, int columns, int frameTimeMs) {
        this(name, true, frameSize, frameCount, columns, frameTimeMs);
    }

    ReactionType(String name, boolean emoji, int frameSize, int frameCount, int columns, int frameTimeMs) {
        this.name = name;
        this.emoji = emoji;
        this.texture = emoji ? ArthousReactions.id("textures/reactions/" + name + ".png") : null;
        this.frameSize = frameSize;
        this.frameCount = frameCount;
        this.columns = columns;
        this.frameTimeMs = frameTimeMs;
    }

    public int index() {
        return ordinal();
    }

    public String translationKey() {
        return "reaction." + ArthousReactions.MOD_ID + "." + name;
    }

    public boolean hasEmoji() {
        return emoji;
    }

    public ResourceLocation texture() {
        return texture;
    }

    public int frameSize() {
        return frameSize;
    }

    public int textureWidth() {
        return frameSize * columns;
    }

    public int textureHeight() {
        return frameSize * ((frameCount + columns - 1) / columns);
    }

    public int frameX(long elapsedMs) {
        return frame(elapsedMs) % columns * frameSize;
    }

    public int frameY(long elapsedMs) {
        return frame(elapsedMs) / columns * frameSize;
    }

    private int frame(long elapsedMs) {
        return (int) (Math.max(0L, elapsedMs) / frameTimeMs % frameCount);
    }

    public static ReactionType byIndex(int index) {
        if (index < 0 || index >= VALUES.length) {
            return null;
        }
        return VALUES[index];
    }
}
