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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.fluids.FluidActionResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.cyclops.cyclopscore.blockentity.CyclopsBlockEntityCommon;
import org.cyclops.cyclopscore.helper.IFluidHelpersNeoForge;
import org.cyclops.flopper.FlopperNeoForge;
import org.cyclops.flopper.blockentity.BlockEntityFlopperNeoForge;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.BiFunction;

/**
 * @author rubensworks
 */
public class BlockFlopperNeoForge extends BlockFlopper {
    public static final MapCodec<BlockFlopper> CODEC = BlockBehaviour.simpleCodec(properties -> new BlockFlopperNeoForge(properties, BlockEntityFlopperNeoForge::new));

    public BlockFlopperNeoForge(Properties properties, BiFunction<BlockPos, BlockState, ? extends CyclopsBlockEntityCommon> blockEntitySupplier) {
        super(properties, blockEntitySupplier);
        NeoForge.EVENT_BUS.register(this);
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

        return FlopperNeoForge._instance.getModHelpers().getCapabilityHelpers().getCapability(level, pos, Capabilities.FluidHandler.BLOCK)
                .map(fluidHandler -> {
                    if (BlockFlopperConfig.showContentsStatusMessageOnClick) {
                        // If the hand is empty, show the tank contents
                        FluidStack fluidStack = fluidHandler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
                        if (fluidStack.isEmpty()) {
                            player.displayClientMessage(Component.literal("0 / "
                                    + String.format("%,d", fluidHandler.getTankCapacity(0))), true);
                        } else {
                            player.displayClientMessage(fluidStack.getHoverName().plainCopy()
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

        IFluidHelpersNeoForge fh = FlopperNeoForge._instance.getModHelpers().getFluidHelpers();
        return FlopperNeoForge._instance.getModHelpers().getCapabilityHelpers().getCapability(level, pos, Capabilities.FluidHandler.BLOCK)
                .map(fluidHandler -> {
                    if (!player.isCrouching()
                            && tryEmptyContainer(itemStack, fluidHandler, fh.getBucketVolume(), player, false).isSuccess()) {
                        // Move fluid from the item into the tank if not sneaking
                        FluidActionResult result = FluidUtil.tryEmptyContainer(itemStack, fluidHandler, fh.getBucketVolume(), player, true);
                        if (result.isSuccess()) {
                            ItemStack drainedItem = result.getResult();
                            if (!player.isCreative()) {
                                FlopperNeoForge._instance.getModHelpers().getInventoryHelpers().tryReAddToStack(player, itemStack, drainedItem, hand);
                            }
                        }
                        return ItemInteractionResult.SUCCESS;
                    } else if (player.isCrouching()
                            && FluidUtil.tryFillContainer(itemStack, fluidHandler, fh.getBucketVolume(), player, false).isSuccess()) {
                        // Move fluid from the tank into the item if sneaking
                        FluidActionResult result = FluidUtil.tryFillContainer(itemStack, fluidHandler, fh.getBucketVolume(), player, true);
                        if (result.isSuccess()) {
                            ItemStack filledItem = result.getResult();
                            if (!player.isCreative()) {
                                FlopperNeoForge._instance.getModHelpers().getInventoryHelpers().tryReAddToStack(player, itemStack, filledItem, hand);
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
        return FluidUtil.getFluidHandler(containerCopy)
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

    @SubscribeEvent
    public void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        // Force allow shift-right clicking with a fluid container passing through to this block
        if (!event.getItemStack().isEmpty()
                && event.getLevel().getBlockState(event.getPos()).getBlock() == this
                && event.getItemStack().getCapability(Capabilities.FluidHandler.ITEM) != null) {
            event.setUseBlock(TriState.TRUE);
        }
    }
}
