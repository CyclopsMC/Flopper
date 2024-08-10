package org.cyclops.flopper.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;
import net.minecraftforge.items.ItemHandlerHelper;
import org.cyclops.cyclopscore.blockentity.CyclopsBlockEntityCommon;
import org.cyclops.flopper.FlopperForge;
import org.cyclops.flopper.blockentity.BlockEntityFlopperForge;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.BiFunction;

/**
 * @author rubensworks
 */
public class BlockFlopperForge extends BlockFlopper {
    public static final MapCodec<BlockFlopper> CODEC = BlockBehaviour.simpleCodec(properties -> new BlockFlopperForge(properties, BlockEntityFlopperForge::new));

    public BlockFlopperForge(Properties properties, BiFunction<BlockPos, BlockState, ? extends CyclopsBlockEntityCommon> blockEntitySupplier) {
        super(properties, blockEntitySupplier);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos pos, Player player, BlockHitResult rayTraceResult) {
        InteractionResult activatedSuper = super.useWithoutItem(blockState, level, pos, player, rayTraceResult);
        if (activatedSuper.consumesAction()) {
            return activatedSuper;
        }

        return FlopperForge._instance.getModHelpers().getCapabilityHelpers().getCapability(level, pos, ForgeCapabilities.FLUID_HANDLER)
                .map(fluidHandler -> {
                    if (BlockFlopperConfig.showContentsStatusMessageOnClick) {
                        // If the hand is empty, show the tank contents
                        FluidStack fluidStack = fluidHandler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
                        if (fluidStack.isEmpty()) {
                            player.displayClientMessage(Component.literal("0 / "
                                    + String.format("%,d", fluidHandler.getTankCapacity(0))), true);
                        } else {
                            player.displayClientMessage(fluidStack.getDisplayName().plainCopy()
                                    .append(Component.literal(": "
                                            + String.format("%,d", fluidStack.getAmount()) + " / "
                                            + String.format("%,d", fluidHandler.getTankCapacity(0)))), true);
                        }
                        return InteractionResult.SUCCESS;
                    }
                    return InteractionResult.PASS;
                })
                .orElse(InteractionResult.PASS);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult rayTraceResult) {
        ItemInteractionResult activatedSuper = super.useItemOn(itemStack, blockState, level, pos, player, hand, rayTraceResult);
        if (activatedSuper.consumesAction()) {
            return activatedSuper;
        }

        int bucketVolume = FlopperForge._instance.getModHelpers().getFluidHelpers().getBucketVolume();
        return FlopperForge._instance.getModHelpers().getCapabilityHelpers().getCapability(level, pos, ForgeCapabilities.FLUID_HANDLER)
                .map(fluidHandler -> {
                    if (!player.isCrouching()
                            && tryEmptyContainer(itemStack, fluidHandler, bucketVolume, player, false).isSuccess()) {
                        // Move fluid from the item into the tank if not sneaking
                        FluidActionResult result = tryEmptyContainer(itemStack, fluidHandler, bucketVolume, player, true);
                        if (result.isSuccess()) {
                            ItemStack drainedItem = result.getResult();
                            if (!player.isCreative()) {
                                FlopperForge._instance.getModHelpers().getInventoryHelpers().tryReAddToStack(player, itemStack, drainedItem, hand);
                            }
                        }
                        return ItemInteractionResult.SUCCESS;
                    } else if (player.isCrouching()
                            && tryFillContainer(itemStack, fluidHandler, bucketVolume, player, false).isSuccess()) {
                        // Move fluid from the tank into the item if sneaking
                        FluidActionResult result = tryFillContainer(itemStack, fluidHandler, bucketVolume, player, true);
                        if (result.isSuccess()) {
                            ItemStack filledItem = result.getResult();
                            if (!player.isCreative()) {
                                FlopperForge._instance.getModHelpers().getInventoryHelpers().tryReAddToStack(player, itemStack, filledItem, hand);
                            }
                        }
                        return ItemInteractionResult.SUCCESS;
                    }
                    return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
                })
                .orElse(ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION);
    }

    // A modified/fixed version of FluidUtil#tryEmptyContainer
    // TODO: Remove this once Forge fixes it.
    @Nonnull
    public static FluidActionResult tryEmptyContainer(@Nonnull ItemStack container, IFluidHandler fluidDestination, int maxAmount, @Nullable Player player, boolean doDrain)
    {
        ItemStack containerCopy = container.copyWithCount(1); // do not modify the input
        return getFluidHandler(containerCopy)
                .map(containerFluidHandler -> {
                    FluidStack transfer = FluidUtil.tryFluidTransfer(fluidDestination, containerFluidHandler, maxAmount, doDrain);
                    if (transfer.isEmpty())
                        return FluidActionResult.FAILURE;

                    if (doDrain && player != null)
                    {
                        SoundEvent soundevent = transfer.getFluid().getFluidType().getSound(SoundActions.BUCKET_EMPTY);
                        if (soundevent != null) {
                            player.level().playSound(null, player.getX(), player.getY() + 0.5, player.getZ(), soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);
                        }
                    }

                    ItemStack resultContainer = containerFluidHandler.getContainer();
                    return new FluidActionResult(resultContainer);
                })
                .orElse(FluidActionResult.FAILURE);
    }

    // TODO: Remove this once Forge fixes it.
    public static @NotNull FluidActionResult tryFillContainer(@NotNull ItemStack container, IFluidHandler fluidSource, int maxAmount, @org.jetbrains.annotations.Nullable Player player, boolean doFill) {
        ItemStack containerCopy = ItemHandlerHelper.copyStackWithSize(container, 1);
        return (FluidActionResult)getFluidHandler(containerCopy).map((containerFluidHandler) -> {
            FluidStack simulatedTransfer = FluidUtil.tryFluidTransfer(containerFluidHandler, fluidSource, maxAmount, false);
            if (!simulatedTransfer.isEmpty()) {
                if (doFill) {
                    FluidUtil.tryFluidTransfer(containerFluidHandler, fluidSource, maxAmount, true);
                    if (player != null) {
                        SoundEvent soundevent = simulatedTransfer.getFluid().getFluidType().getSound(simulatedTransfer, SoundActions.BUCKET_FILL);
                        if (soundevent != null) {
                            player.level().playSound((Player)null, player.getX(), player.getY() + 0.5, player.getZ(), soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);
                        }
                    }
                } else {
                    containerFluidHandler.fill(simulatedTransfer, IFluidHandler.FluidAction.EXECUTE);
                }

                ItemStack resultContainer = containerFluidHandler.getContainer();
                return new FluidActionResult(resultContainer);
            } else {
                return FluidActionResult.FAILURE;
            }
        }).orElse(FluidActionResult.FAILURE);
    }

    @SubscribeEvent
    public void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        // Force allow shift-right clicking with a fluid container passing through to this block
        if (!event.getItemStack().isEmpty()
                && event.getLevel().getBlockState(event.getPos()).getBlock() == this
                && getFluidHandler(event.getItemStack()).isPresent()) {
            event.setUseBlock(Event.Result.ALLOW);
        }
    }

    // TODO: remove when Forge implements Fluid capabilities on items
    public static LazyOptional<IFluidHandlerItem> getFluidHandler(@NotNull ItemStack itemStack) {
//        return FluidUtil.getFluidHandler(itemStack);
        if (itemStack.getItem() instanceof BucketItem) {
            return LazyOptional.of(() -> new FluidBucketWrapper(itemStack));
        }
        return LazyOptional.empty();
    }
}
