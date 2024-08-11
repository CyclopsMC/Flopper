package org.cyclops.flopper.block;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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
import org.cyclops.cyclopscore.blockentity.CyclopsBlockEntityCommon;
import org.cyclops.cyclopscore.helper.IFluidHelpersFabric;
import org.cyclops.flopper.FlopperFabric;
import org.cyclops.flopper.blockentity.BlockEntityFlopperFabric;

import java.util.function.BiFunction;

/**
 * @author rubensworks
 */
public class BlockFlopperFabric extends BlockFlopper {
    public static final MapCodec<BlockFlopper> CODEC = BlockBehaviour.simpleCodec(properties -> new BlockFlopperFabric(properties, BlockEntityFlopperFabric::new));

    public BlockFlopperFabric(Properties properties, BiFunction<BlockPos, BlockState, ? extends CyclopsBlockEntityCommon> blockEntitySupplier) {
        super(properties, blockEntitySupplier);
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

        if (BlockFlopperConfig.showContentsStatusMessageOnClick) {
            Storage<FluidVariant> storage = FluidStorage.SIDED.find(level, pos, rayTraceResult.getDirection());
            if (storage != null) {
                for (StorageView<FluidVariant> view : storage) {
                    if (view.isResourceBlank()) {
                        player.displayClientMessage(Component.literal("0 / "
                                + String.format("%,d", view.getCapacity())), true);
                    } else {
                        player.displayClientMessage(FluidVariantAttributes.getName(view.getResource()).plainCopy()
                                .append(Component.literal(": "
                                        + String.format("%,d", view.getAmount()) + " / "
                                        + String.format("%,d", view.getCapacity()))), true);
                    }
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult rayTraceResult) {
        ItemInteractionResult activatedSuper = super.useItemOn(itemStack, blockState, level, pos, player, hand, rayTraceResult);
        if (activatedSuper.consumesAction()) {
            return activatedSuper;
        }

        IFluidHelpersFabric fh = FlopperFabric._instance.getModHelpers().getFluidHelpers();
        Storage<FluidVariant> storageFlopper = FluidStorage.SIDED.find(level, pos, rayTraceResult.getDirection());
        ContainerItemContext storageItemContext = ContainerItemContext.forPlayerInteraction(player, hand);
        Storage<FluidVariant> storageItem = storageItemContext.find(FluidStorage.ITEM);
        if (storageFlopper != null && storageItem != null) {
            long movedSimulate;
            if (!player.isCrouching()
                    && (movedSimulate = fh.moveFluid(storageItem, storageFlopper, fh.getBucketVolume(), player, true)) > 0) {
                // Move fluid from the item into the tank if not sneaking
                fh.moveFluid(storageItem, storageFlopper, movedSimulate, player, false);
                return ItemInteractionResult.SUCCESS;
            } else if (player.isCrouching()
                    && (movedSimulate = fh.moveFluid(storageFlopper, storageItem, fh.getBucketVolume(), player, true)) > 0) {
                // Move fluid from the tank into the item if sneaking
                fh.moveFluid(storageFlopper, storageItem, movedSimulate, player, false);
                return ItemInteractionResult.SUCCESS;
            }
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
}
