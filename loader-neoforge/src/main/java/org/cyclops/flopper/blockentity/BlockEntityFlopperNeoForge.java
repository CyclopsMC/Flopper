package org.cyclops.flopper.blockentity;

import com.google.common.base.Predicates;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.resource.ResourceStack;
import org.cyclops.cyclopscore.fluid.SingleUseTank;
import org.cyclops.cyclopscore.fluid.Tank;
import org.cyclops.cyclopscore.helper.IModHelpersNeoForge;
import org.cyclops.flopper.FlopperNeoForge;
import org.cyclops.flopper.block.BlockFlopperConfig;

import java.util.Optional;

/**
 * @author rubensworks
 */
public class BlockEntityFlopperNeoForge extends BlockEntityFlopper {
    private Tank tank;

    public BlockEntityFlopperNeoForge(BlockPos blockPos, BlockState blockState) {
        super(blockPos, blockState);

        tank = new SingleUseTank(BlockFlopperConfig.capacityMb) {
            @Override
            protected void sendUpdate() {
                super.sendUpdate();
                BlockEntityFlopperNeoForge.this.sendUpdate();
                BlockEntityFlopperNeoForge.this.onDirty();
            }
        };
    }

    public Tank getTank() {
        return tank;
    }

    @Override
    public void read(ValueInput input) {
        super.read(input);
        tank.deserialize(input.child("tank").orElseThrow());
    }

    @Override
    public void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        tank.serialize(output.child("tank"));
    }

    @Override
    protected boolean pushFluidsToTank() {
        Direction targetSide = getFacing().getOpposite();
        BlockPos targetPos = getBlockPos().relative(getFacing());
        return FlopperNeoForge._instance.getModHelpers().getCapabilityHelpers().getCapability(level, targetPos, targetSide, Capabilities.Fluid.BLOCK)
                .map(fluidHandler -> ResourceHandlerUtil.moveFirst(tank, fluidHandler, Predicates.alwaysTrue(), BlockFlopperConfig.pushFluidRate, null) != null)
                .orElse(false);
    }

    @Override
    protected boolean pullFluidsFromTank() {
        BlockPos targetPos = getBlockPos().relative(Direction.UP);
        return FlopperNeoForge._instance.getModHelpers().getCapabilityHelpers().getCapability(level, targetPos, Direction.DOWN, Capabilities.Fluid.BLOCK)
                .map(fluidHandler -> ResourceHandlerUtil.moveFirst(fluidHandler, tank, Predicates.alwaysTrue(), BlockFlopperConfig.pullFluidRate, null) != null)
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
                return getFluidBlockHandlerForInsertion(fluidStack.getFluid(), level, targetPos)
                        .map(fluidHandler -> {
                            ResourceStack<FluidResource> moved = ResourceHandlerUtil.moveFirst(tank, fluidHandler, Predicates.alwaysTrue(), Integer.MAX_VALUE, null);
                            if (moved != null) {
                                if (BlockFlopperConfig.worldPullPushSounds) {
                                    SoundEvent soundevent = moved.resource().getFluid().getFluidType().getSound(SoundActions.BUCKET_FILL);
                                    if (soundevent != null) {
                                        level.playSound(null, worldPosition, soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);
                                    }
                                }
                                if (BlockFlopperConfig.worldPullPushNeighbourEvents) {
                                    level.neighborChanged(worldPosition, Blocks.AIR, null);
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

    private Optional<ResourceHandler<FluidResource>> getFluidBlockHandlerForInsertion(Fluid fluid, Level world, BlockPos targetPos) {
        if (!fluid.getFluidType().canBePlacedInLevel(world, targetPos, fluid.defaultFluidState())) {
            return Optional.empty();
        }
        BlockState state = fluid.getFluidType().getBlockForFluidState(world, targetPos, fluid.defaultFluidState());
        return Optional.of(new FluidHandlerBlockNeoForgeInsertable(state, world, targetPos));
    }

    @Override
    protected boolean pullFluidsFromWorld() {
        BlockPos targetPos = getBlockPos().relative(Direction.UP);
        BlockState destBlockState = level.getBlockState(targetPos);
        return wrapFluidBlockForExtraction(destBlockState, level, targetPos)
                .map(fluidHandler -> {
                    ResourceStack<FluidResource> moved = ResourceHandlerUtil.moveFirst(fluidHandler, tank, Predicates.alwaysTrue(), Integer.MAX_VALUE, null);
                    if (moved != null) {
                        if (BlockFlopperConfig.worldPullPushSounds) {
                            SoundEvent soundevent = moved.resource().getFluid().getFluidType().getSound(SoundActions.BUCKET_EMPTY);
                            if (soundevent != null) {
                                level.playSound(null, worldPosition, soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);
                            }
                        }
                        if (BlockFlopperConfig.worldPullPushNeighbourEvents) {
                            level.neighborChanged(worldPosition, Blocks.AIR, null);
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

    @Override
    public boolean hasBucket() {
        return getTank().getFluidAmount() == IModHelpersNeoForge.get().getFluidHelpers().getBucketVolume();
    }

    private Optional<ResourceHandler<FluidResource>> wrapFluidBlockForExtraction(BlockState blockState, Level world, BlockPos targetPos) {
        if (blockState.getBlock() instanceof LiquidBlock || blockState.getBlock() instanceof SimpleWaterloggedBlock) {
            return Optional.of(new FluidHandlerBlockNeoForgeExtractable(blockState, world, targetPos));
        }
        return Optional.empty();
    }
}
