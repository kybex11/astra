package astra.mod.client.ui.clickgui;

import astra.mod.client.module.Module;
import astra.mod.client.setting.BooleanSetting;
import astra.mod.client.setting.ModeSetting;
import astra.mod.client.setting.NumberSetting;
import astra.mod.client.setting.Setting;
import astra.mod.client.util.RenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import org.lwjgl.glfw.GLFW;

public class ModuleButton {
    private final Module module;
    private final Panel parent;
    private boolean expanded = false;
    private boolean binding = false;
    private boolean draggingSlider = false;
    private NumberSetting draggingNumberSetting = null;

    // Smooth animations
    private float toggleAnim = 0.0f;
    private float expandAnim = 0.0f;
    private float hoverAnim = 0.0f;

    public ModuleButton(Module module, Panel parent) {
        this.module = module;
        this.parent = parent;
        this.toggleAnim = module.isEnabled() ? 1.0f : 0.0f;
        this.expandAnim = 0.0f;
    }

    public void render(PoseStack poseStack, int x, int y, int width, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        boolean hovered = isHovered(x, y, width, 22, mouseX, mouseY);

        // Smoothly interpolate animations
        hoverAnim = lerp(hoverAnim, hovered ? 1.0f : 0.0f, 0.2f);
        toggleAnim = lerp(toggleAnim, module.isEnabled() ? 1.0f : 0.0f, 0.25f);
        expandAnim = lerp(expandAnim, expanded ? 1.0f : 0.0f, 0.25f);

        int accent = astra.mod.client.theme.ThemeManager.INSTANCE.getAccent();
        int accentSec = astra.mod.client.theme.ThemeManager.INSTANCE.getAccentSecondary();
        int accentDark = astra.mod.client.theme.ThemeManager.INSTANCE.getAccentDark();

        // Background card with dynamic hover & toggle blend
        int baseBg = 0x77161A23;
        int activeBg = 0xBB000000 | (accentDark & 0x00FFFFFF);
        int hoverHighlight = (int)(hoverAnim * 30.0f) << 24 | 0x00FFFFFF;

        RenderUtils.drawRect(poseStack, x, y, width, 22, baseBg | hoverHighlight);

        if (toggleAnim > 0.01f) {
            int alphaActive = (int)(toggleAnim * 200.0f) << 24;
            RenderUtils.drawRect(poseStack, x, y, width, 22, alphaActive | (activeBg & 0x00FFFFFF));
            // Neon accent bar on left
            RenderUtils.drawGradientV(poseStack, x, y, 3, 22, accent, accentSec);
        }

        // Bottom separator
        RenderUtils.drawRect(poseStack, x, y + 21, width, 1, 0x1AFFFFFF);

        // Module title
        int textColor = module.isEnabled() ? 0xFFFFFFFF : (hovered ? 0xFFE2E8F0 : 0xFF94A3B8);
        font.drawShadow(poseStack, module.getName(), x + 8, y + 7, textColor);

        // Keybind badge / Listening text
        String bindText;
        int bindColor = 0xFF64748B;
        if (binding) {
            bindText = "[...]";
            bindColor = 0xFFF59E0B; // Amber waiting
        } else if (module.getKeyCode() != GLFW.GLFW_KEY_UNKNOWN) {
            bindText = "[" + getKeyName(module.getKeyCode()) + "]";
            bindColor = 0xFF818CF8;
        } else {
            bindText = "";
        }

        if (!bindText.isEmpty()) {
            font.drawShadow(poseStack, bindText, x + width - 75, y + 7, bindColor);
        }

        // Right side: animated toggle switch
        int switchW = 20;
        int switchH = 10;
        int switchX = x + width - switchW - 8;
        int switchY = y + 6;

        if (!module.getSettings().isEmpty()) {
            String arrow = expanded ? "v" : ">";
            font.drawShadow(poseStack, arrow, switchX - 10, y + 7, 0xFF64748B);
        }

        RenderUtils.drawSwitch(poseStack, switchX, switchY, switchW, switchH, toggleAnim > 0.5f);

        // Render settings if expanded
        if (expanded && expandAnim > 0.05f) {
            int settingY = y + 22;
            for (Setting<?> setting : module.getSettings()) {
                int settingH = getSettingHeight(setting);
                renderSetting(poseStack, setting, x + 4, settingY, width - 8, mouseX, mouseY);
                settingY += settingH;
            }
        }
    }

