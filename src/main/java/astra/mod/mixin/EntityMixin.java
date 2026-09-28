package astra.mod.mixin;

import astra.mod.client.module.impl.movement.FreeLook;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(method = "turn", at = @At("HEAD"), cancellable = true)
    private void onTurn(double yRot, double xRot, CallbackInfo ci) {
        if (FreeLook.isActive() && (Object) this == Minecraft.getInstance().player) {
            FreeLook.onCameraTurn(yRot, xRot);
            ci.cancel();
        }
    }
}
