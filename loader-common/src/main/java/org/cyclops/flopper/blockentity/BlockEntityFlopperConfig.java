package org.cyclops.flopper.blockentity;

import net.minecraft.world.level.block.entity.BlockEntityType;
import org.cyclops.cyclopscore.config.extendedconfig.BlockEntityConfigCommon;
import org.cyclops.cyclopscore.init.IModBase;

import java.util.function.Function;

/**
 * Config for the {@link BlockEntityFlopper}.
 * @author rubensworks
 *
 */
public class BlockEntityFlopperConfig<BE extends BlockEntityFlopper, M extends IModBase> extends BlockEntityConfigCommon<BE, M> {

    public BlockEntityFlopperConfig(M mod, Function<BlockEntityConfigCommon<BE, M>, BlockEntityType<BE>> elementConstructor) {
        super(
                mod,
                "flopper",
                elementConstructor
        );
    }
}
