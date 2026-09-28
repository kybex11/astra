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
import astra.mod.client.module.impl.render.CustomCrosshair;
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

public class ClickGuiScreen extends Screen {
    public static final ClickGuiScreen INSTANCE = new ClickGuiScreen();

    private enum WebTab {
        COMBAT(Category.COMBAT, "[PvP] Сражения"),
        BUILD(Category.BUILD, "[BLD] Стройка"),
        MOVEMENT(Category.MOVEMENT, "[MOV] Движение"),
        RENDER(Category.RENDER, "[VIS] Визуалы"),
        HUD(Category.HUD, "[HUD] Интерфейс"),
        PLAYER(Category.PLAYER, "[PLY] Игрок"),
        THEMES(null, "[THM] Темы"),
        PROFILE(null, "[PRF] Профиль & Плащ");

        final Category category;
        final String title;

        WebTab(Category category, String title) {
            this.category = category;
            this.title = title;
        }
    }

    private WebTab currentTab = WebTab.COMBAT;
    private final Map<Module, Boolean> expandedModules = new HashMap<>();

    // Draggable sliders, text input & keybinding state
    private NumberSetting draggingNumber = null;
    private Module bindingModule = null;
    private boolean editingNick = false;
    private String nickInput = "";
    private boolean editingSearch = false;
    private String searchInput = "";
    private int scrollY = 0;
    private long openTime = 0;

    public ClickGuiScreen() {
        super(new TextComponent("Astra Dashboard"));
    }

    @Override
    protected void init() {
        super.init();
        ParticleEngine.INSTANCE.init(width, height);
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

        // 1. Fullscreen dark backdrop
        RenderUtils.drawRect(poseStack, 0, 0, width, height, 0xAA05070B);

        // 2. Interactive neural particles
        ParticleEngine.INSTANCE.renderWithMouse(poseStack, mouseX, mouseY);

        float animProgress = Math.min(1.0f, (System.currentTimeMillis() - openTime) / 180.0f);
        float ease = (float) Math.sin(animProgress * Math.PI / 2.0);
        float scaleAnim = 0.92f + 0.08f * ease;

        poseStack.pushPose();
        poseStack.translate(width / 2.0, height / 2.0, 0);
        poseStack.scale(scaleAnim, scaleAnim, 1.0f);
        poseStack.translate(-width / 2.0, -height / 2.0, 0);

        // 3. Central Web Window Container
        int winW = Math.min(560, width - 20);
        int winH = Math.min(370, height - 20);
        int winX = (width - winW) / 2;
        int winY = (height - winH) / 2;

        RenderUtils.drawBorderedRect(poseStack, winX, winY, winW, winH, 1, 0xF80B0F19, 0xFF334155);

        // 4. Header Bar
        int headerH = 28;
        RenderUtils.drawRect(poseStack, winX, winY, winW, headerH, 0xFF0F172A);
        RenderUtils.drawRect(poseStack, winX, winY + headerH - 1, winW, 1, 0xFF334155);

        font.drawShadow(poseStack, "★ ASTRA CLIENT", winX + 12, winY + 10, accent);

        int searchW = Math.min(180, winW - 250);
        int searchX = winX + 115;
        int searchY = winY + 5;
        boolean searchHover = mouseX >= searchX && mouseX <= searchX + searchW && mouseY >= searchY && mouseY <= searchY + 18;
        RenderUtils.drawBorderedRect(poseStack, searchX, searchY, searchW, 18, 1, 0xFF0A0F1D, editingSearch ? accent : (searchHover ? 0xFF475569 : 0xFF334155));

        String searchDisplayText = searchInput.isEmpty() ? (editingSearch ? "|" : "🔍 Поиск модулей...") : ("🔍 " + searchInput + (editingSearch ? "_" : ""));
        int searchTextColor = searchInput.isEmpty() ? 0xFF64748B : 0xFFCBD5E1;
        font.drawShadow(poseStack, searchDisplayText, searchX + 6, searchY + 5, searchTextColor);
        if (!searchInput.isEmpty()) {
            font.drawShadow(poseStack, "✕", searchX + searchW - 14, searchY + 5, 0xFF94A3B8);
        }

        // HUD Editor Button
        int hudBtnW = 95;
        int hudBtnX = winX + winW - hudBtnW - 32;
        int hudBtnY = winY + 5;
        boolean hudHover = mouseX >= hudBtnX && mouseX <= hudBtnX + hudBtnW && mouseY >= hudBtnY && mouseY <= hudBtnY + 18;
        RenderUtils.drawBorderedRect(poseStack, hudBtnX, hudBtnY, hudBtnW, 18, 1, hudHover ? 0xDD1E293B : 0xAA0F172A, hudHover ? accent : 0xFF334155);
        font.drawShadow(poseStack, "📐 Редактор HUD", hudBtnX + 6, hudBtnY + 5, hudHover ? 0xFFFFFFFF : 0xFFCBD5E1);

        // Close Button
        boolean closeHovered = mouseX >= winX + winW - 24 && mouseX <= winX + winW - 8 && mouseY >= winY + 6 && mouseY <= winY + 22;
        font.drawShadow(poseStack, "✕", winX + winW - 20, winY + 9, closeHovered ? 0xFFEF4444 : 0xFF64748B);

        // 5. Left Sidebar Navigation
        int sideW = 125;
        int contentX = winX + sideW;
        int contentY = winY + headerH;
        int contentW = winW - sideW;
        int contentH = winH - headerH;

        RenderUtils.drawRect(poseStack, winX, contentY, sideW, contentH, 0xFF0D111A);
        RenderUtils.drawRect(poseStack, contentX - 1, contentY, 1, contentH, 0xFF334155);
        RenderUtils.drawRect(poseStack, contentX, contentY, contentW, contentH, 0xF80B0F19);

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
            font.drawShadow(poseStack, tab.title, winX + 10, tabY + 7, textCol);

            tabY += 26;
        }

