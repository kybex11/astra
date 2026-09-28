package astra.mod.mixin;

import astra.mod.client.module.impl.render.DarkTheme;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenMixin {
    @Shadow public int width;
    @Shadow public int height;
    @Inject(method = "renderBackground(Lcom/mojang/blaze3d/vertex/PoseStack;I)V", at = @At("HEAD"), cancellable = true)
    private void onRenderBackground(PoseStack poseStack, int vOffset, CallbackInfo ci) {
        if (DarkTheme.INSTANCE != null && DarkTheme.INSTANCE.isEnabled()) {
            // Modern deep obsidian dark theme background
            astra.mod.client.util.RenderUtils.drawGradientV(poseStack, 0, 0, this.width, this.height, 0xEE0B0D13, 0xF50F172A);
            ci.cancel();
        }
    }
}
