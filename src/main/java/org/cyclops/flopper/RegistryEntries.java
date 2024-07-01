package org.cyclops.flopper;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.cyclops.flopper.blockentity.BlockEntityFlopper;

/**
 * Referenced registry entries.
 * @author rubensworks
 */
public class RegistryEntries {

    public static final DeferredHolder<Item, Item> ITEM_FLOPPER = DeferredHolder.create(Registries.ITEM, ResourceLocation.parse("flopper:flopper"));
    public static final DeferredHolder<Block, Block> BLOCK_FLOPPER = DeferredHolder.create(Registries.BLOCK, ResourceLocation.parse("flopper:flopper"));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityFlopper>> BLOCK_ENTITY_FLOPPER = DeferredHolder.create(Registries.BLOCK_ENTITY_TYPE, ResourceLocation.parse("flopper:flopper"));

}
