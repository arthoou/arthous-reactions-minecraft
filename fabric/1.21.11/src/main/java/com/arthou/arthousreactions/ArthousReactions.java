package com.arthou.arthousreactions;

import net.minecraft.resources.Identifier;

public final class ArthousReactions {
    public static final String MOD_ID = "arthousreactions";

    private ArthousReactions() {
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
