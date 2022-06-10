package org.cyclops.flopper;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.ObjectHolder;
import org.cyclops.flopper.blockentity.BlockEntityFlopper;

/**
 * Referenced registry entries.
 * @author rubensworks
 */
public class RegistryEntries {

    @ObjectHolder(registryName = "item", value = "flopper:flopper")
    public static final Item ITEM_FLOPPER = null;
    @ObjectHolder(registryName = "block", value = "flopper:flopper")
    public static final Block BLOCK_FLOPPER = null;
    @ObjectHolder(registryName = "block_entity_type", value = "flopper:flopper")
    public static final BlockEntityType<BlockEntityFlopper> BLOCK_ENTITY_FLOPPER = null;

}
