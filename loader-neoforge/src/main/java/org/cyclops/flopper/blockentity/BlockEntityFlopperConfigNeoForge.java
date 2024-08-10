package org.cyclops.flopper.blockentity;

import com.google.common.collect.Sets;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.flopper.Flopper;
import org.cyclops.flopper.RegistryEntries;
import org.cyclops.flopper.block.BlockFlopperConfig;
import org.cyclops.flopper.client.render.blockentity.RenderBlockEntityFlopperNeoForge;

/**
 * @author rubensworks
 */
public class BlockEntityFlopperConfigNeoForge extends BlockEntityFlopperConfig<BlockEntityFlopperNeoForge, ModBase<?>> {
    public BlockEntityFlopperConfigNeoForge() {
        super(
                Flopper._instance,
                (eConfig) -> new BlockEntityType<>(BlockEntityFlopperNeoForge::new,
                        Sets.newHashSet(RegistryEntries.BLOCK_FLOPPER.value()), null)
        );
        Flopper._instance.getModEventBus().addListener(this::registerCapabilities);
    }

    public void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                getInstance(),
                (blockEntity, context) -> blockEntity.getTank()
        );
    }

    @Override
    public void onRegistered() {
        super.onRegistered();
        if (getMod().getModHelpers().getMinecraftHelpers().isClientSide() && BlockFlopperConfig.renderFluid) {
            getMod().getProxy().registerRenderer(getInstance(), RenderBlockEntityFlopperNeoForge::new);
        }
    }
}
