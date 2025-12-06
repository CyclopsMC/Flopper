package org.cyclops.flopper.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.transfer.VoidingResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.cyclops.flopper.FlopperNeoForge;

/**
 * A fluid handler that wraps around a fluid block for draining it,
 * it can not be filled.
 * @author rubensworks
 */
public class FluidHandlerBlockNeoForgeExtractable extends VoidingResourceHandler<FluidResource> {

    private final BlockState state;
    private final Level world;
    private final BlockPos blockPos;
    private final StackJournal stackJournal;
    private boolean uncomittedEmpty = false;

    public FluidHandlerBlockNeoForgeExtractable(BlockState state, Level world, BlockPos blockPos) {
        super(FluidResource.EMPTY);
        this.state = state;
        this.world = world;
        this.blockPos = blockPos;
        this.stackJournal = new StackJournal();
    }

    @Override
    public FluidResource getResource(int index) {
        if (this.uncomittedEmpty) {
            return FluidResource.EMPTY;
        }
        Block block = this.state.getBlock();
        if (block instanceof LiquidBlock && this.state.getValue(LiquidBlock.LEVEL) == 0) {
            return FluidResource.of(((LiquidBlock) block).fluid);
        } else if (this.state.hasProperty(BlockStateProperties.WATERLOGGED) && this.state.getValue(BlockStateProperties.WATERLOGGED)) {
            return FluidResource.of(Fluids.WATER);
        } else {
            return FluidResource.EMPTY;
        }
    }

    @Override
    public long getAmountAsLong(int index) {
        if (this.uncomittedEmpty) {
            return 0;
        }
        Block block = this.state.getBlock();
        if (block instanceof LiquidBlock && this.state.getValue(LiquidBlock.LEVEL) == 0) {
            return FlopperNeoForge._instance.getModHelpers().getFluidHelpers().getBucketVolume();
        } else if (this.state.hasProperty(BlockStateProperties.WATERLOGGED) && this.state.getValue(BlockStateProperties.WATERLOGGED)) {
            return FlopperNeoForge._instance.getModHelpers().getFluidHelpers().getBucketVolume();
        } else {
            return 0;
        }
    }

    @Override
    public long getCapacityAsLong(int index, FluidResource resource) {
        return getAmountAsLong(index);
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
        if (this.uncomittedEmpty) {
            return 0;
        }
        Block block = this.state.getBlock();
        if (block instanceof LiquidBlock
                && this.state.getValue(LiquidBlock.LEVEL) == 0
                && amount >= FlopperNeoForge._instance.getModHelpers().getFluidHelpers().getBucketVolume()) {
            this.stackJournal.updateSnapshots(transaction);
            this.uncomittedEmpty = true;
            return FlopperNeoForge._instance.getModHelpers().getFluidHelpers().getBucketVolume();
        } else if (this.state.hasProperty(BlockStateProperties.WATERLOGGED)
                && this.state.getValue(BlockStateProperties.WATERLOGGED)
                && block instanceof SimpleWaterloggedBlock
                && amount >= FlopperNeoForge._instance.getModHelpers().getFluidHelpers().getBucketVolume()) {
            this.stackJournal.updateSnapshots(transaction);
            this.uncomittedEmpty = true;
            return FlopperNeoForge._instance.getModHelpers().getFluidHelpers().getBucketVolume();
        }
        return 0;
    }

    private class StackJournal extends SnapshotJournal<FluidResource> {

        @Override
        protected FluidResource createSnapshot() {
            return FluidResource.of(FluidHandlerBlockNeoForgeExtractable.this.getResource(0).getFluid());
        }

        @Override
        protected void revertToSnapshot(FluidResource snapshot) {
            FluidHandlerBlockNeoForgeExtractable.this.uncomittedEmpty = false;
        }

        @Override
        protected void onRootCommit(FluidResource originalState) {
            if (FluidHandlerBlockNeoForgeExtractable.this.uncomittedEmpty) {
                Block block = FluidHandlerBlockNeoForgeExtractable.this.state.getBlock();
                if (block instanceof LiquidBlock) {
                    FluidHandlerBlockNeoForgeExtractable.this.world.setBlock(FluidHandlerBlockNeoForgeExtractable.this.blockPos, Blocks.AIR.defaultBlockState(), 11);
                } else if (FluidHandlerBlockNeoForgeExtractable.this.state.hasProperty(BlockStateProperties.WATERLOGGED)) {
                    ((SimpleWaterloggedBlock) block).pickupBlock(null, world, blockPos, state);
                }
                FluidHandlerBlockNeoForgeExtractable.this.uncomittedEmpty = false;
            }
        }
    }
}
