package org.cyclops.flopper.mixin;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.BlockHitResult;
import org.cyclops.flopper.RegistryEntries;
import org.cyclops.flopper.block.IPlayerDisableableSneak;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * @author rubensworks
 */
@Mixin(MultiPlayerGameMode.class)
public class MixinMultiPlayerGameMode {

    @Inject(method = "performUseItemOn", at = @At("HEAD"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void beforePerformUseItemOn(LocalPlayer player, InteractionHand hand, BlockHitResult result, CallbackInfoReturnable<InteractionResult> callbackInfo) {
        // If player is sneak-clicking on a flopper block, temporarily set the return value of sneaking to false, so we can drop through to BlockFlopperFabric#useItemOn.
        if (player.isSecondaryUseActive() && !player.isSpectator() && (!player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty()) && player.level().getBlockState(result.getBlockPos()).getBlock() == RegistryEntries.BLOCK_FLOPPER.value()) {
            ((IPlayerDisableableSneak) player).setTemporarilyDisableSneak(true);
        }
    }

    @Inject(method = "performUseItemOn", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void afterPerformUseItemOn(LocalPlayer player, InteractionHand hand, BlockHitResult result, CallbackInfoReturnable<InteractionResult> callbackInfo) {
        // Disable the override after the method calls ends
        ((IPlayerDisableableSneak) player).setTemporarilyDisableSneak(false);
    }

}
