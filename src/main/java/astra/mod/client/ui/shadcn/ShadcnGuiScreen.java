package astra.mod.client.ui.shadcn;

import astra.mod.client.Category;
import astra.mod.client.cosmetics.CosmeticManager;
import astra.mod.client.module.Module;
import astra.mod.client.module.ModuleManager;
import astra.mod.client.setting.BooleanSetting;
import astra.mod.client.setting.ModeSetting;
import astra.mod.client.setting.NumberSetting;
import astra.mod.client.setting.Setting;
import astra.mod.client.theme.ThemeManager;
import astra.mod.client.ui.crosshair.CrosshairEditorScreen;
import astra.mod.client.ui.hud.HudEditorScreen;
import astra.mod.client.util.RenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShadcnGuiScreen extends Screen {
    public static final ShadcnGuiScreen INSTANCE = new ShadcnGuiScreen();

    private enum Tab {
        COMBAT(Category.COMBAT, "Сражения"),
        BUILD(Category.BUILD, "Стройка"),
        MOVEMENT(Category.MOVEMENT, "Движение"),
        RENDER(Category.RENDER, "Визуалы"),
        HUD(Category.HUD, "Интерфейс"),
        PLAYER(Category.PLAYER, "Игрок"),
        THEMES(null, "Темы"),
        PROFILE(null, "Профиль");

        final Category category;
        final String title;

        Tab(Category category, String title) {
            this.category = category;
            this.title = title;
        }
    }

    private Tab currentTab = Tab.COMBAT;
    private final Map<Module, Boolean> expandedModules = new HashMap<>();

    private NumberSetting draggingNumber = null;
    private Module bindingModule = null;
    private boolean editingNick = false;
    private String nickInput = "";
    private boolean editingSearch = false;
    private String searchInput = "";
    private int scrollY = 0;
    private long openTime = 0;

    private final ParticleSystem particles = new ParticleSystem();

    public ShadcnGuiScreen() {
        super(new TextComponent("Astra Client"));
    }

    @Override
    protected void init() {
        super.init();
        particles.init(width, height);
        nickInput = CosmeticManager.INSTANCE.getCustomName();
        scrollY = 0;
        bindingModule = null;
        editingSearch = false;
        openTime = System.currentTimeMillis();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        scrollY += (int) (amount * 24);
        if (scrollY > 0) scrollY = 0;
        return true;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        int accent = ThemeManager.INSTANCE.getAccent();
        int accentDark = ThemeManager.INSTANCE.getAccentDark();

        renderBackground(poseStack, mouseX, mouseY, delta);

        float animProgress = Math.min(1.0f, (System.currentTimeMillis() - openTime) / 200.0f);
        float ease = (float) Math.sin(animProgress * Math.PI / 2.0);
        float scaleAnim = 0.94f + 0.06f * ease;

        poseStack.pushPose();
        poseStack.translate(width / 2.0, height / 2.0, 0);
        poseStack.scale(scaleAnim, scaleAnim, 1.0f);
        poseStack.translate(-width / 2.0, -height / 2.0, 0);

        int winW = Math.min(600, width - 20);
        int winH = Math.min(400, height - 20);
        int winX = (width - winW) / 2;
        int winY = (height - winH) / 2;

        RenderUtils.drawRoundedBorderedRect(poseStack, winX, winY, winW, winH, 12, 2, 0xF00B0F19, 0xFF334155);

        int headerH = 32;
        RenderUtils.drawRoundedRect(poseStack, winX, winY, winW, headerH + 12, 12, 0xFF0F172A);
        RenderUtils.drawRect(poseStack, winX, winY + headerH, winW, 12, 0xFF0F172A);

        font.drawShadow(poseStack, "Astra Client", winX + 16, winY + 10, accent);

        int searchW = Math.min(200, winW - 280);
        int searchX = winX + 130;
        int searchY = winY + 6;
        boolean searchHover = mouseX >= searchX && mouseX <= searchX + searchW && mouseY >= searchY && mouseY <= searchY + 20;
        RenderUtils.drawRoundedBorderedRect(poseStack, searchX, searchY, searchW, 20, 10, 1, 0xFF0A0F1D, editingSearch ? accent : (searchHover ? 0xFF475569 : 0xFF334155));

        String searchDisplayText = searchInput.isEmpty() ? (editingSearch ? "|" : "Поиск...") : (searchInput + (editingSearch ? "_" : ""));
        int searchTextColor = searchInput.isEmpty() ? 0xFF64748B : 0xFFCBD5E1;
        font.drawShadow(poseStack, searchDisplayText, searchX + 8, searchY + 6, searchTextColor);
        if (!searchInput.isEmpty()) {
            font.drawShadow(poseStack, "x", searchX + searchW - 14, searchY + 6, 0xFF94A3B8);
        }

        int hudBtnW = 100;
        int hudBtnX = winX + winW - hudBtnW - 40;
        int hudBtnY = winY + 6;
        boolean hudHover = mouseX >= hudBtnX && mouseX <= hudBtnX + hudBtnW && mouseY >= hudBtnY && mouseY <= hudBtnY + 20;
        RenderUtils.drawRoundedBorderedRect(poseStack, hudBtnX, hudBtnY, hudBtnW, 20, 10, 1, hudHover ? 0xDD1E293B : 0xAA0F172A, hudHover ? accent : 0xFF334155);
        font.drawShadow(poseStack, "HUD Editor", hudBtnX + 8, hudBtnY + 6, hudHover ? 0xFFFFFFFF : 0xFFCBD5E1);

        boolean closeHovered = mouseX >= winX + winW - 28 && mouseX <= winX + winW - 10 && mouseY >= winY + 8 && mouseY <= winY + 24;
        font.drawShadow(poseStack, "X", winX + winW - 22, winY + 10, closeHovered ? 0xFFEF4444 : 0xFF64748B);

        int sideW = 130;
        int contentX = winX + sideW;
        int contentY = winY + headerH;
        int contentW = winW - sideW;
        int contentH = winH - headerH;

        RenderUtils.drawRect(poseStack, winX, contentY, sideW, contentH, 0xFF0D111A);
        RenderUtils.drawRect(poseStack, contentX - 1, contentY, 1, contentH, 0xFF334155);
        RenderUtils.drawRect(poseStack, contentX, contentY, contentW, contentH, 0xF00B0F19);

        int tabY = contentY + 12;
        for (Tab tab : Tab.values()) {
            boolean active = (tab == currentTab);
            boolean hovered = mouseX >= winX + 8 && mouseX <= winX + sideW - 8 && mouseY >= tabY && mouseY <= tabY + 24;

            if (active) {
                RenderUtils.drawRoundedBorderedRect(poseStack, winX + 8, tabY, sideW - 16, 24, 8, 1, 0xEE1E1B4B, accent);
                RenderUtils.drawRoundedRect(poseStack, winX + 8, tabY, 3, 24, 2, accent);
            } else if (hovered) {
                RenderUtils.drawRoundedRect(poseStack, winX + 8, tabY, sideW - 16, 24, 8, 0x33334155);
            }

            int textCol = active ? 0xFFFFFFFF : (hovered ? 0xFFE2E8F0 : 0xFF94A3B8);
            font.drawShadow(poseStack, tab.title, winX + 14, tabY + 8, textCol);

            tabY += 28;
        }

        double scale = minecraft.getWindow().getGuiScale();
        int scissorX = (int) (contentX * scale);
        int scissorY = (int) ((height - (contentY + contentH)) * scale);
        int scissorW = (int) (contentW * scale);
        int scissorH = (int) (contentH * scale);

        com.mojang.blaze3d.systems.RenderSystem.enableScissor(scissorX, scissorY, scissorW, scissorH);

        if (currentTab.category != null) {
            renderModuleCards(poseStack, currentTab.category, contentX + 12, contentY + 12, contentW - 24, mouseX, mouseY, accent, accentDark);
        } else if (currentTab == Tab.THEMES) {
            renderThemesTab(poseStack, contentX + 12, contentY + 12 + scrollY, contentW - 24, mouseX, mouseY, accent);
        } else if (currentTab == Tab.PROFILE) {
            renderProfileTab(poseStack, contentX + 12, contentY + 12 + scrollY, contentW - 24, mouseX, mouseY, accent);
        }

        com.mojang.blaze3d.systems.RenderSystem.disableScissor();

        poseStack.popPose();

        super.render(poseStack, mouseX, mouseY, delta);
    }

    private void renderBackground(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        RenderUtils.drawRect(poseStack, 0, 0, width, height, 0xFF05070B);

        particles.render(poseStack, mouseX, mouseY, delta);

        float time = System.currentTimeMillis() / 1000.0f;
        for (int i = 0; i < 3; i++) {
            float x = (float) (width * (0.2 + 0.3 * i) + Math.sin(time * 0.3 + i) * 50);
            float y = (float) (height * (0.3 + 0.2 * i) + Math.cos(time * 0.2 + i) * 30);
            RenderUtils.drawCircle(poseStack, x, y, 80 + i * 40, 0x0A0B0F19);
        }
    }

    private List<Module> getDisplayedModules(Category category) {
        String query = searchInput.trim().toLowerCase();
        if (query.isEmpty()) {
            return category != null ? ModuleManager.INSTANCE.getModulesByCategory(category) : java.util.Collections.emptyList();
        }
        List<Module> res = new java.util.ArrayList<>();
        for (Module m : ModuleManager.INSTANCE.getModules()) {
            if (m.getName().toLowerCase().contains(query) || m.getDescription().toLowerCase().contains(query)) {
                res.add(m);
            }
        }
        return res;
    }

    private void renderModuleCards(PoseStack poseStack, Category category, int cx, int cy, int cw, int mouseX, int mouseY, int accent, int accentDark) {
        List<Module> modules = getDisplayedModules(category);
        if (modules.isEmpty()) {
            font.drawShadow(poseStack, "Ничего не найдено", cx + 10, cy + 20, 0xFF94A3B8);
            return;
        }
        int colW = (cw - 12) / 2;

        int row0Y = cy + scrollY;
        int row1Y = cy + scrollY;

        for (int i = 0; i < modules.size(); i++) {
            Module m = modules.get(i);
            boolean isCol1 = (i % 2 == 1);
            int cardX = isCol1 ? (cx + colW + 12) : cx;
            int cardY = isCol1 ? row1Y : row0Y;

            boolean expanded = expandedModules.getOrDefault(m, false);
            int cardH = getCardHeight(m, expanded);

            boolean cardHover = mouseX >= cardX && mouseX <= cardX + colW && mouseY >= cardY && mouseY <= cardY + cardH;
            int cardBg = m.isEnabled() ? 0xEE111827 : 0xEE0B0F19;
            int borderCol = m.isEnabled() ? accent : (cardHover ? 0xFF64748B : 0xFF223348);

            RenderUtils.drawRoundedBorderedRect(poseStack, cardX, cardY, colW, cardH, 10, 1, cardBg, borderCol);

            if (m.isEnabled()) {
                RenderUtils.drawRoundedRect(poseStack, cardX + 1, cardY + 1, 3, 34, 2, accent);
            }

            font.drawShadow(poseStack, m.getName(), cardX + 12, cardY + 10, m.isEnabled() ? 0xFFFFFFFF : 0xFFCBD5E1);

            String bindText = (bindingModule == m) ? "[...]" : (m.getKeyCode() != 0 ? "[" + getKeyName(m.getKeyCode()) + "]" : "[Bind]");
            int bindW = font.width(bindText) + 8;
            int bindX = cardX + colW - bindW - 32;
            int bindY = cardY + 8;
            boolean bindHover = mouseX >= bindX && mouseX <= bindX + bindW && mouseY >= bindY && mouseY <= bindY + 12;
            int bindColor = (bindingModule == m) ? 0xFFF59E0B : (m.getKeyCode() != 0 ? 0xFF818CF8 : 0xFF64748B);

            RenderUtils.drawRoundedBorderedRect(poseStack, bindX, bindY, bindW, 12, 6, 1, bindHover ? 0xDD1E293B : 0x660F172A, bindColor);
            font.drawShadow(poseStack, bindText, bindX + 4, bindY + 2, bindColor);

            RenderUtils.drawSwitch(poseStack, cardX + colW - 26, cardY + 8, 20, 12, m.isEnabled());

            if (!m.getSettings().isEmpty()) {
                font.drawShadow(poseStack, expanded ? "v" : ">", cardX + colW - 24, cardY + 26, 0xFF64748B);
            }

            String desc = m.getDescription();
            if (font.width(desc) > colW - 40) {
                desc = font.plainSubstrByWidth(desc, colW - 44) + "...";
            }
            font.drawShadow(poseStack, desc, cardX + 12, cardY + 26, 0xFF64748B);

            if (expanded) {
                int sY = cardY + 40;
                for (Setting<?> setting : m.getSettings()) {
                    renderSetting(poseStack, setting, cardX + 12, sY, colW - 24, mouseX, mouseY, accent);
                    sY += getSettingHeight(setting);
                }
                if (m instanceof astra.mod.client.module.impl.render.CustomCrosshair) {
                    int btnW = colW - 24;
                    int btnH = 20;
                    boolean btnHover = mouseX >= cardX + 12 && mouseX <= cardX + 12 + btnW && mouseY >= sY + 2 && mouseY <= sY + 2 + btnH;
                    RenderUtils.drawRoundedBorderedRect(poseStack, cardX + 12, sY + 2, btnW, btnH, 8, 1, btnHover ? 0xDD1E1B4B : 0xAA0F172A, btnHover ? accent : 0xFF334155);
                    font.drawShadow(poseStack, "Нарисовать прицел", cardX + 12 + (btnW - font.width("Нарисовать прицел")) / 2.0f, sY + 6, btnHover ? 0xFFFFFFFF : 0xFFCBD5E1);
                }
            }

            if (isCol1) row1Y += cardH + 10;
            else row0Y += cardH + 10;
        }
    }

    private String getKeyName(int keyCode) {
        if (keyCode <= 0 && keyCode >= -90) return "None";
        if (keyCode < -90) {
            int mouseBtn = -100 - keyCode;
            if (mouseBtn == 0) return "M1";
            if (mouseBtn == 1) return "M2";
            if (mouseBtn == 2) return "M3";
            return "M" + (mouseBtn + 1);
        }
        if (keyCode == GLFW.GLFW_KEY_UNKNOWN || keyCode <= 0) return "None";
        String name = GLFW.glfwGetKeyName(keyCode, 0);
        if (name != null && !name.trim().isEmpty()) return name.toUpperCase();
        if (keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) return "RSHIFT";
        if (keyCode == GLFW.GLFW_KEY_LEFT_SHIFT) return "LSHIFT";
        if (keyCode == GLFW.GLFW_KEY_RIGHT_CONTROL) return "RCTRL";
        if (keyCode == GLFW.GLFW_KEY_LEFT_CONTROL) return "LCTRL";
        if (keyCode == GLFW.GLFW_KEY_RIGHT_ALT) return "RALT";
        if (keyCode == GLFW.GLFW_KEY_LEFT_ALT) return "LALT";
        if (keyCode == GLFW.GLFW_KEY_SPACE) return "SPACE";
        if (keyCode == GLFW.GLFW_KEY_TAB) return "TAB";
        if (keyCode == GLFW.GLFW_KEY_CAPS_LOCK) return "CAPS";
        if (keyCode == GLFW.GLFW_KEY_UP) return "UP";
        if (keyCode == GLFW.GLFW_KEY_DOWN) return "DOWN";
        if (keyCode == GLFW.GLFW_KEY_LEFT) return "LEFT";
        if (keyCode == GLFW.GLFW_KEY_RIGHT) return "RIGHT";
        return "KEY_" + keyCode;
    }

    private void renderSetting(PoseStack poseStack, Setting<?> setting, int sx, int sy, int sw, int mouseX, int mouseY, int accent) {
        int h = getSettingHeight(setting);
        RenderUtils.drawRoundedRect(poseStack, sx, sy, sw, h, 6, 0x440F172A);

        if (setting instanceof BooleanSetting) {
            BooleanSetting bs = (BooleanSetting) setting;
            font.drawShadow(poseStack, bs.getName(), sx + 8, sy + 5, bs.getValue() ? 0xFFE2E8F0 : 0xFF64748B);
            RenderUtils.drawSwitch(poseStack, sx + sw - 24, sy + 4, 18, 10, bs.getValue());
        } else if (setting instanceof NumberSetting) {
            NumberSetting ns = (NumberSetting) setting;
            font.drawShadow(poseStack, ns.getName(), sx + 8, sy + 3, 0xFFCBD5E1);
            String valStr = String.valueOf(ns.getValue());
            font.drawShadow(poseStack, valStr, sx + sw - font.width(valStr) - 8, sy + 3, accent);

            double progress = (ns.getValue() - ns.getMin()) / (ns.getMax() - ns.getMin());
            RenderUtils.drawProgressBar(poseStack, sx + 8, sy + 14, sw - 16, 5, progress, 0xFF1E293B, accent);

            if (draggingNumber == ns) {
                double mouseProgress = (double) (mouseX - (sx + 8)) / (double) (sw - 16);
                mouseProgress = Math.max(0.0, Math.min(1.0, mouseProgress));
                double val = ns.getMin() + mouseProgress * (ns.getMax() - ns.getMin());
                ns.setValue(Math.round(val * 10.0) / 10.0);
            }
        } else if (setting instanceof ModeSetting) {
            ModeSetting ms = (ModeSetting) setting;
            font.drawShadow(poseStack, ms.getName(), sx + 8, sy + 5, 0xFFCBD5E1);
            String modeStr = "[" + ms.getValue() + "]";
            font.drawShadow(poseStack, modeStr, sx + sw - font.width(modeStr) - 8, sy + 5, accent);
        }
    }

    private void renderThemesTab(PoseStack poseStack, int cx, int cy, int cw, int mouseX, int mouseY, int accent) {
        font.drawShadow(poseStack, "ЦВЕТОВЫЕ ТЕМЫ", cx, cy, 0xFFFFFFFF);
        font.drawShadow(poseStack, "Выберите тему интерфейса", cx, cy + 14, 0xFF64748B);

        int gridY = cy + 34;
        int cardW = (cw - 18) / 2;
        int cardH = 32;

        ThemeManager.Theme[] themes = ThemeManager.Theme.values();
        for (int i = 0; i < themes.length; i++) {
            ThemeManager.Theme theme = themes[i];
            int x = (i % 2 == 0) ? cx : (cx + cardW + 18);
            int y = gridY + (i / 2) * 38;

            boolean active = (ThemeManager.INSTANCE.getCurrentTheme() == theme);
            boolean hover = mouseX >= x && mouseX <= x + cardW && mouseY >= y && mouseY <= y + cardH;

            RenderUtils.drawRoundedBorderedRect(poseStack, x, y, cardW, cardH, 10, 1, active ? 0xDD1E1B4B : 0xBB0F172A, active ? accent : 0x33475569);

            int dotColor = (theme == ThemeManager.Theme.CHROMA) ? ThemeManager.getRainbow(0) : theme.getPrimary();
            RenderUtils.drawCircle(poseStack, x + 16, y + 16, 7, dotColor);

            font.drawShadow(poseStack, theme.getDisplayName(), x + 30, y + 11, active ? 0xFFFFFFFF : 0xFFCBD5E1);
            if (active) {
                font.drawShadow(poseStack, "✓", x + cardW - 18, y + 11, accent);
            }
        }

        int partY = gridY + 160;
        RenderUtils.drawRoundedBorderedRect(poseStack, cx, partY, cw, 34, 10, 1, 0xBB0F172A, 0x33475569);
        font.drawShadow(poseStack, "Фоновые частицы", cx + 14, partY + 11, 0xFFFFFFFF);
        RenderUtils.drawSwitch(poseStack, cx + cw - 36, partY + 10, 22, 14, ThemeManager.INSTANCE.isParticles());
    }

    private void renderProfileTab(PoseStack poseStack, int cx, int cy, int cw, int mouseX, int mouseY, int accent) {
        font.drawShadow(poseStack, "ПРОФИЛЬ И КОСМЕТИКА", cx, cy, 0xFFFFFFFF);
        font.drawShadow(poseStack, "Визуальные настройки для сети", cx, cy + 14, 0xFF64748B);

        int nickY = cy + 34;
        RenderUtils.drawRoundedBorderedRect(poseStack, cx, nickY, cw, 66, 10, 1, 0xBB0F172A, 0x33475569);
        font.drawShadow(poseStack, "Визуальный ник", cx + 14, nickY + 12, 0xFFFFFFFF);

        int inputX = cx + 14;
        int inputY = nickY + 32;
        int inputW = cw - 100;
        RenderUtils.drawRoundedBorderedRect(poseStack, inputX, inputY, inputW, 22, 8, 1, 0xFF0A0F1D, editingNick ? accent : 0x44475569);

        String displayNick = nickInput.isEmpty() ? (editingNick ? "" : "Введите ник...") : nickInput;
        font.drawShadow(poseStack, displayNick, inputX + 8, inputY + 7, nickInput.isEmpty() ? 0xFF64748B : 0xFFFFFFFF);

        int btnX = inputX + inputW + 10;
        RenderUtils.drawRoundedBorderedRect(poseStack, btnX, inputY, 66, 22, 8, 1, 0xEE1E1B4B, accent);
        font.drawShadow(poseStack, "ОК", btnX + 8, inputY + 7, 0xFFFFFFFF);

        int capeY = nickY + 76;
        RenderUtils.drawRoundedBorderedRect(poseStack, cx, capeY, cw, 46, 10, 1, 0xBB0F172A, 0x33475569);
        font.drawShadow(poseStack, "Плащ Astra Cosmic", cx + 14, capeY + 12, 0xFFFFFFFF);
        font.drawShadow(poseStack, "Космический плащ HD", cx + 14, capeY + 26, 0xFF64748B);
        RenderUtils.drawSwitch(poseStack, cx + cw - 36, capeY + 16, 22, 14, CosmeticManager.INSTANCE.isCapeEnabled());

        int pvpY = capeY + 56;
        RenderUtils.drawRoundedBorderedRect(poseStack, cx, pvpY, cw, 46, 10, 1, 0xBB0F172A, 0x33475569);
        font.drawShadow(poseStack, "Ресурс-пак Astra PvP", cx + 14, pvpY + 12, 0xFFFFFFFF);
        font.drawShadow(poseStack, "Активен", cx + 14, pvpY + 26, 0xFF10B981);
    }

    private int getCardHeight(Module m, boolean expanded) {
        int h = 40;
        if (expanded) {
            for (Setting<?> s : m.getSettings()) {
                h += getSettingHeight(s);
            }
            if (m instanceof astra.mod.client.module.impl.render.CustomCrosshair) {
                h += 24;
            }
        }
        return h;
    }

    private int getSettingHeight(Setting<?> s) {
        return (s instanceof NumberSetting) ? 22 : 18;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (bindingModule != null) {
            bindingModule.setKeyCode(-100 - button);
            bindingModule = null;
            astra.mod.client.config.ConfigManager.INSTANCE.save();
            return true;
        }

        int winW = Math.min(600, width - 20);
        int winH = Math.min(400, height - 20);
        int winX = (width - winW) / 2;
        int winY = (height - winH) / 2;
        int headerH = 32;

        int hudBtnW = 100;
        int hudBtnX = winX + winW - hudBtnW - 40;
        int hudBtnY = winY + 6;
        if (mouseX >= hudBtnX && mouseX <= hudBtnX + hudBtnW && mouseY >= hudBtnY && mouseY <= hudBtnY + 20) {
            if (minecraft != null) {
                minecraft.setScreen(HudEditorScreen.INSTANCE);
            }
            return true;
        }

        int searchW = Math.min(200, winW - 280);
        int searchX = winX + 130;
        int searchY = winY + 6;
        if (mouseX >= searchX && mouseX <= searchX + searchW && mouseY >= searchY && mouseY <= searchY + 20) {
            if (!searchInput.isEmpty() && mouseX >= searchX + searchW - 18) {
                searchInput = "";
            } else {
                editingSearch = true;
                editingNick = false;
            }
            return true;
        } else {
            editingSearch = false;
        }

        if (mouseX >= winX + winW - 28 && mouseX <= winX + winW - 10 && mouseY >= winY + 8 && mouseY <= winY + 24) {
            onClose();
            return true;
        }

        int sideW = 130;
        int tabY = winY + headerH + 12;
        for (Tab tab : Tab.values()) {
            if (mouseX >= winX + 8 && mouseX <= winX + sideW - 8 && mouseY >= tabY && mouseY <= tabY + 24) {
                currentTab = tab;
                editingNick = false;
                return true;
            }
            tabY += 28;
        }

        int contentX = winX + sideW;
        int contentY = winY + headerH;
        int contentW = winW - sideW;
        int contentH = winH - headerH;

        if (mouseX < contentX || mouseX > contentX + contentW || mouseY < contentY || mouseY > contentY + contentH) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        if (currentTab.category != null || !searchInput.trim().isEmpty()) {
            List<Module> modules = getDisplayedModules(currentTab.category);
            int colW = (contentW - 36) / 2;
            int row0Y = contentY + 12 + scrollY;
            int row1Y = contentY + 12 + scrollY;

            for (int i = 0; i < modules.size(); i++) {
                Module m = modules.get(i);
                boolean isCol1 = (i % 2 == 1);
                int cardX = isCol1 ? (contentX + 12 + colW + 12) : (contentX + 12);
                int cardY = isCol1 ? row1Y : row0Y;

                boolean expanded = expandedModules.getOrDefault(m, false);
                int cardH = getCardHeight(m, expanded);

                String bindText = (bindingModule == m) ? "[...]" : (m.getKeyCode() != 0 ? "[" + getKeyName(m.getKeyCode()) + "]" : "[Bind]");
                int bindW = font.width(bindText) + 8;
                int bindX = cardX + colW - bindW - 32;
                int bindY = cardY + 8;
                if (mouseX >= bindX && mouseX <= bindX + bindW && mouseY >= bindY && mouseY <= bindY + 12) {
                    bindingModule = (bindingModule == m) ? null : m;
                    return true;
                }

                if (mouseX >= cardX && mouseX <= cardX + colW && mouseY >= cardY && mouseY <= cardY + 40) {
                    if (button == 0) {
                        m.toggle();
                        return true;
                    } else if (button == 1) {
                        if (!m.getSettings().isEmpty()) {
                            expandedModules.put(m, !expanded);
                            return true;
                        }
                    } else if (button == 2) {
                        bindingModule = (bindingModule == m) ? null : m;
                        return true;
                    }
                }

                if (expanded) {
                    int sY = cardY + 40;
                    for (Setting<?> setting : m.getSettings()) {
                        int sH = getSettingHeight(setting);
                        int sx = cardX + 12;
                        int sw = colW - 24;
                        if (mouseX >= sx && mouseX <= sx + sw && mouseY >= sY && mouseY <= sY + sH) {
                            if (setting instanceof BooleanSetting && button == 0) {
                                ((BooleanSetting) setting).toggle();
                                return true;
                            } else if (setting instanceof NumberSetting && button == 0) {
                                NumberSetting ns = (NumberSetting) setting;
                                draggingNumber = ns;
                                double mouseProgress = (mouseX - (sx + 8)) / (double) (sw - 16);
                                mouseProgress = Math.max(0.0, Math.min(1.0, mouseProgress));
                                double val = ns.getMin() + mouseProgress * (ns.getMax() - ns.getMin());
                                ns.setValue(Math.round(val * 10.0) / 10.0);
                                return true;
                            } else if (setting instanceof ModeSetting && (button == 0 || button == 1)) {
                                ((ModeSetting) setting).cycle();
                                return true;
                            }
                        }
                        sY += sH;
                    }
                    if (m instanceof astra.mod.client.module.impl.render.CustomCrosshair) {
                        int btnW = colW - 24;
                        int btnH = 20;
                        if (mouseX >= cardX + 12 && mouseX <= cardX + 12 + btnW && mouseY >= sY + 2 && mouseY <= sY + 2 + btnH) {
                            if (minecraft != null) {
                                minecraft.setScreen(CrosshairEditorScreen.INSTANCE);
                            }
                            return true;
                        }
                    }
                }

                if (isCol1) row1Y += cardH + 10;
                else row0Y += cardH + 10;
            }
        } else if (currentTab == Tab.THEMES) {
            int gridY = contentY + 46;
            int cardW = (contentW - 42) / 2;
            ThemeManager.Theme[] themes = ThemeManager.Theme.values();
            for (int i = 0; i < themes.length; i++) {
                int x = (i % 2 == 0) ? (contentX + 12) : (contentX + 12 + cardW + 18);
                int y = gridY + (i / 2) * 38;
                if (mouseX >= x && mouseX <= x + cardW && mouseY >= y && mouseY <= y + 32) {
                    ThemeManager.INSTANCE.setTheme(themes[i]);
                    return true;
                }
            }

            int partY = gridY + 160;
            for (mouseX >= contentX + 12 && mouseX <= contentX + contentW - 12 && mouseY >= partY && mouseY <= partY + 34) {
                ThemeManager.INSTANCE.setParticles(!ThemeManager.INSTANCE.isParticles());
                return true;
            }
        } else if (currentTab == Tab.PROFILE) {
            int nickY = contentY + 46;
            int inputX = contentX + 26;
            int inputY = nickY + 32;
            int inputW = contentW - 124;

            for (mouseX >= inputX && mouseX <= inputX + inputW && mouseY >= inputY && mouseY <= inputY + 22) {
                editingNick = true;
                return true;
            } else {
                editingNick = false;
            }

            int btnX = inputX + inputW + 10;
            for (mouseX >= btnX && mouseX <= btnX + 66 && mouseY >= inputY && mouseY <= inputY + 22) {
                CosmeticManager.INSTANCE.setSystemNick(nickInput);
                return true;
            }

            int capeY = nickY + 76;
            for (mouseX >= contentX + 12 && mouseX <= contentX + contentW - 12 && mouseY >= capeY && mouseY <= capeY + 46) {
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
        if (editingSearch) {
            if (searchInput.length() < 30 && (Character.isLetterOrDigit(codePoint) || Character.isWhitespace(codePoint) || codePoint == '_' || codePoint == '-')) {
                searchInput += codePoint;
                return true;
            }
        }
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
        if (bindingModule != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_DELETE || keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                bindingModule.setKeyCode(0);
            } else {
                bindingModule.setKeyCode(keyCode);
            }
            bindingModule = null;
            astra.mod.client.config.ConfigManager.INSTANCE.save();
            return true;
        }

        if (editingSearch) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !searchInput.isEmpty()) {
                searchInput = searchInput.substring(0, searchInput.length() - 1);
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_ESCAPE) {
                editingSearch = false;
                return true;
            }
        }

        if (editingNick) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !nickInput.isEmpty()) {
                nickInput = nickInput.substring(0, nickInput.length() - 1);
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_ENTER) {
                CosmeticManager.INSTANCE.setSystemNick(nickInput);
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
    public void onClose() {
        astra.mod.client.config.ConfigManager.INSTANCE.save();
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
