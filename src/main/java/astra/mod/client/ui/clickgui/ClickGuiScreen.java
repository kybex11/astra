package astra.mod.client.ui.clickgui;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.util.RenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ClickGuiScreen extends Screen {
    public static final ClickGuiScreen INSTANCE = new ClickGuiScreen();

    private final List<Panel> panels = new ArrayList<>();
    private boolean initialized = false;

    public ClickGuiScreen() {
        super(new TextComponent("Astra ClickGUI"));
    }

    @Override
    protected void init() {
        super.init();
        if (!initialized) {
            int startX = 30;
            int startY = 40;
            int spacing = 135;

            for (Category category : Category.values()) {
                panels.add(new Panel(category, startX, startY));
                startX += spacing;
            }
            initialized = true;
        }
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        // Dark translucent overlay
        RenderUtils.drawRect(poseStack, 0, 0, width, height, 0x7705070B);

        // Header brand bar
        RenderUtils.drawGradientH(poseStack, 0, 0, width, 24, 0xEE1E1B4B, 0xEE312E81);
        RenderUtils.drawRect(poseStack, 0, 23, width, 1, 0x66818CF8);

        // Client logo & status text
        font.drawShadow(poseStack, "★ ASTRA CLIENT", 12, 8, 0xFFFFFFFF);
        font.drawShadow(poseStack, "v1.0.0 (1.16.5)", 105, 8, 0xFF818CF8);

        String tip = "Подсказка: ЛКМ - Вкл/Выкл | ПКМ - Настройки/Категория | RShift/ESC - Закрыть";
        font.drawShadow(poseStack, tip, width - font.width(tip) - 12, 8, 0xFF94A3B8);

        // Render all panels
        Module hoveredModule = null;
        for (Panel panel : panels) {
            panel.render(poseStack, mouseX, mouseY);
            if (hoveredModule == null) {
                hoveredModule = panel.getHoveredModule(mouseX, mouseY);
            }
        }

        // Render floating tooltip for hovered module
        if (hoveredModule != null) {
            renderTooltipCard(poseStack, hoveredModule, mouseX + 12, mouseY + 12);
        }

        super.render(poseStack, mouseX, mouseY, delta);
    }

    private void renderTooltipCard(PoseStack poseStack, Module module, int tx, int ty) {
        String desc = module.getDescription();
        String title = module.getName() + " (" + module.getCategory().getDisplayName() + ")";

        int textWidth = Math.max(font.width(title), font.width(desc));
        int boxW = textWidth + 16;
        int boxH = 34;

        // Prevent tooltip from overflowing the right/bottom of screen
        if (tx + boxW > width) tx = width - boxW - 8;
        if (ty + boxH > height) ty = height - boxH - 8;

        // Tooltip card background and accent border
        RenderUtils.drawBorderedRect(poseStack, tx, ty, boxW, boxH, 1, 0xF00F172A, 0xFF6366F1);
        RenderUtils.drawRect(poseStack, tx + 1, ty + 1, 2, boxH - 2, 0xFF818CF8);

        font.drawShadow(poseStack, title, tx + 8, ty + 6, 0xFFFFFFFF);
        font.drawShadow(poseStack, desc, tx + 8, ty + 19, 0xFF94A3B8);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Iterate in reverse so topmost panel gets click first
        for (int i = panels.size() - 1; i >= 0; i--) {
            if (panels.get(i).mouseClicked(mouseX, mouseY, button)) {
                // Move focused panel to the front
                Panel p = panels.remove(i);
                panels.add(p);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (Panel panel : panels) {
            panel.mouseReleased(mouseX, mouseY, button);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
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
