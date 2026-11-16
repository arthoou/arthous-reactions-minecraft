package com.arthou.arthousreactions.client;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@Tag("without-skinlayers")
class SkinLayersAbsentTest {
    @Test void optionalApiIsActuallyAbsentAndInitializationDoesNotResolveIt() {
        assertThrows(ClassNotFoundException.class,
            () -> Class.forName("dev.tr7zw.skinlayers.api.SkinLayersAPI"));
        assertDoesNotThrow(() -> OptionalSkinLayers.initialize(false));
    }
}

