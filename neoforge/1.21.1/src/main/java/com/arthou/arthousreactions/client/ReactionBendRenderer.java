package com.arthou.arthousreactions.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.kosmx.bendylib.MutableCuboid;
import io.github.kosmx.bendylib.impl.BendableCuboid;

/** Bent geometry must reach the buffer before an optimizer substitutes a cached rigid cube. */
public final class ReactionBendRenderer {
    private static final ThreadLocal<Boolean> ACTIVE = ThreadLocal.withInitial(() -> false);

    private ReactionBendRenderer() { }

    static boolean isActive() { return ACTIVE.get(); }

    static void scoped(boolean active, Runnable render) {
        boolean previous = ACTIVE.get();
        ACTIVE.set(active);
        try { render.run(); }
        finally {
            if (previous) ACTIVE.set(true);
            else ACTIVE.remove();
        }
    }

    public static boolean draw(Object cube, PoseStack.Pose pose, VertexConsumer consumer,
                               int light, int overlay, int color) {
        if (!ACTIVE.get() || !(cube instanceof MutableCuboid mutable)) return false;
        var active = mutable.getActiveMutator();
        if (active == null || !(active.getB() instanceof BendableCuboid || active.getB() instanceof SkinLayerCube)) return false;
        // Use the existing Bendy vertices/UVs/normals, without resampling the animation
        // or changing either skin layer's cuboid, bend axis or thickness.
        active.getB().render(pose, consumer, light, overlay, color);
        if (active.getB() instanceof SkinLayerCube) mutable.getAndActivateMutator(null);
        return true;
    }
}
