package astra.mod.client.web;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.module.ModuleManager;
import astra.mod.client.theme.ThemeManager;
import astra.mod.client.ui.clickgui.ParticleEngine;
import astra.mod.client.util.RenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.stream.Collectors;

public class WebGuiScreen extends Screen {
    public static final WebGuiScreen INSTANCE = new WebGuiScreen();

    private Category selectedCategory = Category.COMBAT;
    private String searchQuery = "";
    private double scrollY = 0;

    public WebGuiScreen() {
        super(new TextComponent("Astra Ultralight Web Dashboard"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        super.init();
        ParticleEngine.INSTANCE.init(this.width, this.height);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        int accent = ThemeManager.INSTANCE.getAccent();

        // 1. Dark translucent backdrop with particles
        RenderUtils.drawGradientV(poseStack, 0, 0, width, height, 0xEE080B11, 0xF20F172A);
        ParticleEngine.INSTANCE.renderWithMouse(poseStack, mouseX, mouseY);

        // 2. Web Window Frame (Glassmorphism)
        int winW = Math.min(880, width - 20);
        int winH = Math.min(560, height - 20);
        int winX = (width - winW) / 2;
        int winY = (height - winH) / 2;

        RenderUtils.drawRoundedBorderedRect(poseStack, winX, winY, winW, winH, 8, 1, 0xF40B0F19, 0xFF334155);

        // 3. Sidebar (200px)
        int sideW = 190;
        RenderUtils.drawRoundedRect(poseStack, winX, winY, sideW, winH, 8, 0xCC0F172A);
        RenderUtils.drawRect(poseStack, winX + sideW, winY, 1, winH, 0xFF334155);

        // Logo
        RenderUtils.drawRoundedRect(poseStack, winX + 16, winY + 16, 28, 28, 6, accent);
        font.drawShadow(poseStack, "★", winX + 25, winY + 24, 0xFFFFFFFF);
        font.drawShadow(poseStack, "ASTRA", winX + 50, winY + 18, 0xFFFFFFFF);
        font.drawShadow(poseStack, "Web Dashboard", winX + 50, winY + 30, 0xFF94A3B8);

        // Category Navigation Tabs
        int tabY = winY + 60;
        for (Category cat : Category.values()) {
            boolean isSel = (cat == selectedCategory && searchQuery.isEmpty());
            boolean hover = mouseX >= winX + 10 && mouseX <= winX + sideW - 10 && mouseY >= tabY && mouseY <= tabY + 24;

            int tabBg = isSel ? accent : (hover ? 0x44334155 : 0x00000000);
            RenderUtils.drawRoundedRect(poseStack, winX + 10, tabY, sideW - 20, 24, 4, tabBg);

            int textCol = isSel ? 0xFFFFFFFF : (hover ? 0xFFE2E8F0 : 0xFF94A3B8);
            font.drawShadow(poseStack, cat.getDisplayName(), winX + 22, tabY + 8, textCol);
            tabY += 28;
        }

        // Status Indicator at bottom of sidebar
        int dotY = winY + winH - 22;
        RenderUtils.drawCircle(poseStack, winX + 20, dotY + 4, 3, 0xFF10B981);
        font.drawShadow(poseStack, "Live Bridge Active", winX + 28, dotY, 0xFF94A3B8);

        // 4. Content Panel Top Bar
        int contentX = winX + sideW + 18;
        int contentW = winW - sideW - 36;
        int topBarY = winY + 14;

        // Search Bar
        int searchW = 240;
        RenderUtils.drawRoundedBorderedRect(poseStack, contentX, topBarY, searchW, 24, 4, 1, 0xEE0F172A, 0xFF334155);
        font.drawShadow(poseStack, "🔍 " + (searchQuery.isEmpty() ? "Поиск модулей..." : searchQuery), contentX + 8, topBarY + 8, searchQuery.isEmpty() ? 0xFF64748B : 0xFFFFFFFF);

        // Close button
        int closeX = winX + winW - 28;
        boolean closeHover = mouseX >= closeX && mouseX <= closeX + 18 && mouseY >= topBarY && mouseY <= topBarY + 18;
        RenderUtils.drawRoundedRect(poseStack, closeX, topBarY, 18, 18, 4, closeHover ? 0xFFEF4444 : 0x44334155);
        font.drawShadow(poseStack, "✕", closeX + 5, topBarY + 5, 0xFFFFFFFF);

        // 5. Modules Grid
        int gridY = topBarY + 36;
        int gridH = winH - 60;

        List<Module> visibleMods = ModuleManager.INSTANCE.getModules().stream()
                .filter(m -> {
                    if (!searchQuery.isEmpty()) {
                        return m.getName().toLowerCase().contains(searchQuery.toLowerCase()) ||
                                m.getDescription().toLowerCase().contains(searchQuery.toLowerCase());
                    }
                    return m.getCategory() == selectedCategory;
                })
                .collect(Collectors.toList());

        // Header
        String catTitle = searchQuery.isEmpty() ? selectedCategory.getDisplayName() : "Результаты поиска: " + searchQuery;
        font.drawShadow(poseStack, catTitle, contentX, gridY, 0xFFFFFFFF);
        String countBadge = visibleMods.size() + " модулей";
        font.drawShadow(poseStack, countBadge, contentX + font.width(catTitle) + 12, gridY, accent);

        // Grid cards (2 columns)
        int cardStartY = gridY + 18 - (int) scrollY;
        int colW = (contentW - 12) / 2;
        int cardH = 50;

        for (int i = 0; i < visibleMods.size(); i++) {
            Module mod = visibleMods.get(i);
            int col = i % 2;
            int row = i / 2;

            int cx = contentX + col * (colW + 12);
            int cy = cardStartY + row * (cardH + 8);

            if (cy + cardH < gridY + 16 || cy > winY + winH - 10) continue;

            boolean hover = mouseX >= cx && mouseX <= cx + colW && mouseY >= cy && mouseY <= cy + cardH;
            int cardBg = mod.isEnabled() ? 0xEE172033 : 0xAA0F172A;
            int border = mod.isEnabled() ? accent : (hover ? 0xFF475569 : 0xFF334155);

            RenderUtils.drawRoundedBorderedRect(poseStack, cx, cy, colW, cardH, 6, 1, cardBg, border);
            if (mod.isEnabled()) {
                RenderUtils.drawRoundedRect(poseStack, cx, cy, 3, cardH, 2, accent);
            }

            // Name
            font.drawShadow(poseStack, mod.getName(), cx + 10, cy + 8, mod.isEnabled() ? 0xFFFFFFFF : 0xFFCBD5E1);

            // Toggle Switch
            int swX = cx + colW - 32;
            int swY = cy + 8;
            RenderUtils.drawRoundedRect(poseStack, swX, swY, 24, 12, 6, mod.isEnabled() ? accent : 0xFF334155);
            int knobX = mod.isEnabled() ? swX + 13 : swX + 1;
            RenderUtils.drawRoundedRect(poseStack, knobX, swY + 1, 10, 10, 5, 0xFFFFFFFF);

            // Desc
            String desc = font.plainSubstrByWidth(mod.getDescription(), colW - 20);
            font.drawShadow(poseStack, desc, cx + 10, cy + 24, 0xFF64748B);
        }

        super.render(poseStack, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int winW = Math.min(880, width - 20);
        int winH = Math.min(560, height - 20);
        int winX = (width - winW) / 2;
        int winY = (height - winH) / 2;

        // Close button
        int closeX = winX + winW - 28;
        int topBarY = winY + 14;
        if (mouseX >= closeX && mouseX <= closeX + 18 && mouseY >= topBarY && mouseY <= topBarY + 18) {
            onClose();
            return true;
        }

        // Category click
        int sideW = 190;
        int tabY = winY + 60;
        for (Category cat : Category.values()) {
            if (mouseX >= winX + 10 && mouseX <= winX + sideW - 10 && mouseY >= tabY && mouseY <= tabY + 24) {
                selectedCategory = cat;
                searchQuery = "";
                scrollY = 0;
                return true;
            }
            tabY += 28;
        }

        // Module toggle click
        int contentX = winX + sideW + 18;
        int contentW = winW - sideW - 36;
        int gridY = topBarY + 36;
        int cardStartY = gridY + 18 - (int) scrollY;
        int colW = (contentW - 12) / 2;
        int cardH = 50;

        List<Module> visibleMods = ModuleManager.INSTANCE.getModules().stream()
                .filter(m -> {
                    if (!searchQuery.isEmpty()) {
                        return m.getName().toLowerCase().contains(searchQuery.toLowerCase()) ||
                                m.getDescription().toLowerCase().contains(searchQuery.toLowerCase());
                    }
                    return m.getCategory() == selectedCategory;
                })
                .collect(Collectors.toList());

        for (int i = 0; i < visibleMods.size(); i++) {
            Module mod = visibleMods.get(i);
            int col = i % 2;
            int row = i / 2;
            int cx = contentX + col * (colW + 12);
            int cy = cardStartY + row * (cardH + 8);

            if (mouseX >= cx && mouseX <= cx + colW && mouseY >= cy && mouseY <= cy + cardH) {
                mod.toggle();
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (delta > 0) {
            scrollY = Math.max(0, scrollY - 24);
        } else if (delta < 0) {
            scrollY += 24;
        }
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            onClose();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !searchQuery.isEmpty()) {
            searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (Character.isLetterOrDigit(codePoint) || codePoint == ' ' || codePoint == '_') {
            searchQuery += codePoint;
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }
}
