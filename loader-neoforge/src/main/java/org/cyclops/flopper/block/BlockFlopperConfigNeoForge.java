package org.cyclops.flopper.block;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import org.cyclops.cyclopscore.init.ModBaseNeoForge;
import org.cyclops.flopper.FlopperNeoForge;
import org.cyclops.flopper.blockentity.BlockEntityFlopperNeoForge;

/**
 * Config for the {@link BlockFlopper}.
 * @author rubensworks
 *
 */
public class BlockFlopperConfigNeoForge extends BlockFlopperConfig<ModBaseNeoForge<?>> {

    public BlockFlopperConfigNeoForge() {
        super(
                FlopperNeoForge._instance,
                (eConfig, properties) -> new BlockFlopperNeoForge(properties
                        .mapColor(MapColor.STONE)
                        .strength(3.0F, 4.8F)
                        .sound(SoundType.METAL), BlockEntityFlopperNeoForge::new)
        );
    }

}
