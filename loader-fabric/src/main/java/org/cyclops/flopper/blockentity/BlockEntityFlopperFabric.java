package org.cyclops.flopper.blockentity;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
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
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import org.cyclops.flopper.FlopperFabric;
import org.cyclops.flopper.block.BlockFlopperConfig;
import org.cyclops.flopper.block.BlockFlopperConfigFabric;

import java.util.Optional;

/**
 * @author rubensworks
 */
public class BlockEntityFlopperFabric extends BlockEntityFlopper {
    private SingleVariantStorage<FluidVariant> tank;

    public BlockEntityFlopperFabric(BlockPos blockPos, BlockState blockState) {
        super(blockPos, blockState);

        tank = new SingleVariantStorage<>() {
            @Override
            protected FluidVariant getBlankVariant() {
                return FluidVariant.blank();
            }

            @Override
            protected long getCapacity(FluidVariant variant) {
                return BlockFlopperConfigFabric.capacityDroplets;
            }

            @Override
            protected void onFinalCommit() {
                super.onFinalCommit();
                BlockEntityFlopperFabric.this.sendUpdate();
            }
        };
    }

    public SingleVariantStorage<FluidVariant> getTank() {
        return tank;
    }

    @Override
    public void read(CompoundTag tag, HolderLookup.Provider provider) {
        super.read(tag, provider);
        SingleVariantStorage.readNbt(getTank(), FluidVariant.CODEC, FluidVariant::blank, tag, provider);
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        SingleVariantStorage.writeNbt(getTank(), FluidVariant.CODEC, tag, provider);
    }

    @Override
    protected boolean pushFluidsToTank() {
        Direction targetSide = getFacing().getOpposite();
        BlockPos targetPos = getBlockPos().relative(getFacing());
        Storage<FluidVariant> target = FluidStorage.SIDED.find(level, targetPos, targetSide);
        if (target == null) {
            return false;
        }
        return FlopperFabric._instance.getModHelpers().getFluidHelpers().moveFluid(getTank(), target, BlockFlopperConfigFabric.pushFluidRateDroplets) > 0;
    }

    @Override
    protected boolean pullFluidsFromTank() {
        BlockPos targetPos = getBlockPos().relative(Direction.UP);
        Storage<FluidVariant> source = FluidStorage.SIDED.find(level, targetPos, Direction.DOWN);
        if (source == null) {
            return false;
        }
        return FlopperFabric._instance.getModHelpers().getFluidHelpers().moveFluid(source, getTank(), BlockFlopperConfigFabric.pullFluidRateDroplets) > 0;
    }

    @Override
    protected boolean pushFluidsToWorld() {
        BlockPos targetPos = getBlockPos().relative(getFacing());
        BlockState destBlockState = level.getBlockState(targetPos);
        final boolean isDestNonSolid = !destBlockState.isSolid();
        final boolean isDestReplaceable = destBlockState.getPistonPushReaction() == PushReaction.DESTROY;
        if (level.isEmptyBlock(targetPos)
                || (isDestNonSolid && isDestReplaceable && !destBlockState.liquid())) {
            return getFluidBlockHandler(level, targetPos, tank.variant)
                    .map(target -> {
                        long moved = FlopperFabric._instance.getModHelpers().getFluidHelpers().moveFluid(getTank(), target, tank.amount);
                        if (moved > 0) {
                            if (BlockFlopperConfig.worldPullPushSounds) {
                                SoundEvent soundevent = FluidVariantAttributes.getFillSound(target.variant);
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
        return false;
    }

    private Optional<FluidStorageBlockFabricInsertable> getFluidBlockHandler(Level world, BlockPos targetPos, FluidVariant fluidVariant) {
        if (world.dimensionType().ultraWarm() && fluidVariant.getFluid().isSame(Fluids.WATER)) {
            return Optional.empty();
        }
        BlockState state = fluidVariant.getFluid().defaultFluidState().createLegacyBlock();
        return Optional.of(new FluidStorageBlockFabricInsertable(state, world, targetPos, fluidVariant));
    }

    @Override
    protected boolean pullFluidsFromWorld() {
        BlockPos targetPos = getBlockPos().relative(Direction.UP);
        BlockState destBlockState = level.getBlockState(targetPos);
        return wrapFluidBlock(destBlockState, level, targetPos)
                .map(source -> {
                    long moved = FlopperFabric._instance.getModHelpers().getFluidHelpers().moveFluid(source, getTank(), Integer.MAX_VALUE);
                    if (moved > 0) {
                        if (BlockFlopperConfig.worldPullPushSounds) {
                            SoundEvent soundevent = FluidVariantAttributes.getEmptySound(getTank().variant);
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
        return getTank().isResourceBlank();
    }

    @Override
    protected boolean isTankFull() {
        return getTank().getAmount() == getTank().getCapacity();
    }

    @Override
    public int getFluidAmount() {
        return (int) getTank().getAmount();
    }

    @Override
    public int getFluidCapacity() {
        return (int) getTank().getCapacity();
    }

    private Optional<Storage<FluidVariant>> wrapFluidBlock(BlockState blockState, Level world, BlockPos targetPos) {
        if (blockState.getBlock() instanceof LiquidBlock || blockState.getBlock() instanceof SimpleWaterloggedBlock) {
            return Optional.of(new FluidStorageBlockFabricExtractable(blockState, world, targetPos));
        }
        return Optional.empty();
    }
}
