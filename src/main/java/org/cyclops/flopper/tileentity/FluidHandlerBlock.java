package org.cyclops.flopper.tileentity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.fluid.Fluids;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.cyclops.cyclopscore.helper.FluidHelpers;

import javax.annotation.Nonnull;

import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

/**
 * A fluid handler that wraps around a fluid block for draining it,
 * it can not be filled.
 * @author rubensworks
 */
public class FluidHandlerBlock implements IFluidHandler {

    private final BlockState state;
    private final World world;
    private final BlockPos blockPos;

    public FluidHandlerBlock(BlockState state, World world, BlockPos blockPos) {
        this.state = state;
        this.world = world;
        this.blockPos = blockPos;
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Nonnull
    @Override
    public FluidStack getFluidInTank(int tank) {
        Block block = this.state.getBlock();
        if (block instanceof FlowingFluidBlock && this.state.getValue(FlowingFluidBlock.LEVEL) == 0) {
            return new FluidStack(((FlowingFluidBlock) block).getFluid(), FluidHelpers.BUCKET_VOLUME);
        } else if (this.state.hasProperty(BlockStateProperties.WATERLOGGED) && this.state.getValue(BlockStateProperties.WATERLOGGED)) {
            return new FluidStack(Fluids.WATER, FluidHelpers.BUCKET_VOLUME);
        } else {
            return FluidStack.EMPTY;
        }
    }

    @Override
    public int getTankCapacity(int tank) {
        return FluidHelpers.BUCKET_VOLUME;
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
        if (block instanceof FlowingFluidBlock
                && ((FlowingFluidBlock) block).getFluid() == resource.getFluid()) {
            return this.drain(resource.getAmount(), action);
        } else if (this.state.hasProperty(BlockStateProperties.WATERLOGGED)
                && this.state.getValue(BlockStateProperties.WATERLOGGED)
                && block instanceof IWaterLoggable) {
            return this.drain(resource.getAmount(), action);
        }
        return FluidStack.EMPTY;
    }

    @Nonnull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        Block block = this.state.getBlock();
        if (block instanceof FlowingFluidBlock
                && this.state.getValue(FlowingFluidBlock.LEVEL) == 0
                && maxDrain >= FluidHelpers.BUCKET_VOLUME) {
            if (action.execute()) {
                this.world.setBlock(this.blockPos, Blocks.AIR.defaultBlockState(), 11);
            }
            return new FluidStack(((FlowingFluidBlock) block).getFluid(), FluidHelpers.BUCKET_VOLUME);
        } else if (this.state.hasProperty(BlockStateProperties.WATERLOGGED)
                && this.state.getValue(BlockStateProperties.WATERLOGGED)
                && block instanceof IWaterLoggable
                && maxDrain >= FluidHelpers.BUCKET_VOLUME) {
            if (action.execute()) {
                ((IWaterLoggable) block).takeLiquid(world, blockPos, state);
            }
            return new FluidStack(Fluids.WATER, FluidHelpers.BUCKET_VOLUME);
        }
        return FluidStack.EMPTY;
    }
}
