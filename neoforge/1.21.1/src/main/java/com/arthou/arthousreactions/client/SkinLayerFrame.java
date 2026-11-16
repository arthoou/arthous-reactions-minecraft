package com.arthou.arthousreactions.client;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;

/** The skin mesh's offset/scale, excluding the body's rigid pose. */
final class SkinLayerFrame {
    static final SkinLayerFrame IDENTITY = new SkinLayerFrame(new Matrix4f());
    private final Matrix4f toPart;
    private final Matrix4f toMesh;

    SkinLayerFrame(Matrix4fc meshToPart) {
        toPart = new Matrix4f(meshToPart);
        toMesh = new Matrix4f(meshToPart).invert();
    }
    void toPart(Vector4f[] points) {
        if (this != IDENTITY) for (var point : points) toPart.transform(point);
    }
    void toMesh(Vector4f[] points) {
        if (this != IDENTITY) for (var point : points) toMesh.transform(point);
    }
    Vector3f toPartPixels(Vector3f point) {
        return toPart.transformPosition(point.div(16)).mul(16);
    }
    Vector3f toMeshPixels(Vector3f point) {
        return toMesh.transformPosition(point.div(16)).mul(16);
    }
    static void normal(Vector3f normal, Vector4f[] vertices) {
        Vector3f a = new Vector3f(vertices[0].x - vertices[2].x,
            vertices[0].y - vertices[2].y, vertices[0].z - vertices[2].z);
        Vector3f b = new Vector3f(vertices[1].x - vertices[3].x,
            vertices[1].y - vertices[3].y, vertices[1].z - vertices[3].z);
        a.cross(b);
        if (a.lengthSquared() > 1.0E-12F && a.isFinite()) normal.set(a.normalize());
    }
}

