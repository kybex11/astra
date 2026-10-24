package astra.mod.mixin;

import astra.mod.client.module.impl.render.ESP;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(method = "shouldEntityAppearGlowing", at = @At("HEAD"), cancellable = true)
    private void onShouldEntityAppearGlowing(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (ESP.INSTANCE != null && ESP.INSTANCE.isEnabled() && ESP.INSTANCE.shouldGlow(entity)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "allowsMultiplayer", at = @At("HEAD"), cancellable = true)
    private void onAllowsMultiplayer(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }

    @Inject(method = "allowsChat", at = @At("HEAD"), cancellable = true)
    private void onAllowsChat(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }
}
