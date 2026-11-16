package com.arthou.arthousreactions.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.tr7zw.skinlayers.api.MeshTransformer;
import dev.tr7zw.skinlayers.api.MeshTransformerProvider;
import dev.tr7zw.skinlayers.api.SkinLayersAPI;
import dev.tr7zw.skinlayers.render.CustomizableModelPart;
import io.github.kosmx.bendylib.ICuboidBuilder;
import io.github.kosmx.bendylib.MutableCuboid;
import io.github.kosmx.bendylib.impl.BendableCuboid;
import io.github.kosmx.bendylib.impl.ICuboid;
import io.github.kosmx.bendylib.impl.RememberingPos;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Direction;
import net.minecraft.util.Tuple;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.joml.Vector3f;
import org.joml.Vector4f;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static com.arthou.arthousreactions.client.SkinLayersGeometry.*;

@Tag("skinlayers")
class SkinLayersBridgeTest {
    private final MeshTransformerProvider previous=SkinLayersAPI.getMeshTransformerProvider();
    @AfterEach void restore(){SkinLayersAPI.setupMeshTransformerProvider(previous);}

    @Test void realSkinLayersMeshUsesTheExistingArmLegAndTorsoJointWithoutChangingTheAnimation() {
        for(float width:new float[]{3,4,8})for(float y:new float[]{-2,0})for(float axis:new float[]{0,.45F}) {
            var source=bend(width,y);source.applyBend(axis,-1.35F);
            ModelPart model=null;
            var mesh=mesh(width);
            SkinLayersAPI.setupMeshTransformerProvider(MeshTransformerProvider.EMPTY_PROVIDER);
            var flat=render(mesh,model);
            // Supply the joint normally obtained through Bendy's injected ModelPart accessor.
            SkinLayersAPI.setupMeshTransformerProvider(part->SkinLayersBridge.transformer(source,MeshTransformer.EMPTY_TRANSFORMER));
            ReactionBendRenderer.scoped(true,()->{
                var bent=render(mesh,model);
                samePayload(flat,bent);
                for(int i=0;i<flat.vertices.size();i++) {
                    var p=flat.vertices.get(i).position;
                    var expected=new RememberingPos(p.x*16,p.y*16,p.z*16);
                    source.applyBend(axis,-1.35F,consumer->consumer.accept(expected));
                    assertTrue(new Vector3f(bent.vertices.get(i).position.x,bent.vertices.get(i).position.y,
                        bent.vertices.get(i).position.z).distance(expected.getPos().div(16))<.00001);
                }
                assertNotEquals(flat.vertices.getFirst().position,bent.vertices.getFirst().position);
            });
            assertEquals(axis,source.getBendAxis());assertEquals(-1.35F,source.getBend());
        }
    }

    @Test void ordinaryPlayersHeadAndOtherProvidersKeepTheirNormalRendering() {
        int[] calls={0};
        MeshTransformer delegate=new MeshTransformer() {
            public void transform(Vector3f n,Vector4f[] v){calls[0]++;}
            public void transform(ModelPart.Cube c){calls[0]++;}
        };
        SkinLayersAPI.setupMeshTransformerProvider(model->delegate);
        SkinLayersBridge.install();
        var installed=SkinLayersAPI.getMeshTransformerProvider();
        SkinLayersBridge.install();assertSame(installed,SkinLayersAPI.getMeshTransformerProvider());
        var normal=new ModelPart(List.of(),Map.of());
        var mesh=mesh(4);
        var out=render(mesh,normal);
        assertFalse(out.vertices.isEmpty());assertTrue(calls[0]>0);
        // A head has no bend mutator, including while another body part is emoting.
        ReactionBendRenderer.scoped(true,()->assertDoesNotThrow(()->render(mesh,null)));
    }

