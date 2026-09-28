package astra.mod.client.ui.clickgui;

import astra.mod.client.Category;
import astra.mod.client.cosmetics.CosmeticManager;
import astra.mod.client.module.Module;
import astra.mod.client.module.ModuleManager;
import astra.mod.client.setting.BooleanSetting;
import astra.mod.client.setting.ModeSetting;
import astra.mod.client.setting.NumberSetting;
import astra.mod.client.setting.Setting;
import astra.mod.client.theme.ThemeManager;
import astra.mod.client.util.RenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClickGuiScreen extends Screen {
    public static final ClickGuiScreen INSTANCE = new ClickGuiScreen();

    private enum WebTab {
        COMBAT(Category.COMBAT, "⚔ Combat"),
        MOVEMENT(Category.MOVEMENT, "⚡ Movement"),
        RENDER(Category.RENDER, "👁 Render"),
        PLAYER(Category.PLAYER, "🎮 Player"),
        THEMES(null, "🎨 Themes"),
        PROFILE(null, "👤 Profile & Cape");

        final Category category;
        final String title;

        WebTab(Category category, String title) {
            this.category = category;
            this.title = title;
        }
    }

    private WebTab currentTab = WebTab.COMBAT;
    private final Map<Module, Boolean> expandedModules = new HashMap<>();

    // Draggable sliders & text input state
    private NumberSetting draggingNumber = null;
    private boolean editingNick = false;
    private String nickInput = "";

    public ClickGuiScreen() {
        super(new TextComponent("Astra Dashboard"));
    }

    @Override
    protected void init() {
        super.init();
        ParticleEngine.INSTANCE.init(width, height);
        nickInput = CosmeticManager.INSTANCE.getCustomName();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        int accent = ThemeManager.INSTANCE.getAccent();
        int accentDark = ThemeManager.INSTANCE.getAccentDark();

        // 1. Fullscreen dark backdrop
        RenderUtils.drawRect(poseStack, 0, 0, width, height, 0x9905070B);

        // 2. Cosmic background particles
        ParticleEngine.INSTANCE.render(poseStack);

        // 3. Central Web Window Container
        int winW = Math.min(540, width - 20);
        int winH = Math.min(360, height - 20);
        int winX = (width - winW) / 2;
        int winY = (height - winH) / 2;

        // Window drop shadow & outer border
        RenderUtils.drawBorderedRect(poseStack, winX, winY, winW, winH, 1, 0xF50B0E14, 0x44334155);

        // 4. Web Browser Window Header Bar (height: 28px)
        int headerH = 28;
        RenderUtils.drawRect(poseStack, winX, winY, winW, headerH, 0xFF0F172A);
        RenderUtils.drawRect(poseStack, winX, winY + headerH - 1, winW, 1, 0x33475569);

        // Browser traffic light control dots
        RenderUtils.drawRect(poseStack, winX + 12, winY + 10, 8, 8, 0xFFEF4444); // Red
        RenderUtils.drawRect(poseStack, winX + 24, winY + 10, 8, 8, 0xFFF59E0B); // Yellow
        RenderUtils.drawRect(poseStack, winX + 36, winY + 10, 8, 8, 0xFF10B981); // Green

        // Browser URL search bar
        int urlW = Math.min(220, winW - 140);
        int urlX = winX + 54;
        int urlY = winY + 5;
        RenderUtils.drawBorderedRect(poseStack, urlX, urlY, urlW, 18, 1, 0xFF0A0F1D, 0x44475569);
        font.drawShadow(poseStack, "🔒 astra://client/dashboard", urlX + 8, urlY + 5, 0xFF94A3B8);

        // Top right close button
        boolean closeHovered = mouseX >= winX + winW - 24 && mouseX <= winX + winW - 8 && mouseY >= winY + 6 && mouseY <= winY + 22;
        font.drawShadow(poseStack, "✕", winX + winW - 20, winY + 9, closeHovered ? 0xFFEF4444 : 0xFF64748B);

        // 5. Left Sidebar Navigation (width: 125px)
        int sideW = 125;
        int contentX = winX + sideW;
        int contentY = winY + headerH;
        int contentW = winW - sideW;
        int contentH = winH - headerH;

        RenderUtils.drawRect(poseStack, winX, contentY, sideW, contentH, 0xFF0D111A);
        RenderUtils.drawRect(poseStack, contentX - 1, contentY, 1, contentH, 0x22FFFFFF);

        // Render Tabs
        int tabY = contentY + 10;
        for (WebTab tab : WebTab.values()) {
            boolean active = (tab == currentTab);
            boolean hovered = mouseX >= winX + 6 && mouseX <= winX + sideW - 6 && mouseY >= tabY && mouseY <= tabY + 22;

            if (active) {
                RenderUtils.drawBorderedRect(poseStack, winX + 6, tabY, sideW - 12, 22, 1, 0xEE1E1B4B, accent);
                RenderUtils.drawRect(poseStack, winX + 6, tabY, 2, 22, accent);
            } else if (hovered) {
                RenderUtils.drawRect(poseStack, winX + 6, tabY, sideW - 12, 22, 0x33334155);
            }

            int textCol = active ? 0xFFFFFFFF : (hovered ? 0xFFE2E8F0 : 0xFF94A3B8);
            font.drawShadow(poseStack, tab.title, winX + 14, tabY + 7, textCol);

            tabY += 26;
        }

        // 6. Right Content Area (Modules / Themes / Profile)
        if (currentTab.category != null) {
            renderModuleCards(poseStack, currentTab.category, contentX + 10, contentY + 10, contentW - 20, mouseX, mouseY, accent, accentDark);
        } else if (currentTab == WebTab.THEMES) {
            renderThemesTab(poseStack, contentX + 10, contentY + 10, contentW - 20, mouseX, mouseY, accent);
        } else if (currentTab == WebTab.PROFILE) {
            renderProfileTab(poseStack, contentX + 10, contentY + 10, contentW - 20, mouseX, mouseY, accent);
        }

        super.render(poseStack, mouseX, mouseY, delta);
    }

    private void renderModuleCards(PoseStack poseStack, Category category, int cx, int cy, int cw, int mouseX, int mouseY, int accent, int accentDark) {
        List<Module> modules = ModuleManager.INSTANCE.getModulesByCategory(category);
        int colW = (cw - 10) / 2;

        int row0Y = cy;
        int row1Y = cy;

        for (int i = 0; i < modules.size(); i++) {
            Module m = modules.get(i);
            boolean isCol1 = (i % 2 == 1);
            int cardX = isCol1 ? (cx + colW + 10) : cx;
            int cardY = isCol1 ? row1Y : row0Y;

            boolean expanded = expandedModules.getOrDefault(m, false);
            int cardH = getCardHeight(m, expanded);

            // Card background & modern rounded border
            boolean cardHover = mouseX >= cardX && mouseX <= cardX + colW && mouseY >= cardY && mouseY <= cardY + cardH;
            int cardBg = m.isEnabled() ? 0xDD111827 : 0xBB0B0F19;
            int borderCol = m.isEnabled() ? accent : (cardHover ? 0x66475569 : 0x22334155);

            RenderUtils.drawBorderedRect(poseStack, cardX, cardY, colW, cardH, 1, cardBg, borderCol);

            if (m.isEnabled()) {
                RenderUtils.drawRect(poseStack, cardX + 1, cardY + 1, 2, 34, accent);
            }

            // Card Title & Toggle
            font.drawShadow(poseStack, m.getName(), cardX + 10, cardY + 7, m.isEnabled() ? 0xFFFFFFFF : 0xFFCBD5E1);

            // Settings gear / arrow
            if (!m.getSettings().isEmpty()) {
                font.drawShadow(poseStack, expanded ? "▼" : "▶", cardX + colW - 38, cardY + 8, 0xFF64748B);
            }

            // iOS-style switch
            RenderUtils.drawSwitch(poseStack, cardX + colW - 26, cardY + 6, 18, 10, m.isEnabled());

            // Card description
            String desc = m.getDescription();
            if (font.width(desc) > colW - 20) {
                desc = font.plainSubstrByWidth(desc, colW - 24) + "...";
            }
            font.drawShadow(poseStack, desc, cardX + 10, cardY + 22, 0xFF64748B);

            // Expanded settings inside card container
            if (expanded) {
                int sY = cardY + 36;
                for (Setting<?> setting : m.getSettings()) {
                    renderSetting(poseStack, setting, cardX + 8, sY, colW - 16, mouseX, mouseY, accent);
                    sY += getSettingHeight(setting);
                }
            }

            if (isCol1) {
                row1Y += cardH + 8;
            } else {
                row0Y += cardH + 8;
            }
        }
    }

    private void renderSetting(PoseStack poseStack, Setting<?> setting, int sx, int sy, int sw, int mouseX, int mouseY, int accent) {
        int h = getSettingHeight(setting);
        RenderUtils.drawRect(poseStack, sx, sy, sw, h, 0x440F172A);

        if (setting instanceof BooleanSetting) {
            BooleanSetting bs = (BooleanSetting) setting;
            font.drawShadow(poseStack, bs.getName(), sx + 6, sy + 4, bs.getValue() ? 0xFFE2E8F0 : 0xFF64748B);
            RenderUtils.drawSwitch(poseStack, sx + sw - 20, sy + 3, 16, 8, bs.getValue());
        } else if (setting instanceof NumberSetting) {
            NumberSetting ns = (NumberSetting) setting;
            font.drawShadow(poseStack, ns.getName(), sx + 6, sy + 2, 0xFFCBD5E1);
            String valStr = String.valueOf(ns.getValue());
            font.drawShadow(poseStack, valStr, sx + sw - font.width(valStr) - 6, sy + 2, accent);

            double progress = (ns.getValue() - ns.getMin()) / (ns.getMax() - ns.getMin());
            RenderUtils.drawProgressBar(poseStack, sx + 6, sy + 12, sw - 12, 4, progress, 0xFF1E293B, accent);

            if (draggingNumber == ns) {
                double mouseProgress = (double) (mouseX - (sx + 6)) / (double) (sw - 12);
                double val = ns.getMin() + mouseProgress * (ns.getMax() - ns.getMin());
                ns.setValue(val);
            }
        } else if (setting instanceof ModeSetting) {
            ModeSetting ms = (ModeSetting) setting;
            font.drawShadow(poseStack, ms.getName(), sx + 6, sy + 4, 0xFFCBD5E1);
            String modeStr = "[" + ms.getValue() + "]";
            font.drawShadow(poseStack, modeStr, sx + sw - font.width(modeStr) - 6, sy + 4, accent);
        }
    }

    private void renderThemesTab(PoseStack poseStack, int cx, int cy, int cw, int mouseX, int mouseY, int accent) {
        font.drawShadow(poseStack, "ВЫБОР ЦВЕТОВОЙ ТЕМЫ ИНТЕРФЕЙСА", cx, cy, 0xFFFFFFFF);
        font.drawShadow(poseStack, "Кликните по любой карточке для мгновенного применения темы", cx, cy + 12, 0xFF64748B);

        int gridY = cy + 30;
        int cardW = (cw - 15) / 2;
        int cardH = 28;

        ThemeManager.Theme[] themes = ThemeManager.Theme.values();
        for (int i = 0; i < themes.length; i++) {
            ThemeManager.Theme theme = themes[i];
            int x = (i % 2 == 0) ? cx : (cx + cardW + 15);
            int y = gridY + (i / 2) * 34;

            boolean active = (ThemeManager.INSTANCE.getCurrentTheme() == theme);
            boolean hover = mouseX >= x && mouseX <= x + cardW && mouseY >= y && mouseY <= y + cardH;

            RenderUtils.drawBorderedRect(poseStack, x, y, cardW, cardH, 1, active ? 0xDD1E1B4B : 0xBB0F172A, active ? accent : 0x33475569);

            // Color circle preview
            int dotColor = (theme == ThemeManager.Theme.CHROMA) ? ThemeManager.getRainbow(0) : theme.getPrimary();
            RenderUtils.drawRect(poseStack, x + 8, y + 8, 12, 12, dotColor);

            font.drawShadow(poseStack, theme.getDisplayName(), x + 26, y + 10, active ? 0xFFFFFFFF : 0xFFCBD5E1);
            if (active) {
                font.drawShadow(poseStack, "✓", x + cardW - 16, y + 10, accent);
            }
        }

        // Particle toggle card
        int partY = gridY + 145;
        RenderUtils.drawBorderedRect(poseStack, cx, partY, cw, 30, 1, 0xBB0F172A, 0x33475569);
        font.drawShadow(poseStack, "Космические фоновые частицы", cx + 12, partY + 10, 0xFFFFFFFF);
        RenderUtils.drawSwitch(poseStack, cx + cw - 32, partY + 9, 20, 12, ThemeManager.INSTANCE.isParticles());
    }

    private void renderProfileTab(PoseStack poseStack, int cx, int cy, int cw, int mouseX, int mouseY, int accent) {
        font.drawShadow(poseStack, "ПРОФИЛЬ, ВИЗУАЛЬНЫЙ НИК И КОСМЕТИКА", cx, cy, 0xFFFFFFFF);
        font.drawShadow(poseStack, "Работает на любых серверах в сетевой игре (Multiplayer)", cx, cy + 12, 0xFF64748B);

        // Card 1: Visual Nick Changer
        int nickY = cy + 30;
        RenderUtils.drawBorderedRect(poseStack, cx, nickY, cw, 60, 1, 0xBB0F172A, 0x33475569);
        font.drawShadow(poseStack, "Визуальная смена ника", cx + 12, nickY + 10, 0xFFFFFFFF);

        int inputX = cx + 12;
        int inputY = nickY + 28;
        int inputW = cw - 90;
        RenderUtils.drawBorderedRect(poseStack, inputX, inputY, inputW, 20, 1, 0xFF0A0F1D, editingNick ? accent : 0x44475569);

        String displayNick = nickInput.isEmpty() ? (editingNick ? "" : "Введите ваш новый ник...") : nickInput;
        font.drawShadow(poseStack, displayNick, inputX + 6, inputY + 6, nickInput.isEmpty() ? 0xFF64748B : 0xFFFFFFFF);

        // Apply Nick Button
        int btnX = inputX + inputW + 8;
        RenderUtils.drawBorderedRect(poseStack, btnX, inputY, 60, 20, 1, 0xEE1E1B4B, accent);
        font.drawShadow(poseStack, "Применить", btnX + 6, inputY + 6, 0xFFFFFFFF);

        // Card 2: Visual Astra Cape
        int capeY = nickY + 70;
        RenderUtils.drawBorderedRect(poseStack, cx, capeY, cw, 42, 1, 0xBB0F172A, 0x33475569);
        font.drawShadow(poseStack, "Фирменный плащ Astra Cosmic Cape", cx + 12, capeY + 10, 0xFFFFFFFF);
        font.drawShadow(poseStack, "Отображает космический плащ за спиной", cx + 12, capeY + 22, 0xFF64748B);
        RenderUtils.drawSwitch(poseStack, cx + cw - 32, capeY + 15, 20, 12, CosmeticManager.INSTANCE.isCapeEnabled());

        // Card 3: Built-in PvP Pack Status
        int pvpY = capeY + 52;
        RenderUtils.drawBorderedRect(poseStack, cx, pvpY, cw, 42, 1, 0xBB0F172A, 0x33475569);
        font.drawShadow(poseStack, "Встроенный ресурс-пак Astra PvP Pack", cx + 12, pvpY + 10, 0xFFFFFFFF);
        font.drawShadow(poseStack, "Статус: Всегда активен по умолчанию [OK]", cx + 12, pvpY + 22, 0xFF10B981);
    }

    private int getCardHeight(Module m, boolean expanded) {
        int h = 36;
        if (expanded) {
            for (Setting<?> s : m.getSettings()) {
                h += getSettingHeight(s);
            }
        }
        return h;
    }

    private int getSettingHeight(Setting<?> s) {
        return (s instanceof NumberSetting) ? 20 : 16;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int winW = Math.min(540, width - 20);
        int winH = Math.min(360, height - 20);
        int winX = (width - winW) / 2;
        int winY = (height - winH) / 2;
        int headerH = 28;

        // Close button click
        if (mouseX >= winX + winW - 24 && mouseX <= winX + winW - 8 && mouseY >= winY + 6 && mouseY <= winY + 22) {
            onClose();
            return true;
        }

        // Tabs click
        int sideW = 125;
        int tabY = winY + headerH + 10;
        for (WebTab tab : WebTab.values()) {
            if (mouseX >= winX + 6 && mouseX <= winX + sideW - 6 && mouseY >= tabY && mouseY <= tabY + 22) {
                currentTab = tab;
                editingNick = false;
                return true;
            }
            tabY += 26;
        }

        int contentX = winX + sideW;
        int contentY = winY + headerH;
        int contentW = winW - sideW;

        if (currentTab.category != null) {
            List<Module> modules = ModuleManager.INSTANCE.getModulesByCategory(currentTab.category);
            int colW = (contentW - 30) / 2;
            int row0Y = contentY + 10;
            int row1Y = contentY + 10;

            for (int i = 0; i < modules.size(); i++) {
                Module m = modules.get(i);
                boolean isCol1 = (i % 2 == 1);
                int cardX = isCol1 ? (contentX + 10 + colW + 10) : (contentX + 10);
                int cardY = isCol1 ? row1Y : row0Y;

                boolean expanded = expandedModules.getOrDefault(m, false);
                int cardH = getCardHeight(m, expanded);

                if (mouseX >= cardX && mouseX <= cardX + colW && mouseY >= cardY && mouseY <= cardY + 36) {
                    if (button == 0) { // Toggle module
                        m.toggle();
                        return true;
                    } else if (button == 1) { // Expand/collapse settings
                        if (!m.getSettings().isEmpty()) {
                            expandedModules.put(m, !expanded);
                            return true;
                        }
                    }
                }

                // Clicks inside settings
                if (expanded) {
                    int sY = cardY + 36;
                    for (Setting<?> setting : m.getSettings()) {
                        int sH = getSettingHeight(setting);
                        int sx = cardX + 8;
                        int sw = colW - 16;
                        if (mouseX >= sx && mouseX <= sx + sw && mouseY >= sY && mouseY <= sY + sH) {
                            if (setting instanceof BooleanSetting && button == 0) {
                                ((BooleanSetting) setting).toggle();
                                return true;
                            } else if (setting instanceof NumberSetting && button == 0) {
                                NumberSetting ns = (NumberSetting) setting;
                                draggingNumber = ns;
                                double mouseProgress = (mouseX - (sx + 6)) / (double) (sw - 12);
                                double val = ns.getMin() + mouseProgress * (ns.getMax() - ns.getMin());
                                ns.setValue(val);
                                return true;
                            } else if (setting instanceof ModeSetting && (button == 0 || button == 1)) {
                                ((ModeSetting) setting).cycle();
                                return true;
                            }
                        }
                        sY += sH;
                    }
                }

                if (isCol1) row1Y += cardH + 8;
                else row0Y += cardH + 8;
            }
        } else if (currentTab == WebTab.THEMES) {
            int gridY = contentY + 40;
            int cardW = (contentW - 35) / 2;
            ThemeManager.Theme[] themes = ThemeManager.Theme.values();
            for (int i = 0; i < themes.length; i++) {
                int x = (i % 2 == 0) ? (contentX + 10) : (contentX + 10 + cardW + 15);
                int y = gridY + (i / 2) * 34;
                if (mouseX >= x && mouseX <= x + cardW && mouseY >= y && mouseY <= y + 28) {
                    ThemeManager.INSTANCE.setTheme(themes[i]);
                    return true;
                }
            }

            // Particle toggle click
            int partY = gridY + 145;
            if (mouseX >= contentX + 10 && mouseX <= contentX + contentW - 10 && mouseY >= partY && mouseY <= partY + 30) {
                ThemeManager.INSTANCE.setParticles(!ThemeManager.INSTANCE.isParticles());
                return true;
            }
        } else if (currentTab == WebTab.PROFILE) {
            int nickY = contentY + 40;
            int inputX = contentX + 22;
            int inputY = nickY + 28;
            int inputW = contentW - 110;

            // Nick input click
            if (mouseX >= inputX && mouseX <= inputX + inputW && mouseY >= inputY && mouseY <= inputY + 20) {
                editingNick = true;
                return true;
            } else {
                editingNick = false;
            }

            // Apply button click
            int btnX = inputX + inputW + 8;
            if (mouseX >= btnX && mouseX <= btnX + 60 && mouseY >= inputY && mouseY <= inputY + 20) {
                CosmeticManager.INSTANCE.setCustomName(nickInput);
                return true;
            }

            // Cape toggle click
            int capeY = nickY + 70;
            if (mouseX >= contentX + 10 && mouseX <= contentX + contentW - 10 && mouseY >= capeY && mouseY <= capeY + 42) {
                CosmeticManager.INSTANCE.setCapeEnabled(!CosmeticManager.INSTANCE.isCapeEnabled());
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingNumber = null;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (editingNick) {
            if (nickInput.length() < 16 && (Character.isLetterOrDigit(codePoint) || codePoint == '_')) {
                nickInput += codePoint;
                return true;
            }
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (editingNick) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !nickInput.isEmpty()) {
                nickInput = nickInput.substring(0, nickInput.length() - 1);
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_ENTER) {
                CosmeticManager.INSTANCE.setCustomName(nickInput);
                editingNick = false;
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                editingNick = false;
                return true;
            }
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
