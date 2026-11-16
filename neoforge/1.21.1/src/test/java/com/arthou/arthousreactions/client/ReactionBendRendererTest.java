package com.arthou.arthousreactions.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.kosmx.playerAnim.core.impl.AnimationProcessor;
import io.github.kosmx.bendylib.ICuboidBuilder;
import io.github.kosmx.bendylib.MutableCuboid;
import io.github.kosmx.bendylib.impl.BendableCuboid;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Direction;
import net.minecraft.util.Tuple;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ReactionBendRendererTest {
    private static final PoseStack.Pose POSE = new PoseStack().last();
    private static final int LIGHT = 0x00f000f0, OVERLAY = 0x000a000b, COLOR = 0x9f123456;

    @Test void bothSkinLayersEmitActualBentVerticesInsteadOfTheCachedRigidCube() {
        for (float width : new float[]{3, 4, 8}) {
            for (float inflation : new float[]{0, .25F}) {
                float x = -width / 2;
                var bend = cuboid(x, width, inflation);
                bend.applyBend(0, -1.6F);
                var expected = capture(bend);
                var rigid = new Capture();
                new ModelPart.Cube(0, 0, x, -2, -2, width, 12, 4,
                    inflation, inflation, inflation, false, 64, 64, EnumSet.allOf(Direction.class))
                    .compile(POSE, rigid, LIGHT, OVERLAY, COLOR);
                assertTrue(expected.minZ() < rigid.minZ() - .1F,
                    "The optimizer's rigid cube must reproduce the missing bend");
                var output = new Capture();
                Object mutable = mutable(bend);
                ReactionBendRenderer.scoped(true, () -> assertTrue(
                    ReactionBendRenderer.draw(mutable, POSE, output, LIGHT, OVERLAY, COLOR)));
                assertEquals(expected.vertices, output.vertices,
                    "Positions, UVs, color, light, overlay and normals must reach the real vertex consumer intact");
            }
        }
    }

    @Test void scubaUsesItsExistingDeformedGeometryWithoutChangingAnyBendState() {
        var animation = ReactionAnimations.previewAnimation(com.arthou.arthousreactions.reaction.ReactionType.SCUBA, 0);
        var processor = new AnimationProcessor(animation);
        int samples = 0;
        for (int tick = 0; tick < 90; tick++, animation.tick()) {
            processor.setTickDelta(.4F);
            for (String bone : new String[]{"rightArm", "leftArm", "rightLeg", "leftLeg"}) {
                var value = processor.getBend(bone);
                if (Math.abs(value.getRight()) < .01F) continue;
                for (float inflation : new float[]{0, .25F}) {
                    var bend = cuboid(-2, 4, inflation);
                    bend.applyBend(value.getLeft(), value.getRight());
                    var before = capture(bend);
                    var output = new Capture();
                    Object mutable = mutable(bend);
                    ReactionBendRenderer.scoped(true, () -> assertTrue(
                        ReactionBendRenderer.draw(mutable, POSE, output, LIGHT, OVERLAY, COLOR)));
                    assertEquals(before.vertices, output.vertices);
                    assertEquals(value.getLeft(), bend.getBendAxis());
                    assertEquals(value.getRight(), bend.getBend());
                    samples++;
                }
            }
        }
        assertTrue(samples > 100, "Exercise the actual dance's changing joint angles and axes");
    }

    @Test void normalRenderingAndNestedOrFailedRendersDoNotInheritTheCompatibilityScope() {
        Object cube = mutable(cuboid(-2, 4, 0));
        var output = new Capture();
        assertFalse(ReactionBendRenderer.draw(cube, POSE, output, LIGHT, OVERLAY, COLOR));
        assertThrows(IllegalStateException.class, () -> ReactionBendRenderer.scoped(true, () -> {
            ReactionBendRenderer.scoped(false, () -> assertFalse(
                ReactionBendRenderer.draw(cube, POSE, output, LIGHT, OVERLAY, COLOR)));
            assertTrue(ReactionBendRenderer.draw(cube, POSE, output, LIGHT, OVERLAY, COLOR));
            assertFalse(ReactionBendRenderer.draw(new Object(), POSE, output, LIGHT, OVERLAY, COLOR));
            assertFalse(ReactionBendRenderer.draw(mutable(null), POSE, output, LIGHT, OVERLAY, COLOR));
            throw new IllegalStateException("simulated render failure");
        }));
        assertFalse(ReactionBendRenderer.draw(cube, POSE, output, LIGHT, OVERLAY, COLOR));
    }

    private static BendableCuboid cuboid(float x, float width, float inflation) {
        return new BendableCuboid.Builder().setDirection(Direction.UP).build(new ICuboidBuilder.Data(
            0, 0, x, -2, -2, width, 12, 4, inflation, inflation, inflation, false, 64, 64));
    }

    private static Object mutable(BendableCuboid bend) {
        // Only the mixin-provided cuboid accessor is simulated; deformation and rendering are real Bendy code.
        return Proxy.newProxyInstance(MutableCuboid.class.getClassLoader(), new Class<?>[]{MutableCuboid.class},
            (proxy, method, args) -> {
                if (method.getName().equals("getActiveMutator")) return bend == null ? null : new Tuple<>("bend", bend);
                throw new AssertionError("Rendering must not modify or reconstruct the cuboid: " + method.getName());
            });
    }

    private static Capture capture(BendableCuboid bend) {
        var result = new Capture();
        bend.render(POSE, result, LIGHT, OVERLAY, COLOR);
        return result;
    }

    private static final class Capture implements VertexConsumer {
        final List<List<Number>> vertices = new ArrayList<>();
        private List<Number> current;
        @Override public VertexConsumer addVertex(float x, float y, float z) {
            current = new ArrayList<>(List.of(x, y, z)); vertices.add(current); return this;
        }
        @Override public VertexConsumer setColor(int r, int g, int b, int a) {
            current.addAll(List.of(r, g, b, a)); return this;
        }
        @Override public VertexConsumer setUv(float u, float v) { current.addAll(List.of(u, v)); return this; }
        @Override public VertexConsumer setUv1(int u, int v) { current.addAll(List.of(u, v)); return this; }
        @Override public VertexConsumer setUv2(int u, int v) { current.addAll(List.of(u, v)); return this; }
        @Override public VertexConsumer setNormal(float x, float y, float z) { current.addAll(List.of(x, y, z)); return this; }
        float minZ() { return (float) vertices.stream().mapToDouble(v -> v.get(2).floatValue()).min().orElseThrow(); }
    }
}
