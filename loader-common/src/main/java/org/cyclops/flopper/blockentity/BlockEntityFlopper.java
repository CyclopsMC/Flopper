package org.cyclops.flopper.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.cyclops.cyclopscore.blockentity.BlockEntityTickerDelayed;
import org.cyclops.cyclopscore.blockentity.CyclopsBlockEntityCommon;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.flopper.RegistryEntries;
import org.cyclops.flopper.block.BlockFlopper;
import org.cyclops.flopper.block.BlockFlopperConfig;

/**
 * Fluid hopper tile.
 * @author rubensworks
 */
public abstract class BlockEntityFlopper extends CyclopsBlockEntityCommon {

    @NBTPersist
    private int transferCooldown = -1;

    public BlockEntityFlopper(BlockPos blockPos, BlockState blockState) {
        super(RegistryEntries.BLOCK_ENTITY_FLOPPER.value(), blockPos, blockState);
    }

    public void setTransferCooldown(int ticks) {
        this.transferCooldown = ticks;
    }

    public int getTransferCooldown() {
        return transferCooldown;
    }

    protected Direction getFacing() {
        return getLevel().getBlockState(getBlockPos()).getValue(BlockFlopper.FACING);
    }

    /**
     * Push fluids from the inner tank to a target tank.
     * @return If some fluid was moved.
     */
    protected abstract boolean pushFluidsToTank();

    /**
     * Push fluids from a tank above the flopper to the inner tank.
     * @return If some fluid was moved.
     */
    protected abstract boolean pullFluidsFromTank();

    /**
     * Push fluids from the inner tank into the world at the target space.
     * @return If some fluid was moved.
     */
    protected abstract boolean pushFluidsToWorld();

    /**
     * Pull fluids from the world at the target space to the inner tank.
     * @return If some fluid was moved.
     */
    protected abstract boolean pullFluidsFromWorld();

    protected abstract boolean isTankEmpty();

    protected abstract boolean isTankFull();

    public abstract int getFluidAmount();

    public abstract int getFluidCapacity();

    public static class Ticker extends BlockEntityTickerDelayed<BlockEntityFlopper> {
        @Override
        protected void update(Level level, BlockPos pos, BlockState blockState, BlockEntityFlopper blockEntity) {
            super.update(level, pos, blockState, blockEntity);

            if (level != null && !level.isClientSide) {
                blockEntity.setTransferCooldown(blockEntity.getTransferCooldown() - 1);
                if (!this.isOnTransferCooldown(blockEntity)) {
                    blockEntity.setTransferCooldown(0);
                    this.updateHopper(level, pos, blockState, blockEntity);
                }
            }
        }

        private boolean isOnTransferCooldown(BlockEntityFlopper blockEntity) {
            return blockEntity.getTransferCooldown() > 0;
        }

        protected boolean updateHopper(Level level, BlockPos pos, BlockState blockState, BlockEntityFlopper blockEntity) {
            if (level != null && !level.isClientSide) {
                if (!this.isOnTransferCooldown(blockEntity) && IModHelpers.get().getBlockHelpers().getSafeBlockStateProperty(blockState, BlockFlopper.ENABLED, false)) {
                    boolean worked = false;
                    boolean workedWorld = false;

                    // Push fluids
                    if (!blockEntity.isTankEmpty()) {
                        worked = (BlockFlopperConfig.pushFluidRate > 0 && blockEntity.pushFluidsToTank())
                                || (workedWorld = (BlockFlopperConfig.pushFluidsWorld && blockEntity.pushFluidsToWorld()));
                    }

                    // Pull fluids
                    if (!blockEntity.isTankFull()) {
                        worked = (BlockFlopperConfig.pullFluidRate > 0 && blockEntity.pullFluidsFromTank())
                                || (workedWorld = (BlockFlopperConfig.pullFluidsWorld && blockEntity.pullFluidsFromWorld()) || workedWorld)
                                || worked;
                    }

                    if (worked) {
                        blockEntity.setTransferCooldown(workedWorld
                                ? BlockFlopperConfig.workWorldCooldown : BlockFlopperConfig.workCooldown);
                        blockEntity.setChanged();
                        return true;
                    }
                }

                return false;
            } else {
                return false;
            }
        }
    }
}
