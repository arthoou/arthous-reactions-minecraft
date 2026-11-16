package com.arthou.arthousreactions.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.joml.Quaternionf;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.StreamSupport;

/** The supplied CPM hierarchy, with its original per-face UVs and pixel dimensions. */
final class AngelWingModel {
    private static final AngelWingModel INSTANCE = load();
    private final Node root;
    private final float[] mount;

    private AngelWingModel(Node root, float[] mount) { this.root = root; this.mount = mount; }

    static AngelWingModel instance() { return INSTANCE; }

    void render(PoseStack stack, VertexConsumer consumer, int light, int alpha, float seconds) {
        visit(stack, seconds, (pose, quad) -> {
            for (float[] vertex : quad.vertices()) {
                consumer.addVertex(pose.pose(), vertex[0] / 16.0F, vertex[1] / 16.0F, vertex[2] / 16.0F)
                    .setColor(255, 255, 255, alpha).setUv(vertex[3], vertex[4])
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                    .setNormal(pose, quad.normal()[0], quad.normal()[1], quad.normal()[2]);
            }
        });
    }

    void visit(PoseStack stack, float seconds, QuadSink sink) {
        stack.pushPose();
        try {
            stack.translate(mount[0] / 16.0F, mount[1] / 16.0F, mount[2] / 16.0F);
            root.visit(stack, seconds, sink);
        } finally { stack.popPose(); }
    }

    @FunctionalInterface
    interface QuadSink { void accept(PoseStack.Pose pose, Quad quad); }

    record Quad(float[] normal, float[][] vertices) { }

    private record Node(float[] position, Quaternionf rotation, float[] scale, String motion, float[] pivot,
                        List<Quad> quads, List<Node> children) {
        void visit(PoseStack stack, float seconds, QuadSink sink) {
            stack.pushPose();
            try {
                stack.translate(position[0] / 16.0F, position[1] / 16.0F, position[2] / 16.0F);
                stack.mulPose(rotation);
                stack.scale(scale[0], scale[1], scale[2]);
                if (!motion.isEmpty()) {
                    boolean second = motion.endsWith("_b");
                    float degrees = motion.startsWith("tip") ? AngelMotion.tipAngle(seconds, second)
                        : AngelMotion.wingAngle(seconds, second);
                    stack.translate(pivot[0] / 16.0F, pivot[1] / 16.0F, pivot[2] / 16.0F);
                    stack.mulPose(new Quaternionf().rotateY(degrees * (float) Math.PI / 180.0F));
                    stack.translate(-pivot[0] / 16.0F, -pivot[1] / 16.0F, -pivot[2] / 16.0F);
                }
                for (Quad quad : quads) sink.accept(stack.last(), quad);
                for (Node child : children) child.visit(stack, seconds, sink);
            } finally { stack.popPose(); }
        }
    }

    private static AngelWingModel load() {
        try (var input = AngelWingModel.class.getResourceAsStream("/assets/arthousreactions/models/effect/angel_wings.json")) {
            if (input == null) throw new IOException("Missing converted angel wings");
            JsonObject data = JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
            return new AngelWingModel(node(data.getAsJsonObject("root")), floats(data.getAsJsonArray("mount")));
        } catch (IOException exception) { throw new UncheckedIOException(exception); }
    }

    private static Node node(JsonObject object) {
        float[] degrees = floats(object.getAsJsonArray("rotation"));
        float r = (float) Math.PI / 180.0F;
        Quaternionf rotation = new Quaternionf().rotationZYX(degrees[2] * r, degrees[1] * r, degrees[0] * r);
        List<Quad> quads = StreamSupport.stream(object.getAsJsonArray("quads").spliterator(), false)
            .map(element -> {
                JsonObject quad = element.getAsJsonObject();
                float[][] vertices = StreamSupport.stream(quad.getAsJsonArray("vertices").spliterator(), false)
                    .map(vertex -> floats(vertex.getAsJsonArray())).toArray(float[][]::new);
                return new Quad(floats(quad.getAsJsonArray("normal")), vertices);
            }).toList();
        List<Node> children = StreamSupport.stream(object.getAsJsonArray("children").spliterator(), false)
            .map(child -> node(child.getAsJsonObject())).toList();
        return new Node(floats(object.getAsJsonArray("position")), rotation, floats(object.getAsJsonArray("scale")),
            object.has("motion") ? object.get("motion").getAsString() : "",
            object.has("pivot") ? floats(object.getAsJsonArray("pivot")) : new float[3], quads, children);
    }

    private static float[] floats(JsonArray array) {
        float[] values = new float[array.size()];
        for (int index = 0; index < values.length; index++) values[index] = array.get(index).getAsFloat();
        return values;
    }
}
