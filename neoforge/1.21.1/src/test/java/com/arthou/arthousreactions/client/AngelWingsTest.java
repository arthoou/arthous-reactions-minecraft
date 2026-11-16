package com.arthou.arthousreactions.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.IdentityHashMap;
import javax.imageio.ImageIO;
import static org.junit.jupiter.api.Assertions.*;

class AngelWingsTest {
    @Test void conversionPreservesOriginalTextureHierarchyAndAllFaces() throws Exception {
        try (var input = getClass().getResourceAsStream("/assets/arthousreactions/textures/effect/angel_wings.png")) {
            String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(input.readAllBytes()));
            assertEquals("d038ccca7f11772bcb9db7e240a8fa13d32e51d0c56ebc95c0cc9eba57883bd6", hash);
        }
        try (var reader = new InputStreamReader(getClass().getResourceAsStream(
                "/assets/arthousreactions/models/effect/angel_wings.json"), StandardCharsets.UTF_8)) {
            var data = JsonParser.parseReader(reader).getAsJsonObject();
            assertEquals("0677318f9cf07522f86cd89928fec6b12c2ad597d61ae2922e30d7a8734b95de", data.getAsJsonObject("source").get("sha256").getAsString());
            int[] counts = count(data.getAsJsonObject("root"));
            assertArrayEquals(new int[]{19, 78}, counts);
        }
    }

    @Test void wingsRemainRigidlyAttachedUnderCombinedWorldAndTorsoRotations() {
        List<Vector3f> local = vertices(new PoseStack(), 1.0F);
        for (Vector3f angles : List.of(new Vector3f(0, 1.5F, 0), new Vector3f(.5F, -.7F, .25F), new Vector3f(-.4F, 2.8F, -.3F))) {
            PoseStack stack = new PoseStack();
            stack.translate(4, 2, -8);
            stack.mulPose(new Quaternionf().rotationZYX(angles.z, angles.y, angles.x));
            stack.translate(.12F, .04F, -.03F);
            Matrix4f inverseTorso = new Matrix4f(stack.last().pose()).invert();
            List<Vector3f> moved = vertices(stack, 1.0F);
            assertEquals(local.size(), moved.size());
            for (int i = 0; i < moved.size(); i++) {
                Vector3f restored = inverseTorso.transformPosition(moved.get(i));
                assertEquals(0, restored.distance(local.get(i)), 0.00001);
            }
        }
    }

    @Test void fadeAndOpeningAreSmoothFiniteAndWingTipsTrailTheBase() {
        assertEquals(0, AngelMotion.fade(0));
        assertEquals(0, AngelMotion.fade(.14F));
        assertEquals(1, AngelMotion.fade(1));
        assertEquals(0, AngelMotion.fade(2.2F), 0.0001);
        assertTrue(AngelMotion.fade(.3F) > 0 && AngelMotion.fade(.3F) < 1);
        assertTrue(AngelMotion.opening(.6F) > AngelMotion.opening(.5F));
        for (float time = 0; time < 2.2F; time += .001F) {
            assertTrue(Math.abs(AngelMotion.fade(time + .001F) - AngelMotion.fade(time)) < .006F);
            assertEquals(-AngelMotion.wingAngle(time, false), AngelMotion.wingAngle(time, true), 0.0001);
            assertEquals(-AngelMotion.tipAngle(time, false), AngelMotion.tipAngle(time, true), 0.0001);
        }
        assertTrue(Math.abs(AngelMotion.tipAngle(.55F, false)) > Math.abs(AngelMotion.tipAngle(.65F, false)));
    }

    @Test void renderTraversalAlwaysRestoresItsPoseStack() {
        PoseStack stack = new PoseStack();
        Matrix4f before = new Matrix4f(stack.last().pose());
        assertThrows(IllegalStateException.class, () -> AngelWingModel.instance().visit(stack, 1, (pose, quad) -> {
            throw new IllegalStateException("test renderer failure");
        }));
        assertEquals(before, stack.last().pose());
    }

    @Test void visibleWingSurfacesStayBehindTheTorsoThroughoutTheOpening() throws Exception {
        var image = ImageIO.read(getClass().getResourceAsStream("/assets/arthousreactions/textures/effect/angel_wings.png"));
        var visible = new IdentityHashMap<AngelWingModel.Quad, Boolean>();
        AngelWingModel.instance().visit(new PoseStack(), 0, (pose, quad) -> {
            float minU = 1, minV = 1, maxU = 0, maxV = 0;
            for (float[] vertex : quad.vertices()) {
                minU = Math.min(minU, vertex[3]); minV = Math.min(minV, vertex[4]);
                maxU = Math.max(maxU, vertex[3]); maxV = Math.max(maxV, vertex[4]);
            }
            int x0 = (int) Math.floor(minU * image.getWidth()), y0 = (int) Math.floor(minV * image.getHeight());
            int x1 = Math.min(image.getWidth(), Math.max(x0 + 1, (int) Math.ceil(maxU * image.getWidth())));
            int y1 = Math.min(image.getHeight(), Math.max(y0 + 1, (int) Math.ceil(maxV * image.getHeight())));
            boolean opaque = false;
            for (int y = y0; y < y1 && !opaque; y++) for (int x = x0; x < x1; x++) {
                if ((image.getRGB(x, y) >>> 24) > 0) { opaque = true; break; }
            }
            visible.put(quad, opaque);
        });
        assertTrue(visible.containsValue(true));
        for (int ms = 0; ms <= 2200; ms += 10) {
            AngelWingModel.instance().visit(new PoseStack(), ms / 1000.0F, (pose, quad) -> {
                if (!visible.get(quad)) return;
                for (float[] vertex : quad.vertices()) {
                    Vector3f point = pose.pose().transformPosition(new Vector3f(vertex[0] / 16, vertex[1] / 16, vertex[2] / 16));
                    assertTrue(point.z > 2.25F / 16.0F, "Visible feather intersects the torso: " + point);
                }
            });
        }
    }

    private static List<Vector3f> vertices(PoseStack stack, float seconds) {
        var result = new ArrayList<Vector3f>();
        AngelWingModel.instance().visit(stack, seconds, (pose, quad) -> {
            for (float[] p : quad.vertices()) result.add(pose.pose().transformPosition(new Vector3f(p[0] / 16, p[1] / 16, p[2] / 16)));
        });
        return result;
    }

    private static int[] count(JsonObject node) {
        int nodes = 1, faces = node.getAsJsonArray("quads").size();
        for (var child : node.getAsJsonArray("children")) {
            int[] count = count(child.getAsJsonObject()); nodes += count[0]; faces += count[1];
        }
        return new int[]{nodes, faces};
    }
}
