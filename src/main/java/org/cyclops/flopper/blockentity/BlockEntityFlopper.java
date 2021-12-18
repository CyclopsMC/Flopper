package org.cyclops.flopper.blockentity;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.wrappers.BlockWrapper;
import org.cyclops.cyclopscore.blockentity.BlockEntityTickerDelayed;
import org.cyclops.cyclopscore.fluid.SingleUseTank;
import org.cyclops.cyclopscore.fluid.Tank;
import org.cyclops.cyclopscore.helper.BlockHelpers;
import org.cyclops.cyclopscore.helper.BlockEntityHelpers;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.cyclopscore.blockentity.CyclopsBlockEntity;
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

    public BlockEntityFlopper(BlockPos blockPos, BlockState blockState) {
        super(RegistryEntries.BLOCK_ENTITY_FLOPPER, blockPos, blockState);
        tank = new SingleUseTank(BlockFlopperConfig.capacityMb) {
            @Override
            protected void sendUpdate() {
                super.sendUpdate();
                BlockEntityFlopper.this.sendUpdate();
            }
        };
        addCapabilityInternal(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, LazyOptional.of(this::getTank));
    }

    public Tank getTank() {
        return tank;
    }

    @Override
    public void read(CompoundTag tag) {
        super.read(tag);
        tank.readFromNBT(tag.getCompound("tank"));
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        CompoundTag tagTank = new CompoundTag();
        tank.writeToNBT(tagTank);
        tag.put("tank", tagTank);
        return super.save(tag);
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
        return BlockEntityHelpers.getCapability(level, targetPos, targetSide, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
                .map(fluidHandler -> !FluidUtil.tryFluidTransfer(fluidHandler, tank, BlockFlopperConfig.pushFluidRate, true).isEmpty())
                .orElse(false);
    }

    /**
     * Push fluids from a tank above the flopper to the inner tank.
     * @return If some fluid was moved.
     */
    protected boolean pullFluidsFromTank() {
        BlockPos targetPos = getBlockPos().relative(Direction.UP);
        return BlockEntityHelpers.getCapability(level, targetPos, Direction.DOWN, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
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
        final Material destMaterial = destBlockState.getMaterial();
        final boolean isDestNonSolid = !destMaterial.isSolid();
        final boolean isDestReplaceable = destBlockState.getMaterial().isReplaceable();
        if (level.isEmptyBlock(targetPos)
                || (isDestNonSolid && isDestReplaceable && !destMaterial.isLiquid())) {
            FluidStack fluidStack = tank.getFluid();

            if (!level.dimensionType().ultraWarm() || !fluidStack.getFluid().getAttributes().doesVaporize(level, worldPosition, fluidStack)) {
                return getFluidBlockHandler(fluidStack.getFluid(), level, targetPos)
                        .map(fluidHandler -> {
                            FluidStack moved = FluidUtil.tryFluidTransfer(fluidHandler, tank, Integer.MAX_VALUE, true);
                            if (!moved.isEmpty()) {
                                if (BlockFlopperConfig.worldPullPushSounds) {
                                    SoundEvent soundevent = moved.getFluid().getAttributes().getFillSound(moved);
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
        if (!fluid.getAttributes().canBePlacedInWorld(world, targetPos, fluid.defaultFluidState())) {
            return Optional.empty();
        }
        BlockState state = fluid.getAttributes().getBlock(world, targetPos, fluid.defaultFluidState());
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
                            SoundEvent soundevent = moved.getFluid().getAttributes().getEmptySound(moved);
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
        @NBTPersist
        private int transferCooldown = -1;

        @Override
        protected void update(Level level, BlockPos pos, BlockState blockState, BlockEntityFlopper blockEntity) {
            super.update(level, pos, blockState, blockEntity);

            if (level != null && !level.isClientSide) {
                --this.transferCooldown;
                if (!this.isOnTransferCooldown()) {
                    this.setTransferCooldown(0);
                    this.updateHopper(level, pos, blockState, blockEntity);
                }
            }
        }

        public void setTransferCooldown(int ticks) {
            this.transferCooldown = ticks;
        }

        private boolean isOnTransferCooldown() {
            return this.transferCooldown > 0;
        }

        protected boolean updateHopper(Level level, BlockPos pos, BlockState blockState, BlockEntityFlopper blockEntity) {
            if (level != null && !level.isClientSide) {
                if (!this.isOnTransferCooldown() && BlockHelpers.getSafeBlockStateProperty(blockState, BlockFlopper.ENABLED, false)) {
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
                        this.setTransferCooldown(workedWorld
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
