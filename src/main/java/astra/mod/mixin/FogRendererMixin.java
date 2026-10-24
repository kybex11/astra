package astra.mod.mixin;

import astra.mod.client.module.impl.render.FogCustomizer;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
public class FogRendererMixin {
    @Inject(method = "setupFog", at = @At("HEAD"), cancellable = true)
    private static void onSetupFog(Camera camera, FogRenderer.FogMode fogMode, float farDistance, boolean bossFight, CallbackInfo ci) {
        try {
            if (FogCustomizer.INSTANCE != null && FogCustomizer.INSTANCE.shouldCancelTerrainFog()
                    && fogMode == FogRenderer.FogMode.FOG_TERRAIN) {
                ci.cancel();
            }
        } catch (Exception ignored) {}
    }
}
