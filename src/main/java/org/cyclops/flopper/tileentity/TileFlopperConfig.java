package org.cyclops.flopper.tileentity;

import com.google.common.collect.Sets;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.config.extendedconfig.TileEntityConfig;
import org.cyclops.flopper.Flopper;
import org.cyclops.flopper.RegistryEntries;
import org.cyclops.flopper.block.BlockFlopperConfig;
import org.cyclops.flopper.client.render.tileentity.RenderTileEntityFlopper;

/**
 * Config for the {@link TileFlopper}.
 * @author rubensworks
 *
 */
public class TileFlopperConfig extends TileEntityConfig<TileFlopper> {

    public TileFlopperConfig() {
        super(
                Flopper._instance,
                "flopper",
                (eConfig) -> new TileEntityType<>(TileFlopper::new,
                        Sets.newHashSet(RegistryEntries.BLOCK_FLOPPER), null)
        );
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onRegistered() {
        super.onRegistered();
        if (BlockFlopperConfig.renderFluid) {
            getMod().getProxy().registerRenderer(getInstance(), RenderTileEntityFlopper::new);
        }
    }
}
