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
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.cyclops.cyclopscore.helper.IFluidHelpersForge;
import org.cyclops.flopper.FlopperForge;

import javax.annotation.Nonnull;

/**
 * A fluid handler that wraps around a fluid block for draining it,
 * it can not be filled.
 * @author rubensworks
 */
public class FluidHandlerBlockForge implements IFluidHandler {

    private final BlockState state;
    private final Level world;
    private final BlockPos blockPos;

    public FluidHandlerBlockForge(BlockState state, Level world, BlockPos blockPos) {
        this.state = state;
        this.world = world;
        this.blockPos = blockPos;
    }

    protected IFluidHelpersForge getFluidHelpers() {
        return FlopperForge._instance.getModHelpers().getFluidHelpers();
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Nonnull
    @Override
    public FluidStack getFluidInTank(int tank) {
        Block block = this.state.getBlock();
        if (block instanceof LiquidBlock && this.state.getValue(LiquidBlock.LEVEL) == 0) {
            return new FluidStack(((LiquidBlock) block).getFluid(), getFluidHelpers().getBucketVolume());
        } else if (this.state.hasProperty(BlockStateProperties.WATERLOGGED) && this.state.getValue(BlockStateProperties.WATERLOGGED)) {
            return new FluidStack(Fluids.WATER, getFluidHelpers().getBucketVolume());
        } else {
            return FluidStack.EMPTY;
        }
    }

    @Override
    public int getTankCapacity(int tank) {
        return getFluidHelpers().getBucketVolume();
    }

    @Override
    public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
        return true;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return 0;
    }

    @Nonnull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        Block block = this.state.getBlock();
        if (block instanceof LiquidBlock
                && ((LiquidBlock) block).getFluid() == resource.getFluid()) {
            return this.drain(resource.getAmount(), action);
        } else if (this.state.hasProperty(BlockStateProperties.WATERLOGGED)
                && this.state.getValue(BlockStateProperties.WATERLOGGED)
                && block instanceof SimpleWaterloggedBlock) {
            return this.drain(resource.getAmount(), action);
        }
        return FluidStack.EMPTY;
    }

    @Nonnull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        Block block = this.state.getBlock();
        if (block instanceof LiquidBlock
                && this.state.getValue(LiquidBlock.LEVEL) == 0
                && maxDrain >= getFluidHelpers().getBucketVolume()) {
            if (action.execute()) {
                this.world.setBlock(this.blockPos, Blocks.AIR.defaultBlockState(), 11);
            }
            return new FluidStack(((LiquidBlock) block).getFluid(), getFluidHelpers().getBucketVolume());
        } else if (this.state.hasProperty(BlockStateProperties.WATERLOGGED)
                && this.state.getValue(BlockStateProperties.WATERLOGGED)
                && block instanceof SimpleWaterloggedBlock
                && maxDrain >= getFluidHelpers().getBucketVolume()) {
            if (action.execute()) {
                ((SimpleWaterloggedBlock) block).pickupBlock(null, world, blockPos, state);
            }
            return new FluidStack(Fluids.WATER, getFluidHelpers().getBucketVolume());
        }
        return FluidStack.EMPTY;
    }
}
