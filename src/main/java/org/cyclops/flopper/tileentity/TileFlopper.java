package org.cyclops.flopper.tileentity;

import lombok.experimental.Delegate;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.block.material.Material;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.wrappers.BlockWrapper;
import org.cyclops.cyclopscore.fluid.SingleUseTank;
import org.cyclops.cyclopscore.fluid.Tank;
import org.cyclops.cyclopscore.helper.BlockHelpers;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.cyclopscore.tileentity.CyclopsTileEntity;
import org.cyclops.flopper.RegistryEntries;
import org.cyclops.flopper.block.BlockFlopper;
import org.cyclops.flopper.block.BlockFlopperConfig;

import java.util.Optional;

import org.cyclops.cyclopscore.tileentity.CyclopsTileEntity.ITickingTile;
import org.cyclops.cyclopscore.tileentity.CyclopsTileEntity.TickingTileComponent;

/**
 * Fluid hopper tile.
 * @author rubensworks
 */
public class TileFlopper extends CyclopsTileEntity implements CyclopsTileEntity.ITickingTile {

    @Delegate
    private final ITickingTile tickingTileComponent = new TickingTileComponent(this);

    @NBTPersist
    private int transferCooldown = -1;
    private Tank tank;

    public TileFlopper() {
        super(RegistryEntries.TILE_ENTITY_FLOPPER);
        tank = new SingleUseTank(BlockFlopperConfig.capacityMb) {
            @Override
            protected void sendUpdate() {
                super.sendUpdate();
                TileFlopper.this.sendUpdate();
            }
        };
        addCapabilityInternal(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, LazyOptional.of(this::getTank));
    }

    public Tank getTank() {
        return tank;
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
        tank.readFromNBT(tag.getCompound("tank"));
    }

    @Override
    public CompoundNBT save(CompoundNBT tag) {
        CompoundNBT tagTank = new CompoundNBT();
        tank.writeToNBT(tagTank);
        tag.put("tank", tagTank);
        return super.save(tag);
    }

    @Override
    protected void updateTileEntity() {
        super.updateTileEntity();

        if (this.level != null && !this.level.isClientSide) {
            --this.transferCooldown;
            if (!this.isOnTransferCooldown()) {
                this.setTransferCooldown(0);
                this.updateHopper();
            }
        }
    }

    public void setTransferCooldown(int ticks) {
        this.transferCooldown = ticks;
    }

    private boolean isOnTransferCooldown() {
        return this.transferCooldown > 0;
    }

    protected boolean updateHopper() {
        if (this.level != null && !this.level.isClientSide) {
            if (!this.isOnTransferCooldown() && BlockHelpers.getSafeBlockStateProperty(
                    getLevel().getBlockState(getBlockPos()), BlockFlopper.ENABLED, false)) {
                boolean worked = false;
                boolean workedWorld = false;

                // Push fluids
                if (!this.tank.isEmpty()) {
                    worked = (BlockFlopperConfig.pushFluidRate > 0 && this.pushFluidsToTank())
                            || (workedWorld = (BlockFlopperConfig.pushFluidsWorld && this.pushFluidsToWorld()));
                }

                // Pull fluids
                if (!this.tank.isFull()) {
                    worked = (BlockFlopperConfig.pullFluidRate > 0 && pullFluidsFromTank())
                            || (workedWorld = (BlockFlopperConfig.pullFluidsWorld && this.pullFluidsFromWorld()) || workedWorld)
                            || worked;
                }

                if (worked) {
                    this.setTransferCooldown(workedWorld
                            ? BlockFlopperConfig.workWorldCooldown : BlockFlopperConfig.workCooldown);
                    this.setChanged();
                    return true;
                }
            }

            return false;
        } else {
            return false;
        }
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
        return TileHelpers.getCapability(level, targetPos, targetSide, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
                .map(fluidHandler -> !FluidUtil.tryFluidTransfer(fluidHandler, tank, BlockFlopperConfig.pushFluidRate, true).isEmpty())
                .orElse(false);
    }

    /**
     * Push fluids from a tank above the flopper to the inner tank.
     * @return If some fluid was moved.
     */
    protected boolean pullFluidsFromTank() {
        BlockPos targetPos = getBlockPos().relative(Direction.UP);
        return TileHelpers.getCapability(level, targetPos, Direction.DOWN, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
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
                                    level.playSound(null, worldPosition, soundevent, SoundCategory.BLOCKS, 1.0F, 1.0F);
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

    private Optional<IFluidHandler> getFluidBlockHandler(Fluid fluid, World world, BlockPos targetPos) {
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
                            level.playSound(null, worldPosition, soundevent, SoundCategory.BLOCKS, 1.0F, 1.0F);
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

    private LazyOptional<IFluidHandler> wrapFluidBlock(BlockState blockState, World world, BlockPos targetPos) {
        if (blockState.getBlock() instanceof FlowingFluidBlock || blockState.getBlock() instanceof IWaterLoggable) {
            return LazyOptional.of(() -> new FluidHandlerBlock(blockState, world, targetPos));
        }
        return LazyOptional.empty();
    }
}
