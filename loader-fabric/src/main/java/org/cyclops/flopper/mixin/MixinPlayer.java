package org.cyclops.flopper.mixin;

import net.minecraft.world.entity.player.Player;
import org.cyclops.flopper.block.IPlayerDisableableSneak;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author rubensworks
 */
@Mixin(Player.class)
public class MixinPlayer implements IPlayerDisableableSneak {

    private boolean temporarilyDisableSneak = false;

    @Override
    public void setTemporarilyDisableSneak(boolean setTemporarilyDisableSneak) {
        this.temporarilyDisableSneak = setTemporarilyDisableSneak;
    }

    @Inject(method = "isSecondaryUseActive", at = @At("RETURN"), cancellable = true)
    private void afterIsSecondaryUseActive(CallbackInfoReturnable<Boolean> callback) {
        if (temporarilyDisableSneak) {
            callback.setReturnValue(false);
        }
    }

}
