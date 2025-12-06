package org.cyclops.flopper.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
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
public class FluidHandlerBlockNeoForgeInsertable extends VoidingResourceHandler<FluidResource> {

    private final BlockState state;
    private final Level world;
    private final BlockPos blockPos;
    private final StackJournal stackJournal;
    private FluidStack uncomittedFilled = FluidStack.EMPTY;

    public FluidHandlerBlockNeoForgeInsertable(BlockState state, Level world, BlockPos blockPos) {
        super(FluidResource.EMPTY);
        this.state = state;
        this.world = world;
        this.blockPos = blockPos;
        this.stackJournal = new StackJournal();
    }

    @Override
    public FluidResource getResource(int index) {
        return FluidResource.of(uncomittedFilled);
    }

    @Override
    public long getAmountAsLong(int index) {
        return uncomittedFilled.getAmount();
    }

    @Override
    public long getCapacityAsLong(int index, FluidResource resource) {
        return FlopperNeoForge._instance.getModHelpers().getFluidHelpers().getBucketVolume();
    }

    @Override
    public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
        if (amount < FlopperNeoForge._instance.getModHelpers().getFluidHelpers().getBucketVolume()) {
            return 0;
        }
        if (!uncomittedFilled.isEmpty()) {
            return 0;
        }
        this.stackJournal.updateSnapshots(transaction);
        this.uncomittedFilled = resource.toStack(FlopperNeoForge._instance.getModHelpers().getFluidHelpers().getBucketVolume());
        return FlopperNeoForge._instance.getModHelpers().getFluidHelpers().getBucketVolume();
    }

    private class StackJournal extends SnapshotJournal<FluidResource> {

        @Override
        protected FluidResource createSnapshot() {
            return FluidResource.of(FluidHandlerBlockNeoForgeInsertable.this.getResource(0).getFluid());
        }

        @Override
        protected void revertToSnapshot(FluidResource snapshot) {
            FluidHandlerBlockNeoForgeInsertable.this.uncomittedFilled = FluidStack.EMPTY;
        }

        @Override
        protected void onRootCommit(FluidResource originalState) {
            if (!FluidHandlerBlockNeoForgeInsertable.this.uncomittedFilled.isEmpty()) {
                // Inspired by NeoForge's old FluidUtil.destroyBlockOnFluidPlacement
                if (!FluidHandlerBlockNeoForgeInsertable.this.world.isClientSide()) {
                    BlockState destBlockState = FluidHandlerBlockNeoForgeInsertable.this.world.getBlockState(FluidHandlerBlockNeoForgeInsertable.this.blockPos);
                    boolean isDestNonSolid = !destBlockState.isSolid();
                    boolean isDestReplaceable = false;
                    if ((isDestNonSolid || isDestReplaceable) && !destBlockState.liquid()) {
                        FluidHandlerBlockNeoForgeInsertable.this.world.destroyBlock(FluidHandlerBlockNeoForgeInsertable.this.blockPos, true);
                    }
                }

                FluidHandlerBlockNeoForgeInsertable.this.world.setBlock(FluidHandlerBlockNeoForgeInsertable.this.blockPos, FluidHandlerBlockNeoForgeInsertable.this.state, 11);
                FluidHandlerBlockNeoForgeInsertable.this.uncomittedFilled = FluidStack.EMPTY;
            }
        }
    }
}
