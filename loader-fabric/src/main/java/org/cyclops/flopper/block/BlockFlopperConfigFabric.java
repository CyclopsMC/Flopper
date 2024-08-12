package org.cyclops.flopper.block;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import org.cyclops.cyclopscore.config.ConfigurablePropertyCommon;
import org.cyclops.cyclopscore.config.ModConfigLocation;
import org.cyclops.cyclopscore.init.ModBaseFabric;
import org.cyclops.flopper.FlopperFabric;
import org.cyclops.flopper.blockentity.BlockEntityFlopperFabric;

/**
 * Config for the {@link BlockFlopper}.
 * @author rubensworks
 *
 */
public class BlockFlopperConfigFabric extends BlockFlopperConfig<ModBaseFabric<?>> {

    @ConfigurablePropertyCommon(category = "machine", comment = "The maximum capacity in mB.", isCommandable = true, requiresMcRestart = true, configLocation = ModConfigLocation.SERVER)
    public static int capacityDroplets = 5 * (int) FluidConstants.BUCKET;

    @ConfigurablePropertyCommon(category = "machine", comment = "The rate at which fluids can be pulled from other tanks.", isCommandable = true, configLocation = ModConfigLocation.SERVER)
    public static int pullFluidRateDroplets = (int) FluidConstants.BUCKET / 10;

    @ConfigurablePropertyCommon(category = "machine", comment = "The rate at which fluids can be pushed to other tanks.", isCommandable = true, configLocation = ModConfigLocation.SERVER)
    public static int pushFluidRateDroplets = (int) FluidConstants.BUCKET / 10;

    public BlockFlopperConfigFabric() {
        super(
                FlopperFabric._instance,
                eConfig -> new BlockFlopperFabric(Block.Properties.of()
                        .mapColor(MapColor.STONE)
                        .strength(3.0F, 4.8F)
                        .sound(SoundType.METAL), BlockEntityFlopperFabric::new)
        );
    }

}
