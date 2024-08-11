package org.cyclops.flopper.blockentity;

import com.google.common.collect.Sets;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.cyclops.cyclopscore.init.ModBaseFabric;
import org.cyclops.flopper.FlopperFabric;
import org.cyclops.flopper.RegistryEntries;
import org.cyclops.flopper.block.BlockFlopperConfig;
import org.cyclops.flopper.client.render.blockentity.RenderBlockEntityFlopperFabric;

/**
 * @author rubensworks
 */
public class BlockEntityFlopperConfigFabric extends BlockEntityFlopperConfig<BlockEntityFlopperFabric, ModBaseFabric<?>> {
    public BlockEntityFlopperConfigFabric() {
        super(
                FlopperFabric._instance,
                (eConfig) -> new BlockEntityType<>(BlockEntityFlopperFabric::new,
                        Sets.newHashSet(RegistryEntries.BLOCK_FLOPPER.value()), null)
        );
    }

    @Override
    public void onForgeRegistered() {
        super.onForgeRegistered();

        // Fluid capability
        FluidStorage.SIDED.registerForBlockEntity((blockEntity, direction) -> blockEntity.getTank(), getInstance());

        // Rendering
        if (getMod().getModHelpers().getMinecraftHelpers().isClientSide() && BlockFlopperConfig.renderFluid) {
            getMod().getProxy().registerRenderer(getInstance(), RenderBlockEntityFlopperFabric::new);
        }
    }
}
