package org.cyclops.flopper.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
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
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.wrappers.BlockWrapper;
import org.cyclops.cyclopscore.fluid.SingleUseTankForge;
import org.cyclops.cyclopscore.fluid.TankForge;
import org.cyclops.flopper.FlopperForge;
import org.cyclops.flopper.block.BlockFlopperConfig;

import java.util.Optional;

/**
 * @author rubensworks
 */
public class BlockEntityFlopperForge extends BlockEntityFlopper {
    private TankForge tank;
    private final LazyOptional<IFluidHandler> fluidCapability;

    public BlockEntityFlopperForge(BlockPos blockPos, BlockState blockState) {
        super(blockPos, blockState);

        tank = new SingleUseTankForge(BlockFlopperConfig.capacityMb) {
            @Override
            protected void sendUpdate() {
                super.sendUpdate();
                BlockEntityFlopperForge.this.sendUpdate();
            }
        };
        fluidCapability = LazyOptional.of(() -> tank);
    }

    public TankForge getTank() {
        return tank;
    }

    @Override
    public void read(CompoundTag tag, HolderLookup.Provider provider) {
        super.read(tag, provider);
        tank.readFromNBT(tag.getCompound("tank"));
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        CompoundTag tagTank = new CompoundTag();
        tank.writeToNBT(tagTank);
        tag.put("tank", tagTank);
    }

    @Override
    protected boolean pushFluidsToTank() {
        Direction targetSide = getFacing().getOpposite();
        BlockPos targetPos = getBlockPos().relative(getFacing());
        return FlopperForge._instance.getModHelpers().getCapabilityHelpers().getCapability(level, targetPos, targetSide, ForgeCapabilities.FLUID_HANDLER)
                .map(fluidHandler -> !FluidUtil.tryFluidTransfer(fluidHandler, tank, BlockFlopperConfig.pushFluidRate, true).isEmpty())
                .orElse(false);
    }

    @Override
    protected boolean pullFluidsFromTank() {
        BlockPos targetPos = getBlockPos().relative(Direction.UP);
        return FlopperForge._instance.getModHelpers().getCapabilityHelpers().getCapability(level, targetPos, Direction.DOWN, ForgeCapabilities.FLUID_HANDLER)
                .map(fluidHandler -> !FluidUtil.tryFluidTransfer(tank, fluidHandler, BlockFlopperConfig.pullFluidRate, true).isEmpty())
                .orElse(false);
    }

    @Override
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
                                    if (soundevent != null) {
                                        level.playSound(null, worldPosition, soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);
                                    }
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

    @Override
    protected boolean pullFluidsFromWorld() {
        BlockPos targetPos = getBlockPos().relative(Direction.UP);
        BlockState destBlockState = level.getBlockState(targetPos);
        return wrapFluidBlock(destBlockState, level, targetPos)
                .map(fluidHandler -> {
                    FluidStack moved = FluidUtil.tryFluidTransfer(tank, fluidHandler, Integer.MAX_VALUE, true);
                    if (!moved.isEmpty()) {
                        if (BlockFlopperConfig.worldPullPushSounds) {
                            SoundEvent soundevent = moved.getFluid().getFluidType().getSound(SoundActions.BUCKET_EMPTY);
                            if (soundevent != null) {
                                level.playSound(null, worldPosition, soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);
                            }
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

    @Override
    protected boolean isTankEmpty() {
        return getTank().isEmpty();
    }

    @Override
    protected boolean isTankFull() {
        return getTank().isFull();
    }

    @Override
    public int getFluidAmount() {
        return getTank().getFluidAmount();
    }

    @Override
    public int getFluidCapacity() {
        return getTank().getCapacity();
    }

    private Optional<IFluidHandler> wrapFluidBlock(BlockState blockState, Level world, BlockPos targetPos) {
        if (blockState.getBlock() instanceof LiquidBlock || blockState.getBlock() instanceof SimpleWaterloggedBlock) {
            return Optional.of(new FluidHandlerBlockForge(blockState, world, targetPos));
        }
        return Optional.empty();
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return fluidCapability.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        fluidCapability.invalidate();
    }
}
