package org.cyclops.flopper.tileentity;

import lombok.experimental.Delegate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
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
import net.minecraftforge.fluids.IFluidBlock;
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
        tank = new SingleUseTank(BlockFlopperConfig.capacityMb);
        addCapabilityInternal(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, tank);
    }

    public Tank getTank() {
        return tank;
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
        tank.readFromNBT(tag);
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        tank.writeToNBT(tag);
        return super.write(tag);
    }

    @Override
    protected void updateTileEntity() {
        super.updateTileEntity();

        if (this.world != null && !this.world.isRemote) {
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
        if (this.world != null && !this.world.isRemote) {
            if (!this.isOnTransferCooldown() && BlockHelpers.getSafeBlockStateProperty(
                    getWorld().getBlockState(getPos()), BlockFlopper.ENABLED, false)) {
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
                    this.markDirty();
                    return true;
                }
            }

            return false;
        } else {
            return false;
        }
    }

    protected Direction getFacing() {
        return getWorld().getBlockState(getPos()).get(BlockFlopper.FACING);
    }

    /**
     * Push fluids from the inner tank to a target tank.
     * @return If some fluid was moved.
     */
    protected boolean pushFluidsToTank() {
        Direction targetSide = getFacing().getOpposite();
        BlockPos targetPos = getPos().offset(getFacing());
        return TileHelpers.getCapability(world, targetPos, targetSide, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
                .map(fluidHandler -> !FluidUtil.tryFluidTransfer(fluidHandler, tank, BlockFlopperConfig.pushFluidRate, true).isEmpty())
                .orElse(false);
    }

    /**
     * Push fluids from a tank above the flopper to the inner tank.
     * @return If some fluid was moved.
     */
    protected boolean pullFluidsFromTank() {
        BlockPos targetPos = getPos().offset(Direction.UP);
        return TileHelpers.getCapability(world, targetPos, Direction.DOWN, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
                .map(fluidHandler -> !FluidUtil.tryFluidTransfer(tank, fluidHandler, BlockFlopperConfig.pullFluidRate, true).isEmpty())
                .orElse(false);
    }

    /**
     * Push fluids from the inner tank into the world at the target space.
     * @return If some fluid was moved.
     */
    protected boolean pushFluidsToWorld() {
        BlockPos targetPos = getPos().offset(getFacing());
        BlockState destBlockState = world.getBlockState(targetPos);
        final Material destMaterial = destBlockState.getMaterial();
        final boolean isDestNonSolid = !destMaterial.isSolid();
        final boolean isDestReplaceable = destBlockState.getMaterial().isReplaceable();
        if (world.isAirBlock(targetPos)
                || (isDestNonSolid && isDestReplaceable && !destMaterial.isLiquid())) {
            FluidStack fluidStack = tank.getFluid();

            if (!world.dimension.doesWaterVaporize() || !fluidStack.getFluid().getAttributes().doesVaporize(world, pos, fluidStack)) {
                IFluidHandler fluidHandler = getFluidBlockHandler(fluidStack.getFluid(), world, targetPos);
                FluidStack moved = FluidUtil.tryFluidTransfer(fluidHandler, tank, Integer.MAX_VALUE, true);
                if (!moved.isEmpty()) {
                    if (BlockFlopperConfig.worldPullPushSounds) {
                        SoundEvent soundevent = moved.getFluid().getAttributes().getFillSound(moved);
                        world.playSound(null, pos, soundevent, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    }
                    if (BlockFlopperConfig.worldPullPushNeighbourEvents) {
                        world.neighborChanged(pos, Blocks.AIR, pos);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private IFluidHandler getFluidBlockHandler(Fluid fluid, World world, BlockPos targetPos) {
        BlockState state = fluid.getAttributes().getBlock(world, pos, fluid.getDefaultState());
        return new BlockWrapper(state, world, pos);
    }

    /**
     * Pull fluids from the world at the target space to the inner tank.
     * @return If some fluid was moved.
     */
    protected boolean pullFluidsFromWorld() {
        BlockPos targetPos = getPos().offset(Direction.UP);
        BlockState destBlockState = world.getBlockState(targetPos);
        return wrapFluidBlock(destBlockState.getBlock(), world, targetPos)
                .map(fluidHandler -> {
                    FluidStack moved = FluidUtil.tryFluidTransfer(tank, fluidHandler, Integer.MAX_VALUE, true);
                    if (!moved.isEmpty()) {
                        if (BlockFlopperConfig.worldPullPushSounds) {
                            SoundEvent soundevent = moved.getFluid().getAttributes().getEmptySound(moved);
                            world.playSound(null, pos, soundevent, SoundCategory.BLOCKS, 1.0F, 1.0F);
                        }
                        if (BlockFlopperConfig.worldPullPushNeighbourEvents) {
                            world.neighborChanged(pos, Blocks.AIR, pos);
                        }
                        return true;
                    }
                    return false;
                })
                .orElse(false);
    }

    private LazyOptional<IFluidHandler> wrapFluidBlock(Block block, World world, BlockPos targetPos) {
        if (block instanceof IFluidBlock) {
            return FluidUtil.getFluidHandler(world, targetPos, Direction.UP);
        }
        return LazyOptional.empty();
    }
}
