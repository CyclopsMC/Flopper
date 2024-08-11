package org.cyclops.flopper;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.apache.logging.log4j.Level;
import org.cyclops.cyclopscore.config.ConfigHandler;
import org.cyclops.cyclopscore.init.ModBaseVersionable;
import org.cyclops.cyclopscore.proxy.IClientProxy;
import org.cyclops.cyclopscore.proxy.ICommonProxy;
import org.cyclops.flopper.block.BlockFlopperConfigNeoForge;
import org.cyclops.flopper.blockentity.BlockEntityFlopperConfigNeoForge;
import org.cyclops.flopper.proxy.ClientProxyNeoForge;
import org.cyclops.flopper.proxy.CommonProxyNeoForge;

/**
 * The main mod class of this mod.
 * @author rubensworks (aka kroeserr)
 *
 */
@Mod(Reference.MOD_ID)
public class FlopperNeoForge extends ModBaseVersionable<FlopperNeoForge> {

    /**
     * The unique instance of this mod.
     */
    public static FlopperNeoForge _instance;

    public FlopperNeoForge(IEventBus modEventBus) {
        super(Reference.MOD_ID, (instance) -> _instance = instance, modEventBus);
    }

    @Override
    protected IClientProxy constructClientProxy() {
        return new ClientProxyNeoForge();
    }

    @Override
    protected ICommonProxy constructCommonProxy() {
        return new CommonProxyNeoForge();
    }

    @Override
    protected boolean hasDefaultCreativeModeTab() {
        return true;
    }

    @Override
    protected CreativeModeTab.Builder constructDefaultCreativeModeTab(CreativeModeTab.Builder builder) {
        return super.constructDefaultCreativeModeTab(builder)
                .icon(() -> new ItemStack(RegistryEntries.ITEM_FLOPPER));
    }

    @Override
    protected void onConfigsRegister(ConfigHandler configHandler) {
        super.onConfigsRegister(configHandler);

        configHandler.addConfigurable(new GeneralConfig());

        configHandler.addConfigurable(new BlockFlopperConfigNeoForge());
        configHandler.addConfigurable(new BlockEntityFlopperConfigNeoForge());
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
        FlopperNeoForge._instance.getLoggerHelper().log(level, message);
    }

}
