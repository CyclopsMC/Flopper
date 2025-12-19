package org.cyclops.flopper;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.cyclops.cyclopscore.config.DeferredHolderCommon;
import org.cyclops.flopper.blockentity.BlockEntityFlopper;

/**
 * Referenced registry entries.
 * @author rubensworks
 */
public class RegistryEntries {

    public static final DeferredHolderCommon<Item, Item> ITEM_FLOPPER = DeferredHolderCommon.create(Registries.ITEM, Identifier.parse("flopper:flopper"));
    public static final DeferredHolderCommon<Block, Block> BLOCK_FLOPPER = DeferredHolderCommon.create(Registries.BLOCK, Identifier.parse("flopper:flopper"));
    public static final DeferredHolderCommon<BlockEntityType<?>, BlockEntityType<BlockEntityFlopper>> BLOCK_ENTITY_FLOPPER = DeferredHolderCommon.create(Registries.BLOCK_ENTITY_TYPE, Identifier.parse("flopper:flopper"));

}
