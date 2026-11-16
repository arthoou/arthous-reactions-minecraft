package com.arthou.arthousreactions.client;

import io.github.kosmx.bendylib.impl.BendableCuboid;
import io.github.kosmx.bendylib.impl.RememberingPos;
import org.joml.Vector3f;
import org.joml.Vector4f;

/** Apply the existing joint, in pixel coordinates, to an extruded skin face. */
final class SkinLayerBend {
    private SkinLayerBend() { }

    static void transform(BendableCuboid source, Vector3f normal, Vector4f[] vertices) {
        transform(source, normal, vertices, SkinLayerFrame.IDENTITY);
    }

    static void transform(BendableCuboid source, Vector3f normal, Vector4f[] vertices, SkinLayerFrame frame) {
        frame.toPart(vertices);
        RememberingPos[] points = new RememberingPos[vertices.length];
        for (int i = 0; i < vertices.length; i++) {
            points[i] = new RememberingPos(vertices[i].x * 16, vertices[i].y * 16, vertices[i].z * 16);
        }
        source.applyBend(source.getBendAxis(), source.getBend(), consumer -> {
            for (var point : points) consumer.accept(point);
        });
        for (int i = 0; i < vertices.length; i++) {
            var position = points[i].getPos();
            vertices[i].set(position.x / 16, position.y / 16, position.z / 16, vertices[i].w);
        }
        frame.toMesh(vertices);
        SkinLayerFrame.normal(normal, vertices);
    }
}
