package org.cyclops.flopper.block;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.config.ConfigurableProperty;
import org.cyclops.cyclopscore.config.ConfigurableTypeCategory;
import org.cyclops.cyclopscore.config.extendedconfig.BlockContainerConfig;
import org.cyclops.flopper.Flopper;
import org.cyclops.flopper.client.render.tileentity.RenderTileEntityFlopper;
import org.cyclops.flopper.tileentity.TileFlopper;

/**
 * Config for the {@link BlockFlopper}.
 * @author rubensworks
 *
 */
public class BlockFlopperConfig extends BlockContainerConfig {

    /**
     * The unique instance.
     */
    public static BlockFlopperConfig _instance;

    /**
     * The maximum capacity in mB.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.MACHINE, comment = "The maximum capacity in mB.", isCommandable = true, requiresMcRestart = true)
    public static int capacityMb = 5000;

    /**
     * The rate at which fluids can be pulled from other tanks per tick.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.MACHINE, comment = "The rate at which fluids can be pulled from other tanks.", isCommandable = true)
    public static int pullFluidRate = 100;

    /**
     * The rate at which fluids can be pushed to other tanks per tick.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.MACHINE, comment = "The rate at which fluids can be pushed to other tanks.", isCommandable = true)
    public static int pushFluidRate = 100;

    /**
     * If fluids should be pulled from the world.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.MACHINE, comment = "If fluids should be pulled from the world.", isCommandable = true)
    public static boolean pullFluidsWorld = true;

    /**
     * If fluids should be pushed into the world.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.MACHINE, comment = "If fluids should be pushed into the world.", isCommandable = true)
    public static boolean pushFluidsWorld = true;

    /**
     * If sounds should be placed when placing or picking up fluids to/from the world.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.CORE, comment = "If sounds should be placed when placing or picking up fluids to/from the world.", isCommandable = true)
    public static boolean worldPullPushSounds = true;

    /**
     * If neighbour-change events should be triggered when placing or picking up fluids to/from the world.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.CORE, comment = "If neighbour-change events should be triggered when placing or picking up fluids to/from the world.", isCommandable = true)
    public static boolean worldPullPushNeighbourEvents = true;

    /**
     * The number of ticks each flopper should sleep after interacting with tanks.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.MACHINE, comment = "The number of ticks each flopper should sleep after interacting with tanks", isCommandable = true)
    public static int workCooldown = 8;

    /**
     * The number of ticks each flopper should sleep after picking up or placing fluids.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.MACHINE, comment = "The number of ticks each flopper should sleep after picking up or placing fluids.", isCommandable = true)
    public static int workWorldCooldown = 20;

    /**
     * If the contained fluid should be rendered.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.CORE, comment = "If the contained fluid should be rendered.", isCommandable = true, requiresMcRestart = true)
    public static boolean renderFluid = true;

    /**
     * If the collision boxes should be made smaller to allow clicking behind the flopper.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.CORE, comment = "If the collision boxes should be made smaller to allow clicking behind the flopper.", isCommandable = true)
    public static boolean narrowCollision = true;

    /**
     * Make a new instance.
     */
    public BlockFlopperConfig() {
        super(
                Flopper._instance,
                true,
                "flopper",
                null,
                BlockFlopper.class
        );
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onRegistered() {
        super.onRegistered();
        if (renderFluid) {
            getMod().getProxy().registerRenderer(TileFlopper.class, new RenderTileEntityFlopper());
        }
    }
}
