package astra.mod.client.ui.clickgui;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.module.ModuleManager;
import astra.mod.client.theme.ThemeManager;
import astra.mod.client.util.RenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;

import java.util.ArrayList;
import java.util.List;

public class Panel {
    private final Category category;
    private int x;
    private int y;
    private final int width = 120;
    private final int headerHeight = 26;
    private boolean open = true;
    private boolean dragging = false;
    private int dragX = 0;
    private int dragY = 0;

    private final List<ModuleButton> buttons = new ArrayList<>();

    public Panel(Category category, int x, int y) {
        this.category = category;
        this.x = x;
        this.y = y;

        List<Module> modules = ModuleManager.INSTANCE.getModulesByCategory(category);
        for (Module module : modules) {
            buttons.add(new ModuleButton(module, this));
        }
    }

    public void render(PoseStack poseStack, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;

        if (dragging) {
            this.x = mouseX - dragX;
            this.y = mouseY - dragY;
        }

        int accent = ThemeManager.INSTANCE.getAccent();
        int accentDark = ThemeManager.INSTANCE.getAccentDark();

        // Panel header gradient
        RenderUtils.drawGradientH(poseStack, x, y, width, headerHeight, accentDark, accent);

        // Header bottom subtle line
        RenderUtils.drawRect(poseStack, x, y + headerHeight - 1, width, 1, 0x55FFFFFF);

        // Category title
        font.drawShadow(poseStack, category.getDisplayName(), x + 8, y + 9, 0xFFFFFFFF);

        // Module counter badge or open/close symbol
        String badge = open ? "-" : "+";
        font.drawShadow(poseStack, badge, x + width - 14, y + 9, 0xFFE0E7FF);

        // Outer border for whole panel
        int totalHeight = headerHeight;

        if (open) {
            int currentY = y + headerHeight;
            for (ModuleButton button : buttons) {
                button.render(poseStack, x, currentY, width, mouseX, mouseY);
                currentY += button.getHeight();
            }
            totalHeight = currentY - y;
        }

        // Subtle glowing border around panel
        RenderUtils.drawBorder(poseStack, x, y, width, totalHeight, 1, 0x44000000 | (accent & 0x00FFFFFF));
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Click on header
        if (isHovered(x, y, width, headerHeight, mouseX, mouseY)) {
            if (button == 0) { // Left click: drag
                dragging = true;
                dragX = (int) mouseX - x;
                dragY = (int) mouseY - y;
                return true;
            } else if (button == 1) { // Right click: collapse/expand
                open = !open;
                return true;
            }
        }

        // Clicks on buttons
        if (open) {
            int currentY = y + headerHeight;
            for (ModuleButton btn : buttons) {
                if (btn.mouseClicked(mouseX, mouseY, button, x, currentY, width)) {
                    return true;
                }
                currentY += btn.getHeight();
            }
        }

        return false;
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            dragging = false;
        }
        for (ModuleButton btn : buttons) {
            btn.mouseReleased(mouseX, mouseY, button);
        }
    }

    public Module getHoveredModule(double mouseX, double mouseY) {
        if (!open) return null;

        int currentY = y + headerHeight;
        for (ModuleButton btn : buttons) {
            if (btn.isModuleHovered(x, currentY, width, mouseX, mouseY)) {
                return btn.getModule();
            }
            currentY += btn.getHeight();
        }
        return null;
    }

    private boolean isHovered(int x, int y, int w, int h, double mx, double my) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    public Category getCategory() {
        return category;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
