package org.cyclops.flopper.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraftforge.fml.config.ModConfig;
import org.cyclops.cyclopscore.config.ConfigurableProperty;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.flopper.Flopper;
import org.cyclops.flopper.tileentity.TileFlopper;

/**
 * Config for the {@link BlockFlopper}.
 * @author rubensworks
 *
 */
public class BlockFlopperConfig extends BlockConfig {

    @ConfigurableProperty(category = "machine", comment = "The maximum capacity in mB.", isCommandable = true, requiresMcRestart = true, configLocation = ModConfig.Type.SERVER)
    public static int capacityMb = 5000;

    @ConfigurableProperty(category = "machine", comment = "The rate at which fluids can be pulled from other tanks.", isCommandable = true, configLocation = ModConfig.Type.SERVER)
    public static int pullFluidRate = 100;

    @ConfigurableProperty(category = "machine", comment = "The rate at which fluids can be pushed to other tanks.", isCommandable = true, configLocation = ModConfig.Type.SERVER)
    public static int pushFluidRate = 100;

    @ConfigurableProperty(category = "machine", comment = "If fluids should be pulled from the world.", isCommandable = true, configLocation = ModConfig.Type.SERVER)
    public static boolean pullFluidsWorld = true;

    @ConfigurableProperty(category = "machine", comment = "If fluids should be pushed into the world.", isCommandable = true, configLocation = ModConfig.Type.SERVER)
    public static boolean pushFluidsWorld = true;

    @ConfigurableProperty(category = "machine", comment = "If sounds should be placed when placing or picking up fluids to/from the world.", isCommandable = true, configLocation = ModConfig.Type.CLIENT)
    public static boolean worldPullPushSounds = true;

    @ConfigurableProperty(category = "machine", comment = "If neighbour-change events should be triggered when placing or picking up fluids to/from the world.", isCommandable = true, configLocation = ModConfig.Type.SERVER)
    public static boolean worldPullPushNeighbourEvents = true;

    @ConfigurableProperty(category = "machine", comment = "The number of ticks each flopper should sleep after interacting with tanks", isCommandable = true, configLocation = ModConfig.Type.SERVER)
    public static int workCooldown = 8;

    @ConfigurableProperty(category = "machine", comment = "The number of ticks each flopper should sleep after picking up or placing fluids.", isCommandable = true, configLocation = ModConfig.Type.SERVER)
    public static int workWorldCooldown = 20;

    @ConfigurableProperty(category = "machine", comment = "If the contained fluid should be rendered.", isCommandable = true, requiresMcRestart = true, configLocation = ModConfig.Type.CLIENT)
    public static boolean renderFluid = true;

    @ConfigurableProperty(category = "machine", comment = "If a status message with the flopper contents should be shown to the player on right click without an item.", isCommandable = true)
    public static boolean showContentsStatusMessageOnClick = true;

    public BlockFlopperConfig() {
        super(
                Flopper._instance,
                "flopper",
                eConfig -> new BlockFlopper(Block.Properties
                        .create(Material.IRON, MaterialColor.STONE)
                        .hardnessAndResistance(3.0F, 4.8F)
                        .sound(SoundType.METAL), TileFlopper::new),
                getDefaultItemConstructor(Flopper._instance)
        );
    }

}
