package astra.mod.mixin;

import astra.mod.client.module.impl.render.NoHurtCam;
import astra.mod.client.module.impl.render.Zoom;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "bobHurt", at = @At("HEAD"), cancellable = true)
    private void onBobHurt(PoseStack poseStack, float tickDelta, CallbackInfo ci) {
        if (NoHurtCam.INSTANCE != null && NoHurtCam.INSTANCE.isEnabled()) {
            ci.cancel();
        }
    }

    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void onGetFov(Camera camera, float partialTicks, boolean useFovSetting, CallbackInfoReturnable<Double> cir) {
        if (Zoom.INSTANCE != null && Zoom.INSTANCE.isEnabled()) {
            double current = cir.getReturnValue();
            double modified = Zoom.INSTANCE.modifyFov(current, partialTicks);
            cir.setReturnValue(modified);
        }
    }
}
