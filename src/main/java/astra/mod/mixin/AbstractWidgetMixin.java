package astra.mod.mixin;

import astra.mod.client.module.impl.render.DarkTheme;
import astra.mod.client.theme.ThemeManager;
import astra.mod.client.util.RenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractWidget.class)
public abstract class AbstractWidgetMixin {
    @Shadow public int x;
    @Shadow public int y;
    @Shadow public int width;
    @Shadow public int height;
    @Shadow public boolean active;
    @Shadow public boolean visible;
    @Shadow protected boolean isHovered;
    @Shadow public abstract Component getMessage();

    @Inject(method = "renderButton", at = @At("HEAD"), cancellable = true)
    private void onRenderButton(PoseStack poseStack, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!this.visible) return;

        if (DarkTheme.INSTANCE != null && DarkTheme.INSTANCE.isEnabled()) {
            ci.cancel();

            Minecraft mc = Minecraft.getInstance();
            Font font = mc.font;
            int accent = ThemeManager.INSTANCE.getAccent();

            boolean hovered = mouseX >= this.x && mouseX <= this.x + this.width && mouseY >= this.y && mouseY <= this.y + this.height;
            int bg = !this.active ? 0x880B0F19 : (hovered ? 0xDD1E1B4B : 0xDD0F172A);
            int border = !this.active ? 0x33334155 : (hovered ? accent : 0xFF334155);

            RenderUtils.drawRoundedBorderedRect(poseStack, this.x, this.y, this.width, this.height, 4, 1, bg, border);

            if (hovered && this.active) {
                RenderUtils.drawRoundedRect(poseStack, this.x, this.y, 2, this.height, 1, accent);
            }

            int textCol = !this.active ? 0xFF64748B : (hovered ? 0xFFFFFFFF : 0xFFCBD5E1);
            String text = this.getMessage().getString();
            font.drawShadow(poseStack, text, this.x + (this.width - font.width(text)) / 2.0f, this.y + (this.height - 8) / 2.0f, textCol);
        }
    }
}
