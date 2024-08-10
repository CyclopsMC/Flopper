package org.cyclops.flopper.blockentity;

import com.google.common.collect.Sets;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.cyclops.cyclopscore.init.ModBaseForge;
import org.cyclops.flopper.FlopperForge;
import org.cyclops.flopper.RegistryEntries;
import org.cyclops.flopper.block.BlockFlopperConfig;
import org.cyclops.flopper.client.render.blockentity.RenderBlockEntityFlopperForge;

/**
 * @author rubensworks
 */
public class BlockEntityFlopperConfigForge extends BlockEntityFlopperConfig<BlockEntityFlopperForge, ModBaseForge<?>> {
    public BlockEntityFlopperConfigForge() {
        super(
                FlopperForge._instance,
                (eConfig) -> new BlockEntityType<>(BlockEntityFlopperForge::new,
                        Sets.newHashSet(RegistryEntries.BLOCK_FLOPPER.value()), null)
        );
    }

    @Override
    public void onRegistered() {
        super.onRegistered();
        if (getMod().getModHelpers().getMinecraftHelpers().isClientSide() && BlockFlopperConfig.renderFluid) {
            getMod().getProxy().registerRenderer(getInstance(), RenderBlockEntityFlopperForge::new);
        }
    }
}