    @Test void singleFaceShaderCubesRetainTheirUvsAndRecoverWhenTheEmoteStops() {
        var cube=new TestCube(EnumSet.of(Direction.NORTH));
        var flat=new Capture();cube.compile(new PoseStack().last(),flat,LIGHT,OVERLAY,COLOR);
        var mesh=new CustomizableModelPart(List.of(cube),List.of(),Map.of());
        var bend=bend(4,-2);bend.applyBend(.2F,-1.4F);
        var transformer=SkinLayersBridge.transformer(bend,MeshTransformer.EMPTY_TRANSFORMER);
        SkinLayersAPI.setupMeshTransformerProvider(model->transformer);
        ReactionBendRenderer.scoped(true,()->{
            render(mesh,null);
            // Plain unit-test cubes have no injected compile method: draw the exact active mutator.
            var emitted=new Capture();
            assertTrue(ReactionBendRenderer.draw(cube,new PoseStack().last(),emitted,LIGHT,OVERLAY,COLOR));
            samePayload(flat,emitted);assertEquals(4,emitted.vertices.size());
            assertNotEquals(flat.vertices.getFirst().position,emitted.vertices.getFirst().position);
        });
        SkinLayersBridge.transformer(null,MeshTransformer.EMPTY_TRANSFORMER).transform(cube);
        assertNull(cube.getActiveMutator(),"Cached pixel cubes must not retain a finished emote");
        var rest=new Capture();cube.compile(new PoseStack().last(),rest,LIGHT,OVERLAY,COLOR);
        assertEquals(flat.vertices.getFirst().position,rest.vertices.getFirst().position);
    }

    @Test void skinLayersRealOffsetAndScaleDoNotMoveTheExistingJoint() {
        dev.tr7zw.skinlayers.versionless.ModBase.config=new dev.tr7zw.skinlayers.versionless.config.Config();
        var offset=dev.tr7zw.skinlayers.api.OffsetProvider.RIGHT_ARM_SLIM;
        var source=bend(3,-2);source.applyBend(.25F,-1.35F);
        var mesh=mesh(3);
        SkinLayersAPI.setupMeshTransformerProvider(MeshTransformerProvider.EMPTY_PROVIDER);
        var flat=renderWithOffset(mesh,null,offset);
        SkinLayersAPI.setupMeshTransformerProvider(part->SkinLayersBridge.transformer(source,MeshTransformer.EMPTY_TRANSFORMER));
        var bent=renderWithOffset(mesh,null,offset);
        for(int i=0;i<flat.vertices.size();i++) {
            var p=flat.vertices.get(i).position;
            var expected=new RememberingPos(p.x*16,p.y*16,p.z*16);
            source.applyBend(source.getBendAxis(),source.getBend(),consumer->consumer.accept(expected));
            var actual=bent.vertices.get(i).position;
            assertEquals(0,new Vector3f(actual.x,actual.y,actual.z).distance(expected.getPos().div(16)),.00001,
                "Apply the real skin offset before bending around the body's existing joint");
        }
    }

    private static BendableCuboid bend(float width,float y) {
        return new BendableCuboid.Builder().setDirection(width==8?Direction.DOWN:Direction.UP)
            .build(new ICuboidBuilder.Data(0,0,-width/2,y,-2,width,12,4,.25F,.25F,.25F,false,64,64));
    }
    // Only the mixin-provided cube registry is supplied; vertices and deformation are real.
    private static class TestCube extends ModelPart.Cube implements MutableCuboid {
        final Map<String,ICuboidBuilder<ICuboid>> builders=new HashMap<>();
        final Map<String,ICuboid> values=new HashMap<>();
        String active;
        TestCube(Set<Direction> faces){super(7,11,-2,8,-2,1,1,1,0,0,0,false,64,64,faces);}
        public boolean registerMutator(String id,ICuboidBuilder<ICuboid> b){return builders.putIfAbsent(id,b)==null;}
        public boolean unregisterMutator(String id){return builders.remove(id)!=null;}
        public Tuple<String,ICuboid> getActiveMutator(){return active==null?null:new Tuple<>(active,values.get(active));}
        public boolean hasMutator(String id){return builders.containsKey(id);}
        public ICuboidBuilder<ICuboid> getCuboidBuilder(String id){return builders.get(id);}
        public ICuboid getMutator(String id){return values.get(id);}
        public ICuboid getAndActivateMutator(String id){
            active=id;if(id==null)return null;
            return values.computeIfAbsent(id,k->builders.get(k).build(null));
        }
        public void copyStateFrom(MutableCuboid other){throw new AssertionError("No geometry copying");}
    }
}

