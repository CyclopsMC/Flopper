package org.cyclops.flopper.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.wrappers.BlockWrapper;
import net.minecraftforge.common.SoundActions;
import org.cyclops.cyclopscore.blockentity.BlockEntityTickerDelayed;
import org.cyclops.cyclopscore.blockentity.CyclopsBlockEntity;
import org.cyclops.cyclopscore.fluid.SingleUseTank;
import org.cyclops.cyclopscore.fluid.Tank;
import org.cyclops.cyclopscore.helper.BlockEntityHelpers;
import org.cyclops.cyclopscore.helper.BlockHelpers;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.flopper.RegistryEntries;
import org.cyclops.flopper.block.BlockFlopper;
import org.cyclops.flopper.block.BlockFlopperConfig;

import java.util.Optional;

/**
 * Fluid hopper tile.
 * @author rubensworks
 */
public class BlockEntityFlopper extends CyclopsBlockEntity {

    private Tank tank;

    @NBTPersist
    private int transferCooldown = -1;

    public BlockEntityFlopper(BlockPos blockPos, BlockState blockState) {
        super(RegistryEntries.BLOCK_ENTITY_FLOPPER, blockPos, blockState);
        tank = new SingleUseTank(BlockFlopperConfig.capacityMb) {
            @Override
            protected void sendUpdate() {
                super.sendUpdate();
                BlockEntityFlopper.this.sendUpdate();
            }
        };
        addCapabilityInternal(ForgeCapabilities.FLUID_HANDLER, LazyOptional.of(this::getTank));
    }

    public Tank getTank() {
        return tank;
    }

    public void setTransferCooldown(int ticks) {
        this.transferCooldown = ticks;
    }

    public int getTransferCooldown() {
        return transferCooldown;
    }

    @Override
    public void read(CompoundTag tag) {
        super.read(tag);
        tank.readFromNBT(tag.getCompound("tank"));
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        CompoundTag tagTank = new CompoundTag();
        tank.writeToNBT(tagTank);
        tag.put("tank", tagTank);
    }

    protected Direction getFacing() {
        return getLevel().getBlockState(getBlockPos()).getValue(BlockFlopper.FACING);
    }

    /**
     * Push fluids from the inner tank to a target tank.
     * @return If some fluid was moved.
     */
    protected boolean pushFluidsToTank() {
        Direction targetSide = getFacing().getOpposite();
        BlockPos targetPos = getBlockPos().relative(getFacing());
        return BlockEntityHelpers.getCapability(level, targetPos, targetSide, ForgeCapabilities.FLUID_HANDLER)
                .map(fluidHandler -> !FluidUtil.tryFluidTransfer(fluidHandler, tank, BlockFlopperConfig.pushFluidRate, true).isEmpty())
                .orElse(false);
    }

    /**
     * Push fluids from a tank above the flopper to the inner tank.
     * @return If some fluid was moved.
     */
    protected boolean pullFluidsFromTank() {
        BlockPos targetPos = getBlockPos().relative(Direction.UP);
        return BlockEntityHelpers.getCapability(level, targetPos, Direction.DOWN, ForgeCapabilities.FLUID_HANDLER)
                .map(fluidHandler -> !FluidUtil.tryFluidTransfer(tank, fluidHandler, BlockFlopperConfig.pullFluidRate, true).isEmpty())
                .orElse(false);
    }

    /**
     * Push fluids from the inner tank into the world at the target space.
     * @return If some fluid was moved.
     */
    protected boolean pushFluidsToWorld() {
        BlockPos targetPos = getBlockPos().relative(getFacing());
        BlockState destBlockState = level.getBlockState(targetPos);
        final boolean isDestNonSolid = !destBlockState.isSolid();
        final boolean isDestReplaceable = destBlockState.getPistonPushReaction() == PushReaction.DESTROY;
        if (level.isEmptyBlock(targetPos)
                || (isDestNonSolid && isDestReplaceable && !destBlockState.liquid())) {
            FluidStack fluidStack = tank.getFluid();

            if (!level.dimensionType().ultraWarm() || !fluidStack.getFluid().getFluidType().isVaporizedOnPlacement(level, worldPosition, fluidStack)) {
                return getFluidBlockHandler(fluidStack.getFluid(), level, targetPos)
                        .map(fluidHandler -> {
                            FluidStack moved = FluidUtil.tryFluidTransfer(fluidHandler, tank, Integer.MAX_VALUE, true);
                            if (!moved.isEmpty()) {
                                if (BlockFlopperConfig.worldPullPushSounds) {
                                    SoundEvent soundevent = moved.getFluid().getFluidType().getSound(SoundActions.BUCKET_FILL);
                                    level.playSound(null, worldPosition, soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);
                                }
                                if (BlockFlopperConfig.worldPullPushNeighbourEvents) {
                                    level.neighborChanged(worldPosition, Blocks.AIR, worldPosition);
                                }
                                return true;
                            }
                            return false;
                        })
                        .orElse(false);

            }
        }
        return false;
    }

    private Optional<IFluidHandler> getFluidBlockHandler(Fluid fluid, Level world, BlockPos targetPos) {
        if (!fluid.getFluidType().canBePlacedInLevel(world, targetPos, fluid.defaultFluidState())) {
            return Optional.empty();
        }
        BlockState state = fluid.getFluidType().getBlockForFluidState(world, targetPos, fluid.defaultFluidState());
        return Optional.of(new BlockWrapper(state, world, targetPos));
    }

    /**
     * Pull fluids from the world at the target space to the inner tank.
     * @return If some fluid was moved.
     */
    protected boolean pullFluidsFromWorld() {
        BlockPos targetPos = getBlockPos().relative(Direction.UP);
        BlockState destBlockState = level.getBlockState(targetPos);
        return wrapFluidBlock(destBlockState, level, targetPos)
                .map(fluidHandler -> {
                    FluidStack moved = FluidUtil.tryFluidTransfer(tank, fluidHandler, Integer.MAX_VALUE, true);
                    if (!moved.isEmpty()) {
                        if (BlockFlopperConfig.worldPullPushSounds) {
                            SoundEvent soundevent = moved.getFluid().getFluidType().getSound(SoundActions.BUCKET_EMPTY);
                            level.playSound(null, worldPosition, soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);
                        }
                        if (BlockFlopperConfig.worldPullPushNeighbourEvents) {
                            level.neighborChanged(worldPosition, Blocks.AIR, worldPosition);
                        }
                        return true;
                    }
                    return false;
                })
                .orElse(false);
    }

    private LazyOptional<IFluidHandler> wrapFluidBlock(BlockState blockState, Level world, BlockPos targetPos) {
        if (blockState.getBlock() instanceof LiquidBlock || blockState.getBlock() instanceof SimpleWaterloggedBlock) {
            return LazyOptional.of(() -> new FluidHandlerBlock(blockState, world, targetPos));
        }
        return LazyOptional.empty();
    }

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
                if (!this.isOnTransferCooldown(blockEntity) && BlockHelpers.getSafeBlockStateProperty(blockState, BlockFlopper.ENABLED, false)) {
                    boolean worked = false;
                    boolean workedWorld = false;

                    // Push fluids
                    if (!blockEntity.getTank().isEmpty()) {
                        worked = (BlockFlopperConfig.pushFluidRate > 0 && blockEntity.pushFluidsToTank())
                                || (workedWorld = (BlockFlopperConfig.pushFluidsWorld && blockEntity.pushFluidsToWorld()));
                    }

                    // Pull fluids
                    if (!blockEntity.getTank().isFull()) {
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
