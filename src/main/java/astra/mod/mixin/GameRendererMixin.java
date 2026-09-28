package astra.mod.mixin;

import astra.mod.client.module.impl.render.NoHurtCam;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "bobHurt", at = @At("HEAD"), cancellable = true)
    private void onBobHurt(PoseStack poseStack, float tickDelta, CallbackInfo ci) {
        if (NoHurtCam.INSTANCE != null && NoHurtCam.INSTANCE.isEnabled()) {
            ci.cancel();
        }
    }
}
