package astra.mod.mixin;

import astra.mod.client.module.impl.render.DarkTheme;
import astra.mod.client.theme.ThemeManager;
import astra.mod.client.util.RenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {
    @Shadow protected int leftPos;
    @Shadow protected int topPos;
    @Shadow protected int imageWidth;
    @Shadow protected int imageHeight;

    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(PoseStack poseStack, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (DarkTheme.INSTANCE != null && DarkTheme.INSTANCE.isEnabled()) {
            int accent = ThemeManager.INSTANCE.getAccent();
            // Dark glass container frame
            RenderUtils.drawBorderedRect(poseStack, this.leftPos - 4, this.topPos - 4, this.imageWidth + 8, this.imageHeight + 8, 2, 0x880B0F19, accent);
        }
    }
}
