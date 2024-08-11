package org.cyclops.flopper.blockentity;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;

/**
 * A fluid handler that wraps around a fluid block for draining it,
 * it can not be filled.
 * @author rubensworks
 */
public class FluidStorageBlockFabricExtractable extends SingleVariantStorage<FluidVariant> {

    private final BlockState state;
    private final Level world;
    private final BlockPos blockPos;

    public FluidStorageBlockFabricExtractable(BlockState state, Level world, BlockPos blockPos) {
        this.state = state;
        this.world = world;
        this.blockPos = blockPos;

        // Re-init this.variant after defining this.state
        initializeState();
    }

    @Override
    public long extract(FluidVariant extractedVariant, long maxAmount, TransactionContext transaction) {
        Block block = this.state.getBlock();

        if (block instanceof LiquidBlock
                && ((LiquidBlock) block).fluid.isSame(extractedVariant.getFluid())
                && this.state.getValue(LiquidBlock.LEVEL) == 0) {
            return super.extract(extractedVariant, FluidConstants.BLOCK, transaction);
        } else if (this.state.hasProperty(BlockStateProperties.WATERLOGGED)
                && this.state.getValue(BlockStateProperties.WATERLOGGED)
                && block instanceof SimpleWaterloggedBlock) {
            return super.extract(extractedVariant, FluidConstants.BLOCK, transaction);
        }
        return 0;
    }

    @Override
    protected boolean canInsert(FluidVariant variant) {
        return false;
    }

    @Override
    protected FluidVariant getBlankVariant() {
        return FluidVariant.blank();
    }

    protected void initializeState() {
        Block block = this.state.getBlock();
        if (block instanceof LiquidBlock && this.state.getValue(LiquidBlock.LEVEL) == 0) {
            this.amount = FluidConstants.BLOCK;
            this.variant = FluidVariant.of(((LiquidBlock) block).fluid);
        } else if (this.state.hasProperty(BlockStateProperties.WATERLOGGED) && this.state.getValue(BlockStateProperties.WATERLOGGED)) {
            this.amount = FluidConstants.BLOCK;
            this.variant = FluidVariant.of(Fluids.WATER);
        } else {
            this.amount = 0;
            this.variant = FluidVariant.blank();
        }
    }

    @Override
    protected long getCapacity(FluidVariant variant) {
        return FluidConstants.BLOCK;
    }

    @Override
    protected void onFinalCommit() {
        super.onFinalCommit();

        if (getAmount() == 0) {
            Block block = this.state.getBlock();
            if (block instanceof LiquidBlock
                    && this.state.getValue(LiquidBlock.LEVEL) == 0) {
                this.world.setBlock(this.blockPos, Blocks.AIR.defaultBlockState(), 11);
            } else if (this.state.hasProperty(BlockStateProperties.WATERLOGGED)
                    && this.state.getValue(BlockStateProperties.WATERLOGGED)
                    && block instanceof SimpleWaterloggedBlock) {
                ((SimpleWaterloggedBlock) block).pickupBlock(null, world, blockPos, state);
            }
        }
    }
}
