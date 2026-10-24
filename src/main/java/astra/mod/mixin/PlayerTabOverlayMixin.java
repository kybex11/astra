package astra.mod.mixin;

import astra.mod.client.presence.AstraPresenceManager;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerTabOverlay.class)
public class PlayerTabOverlayMixin {
    @Inject(method = "getNameForDisplay", at = @At("RETURN"), cancellable = true)
    private void onGetNameForDisplay(PlayerInfo info, CallbackInfoReturnable<Component> cir) {
        Component original = cir.getReturnValue();
        Component modified = AstraPresenceManager.INSTANCE.formatTabName(info, original);
        if (modified != null) {
            cir.setReturnValue(modified);
        }
    }
}
