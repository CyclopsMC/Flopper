package org.cyclops.flopper.tileentity;

import lombok.experimental.Delegate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.wrappers.BlockLiquidWrapper;
import net.minecraftforge.fluids.capability.wrappers.BlockWrapper;
import net.minecraftforge.fluids.capability.wrappers.FluidBlockWrapper;
import org.cyclops.cyclopscore.fluid.SingleUseTank;
import org.cyclops.cyclopscore.fluid.Tank;
import org.cyclops.cyclopscore.helper.BlockHelpers;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.cyclopscore.tileentity.CyclopsTileEntity;
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
        tank = new SingleUseTank(BlockFlopperConfig.capacityMb, this);
        addCapabilityInternal(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, tank);
    }

    public Tank getTank() {
        return tank;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        tank.readFromNBT(tag);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tank.writeToNBT(tag);
        return super.writeToNBT(tag);
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

                // Push fluids
                if (!this.tank.isEmpty()) {
                    worked = (BlockFlopperConfig.pushFluidRate > 0 && this.pushFluidsToTank())
                            || (BlockFlopperConfig.pushFluidsWorld && this.pushFluidsToWorld());
                }

                // Pull fluids
                if (!this.tank.isFull()) {
                    worked = (BlockFlopperConfig.pullFluidRate > 0 && pullFluidsFromTank())
                            || (BlockFlopperConfig.pullFluidsWorld && this.pullFluidsFromWorld())
                            || worked;
                }

                if (worked) {
                    this.setTransferCooldown(BlockFlopperConfig.workCooldown);
                    this.markDirty();
                    return true;
                }
            }

            return false;
        } else {
            return false;
        }
    }

    protected EnumFacing getFacing() {
        return getWorld().getBlockState(getPos()).getValue(BlockFlopper.FACING);
    }

    /**
     * Push fluids from the inner tank to a target tank.
     * @return If some fluid was moved.
     */
    protected boolean pushFluidsToTank() {
        EnumFacing targetSide = getFacing().getOpposite();
        BlockPos targetPos = getPos().offset(getFacing());
        IFluidHandler fluidHandler = TileHelpers.getCapability(world, targetPos, targetSide, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
        return fluidHandler != null && FluidUtil.tryFluidTransfer(fluidHandler, tank, BlockFlopperConfig.pushFluidRate, true) != null;
    }

    /**
     * Push fluids from a tank above the flopper to the inner tank.
     * @return If some fluid was moved.
     */
    protected boolean pullFluidsFromTank() {
        BlockPos targetPos = getPos().offset(EnumFacing.UP);
        IFluidHandler fluidHandler = TileHelpers.getCapability(world, targetPos, EnumFacing.DOWN, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
        return fluidHandler != null && FluidUtil.tryFluidTransfer(tank, fluidHandler, BlockFlopperConfig.pullFluidRate, true) != null;
    }

    protected static IFluidHandler wrapFluidBlock(Block block, World world, BlockPos pos) {
        if (block instanceof IFluidBlock) {
            return new FluidBlockWrapper((IFluidBlock) block, world, pos);
        } else if (block instanceof BlockLiquid) {
            return new BlockLiquidWrapper((BlockLiquid) block, world, pos);
        } else {
            return new BlockWrapper(block, world, pos);
        }
    }

    /**
     * Push fluids from the inner tank into the world at the target space.
     * @return If some fluid was moved.
     */
    protected boolean pushFluidsToWorld() {
        BlockPos targetPos = getPos().offset(getFacing());
        IBlockState destBlockState = world.getBlockState(targetPos);
        final Material destMaterial = destBlockState.getMaterial();
        final boolean isDestNonSolid = !destMaterial.isSolid();
        final boolean isDestReplaceable = destBlockState.getBlock().isReplaceable(world, targetPos);
        if (world.isAirBlock(targetPos)
                || (isDestNonSolid && isDestReplaceable && !destMaterial.isLiquid())) {
            FluidStack fluidStack = tank.getFluid();

            if (!world.provider.doesWaterVaporize() || !fluidStack.getFluid().doesVaporize(fluidStack)) {
                Block block = fluidStack.getFluid().getBlock();
                IFluidHandler fluidHandler = wrapFluidBlock(block, world, targetPos);
                FluidStack moved = FluidUtil.tryFluidTransfer(fluidHandler, tank, Integer.MAX_VALUE, true);
                if (moved != null) {
                    if (BlockFlopperConfig.worldPullPushSounds) {
                        SoundEvent soundevent = moved.getFluid().getFillSound(moved);
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

    /**
     * Pull fluids from the world at the target space to the inner tank.
     * @return If some fluid was moved.
     */
    protected boolean pullFluidsFromWorld() {
        BlockPos targetPos = getPos().offset(EnumFacing.UP);
        IBlockState destBlockState = world.getBlockState(targetPos);
        IFluidHandler fluidHandler = wrapFluidBlock(destBlockState.getBlock(), world, targetPos);
        FluidStack moved = FluidUtil.tryFluidTransfer(tank, fluidHandler, Integer.MAX_VALUE, true);
        if (moved != null) {
            if (BlockFlopperConfig.worldPullPushSounds) {
                SoundEvent soundevent = moved.getFluid().getEmptySound(moved);
                world.playSound(null, pos, soundevent, SoundCategory.BLOCKS, 1.0F, 1.0F);
            }
            if (BlockFlopperConfig.worldPullPushNeighbourEvents) {
                world.neighborChanged(pos, Blocks.AIR, pos);
            }
            return true;
        }
        return false;
    }
}
