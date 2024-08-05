package org.cyclops.flopper.blockentity;

import com.google.common.collect.Sets;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.cyclops.cyclopscore.config.extendedconfig.BlockEntityConfig;
import org.cyclops.flopper.Flopper;
import org.cyclops.flopper.RegistryEntries;
import org.cyclops.flopper.block.BlockFlopperConfig;
import org.cyclops.flopper.client.render.blockentity.RenderBlockEntityFlopper;

/**
 * Config for the {@link BlockEntityFlopper}.
 * @author rubensworks
 *
 */
public class BlockEntityFlopperConfig extends BlockEntityConfig<BlockEntityFlopper> {

    public BlockEntityFlopperConfig() {
        super(
                Flopper._instance,
                "flopper",
                (eConfig) -> new BlockEntityType<>(BlockEntityFlopper::new,
                        Sets.newHashSet(RegistryEntries.BLOCK_FLOPPER.get()), null)
        );
        Flopper._instance.getModEventBus().addListener(this::registerCapabilities);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onRegistered() {
        super.onRegistered();
        if (BlockFlopperConfig.renderFluid) {
            getMod().getProxy().registerRenderer(getInstance(), RenderBlockEntityFlopper::new);
        }
    }

    public void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                getInstance(),
                (blockEntity, context) -> blockEntity.getTank()
        );
    }
}
