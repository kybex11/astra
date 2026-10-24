package astra.mod.mixin;

import astra.mod.client.module.impl.render.BossBarMod;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.BossHealthOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BossHealthOverlay.class)
public class BossHealthOverlayMixin {
    private boolean astraPushed = false;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRenderHead(PoseStack poseStack, CallbackInfo ci) {
        astraPushed = false;
        try {
            BossBarMod m = BossBarMod.INSTANCE;
            if (m == null || !m.isEnabled()) return;
            if (m.shouldHideVanilla()) {
                ci.cancel();
                return;
            }
            float scale = m.getScale();
            if (scale != 1.0f) {
                float cx = Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2.0f;
                poseStack.pushPose();
                poseStack.translate(cx, 12.0f, 0.0f);
                poseStack.scale(scale, scale, 1.0f);
                poseStack.translate(-cx, -12.0f, 0.0f);
                astraPushed = true;
            }
        } catch (Exception ignored) {}
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRenderTail(PoseStack poseStack, CallbackInfo ci) {
        if (astraPushed) {
            astraPushed = false;
            try {
                poseStack.popPose();
            } catch (Exception ignored) {}
        }
    }
}