        // 6. Right Content Area with Scissor Clipping
        double scale = minecraft.getWindow().getGuiScale();
        int scissorX = (int) (contentX * scale);
        int scissorY = (int) ((height - (contentY + contentH)) * scale);
        int scissorW = (int) (contentW * scale);
        int scissorH = (int) (contentH * scale);

        com.mojang.blaze3d.systems.RenderSystem.enableScissor(scissorX, scissorY, scissorW, scissorH);

        if (currentTab.category != null) {
            renderModuleCards(poseStack, currentTab.category, contentX + 10, contentY + 10, contentW - 20, mouseX, mouseY, accent, accentDark);
        } else if (currentTab == WebTab.THEMES) {
            renderThemesTab(poseStack, contentX + 10, contentY + 10 + scrollY, contentW - 20, mouseX, mouseY, accent);
        } else if (currentTab == WebTab.PROFILE) {
            renderProfileTab(poseStack, contentX + 10, contentY + 10 + scrollY, contentW - 20, mouseX, mouseY, accent);
        }

        com.mojang.blaze3d.systems.RenderSystem.disableScissor();

        poseStack.popPose();

        super.render(poseStack, mouseX, mouseY, delta);
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
            font.drawShadow(poseStack, "Ничего не найдено по запросу \"" + searchInput + "\"", cx + 10, cy + 20, 0xFF94A3B8);
            return;
        }
        int colW = (cw - 10) / 2;

        int row0Y = cy + scrollY;
        int row1Y = cy + scrollY;

        for (int i = 0; i < modules.size(); i++) {
            Module m = modules.get(i);
            boolean isCol1 = (i % 2 == 1);
            int cardX = isCol1 ? (cx + colW + 10) : cx;
            int cardY = isCol1 ? row1Y : row0Y;

            boolean expanded = expandedModules.getOrDefault(m, false);
            int cardH = getCardHeight(m, expanded);

            boolean cardHover = mouseX >= cardX && mouseX <= cardX + colW && mouseY >= cardY && mouseY <= cardY + cardH;
            int cardBg = m.isEnabled() ? 0xEE111827 : 0xEE0B0F19;
            int borderCol = m.isEnabled() ? accent : (cardHover ? 0xFF64748B : 0xFF223348);

            RenderUtils.drawBorderedRect(poseStack, cardX, cardY, colW, cardH, 1, cardBg, borderCol);

            if (m.isEnabled()) {
                RenderUtils.drawRect(poseStack, cardX + 1, cardY + 1, 2, 34, accent);
            }

            // Badge & Name
            String badge = getModuleIcon(m);
            font.drawShadow(poseStack, badge, cardX + 8, cardY + 7, accent);
            font.drawShadow(poseStack, m.getName(), cardX + font.width(badge) + 12, cardY + 7, m.isEnabled() ? 0xFFFFFFFF : 0xFFCBD5E1);

            // Bind Pill Button
            String bindText = (bindingModule == m) ? "[...]" : ((m.getKeyCode() > 0 || m.getKeyCode() < -90) ? "[" + getKeyName(m.getKeyCode()) + "]" : "[None]");
            int bindW = font.width(bindText) + 6;
            int bindX = cardX + colW - bindW - 28;
            int bindY = cardY + 6;
            boolean bindHover = mouseX >= bindX && mouseX <= bindX + bindW && mouseY >= bindY && mouseY <= bindY + 11;
            int bindColor = (bindingModule == m) ? 0xFFF59E0B : ((m.getKeyCode() > 0 || m.getKeyCode() < -90) ? 0xFF818CF8 : 0xFF64748B);

            RenderUtils.drawBorderedRect(poseStack, bindX, bindY, bindW, 11, 1, bindHover ? 0xDD1E293B : 0x660F172A, bindColor);
            font.drawShadow(poseStack, bindText, bindX + 3, bindY + 2, bindColor);

            // Switch
            RenderUtils.drawSwitch(poseStack, cardX + colW - 24, cardY + 6, 18, 10, m.isEnabled());

            // Expand Arrow
            if (!m.getSettings().isEmpty()) {
                font.drawShadow(poseStack, expanded ? "▼" : "▶", cardX + colW - 22, cardY + 22, 0xFF64748B);
            }

            // Description
            String desc = m.getDescription();
            if (font.width(desc) > colW - 36) {
                desc = font.plainSubstrByWidth(desc, colW - 40) + "...";
            }
            font.drawShadow(poseStack, desc, cardX + 8, cardY + 22, 0xFF64748B);

            // Expanded settings
            if (expanded) {
                int sY = cardY + 36;
                for (Setting<?> setting : m.getSettings()) {
                    renderSetting(poseStack, setting, cardX + 8, sY, colW - 16, mouseX, mouseY, accent);
                    sY += getSettingHeight(setting);
                }
                if (m instanceof CustomCrosshair) {
                    int btnW = colW - 16;
                    int btnH = 18;
                    boolean btnHover = mouseX >= cardX + 8 && mouseX <= cardX + 8 + btnW && mouseY >= sY + 2 && mouseY <= sY + 2 + btnH;
                    RenderUtils.drawRoundedBorderedRect(poseStack, cardX + 8, sY + 2, btnW, btnH, 4, 1, btnHover ? 0xDD1E1B4B : 0xAA0F172A, btnHover ? accent : 0xFF334155);
                    font.drawShadow(poseStack, "🎨 Нарисовать прицел (32×32)", cardX + 8 + (btnW - font.width("🎨 Нарисовать прицел (32×32)")) / 2.0f, sY + 7, btnHover ? 0xFFFFFFFF : 0xFFCBD5E1);
                }
            }

            if (isCol1) row1Y += cardH + 8;
            else row0Y += cardH + 8;
        }
    }

    private String getModuleIcon(Module m) {
        String name = m.getName();
        switch (name) {
            case "KillAura": return "[AURA]";
            case "AimAssist": return "[AIM]";
            case "AutoClicker": return "[AUTO]";
            case "Entity Launcher": return "[LNCH]";
            case "Velocity": return "[KB]";
            case "Reach": return "[RCH]";
            case "Sprint": return "[RUN]";
            case "FastClimb": return "[CLIMB]";
            case "Fly": return "[FLY]";
            case "Speed": return "[SPD]";
            case "FreeLook": return "[CAM]";
            case "Fullbright": return "[GLOW]";
            case "ESP": return "[ESP]";
            case "HUD": return "[HUD]";
            case "ComboCounter": return "[COMBO]";
            case "Keystrokes": return "[KEYS]";
            case "ArmorHUD": return "[ARMR]";
            case "TargetHUD": return "[TARG]";
            case "CPSMod": return "[CPS]";
            case "FPSMod": return "[FPS]";
            case "CustomCrosshair": return "[XHAIR]";
            case "NoHurtCam": return "[NOCAM]";
            case "WeatherChanger": return "[WTHR]";
            case "TimeChanger": return "[TIME]";
            case "ClickGui": return "[GUI]";
            case "DarkTheme": return "[DARK]";
            case "MiniMap": return "[RADAR]";
            case "FastRender": return "[BOOST]";
            case "NoRender": return "[NOVIS]";
            case "Trails": return "[TRAIL]";
            case "NoFall": return "[FALL]";
            case "FastPlace": return "[FAST]";
            case "Scaffold": return "[BUILD]";
            case "NoHunger": return "[HUNGER]";
            case "GodMode": return "[GOD]";
            case "AutoTotem": return "[TOTEM]";
            case "KeySounds": return "[MECH]";
            case "AntiSwimming": return "[JESUS]";
            case "AntiFire": return "[FIRE]";
            case "Zoom": return "[ZOOM]";
            case "XRay": return "[XRAY]";
            default: return "[MOD]";
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
                mouseProgress = Math.max(0.0, Math.min(1.0, mouseProgress));
                double val = ns.getMin() + mouseProgress * (ns.getMax() - ns.getMin());
                ns.setValue(Math.round(val * 10.0) / 10.0);
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

            RenderUtils.drawRoundedBorderedRect(poseStack, x, y, cardW, cardH, 4, 1, active ? 0xDD1E1B4B : 0xBB0F172A, active ? accent : 0x33475569);

            int dotColor = (theme == ThemeManager.Theme.CHROMA) ? ThemeManager.getRainbow(0) : theme.getPrimary();
            RenderUtils.drawCircle(poseStack, x + 14, y + 14, 6, dotColor);

            font.drawShadow(poseStack, theme.getDisplayName(), x + 26, y + 10, active ? 0xFFFFFFFF : 0xFFCBD5E1);
            if (active) {
                font.drawShadow(poseStack, "✓", x + cardW - 16, y + 10, accent);
            }
        }

        int partY = gridY + 145;
        RenderUtils.drawRoundedBorderedRect(poseStack, cx, partY, cw, 30, 6, 1, 0xBB0F172A, 0x33475569);
        font.drawShadow(poseStack, "Космические фоновые частицы (Нейронная сеть)", cx + 12, partY + 10, 0xFFFFFFFF);
        RenderUtils.drawSwitch(poseStack, cx + cw - 32, partY + 9, 20, 12, ThemeManager.INSTANCE.isParticles());
    }

    private void renderProfileTab(PoseStack poseStack, int cx, int cy, int cw, int mouseX, int mouseY, int accent) {
        font.drawShadow(poseStack, "ПРОФИЛЬ, ВИЗУАЛЬНЫЙ НИК И КОСМЕТИКА", cx, cy, 0xFFFFFFFF);
        font.drawShadow(poseStack, "Работает на любых серверах в сетевой игре (Multiplayer)", cx, cy + 12, 0xFF64748B);

        int nickY = cy + 30;
        RenderUtils.drawRoundedBorderedRect(poseStack, cx, nickY, cw, 60, 6, 1, 0xBB0F172A, 0x33475569);
        font.drawShadow(poseStack, "Визуальная смена ника", cx + 12, nickY + 10, 0xFFFFFFFF);

        int inputX = cx + 12;
        int inputY = nickY + 28;
        int inputW = cw - 90;
        RenderUtils.drawRoundedBorderedRect(poseStack, inputX, inputY, inputW, 20, 4, 1, 0xFF0A0F1D, editingNick ? accent : 0x44475569);

        String displayNick = nickInput.isEmpty() ? (editingNick ? "" : "Введите ваш новый ник...") : nickInput;
        font.drawShadow(poseStack, displayNick, inputX + 6, inputY + 6, nickInput.isEmpty() ? 0xFF64748B : 0xFFFFFFFF);

        int btnX = inputX + inputW + 8;
        RenderUtils.drawRoundedBorderedRect(poseStack, btnX, inputY, 60, 20, 4, 1, 0xEE1E1B4B, accent);
        font.drawShadow(poseStack, "Применить", btnX + 6, inputY + 6, 0xFFFFFFFF);

        int capeY = nickY + 70;
        RenderUtils.drawRoundedBorderedRect(poseStack, cx, capeY, cw, 42, 6, 1, 0xBB0F172A, 0x33475569);
        font.drawShadow(poseStack, "Фирменный плащ Astra Cosmic HD Cape", cx + 12, capeY + 10, 0xFFFFFFFF);
        font.drawShadow(poseStack, "Отображает HD космический плащ за спиной", cx + 12, capeY + 22, 0xFF64748B);
        RenderUtils.drawSwitch(poseStack, cx + cw - 32, capeY + 15, 20, 12, CosmeticManager.INSTANCE.isCapeEnabled());

        int pvpY = capeY + 52;
        RenderUtils.drawRoundedBorderedRect(poseStack, cx, pvpY, cw, 42, 6, 1, 0xBB0F172A, 0x33475569);
        font.drawShadow(poseStack, "Встроенный ресурс-пак Astra Esports PvP Pack", cx + 12, pvpY + 10, 0xFFFFFFFF);
        font.drawShadow(poseStack, "Статус: Всегда активен по умолчанию [OK]", cx + 12, pvpY + 22, 0xFF10B981);
    }

    private int getCardHeight(Module m, boolean expanded) {
        int h = 36;
        if (expanded) {
            for (Setting<?> s : m.getSettings()) {
                h += getSettingHeight(s);
            }
            if (m instanceof CustomCrosshair) {
                h += 22;
            }
        }
        return h;
    }

    private int getSettingHeight(Setting<?> s) {
        return (s instanceof NumberSetting) ? 20 : 16;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (bindingModule != null) {
            bindingModule.setKeyCode(-100 - button);
            bindingModule = null;
            astra.mod.client.config.ConfigManager.INSTANCE.save();
            return true;
        }

        int winW = Math.min(560, width - 20);
        int winH = Math.min(370, height - 20);
        int winX = (width - winW) / 2;
        int winY = (height - winH) / 2;
        int headerH = 28;

        int hudBtnW = 95;
        int hudBtnX = winX + winW - hudBtnW - 32;
        int hudBtnY = winY + 5;
        if (mouseX >= hudBtnX && mouseX <= hudBtnX + hudBtnW && mouseY >= hudBtnY && mouseY <= hudBtnY + 18) {
            if (minecraft != null) {
                minecraft.setScreen(HudEditorScreen.INSTANCE);
            }
            return true;
        }

        int searchW = Math.min(180, winW - 250);
        int searchX = winX + 115;
        int searchY = winY + 5;
        if (mouseX >= searchX && mouseX <= searchX + searchW && mouseY >= searchY && mouseY <= searchY + 18) {
            if (!searchInput.isEmpty() && mouseX >= searchX + searchW - 16) {
                searchInput = "";
            } else {
                editingSearch = true;
                editingNick = false;
            }
            return true;
        } else {
            editingSearch = false;
        }

        if (mouseX >= winX + winW - 24 && mouseX <= winX + winW - 8 && mouseY >= winY + 6 && mouseY <= winY + 22) {
            onClose();
            return true;
        }

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
        int contentH = winH - headerH;

        if (mouseX < contentX || mouseX > contentX + contentW || mouseY < contentY || mouseY > contentY + contentH) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        if (currentTab.category != null || !searchInput.trim().isEmpty()) {
            List<Module> modules = getDisplayedModules(currentTab.category);
            int colW = (contentW - 30) / 2;
            int row0Y = contentY + 10 + scrollY;
            int row1Y = contentY + 10 + scrollY;

            for (int i = 0; i < modules.size(); i++) {
                Module m = modules.get(i);
                boolean isCol1 = (i % 2 == 1);
                int cardX = isCol1 ? (contentX + 10 + colW + 10) : (contentX + 10);
                int cardY = isCol1 ? row1Y : row0Y;

                boolean expanded = expandedModules.getOrDefault(m, false);
                int cardH = getCardHeight(m, expanded);

                // Check bind button click
                String bindText = (bindingModule == m) ? "[...]" : (m.getKeyCode() != 0 ? "[" + getKeyName(m.getKeyCode()) + "]" : "[BIND]");
                int bindW = font.width(bindText) + 6;
                int bindX = cardX + colW - bindW - 28;
                int bindY = cardY + 6;
                if (mouseX >= bindX && mouseX <= bindX + bindW && mouseY >= bindY && mouseY <= bindY + 11) {
                    bindingModule = (bindingModule == m) ? null : m;
                    return true;
                }

                if (mouseX >= cardX && mouseX <= cardX + colW && mouseY >= cardY && mouseY <= cardY + 36) {
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
                    if (m instanceof CustomCrosshair) {
                        int btnW = colW - 16;
                        int btnH = 18;
                        if (mouseX >= cardX + 8 && mouseX <= cardX + 8 + btnW && mouseY >= sY + 2 && mouseY <= sY + 2 + btnH) {
                            if (minecraft != null) {
                                minecraft.setScreen(CrosshairEditorScreen.INSTANCE);
                            }
                            return true;
                        }
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

            if (mouseX >= inputX && mouseX <= inputX + inputW && mouseY >= inputY && mouseY <= inputY + 20) {
                editingNick = true;
                return true;
            } else {
                editingNick = false;
            }

            int btnX = inputX + inputW + 8;
            if (mouseX >= btnX && mouseX <= btnX + 60 && mouseY >= inputY && mouseY <= inputY + 20) {
                CosmeticManager.INSTANCE.setSystemNick(nickInput);
                return true;
            }

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
