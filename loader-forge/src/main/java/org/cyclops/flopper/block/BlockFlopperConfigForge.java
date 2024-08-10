package org.cyclops.flopper.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import org.cyclops.cyclopscore.init.ModBaseForge;
import org.cyclops.flopper.FlopperForge;
import org.cyclops.flopper.blockentity.BlockEntityFlopperForge;

/**
 * Config for the {@link BlockFlopper}.
 * @author rubensworks
 *
 */
public class BlockFlopperConfigForge extends BlockFlopperConfig<ModBaseForge<?>> {

    public BlockFlopperConfigForge() {
        super(
                FlopperForge._instance,
                eConfig -> new BlockFlopperForge(Block.Properties.of()
                        .mapColor(MapColor.STONE)
                        .strength(3.0F, 4.8F)
                        .sound(SoundType.METAL), BlockEntityFlopperForge::new)
        );
    }

}
