package org.cyclops.flopper.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.TriState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
import net.neoforged.neoforge.common.SoundAction;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.resource.ResourceStack;
import org.cyclops.cyclopscore.blockentity.CyclopsBlockEntity;
import org.cyclops.flopper.FlopperNeoForge;
import org.cyclops.flopper.blockentity.BlockEntityFlopperNeoForge;

import java.util.function.BiFunction;

/**
 * @author rubensworks
 */
public class BlockFlopperNeoForge extends BlockFlopper {
    public static final MapCodec<BlockFlopper> CODEC = BlockBehaviour.simpleCodec(properties -> new BlockFlopperNeoForge(properties, BlockEntityFlopperNeoForge::new));

    public BlockFlopperNeoForge(Properties properties, BiFunction<BlockPos, BlockState, ? extends CyclopsBlockEntity> blockEntitySupplier) {
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

        return FlopperNeoForge._instance.getModHelpers().getCapabilityHelpers().getCapability(level, pos, Capabilities.Fluid.BLOCK)
                .map(fluidHandler -> {
                    if (BlockFlopperConfig.showContentsStatusMessageOnClick) {
                        // If the hand is empty, show the tank contents
                        int amount = fluidHandler.getAmountAsInt(0);
                        FluidResource fluidResource = fluidHandler.getResource(0);
                        if (amount == 0) {
                            player.sendOverlayMessage(Component.literal("0 / "
                                    + String.format("%,d", fluidHandler.getCapacityAsLong(0, fluidResource))));
                        } else {
                            player.sendOverlayMessage(fluidResource.getHoverName().plainCopy()
                                    .append(Component.literal(": "
                                            + String.format("%,d", amount) + " / "
                                            + String.format("%,d", fluidHandler.getCapacityAsInt(0, fluidResource)))));
                        }
                        return InteractionResult.SUCCESS;
                    }
                    return InteractionResult.PASS;
                })
                .orElse(InteractionResult.PASS);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult rayTraceResult) {
        InteractionResult activatedSuper = super.useItemOn(itemStack, blockState, level, pos, player, hand, rayTraceResult);
        if (activatedSuper.consumesAction()) {
            return activatedSuper;
        }

        ItemAccess itemAccess = ItemAccess.forPlayerInteraction(player, hand).oneByOne();
        ResourceHandler<FluidResource> fluidHandlerItem = itemAccess.getCapability(Capabilities.Fluid.ITEM);
        return FlopperNeoForge._instance.getModHelpers().getCapabilityHelpers().getCapability(level, pos, Capabilities.Fluid.BLOCK)
                .map(fluidHandler -> {
                    if (!player.isCrouching()) {
                        // Move fluid from the item into the tank if not sneaking
                        ResourceStack<FluidResource> moved = ResourceHandlerUtil.moveFirst(fluidHandlerItem, fluidHandler, (fr) -> true, Integer.MAX_VALUE, null);
                        if (moved == null) {
                            return InteractionResult.TRY_WITH_EMPTY_HAND;
                        }
                        playMoveSound(moved, SoundActions.BUCKET_EMPTY, player);
                        return InteractionResult.SUCCESS;
                    } else if (player.isCrouching()) {
                        // Move fluid from the tank into the item if sneaking
                        ResourceStack<FluidResource> moved = ResourceHandlerUtil.moveFirst(fluidHandler, fluidHandlerItem, (fr) -> true, Integer.MAX_VALUE, null);
                        if (moved == null) {
                            return InteractionResult.TRY_WITH_EMPTY_HAND;
                        }
                        playMoveSound(moved, SoundActions.BUCKET_FILL, player);
                        return InteractionResult.SUCCESS;
                    }
                    return InteractionResult.TRY_WITH_EMPTY_HAND;
                })
                .orElse(InteractionResult.TRY_WITH_EMPTY_HAND);
    }

    public static void playMoveSound(ResourceStack<FluidResource> fluidStack, SoundAction soundAction, Player player) {
        SoundEvent soundevent = fluidStack.resource().getFluidType().getSound(soundAction);
        if (soundevent != null) {
            player.level().playSound(null, player.getX(), player.getY() + 0.5, player.getZ(), soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    @SubscribeEvent
    public void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        // Force allow shift-right clicking with a fluid container passing through to this block
        if (!event.getItemStack().isEmpty()
                && event.getLevel().getBlockState(event.getPos()).getBlock() == this
                && event.getItemStack().getCapability(Capabilities.Fluid.ITEM, ItemAccess.forStack(event.getItemStack())) != null) {
            event.setUseBlock(TriState.TRUE);
        }
    }
}
