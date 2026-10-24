package astra.mod.mixin;

import astra.mod.client.cosmetics.CosmeticManager;
import astra.mod.client.theme.ThemeManager;
import astra.mod.client.ui.clickgui.ClickGuiScreen;
import astra.mod.client.ui.clickgui.ParticleEngine;
import astra.mod.client.ui.hud.HudEditorScreen;
import astra.mod.client.util.RenderUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    @Unique
    private static final ResourceLocation ASTRA_LOGO = new ResourceLocation("astra", "icon.png");

    protected TitleScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void onInit(CallbackInfo ci) {
        ParticleEngine.INSTANCE.init(this.width, this.height);
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(PoseStack poseStack, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ci.cancel();

        int accent = ThemeManager.INSTANCE.getAccent();
        int accentDark = ThemeManager.INSTANCE.getAccentDark();

        // 1. Dark cosmic gradient background
        RenderUtils.drawGradientV(poseStack, 0, 0, width, height, 0xFF080B11, 0xFF0F172A);

        // 2. Background particles (neural network constellation)
        ParticleEngine.INSTANCE.renderWithMouse(poseStack, mouseX, mouseY);

        // 3. Astra Logo & Title
        int logoSize = 64;
        int logoX = (width - logoSize) / 2;
        int logoY = Math.max(20, height / 4 - 40);

        RenderSystem.enableBlend();
        minecraft.getTextureManager().bind(ASTRA_LOGO);
        blit(poseStack, logoX, logoY, 0.0f, 0.0f, logoSize, logoSize, logoSize, logoSize);

        String title = "ASTRA CLIENT";
        String ver = "v1.0.0 (1.16.5) | Esports Edition";
        font.drawShadow(poseStack, title, (width - font.width(title)) / 2.0f, logoY + logoSize + 6, accent);
        font.drawShadow(poseStack, ver, (width - font.width(ver)) / 2.0f, logoY + logoSize + 18, 0xFF94A3B8);

        // 4. Menu Buttons
        int btnW = 160;
        int btnH = 22;
        int startY = logoY + logoSize + 36;
        int startX = (width - btnW) / 2;

        astra_drawMenuButton(poseStack, "ОДИНОЧНАЯ ИГРА", startX, startY, btnW, btnH, mouseX, mouseY, accent);
        astra_drawMenuButton(poseStack, "СЕТЕВАЯ ИГРА", startX, startY + 26, btnW, btnH, mouseX, mouseY, accent);
        astra_drawMenuButton(poseStack, "⚡ ASTRA DASHBOARD (RSHIFT)", startX, startY + 52, btnW, btnH, mouseX, mouseY, accent);
        astra_drawMenuButton(poseStack, "📐 РЕДАКТОР HUD", startX, startY + 78, btnW, btnH, mouseX, mouseY, accent);
        astra_drawMenuButton(poseStack, "НАСТРОЙКИ", startX, startY + 104, btnW, btnH, mouseX, mouseY, accent);
        astra_drawMenuButton(poseStack, "ВЫЙТИ ИЗ ИГРЫ", startX, startY + 130, btnW, btnH, mouseX, mouseY, accent);

        // 5. User Profile Card (top right)
        String user = minecraft.getUser().getName();
        String userCard = "Игрок: " + user;
        int cardW = font.width(userCard) + 20;
        RenderUtils.drawRoundedBorderedRect(poseStack, width - cardW - 10, 10, cardW, 20, 4, 1, 0xCC0F172A, 0x44334155);
        font.drawShadow(poseStack, userCard, width - cardW, 16, 0xFFE2E8F0);
    }

    @Unique
    private void astra_drawMenuButton(PoseStack poseStack, String text, int x, int y, int w, int h, int mx, int my, int accent) {
        boolean hover = mx >= x && mx <= x + w && my >= y && my <= y + h;
        int bg = hover ? 0xDD1E1B4B : 0xAA0F172A;
        int border = hover ? accent : 0x33475569;

        RenderUtils.drawRoundedBorderedRect(poseStack, x, y, w, h, 4, 1, bg, border);
        if (hover) {
            RenderUtils.drawRoundedRect(poseStack, x, y, 2, h, 1, accent);
        }

        int textCol = hover ? 0xFFFFFFFF : 0xFFCBD5E1;
        font.drawShadow(poseStack, text, x + (w - font.width(text)) / 2.0f, y + (h - 8) / 2.0f, textCol);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (button == 0) {
            int logoSize = 64;
            int logoY = Math.max(20, height / 4 - 40);
            int btnW = 160;
            int btnH = 22;
            int startY = logoY + logoSize + 36;
            int startX = (width - btnW) / 2;

            if (astra_isInside(mouseX, mouseY, startX, startY, btnW, btnH)) {
                minecraft.setScreen(new SelectWorldScreen(this));
                cir.setReturnValue(true);
            } else if (astra_isInside(mouseX, mouseY, startX, startY + 26, btnW, btnH)) {
                minecraft.setScreen(new JoinMultiplayerScreen(this));
                cir.setReturnValue(true);
            } else if (astra_isInside(mouseX, mouseY, startX, startY + 52, btnW, btnH)) {
                if (astra.mod.client.module.impl.render.ClickGuiModule.INSTANCE != null) {
                    astra.mod.client.module.impl.render.ClickGuiModule.INSTANCE.openGui();
                } else {
                    minecraft.setScreen(astra.mod.client.web.WebGuiScreen.INSTANCE);
                }
                cir.setReturnValue(true);
            } else if (astra_isInside(mouseX, mouseY, startX, startY + 78, btnW, btnH)) {
                minecraft.setScreen(HudEditorScreen.INSTANCE);
                cir.setReturnValue(true);
            } else if (astra_isInside(mouseX, mouseY, startX, startY + 104, btnW, btnH)) {
                minecraft.setScreen(new OptionsScreen(this, minecraft.options));
                cir.setReturnValue(true);
            } else if (astra_isInside(mouseX, mouseY, startX, startY + 130, btnW, btnH)) {
                minecraft.stop();
                cir.setReturnValue(true);
            }
        }
    }

    @Unique
    private boolean astra_isInside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }
}

