package astra.mod.mixin;

import astra.mod.client.theme.ThemeManager;
import astra.mod.client.util.RenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.server.packs.resources.ReloadInstance;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LoadingOverlay.class)
public abstract class LoadingOverlayMixin {
    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private ReloadInstance reload;
    @Shadow private float currentProgress;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(PoseStack poseStack, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ci.cancel();

        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();
        int accent = ThemeManager.INSTANCE.getAccent();
        int accentDark = ThemeManager.INSTANCE.getAccentDark();

        // 1. Deep cosmic gradient background
        RenderUtils.drawGradientV(poseStack, 0, 0, width, height, 0xFF060911, 0xFF0B0F19);

        // 2. Pure geometric glowing Astra Star Emblem (No external texture dependency = zero squares)
        int centerX = width / 2;
        int centerY = (height / 2) - 25;

        // Glowing outer aura
        RenderUtils.drawCircle(poseStack, centerX, centerY, 32, 0x181E1B4B);
        RenderUtils.drawCircle(poseStack, centerX, centerY, 22, 0x336366F1);
        RenderUtils.drawCircle(poseStack, centerX, centerY, 14, accentDark);
        RenderUtils.drawCircle(poseStack, centerX, centerY, 7, accent);
        RenderUtils.drawCircle(poseStack, centerX, centerY, 2, 0xFFFFFFFF);

        // Cross rays
        RenderUtils.drawRect(poseStack, centerX - 28, centerY - 1, 56, 2, accent);
        RenderUtils.drawRect(poseStack, centerX - 1, centerY - 28, 2, 56, accent);

        // Diagonal star spikes
        RenderUtils.drawRect(poseStack, centerX - 12, centerY - 12, 3, 3, accentDark);
        RenderUtils.drawRect(poseStack, centerX + 10, centerY - 12, 3, 3, accentDark);
        RenderUtils.drawRect(poseStack, centerX - 12, centerY + 10, 3, 3, accentDark);
        RenderUtils.drawRect(poseStack, centerX + 10, centerY + 10, 3, 3, accentDark);

        // 3. Smooth Modern Progress Bar
        float progress = reload.getActualProgress();
        currentProgress = currentProgress + (progress - currentProgress) * 0.2f;

        int barW = 240;
        int barH = 5;
        int barX = (width - barW) / 2;
        int barY = centerY + 50;

        RenderUtils.drawRoundedBorderedRect(poseStack, barX - 1, barY - 1, barW + 2, barH + 2, 3, 1, 0xFF0F172A, 0xFF334155);
        RenderUtils.drawProgressBar(poseStack, barX, barY, barW, barH, (double) currentProgress, 0xFF1E293B, accent);

        // Subtle glow indicator under progress bar
        int progressW = (int) (barW * Math.max(0.0, Math.min(1.0, currentProgress)));
        if (progressW > 0) {
            RenderUtils.drawRect(poseStack, barX, barY + barH + 2, progressW, 1, 0x556366F1);
        }

        // Fade out when complete
        if (reload.isDone()) {
            minecraft.setOverlay(null);
        }
    }
}
