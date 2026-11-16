package com.arthou.arthousreactions.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.tr7zw.skinlayers.accessor.ModelPartInjector;
import dev.tr7zw.skinlayers.accessor.PlayerEntityModelAccessor;
import dev.tr7zw.skinlayers.api.Mesh;
import dev.tr7zw.skinlayers.api.OffsetProvider;
import dev.tr7zw.skinlayers.render.CustomizableModelPart;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/** Optional-only lookup, supporting both injected meshes and the legacy feature layer. */
final class SkinLayersFrames {
    private static final Map<ModelPart, OffsetProvider> OFFSETS =
        Collections.synchronizedMap(new WeakHashMap<>());

    private SkinLayersFrames() { }

    static void bind(PlayerModel<?> model) {
        if (OFFSETS.containsKey(model.body) || !((Object) model instanceof PlayerEntityModelAccessor accessor)) return;
        boolean slim = accessor.hasThinArms();
        bind(model.head, model.hat, OffsetProvider.HEAD);
        bind(model.body, model.jacket, OffsetProvider.BODY);
        bind(model.leftArm, model.leftSleeve, slim ? OffsetProvider.LEFT_ARM_SLIM : OffsetProvider.LEFT_ARM);
        bind(model.rightArm, model.rightSleeve, slim ? OffsetProvider.RIGHT_ARM_SLIM : OffsetProvider.RIGHT_ARM);
        bind(model.leftLeg, model.leftPants, OffsetProvider.LEFT_LEG);
        bind(model.rightLeg, model.rightPants, OffsetProvider.RIGHT_LEG);
    }
    private static void bind(ModelPart base, ModelPart outer, OffsetProvider offset) {
        OFFSETS.put(base, offset);
        OFFSETS.put(outer, offset);
    }
    static SkinLayerFrame resolve(ModelPart model) {
        OffsetProvider offset = null;
        Mesh mesh = null;
        if ((Object) model instanceof ModelPartInjector injector) {
            offset = injector.getOffsetProvider();
            mesh = injector.getInjectedMesh();
        }
        if (offset == null) offset = OFFSETS.get(model);
        return offset == null ? SkinLayerFrame.IDENTITY : frame(offset, mesh);
    }
    static SkinLayerFrame frame(OffsetProvider offset, Mesh mesh) {
        var pose = new PoseStack();
        var probe = new PositionProbe();
        // Probe the provider on a separate mesh: never overwrite the player's cached mesh.
        offset.applyOffset(pose, probe);
        if (mesh instanceof CustomizableModelPart actual) actual.translateAndRotate(pose);
        else probe.translateAndRotate(pose);
        return new SkinLayerFrame(pose.last().pose());
    }
    private static final class PositionProbe implements Mesh {
        float x, y, z, pitch, yaw, roll;
        boolean visible = true;
        public void setPosition(float x, float y, float z) { this.x=x; this.y=y; this.z=z; }
        public void setRotation(float x, float y, float z) { pitch=x; yaw=y; roll=z; }
        public void loadPose(PartPose pose) { }
        public void copyFrom(ModelPart model) {
            setPosition(model.x,model.y,model.z);setRotation(model.xRot,model.yRot,model.zRot);
        }
        public void reset() { x=y=z=pitch=yaw=roll=0; }
        public void setVisible(boolean visible) { this.visible=visible; }
        public boolean isVisible() { return visible; }
        public void render(ModelPart model,PoseStack poses,com.mojang.blaze3d.vertex.VertexConsumer output,
                           int light,int overlay,int color) { }
        void translateAndRotate(PoseStack poses) {
            poses.translate(x/16F,y/16F,z/16F);
            if (pitch!=0 || yaw!=0 || roll!=0)
                poses.mulPose(new org.joml.Quaternionf().rotationZYX(roll,yaw,pitch));
        }
    }
}

