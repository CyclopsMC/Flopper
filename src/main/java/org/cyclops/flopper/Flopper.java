package org.cyclops.flopper;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.Level;
import org.cyclops.cyclopscore.config.ConfigHandler;
import org.cyclops.cyclopscore.init.ItemGroupMod;
import org.cyclops.cyclopscore.init.ModBaseVersionable;
import org.cyclops.cyclopscore.proxy.IClientProxy;
import org.cyclops.cyclopscore.proxy.ICommonProxy;
import org.cyclops.flopper.block.BlockFlopperConfig;
import org.cyclops.flopper.proxy.ClientProxy;
import org.cyclops.flopper.proxy.CommonProxy;
import org.cyclops.flopper.blockentity.BlockEntityFlopperConfig;

/**
 * The main mod class of this mod.
 * @author rubensworks (aka kroeserr)
 *
 */
@Mod(Reference.MOD_ID)
public class Flopper extends ModBaseVersionable<Flopper> {

    /**
     * The unique instance of this mod.
     */
    public static Flopper _instance;

    public Flopper() {
        super(Reference.MOD_ID, (instance) -> _instance = instance);
    }

    @Override
    protected IClientProxy constructClientProxy() {
        return new ClientProxy();
    }

    @Override
    protected ICommonProxy constructCommonProxy() {
        return new CommonProxy();
    }

    @Override
    protected CreativeModeTab constructDefaultCreativeModeTab() {
        return new ItemGroupMod(this, () -> RegistryEntries.ITEM_FLOPPER);
    }

    @Override
    protected void onConfigsRegister(ConfigHandler configHandler) {
        super.onConfigsRegister(configHandler);

        configHandler.addConfigurable(new GeneralConfig());

        configHandler.addConfigurable(new BlockFlopperConfig());
        configHandler.addConfigurable(new BlockEntityFlopperConfig());
    }

    /**
     * Log a new info message for this mod.
     * @param message The message to show.
     */
    public static void clog(String message) {
        clog(Level.INFO, message);
    }

    /**
     * Log a new message of the given level for this mod.
     * @param level The level in which the message must be shown.
     * @param message The message to show.
     */
    public static void clog(Level level, String message) {
        Flopper._instance.getLoggerHelper().log(level, message);
    }

}
