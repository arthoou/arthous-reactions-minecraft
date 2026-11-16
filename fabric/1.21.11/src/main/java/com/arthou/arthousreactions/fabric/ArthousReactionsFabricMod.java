package com.arthou.arthousreactions.fabric;

import com.arthou.arthousreactions.item.ReactionItems;
import com.arthou.arthousreactions.network.ReactionNetworking;
import com.arthou.arthousreactions.sound.ReactionSoundEvents;
import net.fabricmc.api.ModInitializer;

public final class ArthousReactionsFabricMod implements ModInitializer {
    @Override
    public void onInitialize() {
        ReactionItems.register();
        ReactionSoundEvents.register();
        ReactionNetworking.registerServer();
    }
}
