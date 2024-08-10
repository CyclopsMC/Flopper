package org.cyclops.flopper;

import net.fabricmc.api.ModInitializer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.cyclops.cyclopscore.Reference;
import org.cyclops.cyclopscore.config.ConfigHandler;
import org.cyclops.cyclopscore.init.ModBaseFabric;
import org.cyclops.cyclopscore.proxy.IClientProxyCommon;
import org.cyclops.cyclopscore.proxy.ICommonProxyCommon;
import org.cyclops.flopper.block.BlockFlopperConfig;
import org.cyclops.flopper.blockentity.BlockEntityFlopperConfig;
import org.cyclops.flopper.proxy.ClientProxyFabric;
import org.cyclops.flopper.proxy.CommonProxyFabric;

/**
 * The main mod class of Flopper.
 * @author rubensworks
 */
public class FlopperMainFabric extends ModBaseFabric<FlopperMainFabric> implements ModInitializer {

    /**
     * The unique instance of this mod.
     */
    public static FlopperMainFabric _instance;

    public FlopperMainFabric() {
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
    protected void onConfigsRegister(ConfigHandler configHandler) {
        super.onConfigsRegister(configHandler);

        configHandler.addConfigurable(new BlockFlopperConfig<>(this, null));
        configHandler.addConfigurable(new BlockEntityFlopperConfig<>(this, null));
    }
}
