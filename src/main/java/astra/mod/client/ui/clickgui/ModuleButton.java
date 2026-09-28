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
    private boolean draggingSlider = false;
    private NumberSetting draggingNumberSetting = null;

    public ModuleButton(Module module, Panel parent) {
        this.module = module;
        this.parent = parent;
    }

    public void render(PoseStack poseStack, int x, int y, int width, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        boolean hovered = isHovered(x, y, width, 22, mouseX, mouseY);

        // Module row background
        int bgColor;
        if (module.isEnabled()) {
            bgColor = hovered ? 0xDD312E81 : 0xBB1E1B4B; // Deep Indigo active
        } else {
            bgColor = hovered ? 0x99232936 : 0x77161A23; // Dark card inactive
        }

        // Module background card
        RenderUtils.drawRect(poseStack, x, y, width, 22, bgColor);

        // Left accent bar if enabled
        if (module.isEnabled()) {
            RenderUtils.drawGradientV(poseStack, x, y, 3, 22, 0xFF6366F1, 0xFF8B5CF6);
        }

        // Bottom separator
        RenderUtils.drawRect(poseStack, x, y + 21, width, 1, 0x22FFFFFF);

        // Module title
        int textColor = module.isEnabled() ? 0xFFFFFFFF : (hovered ? 0xFFE2E8F0 : 0xFF94A3B8);
        font.drawShadow(poseStack, module.getName(), x + 8, y + 7, textColor);

        // Right side: toggle switch
        int switchW = 20;
        int switchH = 10;
        int switchX = x + width - switchW - 8;
        int switchY = y + 6;

        if (!module.getSettings().isEmpty()) {
            // Expand indicator
            String arrow = expanded ? "v" : ">";
            font.drawShadow(poseStack, arrow, switchX - 10, y + 7, 0xFF64748B);
        }

        RenderUtils.drawSwitch(poseStack, switchX, switchY, switchW, switchH, module.isEnabled());

        // Render settings if expanded
        if (expanded) {
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

        // Setting background
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

            // Progress bar
            double progress = (ns.getValue() - ns.getMin()) / (ns.getMax() - ns.getMin());
            int barY = sy + 14;
            int barH = 4;
            RenderUtils.drawProgressBar(poseStack, sx + 6, barY, sw - 12, barH, progress, 0xFF242A38, 0xFF6366F1);

            // Handle slider drag if active
            if (draggingSlider && draggingNumberSetting == ns) {
                double mouseProgress = (double) (mouseX - (sx + 6)) / (double) (sw - 12);
                double val = ns.getMin() + mouseProgress * (ns.getMax() - ns.getMin());
                ns.setValue(val);
            }
        } else if (setting instanceof ModeSetting) {
            ModeSetting ms = (ModeSetting) setting;
            font.drawShadow(poseStack, ms.getName(), sx + 6, sy + 5, 0xFFCBD5E1);
            String modeVal = "[" + ms.getValue() + "]";
            font.drawShadow(poseStack, modeVal, sx + sw - font.width(modeVal) - 6, sy + 5, 0xFF818CF8);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width) {
        if (isHovered(x, y, width, 22, mouseX, mouseY)) {
            if (button == 0) { // Left click: toggle module
                module.toggle();
                return true;
            } else if (button == 1) { // Right click: expand/collapse settings
                if (!module.getSettings().isEmpty()) {
                    expanded = !expanded;
                    return true;
                }
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
                        double val = ns.getMin() + mouseProgress * (ns.getMax() - ns.getMin());
                        ns.setValue(val);
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

    public Module getModule() {
        return module;
    }

    public boolean isModuleHovered(int x, int y, int width, double mouseX, double mouseY) {
        return isHovered(x, y, width, 22, mouseX, mouseY);
    }
}
