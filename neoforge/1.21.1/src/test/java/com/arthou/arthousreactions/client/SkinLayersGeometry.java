package com.arthou.arthousreactions.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.tr7zw.skinlayers.render.CustomizableModelPart;
import dev.tr7zw.skinlayers.versionless.render.CustomizableCube;
import dev.tr7zw.skinlayers.versionless.util.Direction;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Vector3f;
import org.joml.Vector4f;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

final class SkinLayersGeometry {
    static final int LIGHT=0x00f000f0, OVERLAY=0x000a000b, COLOR=0x9f123456;
    static CustomizableModelPart mesh(float width) {
        var voxel=new CustomizableCube(8,16,-width/2,width==8?1:8,-2,1,1,1,0,0,0,
            false,64,64,new Direction[0],new Direction[0][]);
        return new CustomizableModelPart(List.of(),List.of(voxel),Map.of());
    }
    static Capture render(CustomizableModelPart mesh,ModelPart model) {
        var out=new Capture();mesh.render(model,new PoseStack(),out,LIGHT,OVERLAY,COLOR);return out;
    }
    static Capture renderWithOffset(CustomizableModelPart mesh,ModelPart model,
            dev.tr7zw.skinlayers.api.OffsetProvider offset) {
        var pose=new PoseStack();offset.applyOffset(pose,mesh);
        var out=new Capture();mesh.render(model,pose,out,LIGHT,OVERLAY,COLOR);return out;
    }
    static void samePayload(Capture a,Capture b) {
        assertEquals(a.vertices.size(),b.vertices.size());
        for(int i=0;i<a.vertices.size();i++) {
            var x=a.vertices.get(i);var y=b.vertices.get(i);
            assertEquals(x.u,y.u);assertEquals(x.v,y.v);
            assertEquals(COLOR,y.color);assertEquals(LIGHT,y.light);assertEquals(OVERLAY,y.overlay);
            assertTrue(y.position.isFinite());assertTrue(y.normal.isFinite());
            assertEquals(1,y.normal.length(),.0001);
        }
    }
    static class Vertex {
        final Vector4f position;final Vector3f normal=new Vector3f();
        float u,v;int color,light,overlay;
        Vertex(float x,float y,float z) {position=new Vector4f(x,y,z,1);}
    }
    static class Capture implements VertexConsumer {
        final List<Vertex> vertices=new ArrayList<>();Vertex current;
        public VertexConsumer addVertex(float x,float y,float z) {
            current=new Vertex(x,y,z);vertices.add(current);return this;
        }
        public VertexConsumer setColor(int color){current.color=color;return this;}
        public VertexConsumer setColor(int r,int g,int b,int a){return setColor(a<<24|r<<16|g<<8|b);}
        public VertexConsumer setUv(float u,float v){current.u=u;current.v=v;return this;}
        public VertexConsumer setUv1(int u,int v){current.overlay=u|v<<16;return this;}
        public VertexConsumer setUv2(int u,int v){current.light=u|v<<16;return this;}
        public VertexConsumer setNormal(float x,float y,float z){current.normal.set(x,y,z);return this;}
        public VertexConsumer setLineWidth(float width){return this;}
    }
}

