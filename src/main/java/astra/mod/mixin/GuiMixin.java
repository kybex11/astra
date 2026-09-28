package astra.mod.mixin;

import astra.mod.client.ui.hud.HudRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(PoseStack poseStack, float tickDelta, CallbackInfo ci) {
        HudRenderer.INSTANCE.render(poseStack, tickDelta);
    }
}
