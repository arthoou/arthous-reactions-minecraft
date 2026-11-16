package com.arthou.arthousreactions.client;

/** Keeps all references to the optional API behind the presence check. */
public final class OptionalSkinLayers {
    private static boolean installed;

    private OptionalSkinLayers() { }

    public static void initialize(boolean present) {
        if (!present || installed) return;
        SkinLayersBridge.install();
        installed = true;
    }

    static void bindModel(net.minecraft.client.model.PlayerModel<?> model) {
        if (installed) SkinLayersFrames.bind(model);
    }
}
