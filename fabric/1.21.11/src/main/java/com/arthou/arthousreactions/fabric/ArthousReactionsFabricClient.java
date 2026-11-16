package com.arthou.arthousreactions.fabric;

import com.arthou.arthousreactions.client.ReactionClientBootstrap;
import com.arthou.arthousreactions.network.ReactionNetworking;
import net.fabricmc.api.ClientModInitializer;

public final class ArthousReactionsFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ReactionNetworking.registerClient();
        ReactionClientBootstrap.init();
    }
}
