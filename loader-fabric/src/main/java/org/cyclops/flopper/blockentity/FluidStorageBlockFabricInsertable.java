package org.cyclops.flopper.blockentity;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A fluid handler that wraps around a fluid block for draining it,
 * it can not be filled.
 * @author rubensworks
 */
public class FluidStorageBlockFabricInsertable extends SingleVariantStorage<FluidVariant> {

    private final BlockState state;
    private final Level world;
    private final BlockPos blockPos;

    public FluidStorageBlockFabricInsertable(BlockState state, Level world, BlockPos blockPos, FluidVariant fluidVariant) {
        this.state = state;
        this.world = world;
        this.blockPos = blockPos;
        this.variant = fluidVariant;
    }

    @Override
    public long insert(FluidVariant insertedVariant, long maxAmount, TransactionContext transaction) {
        if (maxAmount < FluidConstants.BLOCK) {
            return 0;
        } else {
            return super.insert(insertedVariant, FluidConstants.BLOCK, transaction);
        }
    }

    @Override
    protected boolean canExtract(FluidVariant variant) {
        return false;
    }

    @Override
    protected FluidVariant getBlankVariant() {
        return FluidVariant.blank();
    }

    @Override
    protected long getCapacity(FluidVariant variant) {
        return FluidConstants.BLOCK;
    }

    @Override
    protected void onFinalCommit() {
        super.onFinalCommit();

        if (getAmount() == FluidConstants.BLOCK) {
            destroyBlockOnFluidPlacement(this.world, this.blockPos);
            this.world.setBlock(this.blockPos, this.state, 11);
        }
    }

    public static void destroyBlockOnFluidPlacement(Level level, BlockPos pos) { // Adapted from Forge's FluidUtil.destroyBlockOnFluidPlacement
        if (!level.isClientSide) {
            BlockState destBlockState = level.getBlockState(pos);
            boolean isDestNonSolid = !destBlockState.isSolid();
            if (isDestNonSolid && !destBlockState.liquid()) {
                level.destroyBlock(pos, true);
            }
        }
    }
}
