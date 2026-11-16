package com.arthou.arthousreactions.item;

import com.arthou.arthousreactions.ArthousReactions;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

public final class ReactionItems {
    private static final Identifier ANGEL_HALO_ID = ArthousReactions.id("angel_halo");
    public static final Item ANGEL_HALO = new Item(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, ANGEL_HALO_ID)));

    private ReactionItems() {
    }

    public static void register() {
        Registry.register(BuiltInRegistries.ITEM, ANGEL_HALO_ID, ANGEL_HALO);
    }
}
