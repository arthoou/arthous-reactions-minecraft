package com.arthou.arthousreactions.client;

import dev.tr7zw.skinlayers.api.MeshTransformer;
import dev.tr7zw.skinlayers.api.MeshTransformerProvider;
import dev.tr7zw.skinlayers.api.SkinLayersAPI;
import io.github.kosmx.bendylib.ModelPartAccessor;
import io.github.kosmx.bendylib.MutableCuboid;
import io.github.kosmx.bendylib.impl.BendableCuboid;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Vector3f;
import org.joml.Vector4f;

/** Loaded only with Skin Layers installed; preserve other providers outside our emotes. */
final class SkinLayersBridge implements MeshTransformerProvider {
    private static final String CUBE_KEY = "arthousreactions:skin_layer";
    private final MeshTransformerProvider previous;

    private SkinLayersBridge(MeshTransformerProvider previous) { this.previous = previous; }

    static void install() {
        var provider = SkinLayersAPI.getMeshTransformerProvider();
        if (!(provider instanceof SkinLayersBridge)) {
            SkinLayersAPI.setupMeshTransformerProvider(new SkinLayersBridge(provider));
        }
    }

    @Override public MeshTransformer prepareTransformer(ModelPart model) {
        MeshTransformer fallback = previous.prepareTransformer(model);
        BendableCuboid source = null;
        if (ReactionBendRenderer.isActive()) {
            var cube = ModelPartAccessor.optionalGetCuboid(model, 0);
            if (cube.isPresent()) {
                var active = cube.get().getActiveMutator();
                if (active != null && active.getB() instanceof BendableCuboid bend && bend.getBend() != 0) {
                    source = bend;
                }
            }
        }
        return transformer(source, fallback, SkinLayersFrames.resolve(model));
    }

    static MeshTransformer transformer(BendableCuboid source, MeshTransformer fallback) {
        return transformer(source, fallback, SkinLayerFrame.IDENTITY);
    }

    static MeshTransformer transformer(BendableCuboid source, MeshTransformer fallback, SkinLayerFrame frame) {
        return new MeshTransformer() {
            public void transform(Vector3f normal, Vector4f[] vertices) {
                if (source == null) fallback.transform(normal, vertices);
                else SkinLayerBend.transform(source, normal, vertices, frame);
            }

            public void transform(ModelPart.Cube cube) {
                if (!(cube instanceof MutableCuboid mutable)) {
                    fallback.transform(cube);
                    return;
                }
                if (source == null) {
                    var active = mutable.getActiveMutator();
                    if (active != null && CUBE_KEY.equals(active.getA())) mutable.getAndActivateMutator(null);
                    fallback.transform(cube);
                    return;
                }
                if (!mutable.hasMutator(CUBE_KEY)) {
                    mutable.registerMutator(CUBE_KEY, ignored -> new SkinLayerCube(cube));
                }
                if (mutable.getAndActivateMutator(CUBE_KEY) instanceof SkinLayerCube geometry) geometry.deform(source, frame);
            }
        };
    }
}