    private void renderSetting(PoseStack poseStack, Setting<?> setting, int sx, int sy, int sw, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        boolean hovered = isHovered(sx, sy, sw, getSettingHeight(setting), mouseX, mouseY);

        RenderUtils.drawRect(poseStack, sx, sy, sw, getSettingHeight(setting), hovered ? 0x661E2430 : 0x4412151C);

        if (setting instanceof BooleanSetting) {
            BooleanSetting bs = (BooleanSetting) setting;
            font.drawShadow(poseStack, bs.getName(), sx + 6, sy + 5, bs.getValue() ? 0xFFE2E8F0 : 0xFF64748B);
            RenderUtils.drawSwitch(poseStack, sx + sw - 22, sy + 4, 16, 8, bs.getValue());
        } else if (setting instanceof NumberSetting) {
            NumberSetting ns = (NumberSetting) setting;
            font.drawShadow(poseStack, ns.getName(), sx + 6, sy + 3, 0xFFCBD5E1);
            String valStr = String.valueOf(ns.getValue());
            font.drawShadow(poseStack, valStr, sx + sw - font.width(valStr) - 6, sy + 3, 0xFF818CF8);

            double progress = (ns.getValue() - ns.getMin()) / (ns.getMax() - ns.getMin());
            int barY = sy + 14;
            int barH = 4;
            RenderUtils.drawProgressBar(poseStack, sx + 6, barY, sw - 12, barH, progress, 0xFF242A38, 0xFF6366F1);

            if (draggingSlider && draggingNumberSetting == ns) {
                double mouseProgress = (double) (mouseX - (sx + 6)) / (double) (sw - 12);
                mouseProgress = Math.max(0.0, Math.min(1.0, mouseProgress));
                double val = ns.getMin() + mouseProgress * (ns.getMax() - ns.getMin());
                ns.setValue(Math.round(val * 10.0) / 10.0);
            }
        } else if (setting instanceof ModeSetting) {
            ModeSetting ms = (ModeSetting) setting;
            font.drawShadow(poseStack, ms.getName(), sx + 6, sy + 5, 0xFFCBD5E1);
            String modeVal = "[" + ms.getValue() + "]";
            font.drawShadow(poseStack, modeVal, sx + sw - font.width(modeVal) - 6, sy + 5, 0xFF818CF8);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width) {
        if (binding) {
            if (button == 0 || button == 1 || button == 2 || button >= 3) {
                // Bind to mouse button (e.g. Mouse 3, 4, 5)
                module.setKeyCode(-100 - button);
                binding = false;
                return true;
            }
        }

        if (isHovered(x, y, width, 22, mouseX, mouseY)) {
            if (button == 0) { // Left click: toggle module
                module.toggle();
                return true;
            } else if (button == 1) { // Right click: expand/collapse settings
                if (!module.getSettings().isEmpty()) {
                    expanded = !expanded;
                    return true;
                }
            } else if (button == 2) { // Middle click: enter binding mode
                binding = !binding;
                return true;
            }
        }

        // Check clicks on expanded settings
        if (expanded) {
            int settingY = y + 22;
            for (Setting<?> setting : module.getSettings()) {
                int sH = getSettingHeight(setting);
                int sx = x + 4;
                int sw = width - 8;

                if (isHovered(sx, settingY, sw, sH, mouseX, mouseY)) {
                    if (setting instanceof BooleanSetting && button == 0) {
                        ((BooleanSetting) setting).toggle();
                        return true;
                    } else if (setting instanceof NumberSetting && button == 0) {
                        NumberSetting ns = (NumberSetting) setting;
                        draggingSlider = true;
                        draggingNumberSetting = ns;
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
                settingY += sH;
            }
        }

        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (binding) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_DELETE || keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                module.setKeyCode(GLFW.GLFW_KEY_UNKNOWN);
            } else {
                module.setKeyCode(keyCode);
            }
            binding = false;
            return true;
        }
        return false;
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        draggingSlider = false;
        draggingNumberSetting = null;
    }

    public int getHeight() {
        int h = 22;
        if (expanded) {
            for (Setting<?> setting : module.getSettings()) {
                h += getSettingHeight(setting);
            }
        }
        return h;
    }

    private int getSettingHeight(Setting<?> setting) {
        if (setting instanceof NumberSetting) {
            return 22;
        }
        return 16;
    }

    private boolean isHovered(int x, int y, int w, int h, double mx, double my) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private String getKeyName(int keyCode) {
        if (keyCode < -90) {
            int mouseBtn = -100 - keyCode;
            if (mouseBtn == 0) return "M1";
            if (mouseBtn == 1) return "M2";
            if (mouseBtn == 2) return "M3";
            return "M" + (mouseBtn + 1);
        }
        String name = GLFW.glfwGetKeyName(keyCode, 0);
        if (name != null) return name.toUpperCase();
        if (keyCode == GLFW.GLFW_KEY_R) return "R";
        if (keyCode == GLFW.GLFW_KEY_F) return "F";
        if (keyCode == GLFW.GLFW_KEY_V) return "V";
        if (keyCode == GLFW.GLFW_KEY_C) return "C";
        if (keyCode == GLFW.GLFW_KEY_X) return "X";
        if (keyCode == GLFW.GLFW_KEY_Z) return "Z";
        return "K" + keyCode;
    }

    private float lerp(float a, float b, float f) {
        return a + f * (b - a);
    }

    public boolean isModuleHovered(int x, int y, int width, double mouseX, double mouseY) {
        return isHovered(x, y, width, 22, mouseX, mouseY);
    }

    public Module getModule() {
        return module;
    }

    public boolean isBinding() {
        return binding;
    }
}
