package com.arthou.arthousreactions.client;

import com.arthou.arthousreactions.reaction.ReactionType;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SkinLayerSyncTest {
    @Test void allOuterPartsFollowTheFinalPoseForBothSkinWidths() {
        for (boolean slim : new boolean[]{false, true}) {
            var model = model(slim);
            int n = 1;
            for (var pair : pairs(model)) {
                var base = pair[0];
                base.x += n; base.y -= n; base.z = n * .3F;
                base.xRot = n * .1F; base.yRot = -.2F; base.zRot = .3F;
                base.xScale = 1.1F; base.yScale = .9F; base.zScale = 1.2F;
                pair[1].visible = false;
                n++;
            }
            SkinLayerSync.synchronize(model, ReactionType.EMOTE_HEARTS);
            for (var pair : pairs(model)) {
                assertPoseEquals(pair[0], pair[1]);
                assertFalse(pair[1].visible, "Do not override the player's skin-layer visibility settings");
            }

        }
    }

    @Test void finalSyncDoesNotOverrideOtherPlayerRendering() {
        var model = model(false);
        model.rightSleeve.x = 88;
        SkinLayerSync.synchronize(model, null);
        assertEquals(88, model.rightSleeve.x);
    }

    private static PlayerModel<?> model(boolean slim) {
        return new PlayerModel<>(PlayerModel.createMesh(CubeDeformation.NONE, slim).getRoot().bake(64, 64), slim);
    }

    private static ModelPart[][] pairs(PlayerModel<?> m) {
        return new ModelPart[][]{{m.head, m.hat}, {m.body, m.jacket}, {m.rightArm, m.rightSleeve},
            {m.leftArm, m.leftSleeve}, {m.rightLeg, m.rightPants}, {m.leftLeg, m.leftPants}};
    }

    private static void assertPoseEquals(ModelPart base, ModelPart outer) {
        assertEquals(base.x, outer.x); assertEquals(base.y, outer.y); assertEquals(base.z, outer.z);
        assertEquals(base.xRot, outer.xRot); assertEquals(base.yRot, outer.yRot); assertEquals(base.zRot, outer.zRot);
        assertEquals(base.xScale, outer.xScale); assertEquals(base.yScale, outer.yScale); assertEquals(base.zScale, outer.zScale);
    }

}
