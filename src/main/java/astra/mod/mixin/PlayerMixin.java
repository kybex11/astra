package astra.mod.mixin;

import astra.mod.client.cosmetics.CosmeticManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerMixin {
    @Inject(method = "getName", at = @At("HEAD"), cancellable = true)
    private void onGetName(CallbackInfoReturnable<Component> cir) {
        if (CosmeticManager.INSTANCE.hasCustomName() && (Object) this == Minecraft.getInstance().player) {
            cir.setReturnValue(new TextComponent(CosmeticManager.INSTANCE.getCustomName()));
        }
    }

    @Inject(method = "getDisplayName", at = @At("HEAD"), cancellable = true)
    private void onGetDisplayName(CallbackInfoReturnable<Component> cir) {
        if (CosmeticManager.INSTANCE.hasCustomName() && (Object) this == Minecraft.getInstance().player) {
            cir.setReturnValue(new TextComponent(CosmeticManager.INSTANCE.getCustomName()));
        }
    }
}
