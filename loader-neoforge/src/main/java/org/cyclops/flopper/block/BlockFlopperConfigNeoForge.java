package org.cyclops.flopper.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.flopper.Flopper;
import org.cyclops.flopper.blockentity.BlockEntityFlopperNeoForge;

/**
 * Config for the {@link BlockFlopper}.
 * @author rubensworks
 *
 */
public class BlockFlopperConfigNeoForge extends BlockFlopperConfig<ModBase<?>> {

    public BlockFlopperConfigNeoForge() {
        super(
                Flopper._instance,
                eConfig -> new BlockFlopperNeoForge(Block.Properties.of()
                        .mapColor(MapColor.STONE)
                        .strength(3.0F, 4.8F)
                        .sound(SoundType.METAL), BlockEntityFlopperNeoForge::new)
        );
    }

}
