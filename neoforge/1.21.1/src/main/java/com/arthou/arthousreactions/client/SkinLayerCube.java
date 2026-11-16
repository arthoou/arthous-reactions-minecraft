package com.arthou.arthousreactions.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.kosmx.bendylib.impl.BendableCuboid;
import io.github.kosmx.bendylib.impl.ICuboid;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Vector3f;
import org.joml.Vector4f;
import java.util.ArrayList;
import java.util.List;

/** Retain the original faces and UVs, including Skin Layers' one-face shader cubes. */
final class SkinLayerCube implements ICuboid {
    private final List<Face> faces = new ArrayList<>();

    SkinLayerCube(ModelPart.Cube original) {
        var captured = new Capture();
        ReactionBendRenderer.scoped(false,
            () -> original.compile(new PoseStack().last(), captured, 0, 0, -1));
        for (int i = 0; i < captured.vertices.size(); i += 4) {
            faces.add(new Face(captured.vertices.subList(i, i + 4)));
        }
    }

    void deform(BendableCuboid source, SkinLayerFrame frame) {
        for (var face : faces) {
            for (int i = 0; i < 4; i++) face.transformed[i].set(face.vertices.get(i).position);
            face.normal.set(face.vertices.getFirst().normal);
            SkinLayerBend.transform(source, face.normal, face.transformed, frame);
        }
    }

    @Override public void render(PoseStack.Pose pose, VertexConsumer output, int light, int overlay, int color) {
        for (var face : faces) {
            Vector3f normal = new Vector3f(face.normal).mul(pose.normal());
            for (int i = 0; i < 4; i++) {
                var p = face.transformed[i]; var v = face.vertices.get(i);
                output.addVertex(pose, p.x, p.y, p.z).setColor(color).setUv(v.u, v.v)
                    .setOverlay(overlay).setLight(light).setNormal(normal.x, normal.y, normal.z);
            }
        }
    }

    @Override public void copyState(ICuboid other) { }
    @Override public boolean disableAfterDraw() { return true; }
    // Bendy's optional cached-side optimization must not replace these dynamic faces.
    @Override @SuppressWarnings("rawtypes") public List getQuads() { return null; }

    private static final class Face {
        final List<Vertex> vertices;
        final Vector4f[] transformed = {new Vector4f(), new Vector4f(), new Vector4f(), new Vector4f()};
        final Vector3f normal = new Vector3f();
        Face(List<Vertex> vertices) {
            this.vertices = List.copyOf(vertices);
            for (int i = 0; i < 4; i++) transformed[i].set(vertices.get(i).position);
            normal.set(vertices.getFirst().normal);
        }
    }
    private static final class Vertex {
        final Vector4f position;
        final Vector3f normal = new Vector3f();
        float u, v;
        Vertex(float x, float y, float z) { position = new Vector4f(x, y, z, 1); }
    }
    private static final class Capture implements VertexConsumer {
        final List<Vertex> vertices = new ArrayList<>();
        Vertex current;
        public VertexConsumer addVertex(float x, float y, float z) {
            current = new Vertex(x, y, z); vertices.add(current); return this;
        }
        public VertexConsumer setColor(int r, int g, int b, int a) { return this; }
        public VertexConsumer setUv(float u, float v) { current.u = u; current.v = v; return this; }
        public VertexConsumer setUv1(int u, int v) { return this; }
        public VertexConsumer setUv2(int u, int v) { return this; }
        public VertexConsumer setNormal(float x, float y, float z) { current.normal.set(x, y, z); return this; }
    }
}
