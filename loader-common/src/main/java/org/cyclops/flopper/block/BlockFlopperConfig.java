package org.cyclops.flopper.block;

import net.minecraft.world.level.block.Block;
import org.cyclops.cyclopscore.config.ConfigurablePropertyCommon;
import org.cyclops.cyclopscore.config.ModConfigLocation;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfigCommon;
import org.cyclops.cyclopscore.init.IModBase;

import java.util.function.Function;

/**
 * Config for the {@link BlockFlopper}.
 * @author rubensworks
 *
 */
public class BlockFlopperConfig<M extends IModBase> extends BlockConfigCommon<M> {

    @ConfigurablePropertyCommon(category = "machine", comment = "The maximum capacity in mB.", isCommandable = true, requiresMcRestart = true, configLocation = ModConfigLocation.SERVER)
    public static int capacityMb = 5000;

    @ConfigurablePropertyCommon(category = "machine", comment = "The rate at which fluids can be pulled from other tanks.", isCommandable = true, configLocation = ModConfigLocation.SERVER)
    public static int pullFluidRate = 100;

    @ConfigurablePropertyCommon(category = "machine", comment = "The rate at which fluids can be pushed to other tanks.", isCommandable = true, configLocation = ModConfigLocation.SERVER)
    public static int pushFluidRate = 100;

    @ConfigurablePropertyCommon(category = "machine", comment = "If fluids should be pulled from the world.", isCommandable = true, configLocation = ModConfigLocation.SERVER)
    public static boolean pullFluidsWorld = true;

    @ConfigurablePropertyCommon(category = "machine", comment = "If fluids should be pushed into the world.", isCommandable = true, configLocation = ModConfigLocation.SERVER)
    public static boolean pushFluidsWorld = true;

    @ConfigurablePropertyCommon(category = "machine", comment = "If sounds should be placed when placing or picking up fluids to/from the world.", isCommandable = true, configLocation = ModConfigLocation.CLIENT)
    public static boolean worldPullPushSounds = true;

    @ConfigurablePropertyCommon(category = "machine", comment = "If neighbour-change events should be triggered when placing or picking up fluids to/from the world.", isCommandable = true, configLocation = ModConfigLocation.SERVER)
    public static boolean worldPullPushNeighbourEvents = true;

    @ConfigurablePropertyCommon(category = "machine", comment = "The number of ticks each flopper should sleep after interacting with tanks", isCommandable = true, configLocation = ModConfigLocation.SERVER)
    public static int workCooldown = 8;

    @ConfigurablePropertyCommon(category = "machine", comment = "The number of ticks each flopper should sleep after picking up or placing fluids.", isCommandable = true, configLocation = ModConfigLocation.SERVER)
    public static int workWorldCooldown = 20;

    @ConfigurablePropertyCommon(category = "machine", comment = "If the contained fluid should be rendered.", isCommandable = true, requiresMcRestart = true, configLocation = ModConfigLocation.CLIENT)
    public static boolean renderFluid = true;

    @ConfigurablePropertyCommon(category = "machine", comment = "If a status message with the flopper contents should be shown to the player on right click without an item.", isCommandable = true)
    public static boolean showContentsStatusMessageOnClick = true;

    public BlockFlopperConfig(M mod, Function<BlockConfigCommon<M>, ? extends Block> blockConstructor) {
        super(
                mod,
                "flopper",
                blockConstructor,
                BlockConfigCommon.getDefaultItemConstructor(mod)
        );
    }

}
