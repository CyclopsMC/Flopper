package org.cyclops.flopper;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import org.cyclops.cyclopscore.config.ConfigHandlerCommon;
import org.cyclops.cyclopscore.init.ModBaseForge;
import org.cyclops.cyclopscore.proxy.CommonProxyForge;
import org.cyclops.cyclopscore.proxy.IClientProxyCommon;
import org.cyclops.cyclopscore.proxy.ICommonProxyCommon;
import org.cyclops.flopper.block.BlockFlopperConfigForge;
import org.cyclops.flopper.blockentity.BlockEntityFlopperConfigForge;
import org.cyclops.flopper.proxy.ClientProxyForge;

/**
 * The main mod class of Flopper.
 * @author rubensworks
 *
 */
@Mod(Reference.MOD_ID)
public class FlopperForge extends ModBaseForge<FlopperForge> {

    /**
     * The unique instance of this mod.
     */
    public static FlopperForge _instance;

    public FlopperForge() {
        super(Reference.MOD_ID, (instance) -> _instance = instance);
    }

    @Override
    protected IClientProxyCommon constructClientProxy() {
        return new ClientProxyForge();
    }

    @Override
    protected ICommonProxyCommon constructCommonProxy() {
        return new CommonProxyForge();
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

        configHandler.addConfigurable(new BlockFlopperConfigForge());
        configHandler.addConfigurable(new BlockEntityFlopperConfigForge());
    }
}
