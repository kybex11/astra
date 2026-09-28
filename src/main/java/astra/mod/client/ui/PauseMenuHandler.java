package astra.mod.client.ui;

import astra.mod.client.theme.ThemeManager;
import astra.mod.client.ui.clickgui.ClickGuiScreen;
import astra.mod.client.ui.clickgui.ParticleEngine;
import astra.mod.client.ui.hud.HudEditorScreen;
import astra.mod.client.util.RenderUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

public class PauseMenuHandler {
    private static final ResourceLocation ASTRA_LOGO = new ResourceLocation("astra", "icon.png");

    public static void render(Screen screen, PoseStack poseStack, int mouseX, int mouseY, float delta) {
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        int width = screen.width;
        int height = screen.height;
        int accent = ThemeManager.INSTANCE.getAccent();

        // 1. Dark translucent backdrop
        RenderUtils.drawGradientV(poseStack, 0, 0, width, height, 0xCC080B11, 0xDD0F172A);

        // 2. Cosmic neural constellation particles
        ParticleEngine.INSTANCE.renderWithMouse(poseStack, mouseX, mouseY);

        // 3. Central Window Card
        int winW = 220;
        int winH = 210;
        int winX = (width - winW) / 2;
        int winY = (height - winH) / 2;

        RenderUtils.drawRoundedBorderedRect(poseStack, winX, winY, winW, winH, 8, 1, 0xF20B0F19, 0x44475569);

        // Header logo & Title
        int logoSize = 32;
        int logoX = (width - logoSize) / 2;
        int logoY = winY + 12;

        RenderSystem.enableBlend();
        mc.getTextureManager().bind(ASTRA_LOGO);
        Screen.blit(poseStack, logoX, logoY, 0.0f, 0.0f, logoSize, logoSize, logoSize, logoSize);

        String title = "МЕНЮ ПАУЗЫ";
        font.drawShadow(poseStack, title, (width - font.width(title)) / 2.0f, logoY + logoSize + 4, accent);

        // Menu Buttons
        int btnW = 180;
        int btnH = 20;
        int startY = logoY + logoSize + 20;
        int startX = (width - btnW) / 2;

        drawMenuButton(font, poseStack, "ВЕРНУТЬСЯ В ИГРУ", startX, startY, btnW, btnH, mouseX, mouseY, accent);
        drawMenuButton(font, poseStack, "⚡ ASTRA DASHBOARD (RSHIFT)", startX, startY + 24, btnW, btnH, mouseX, mouseY, accent);
        drawMenuButton(font, poseStack, "📐 РЕДАКТОР HUD", startX, startY + 48, btnW, btnH, mouseX, mouseY, accent);
        drawMenuButton(font, poseStack, "НАСТРОЙКИ", startX, startY + 72, btnW, btnH, mouseX, mouseY, accent);
        drawMenuButton(font, poseStack, "ВЫЙТИ В ГЛАВНОЕ МЕНЮ", startX, startY + 96, btnW, btnH, mouseX, mouseY, 0xFFEF4444);
    }

    private static void drawMenuButton(Font font, PoseStack poseStack, String text, int x, int y, int w, int h, int mx, int my, int accent) {
        boolean hover = mx >= x && mx <= x + w && my >= y && my <= y + h;
        int bg = hover ? 0xDD1E1B4B : 0xBB0F172A;
        int border = hover ? accent : 0x33475569;

        RenderUtils.drawRoundedBorderedRect(poseStack, x, y, w, h, 4, 1, bg, border);
        if (hover) {
            RenderUtils.drawRoundedRect(poseStack, x, y, 2, h, 1, accent);
        }

        int textCol = hover ? 0xFFFFFFFF : 0xFFCBD5E1;
        font.drawShadow(poseStack, text, x + (w - font.width(text)) / 2.0f, y + (h - 8) / 2.0f, textCol);
    }

    public static boolean handleClick(Screen screen, double mouseX, double mouseY, int button) {
        if (button == 0) {
            int winW = 220;
            int winH = 210;
            int winY = (screen.height - winH) / 2;
            int logoSize = 32;
            int logoY = winY + 12;

            int btnW = 180;
            int btnH = 20;
            int startY = logoY + logoSize + 20;
            int startX = (screen.width - btnW) / 2;

            Minecraft mc = Minecraft.getInstance();

            if (mouseX >= startX && mouseX <= startX + btnW && mouseY >= startY && mouseY <= startY + btnH) {
                mc.setScreen(null);
                mc.mouseHandler.grabMouse();
                return true;
            } else if (mouseX >= startX && mouseX <= startX + btnW && mouseY >= startY + 24 && mouseY <= startY + 24 + btnH) {
                mc.setScreen(ClickGuiScreen.INSTANCE);
                return true;
            } else if (mouseX >= startX && mouseX <= startX + btnW && mouseY >= startY + 48 && mouseY <= startY + 48 + btnH) {
                mc.setScreen(HudEditorScreen.INSTANCE);
                return true;
            } else if (mouseX >= startX && mouseX <= startX + btnW && mouseY >= startY + 72 && mouseY <= startY + 72 + btnH) {
                mc.setScreen(new OptionsScreen(screen, mc.options));
                return true;
            } else if (mouseX >= startX && mouseX <= startX + btnW && mouseY >= startY + 96 && mouseY <= startY + 96 + btnH) {
                boolean isLocal = mc.isLocalServer();
                if (mc.level != null) {
                    mc.level.disconnect();
                }
                if (isLocal) {
                    mc.clearLevel(new GenericDirtMessageScreen(new TranslatableComponent("menu.savingLevel")));
                } else {
                    mc.clearLevel();
                }
                if (isLocal) {
                    mc.setScreen(new TitleScreen());
                } else {
                    mc.setScreen(new JoinMultiplayerScreen(new TitleScreen()));
                }
                return true;
            }
        }
        return false;
    }
}
