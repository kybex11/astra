package astra.mod.mixin;

import astra.mod.client.cosmetics.CosmeticManager;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public class AbstractClientPlayerMixin {
    @Inject(method = "isCapeLoaded", at = @At("HEAD"), cancellable = true)
    private void onIsCapeLoaded(CallbackInfoReturnable<Boolean> cir) {
        if (CosmeticManager.INSTANCE.isCapeEnabled()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getCloakTextureLocation", at = @At("HEAD"), cancellable = true)
    private void onGetCloak(CallbackInfoReturnable<ResourceLocation> cir) {
        if (CosmeticManager.INSTANCE.isCapeEnabled()) {
            cir.setReturnValue(CosmeticManager.ASTRA_CAPE);
        }
    }
}

