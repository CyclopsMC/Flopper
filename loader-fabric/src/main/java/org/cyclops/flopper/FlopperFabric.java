package org.cyclops.flopper;

import net.fabricmc.api.ModInitializer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.cyclops.cyclopscore.config.ConfigHandlerCommon;
import org.cyclops.cyclopscore.init.ModBaseFabric;
import org.cyclops.cyclopscore.proxy.IClientProxyCommon;
import org.cyclops.cyclopscore.proxy.ICommonProxyCommon;
import org.cyclops.flopper.block.BlockFlopperConfigFabric;
import org.cyclops.flopper.blockentity.BlockEntityFlopperConfigFabric;
import org.cyclops.flopper.proxy.ClientProxyFabric;
import org.cyclops.flopper.proxy.CommonProxyFabric;

/**
 * The main mod class of Flopper.
 * @author rubensworks
 */
public class FlopperFabric extends ModBaseFabric<FlopperFabric> implements ModInitializer {

    /**
     * The unique instance of this mod.
     */
    public static FlopperFabric _instance;

    public FlopperFabric() {
        super(Reference.MOD_ID, (instance) -> _instance = instance);
    }

    @Override
    protected IClientProxyCommon constructClientProxy() {
        return new ClientProxyFabric();
    }

    @Override
    protected ICommonProxyCommon constructCommonProxy() {
        return new CommonProxyFabric();
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
    protected void onConfigsRegister(ConfigHandlerCommon configHandler) {
        super.onConfigsRegister(configHandler);

        configHandler.addConfigurable(new GeneralConfig(this));

        configHandler.addConfigurable(new BlockFlopperConfigFabric());
        configHandler.addConfigurable(new BlockEntityFlopperConfigFabric());
    }
}
