package astra.mod.client.ui.hud;

import astra.mod.client.module.impl.render.*;
import astra.mod.client.theme.ThemeManager;
import astra.mod.client.ui.clickgui.ClickGuiScreen;
import astra.mod.client.util.RenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import org.lwjgl.glfw.GLFW;

public class HudEditorScreen extends Screen {
    public static final HudEditorScreen INSTANCE = new HudEditorScreen();

    private enum Element {
        KEYSTROKES("keystrokes"),
        FPS("fps"),
        CPS("cps"),
        COMBO("combo"),
        ARMOR("armor"),
        TARGET("target"),
        MINIMAP("minimap");

        final String id;
        Element(String id) {
            this.id = id;
        }
    }

    private Element draggingElement = null;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;

    public HudEditorScreen() {
        super(new TextComponent("Astra HUD Editor"));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        float delta = (float) (amount * 0.1f);
        Element hovered = getHoveredElement(mouseX, mouseY);
        if (hovered != null) {
            float curScale = HudConfig.INSTANCE.getScale(hovered.id);
            HudConfig.INSTANCE.setScale(hovered.id, curScale + delta);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    private Element getHoveredElement(double mx, double my) {
        if (Keystrokes.INSTANCE != null && Keystrokes.INSTANCE.isEnabled() && isInside(mx, my, HudConfig.INSTANCE.keystrokesX, HudConfig.INSTANCE.keystrokesY, 66, 75)) {
            return Element.KEYSTROKES;
        }
        if (FPSMod.INSTANCE != null && FPSMod.INSTANCE.isEnabled() && isInside(mx, my, HudConfig.INSTANCE.fpsX, HudConfig.INSTANCE.fpsY, 55, 16)) {
            return Element.FPS;
        }
        if (CPSMod.INSTANCE != null && CPSMod.INSTANCE.isEnabled() && isInside(mx, my, HudConfig.INSTANCE.cpsX, HudConfig.INSTANCE.cpsY, 65, 16)) {
            return Element.CPS;
        }
        if (ComboCounter.INSTANCE != null && ComboCounter.INSTANCE.isEnabled() && isInside(mx, my, HudConfig.INSTANCE.comboX, HudConfig.INSTANCE.comboY, 65, 16)) {
            return Element.COMBO;
        }
        int armX = HudConfig.INSTANCE.armorX == -1 ? (width / 2 + 95) : HudConfig.INSTANCE.armorX;
        int armY = HudConfig.INSTANCE.armorY == -1 ? (height - 22) : HudConfig.INSTANCE.armorY;
        if (ArmorHUD.INSTANCE != null && ArmorHUD.INSTANCE.isEnabled() && isInside(mx, my, armX, armY, 90, 20)) {
            return Element.ARMOR;
        }
        int tarX = HudConfig.INSTANCE.targetX == -1 ? ((width - 140) / 2) : HudConfig.INSTANCE.targetX;
        int tarY = HudConfig.INSTANCE.targetY == -1 ? (height / 2 + 30) : HudConfig.INSTANCE.targetY;
        if (TargetHUD.INSTANCE != null && TargetHUD.INSTANCE.isEnabled() && isInside(mx, my, tarX, tarY, 140, 42)) {
            return Element.TARGET;
        }
        if (MiniMap.INSTANCE != null && MiniMap.INSTANCE.isEnabled()) {
            int mapSize = (int) MiniMap.INSTANCE.size.getValue().doubleValue();
            if (isInside(mx, my, HudConfig.INSTANCE.miniMapX, HudConfig.INSTANCE.miniMapY, mapSize, mapSize)) {
                return Element.MINIMAP;
            }
        }
        return null;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        RenderUtils.drawRect(poseStack, 0, 0, width, height, 0x66000000);

        int accent = ThemeManager.INSTANCE.getAccent();
        String title = "★ РЕДАКТОР РАСПОЛОЖЕНИЯ И РАЗМЕРА HUD";
        String sub = "Перетаскивайте элементы. Колесико мыши = размер (Scale). ESC = сохранить.";
        font.drawShadow(poseStack, title, (width - font.width(title)) / 2.0f, 15, accent);
        font.drawShadow(poseStack, sub, (width - font.width(sub)) / 2.0f, 28, 0xFFCBD5E1);

        int alignBtnW = 120;
        int alignBtnX = (width - alignBtnW) / 2;
        int alignBtnY = 42;
        boolean alignHover = mouseX >= alignBtnX && mouseX <= alignBtnX + alignBtnW && mouseY >= alignBtnY && mouseY <= alignBtnY + 16;
        RenderUtils.drawBorderedRect(poseStack, alignBtnX, alignBtnY, alignBtnW, 16, 1, alignHover ? 0xDD1E293B : 0xAA0F172A, alignHover ? accent : 0x44475569);
        font.drawShadow(poseStack, "📐 Выровнять сетку", alignBtnX + 10, alignBtnY + 4, alignHover ? 0xFFFFFFFF : 0xFFCBD5E1);

        HudRenderer.INSTANCE.render(poseStack, delta);

        // Bounding boxes
        if (Keystrokes.INSTANCE != null && Keystrokes.INSTANCE.isEnabled()) {
            drawBoundingBox(poseStack, Element.KEYSTROKES, HudConfig.INSTANCE.keystrokesX, HudConfig.INSTANCE.keystrokesY, 66, 75, "Keystrokes", mouseX, mouseY, accent);
        }
        if (FPSMod.INSTANCE != null && FPSMod.INSTANCE.isEnabled()) {
            drawBoundingBox(poseStack, Element.FPS, HudConfig.INSTANCE.fpsX, HudConfig.INSTANCE.fpsY, 55, 16, "FPS", mouseX, mouseY, accent);
        }
        if (CPSMod.INSTANCE != null && CPSMod.INSTANCE.isEnabled()) {
            drawBoundingBox(poseStack, Element.CPS, HudConfig.INSTANCE.cpsX, HudConfig.INSTANCE.cpsY, 65, 16, "CPS", mouseX, mouseY, accent);
        }
        if (ComboCounter.INSTANCE != null && ComboCounter.INSTANCE.isEnabled()) {
            drawBoundingBox(poseStack, Element.COMBO, HudConfig.INSTANCE.comboX, HudConfig.INSTANCE.comboY, 65, 16, "Combo", mouseX, mouseY, accent);
        }
        if (ArmorHUD.INSTANCE != null && ArmorHUD.INSTANCE.isEnabled()) {
            int armX = HudConfig.INSTANCE.armorX == -1 ? (width / 2 + 95) : HudConfig.INSTANCE.armorX;
            int armY = HudConfig.INSTANCE.armorY == -1 ? (height - 22) : HudConfig.INSTANCE.armorY;
            drawBoundingBox(poseStack, Element.ARMOR, armX, armY, 90, 20, "ArmorHUD", mouseX, mouseY, accent);
        }
        if (TargetHUD.INSTANCE != null && TargetHUD.INSTANCE.isEnabled()) {
            int tarX = HudConfig.INSTANCE.targetX == -1 ? ((width - 140) / 2) : HudConfig.INSTANCE.targetX;
            int tarY = HudConfig.INSTANCE.targetY == -1 ? (height / 2 + 30) : HudConfig.INSTANCE.targetY;
            drawBoundingBox(poseStack, Element.TARGET, tarX, tarY, 140, 42, "TargetHUD", mouseX, mouseY, accent);
        }
        if (MiniMap.INSTANCE != null && MiniMap.INSTANCE.isEnabled()) {
            int mapSize = (int) MiniMap.INSTANCE.size.getValue().doubleValue();
            drawBoundingBox(poseStack, Element.MINIMAP, HudConfig.INSTANCE.miniMapX, HudConfig.INSTANCE.miniMapY, mapSize, mapSize, "MiniMap", mouseX, mouseY, accent);
        }

        // Dragging update
        if (draggingElement != null) {
            int newX = mouseX - dragOffsetX;
            int newY = mouseY - dragOffsetY;
            switch (draggingElement) {
                case KEYSTROKES:
                    HudConfig.INSTANCE.keystrokesX = Math.max(0, Math.min(width - 66, newX));
                    HudConfig.INSTANCE.keystrokesY = Math.max(0, Math.min(height - 75, newY));
                    break;
                case FPS:
                    HudConfig.INSTANCE.fpsX = Math.max(0, Math.min(width - 55, newX));
                    HudConfig.INSTANCE.fpsY = Math.max(0, Math.min(height - 16, newY));
                    break;
                case CPS:
                    HudConfig.INSTANCE.cpsX = Math.max(0, Math.min(width - 65, newX));
                    HudConfig.INSTANCE.cpsY = Math.max(0, Math.min(height - 16, newY));
                    break;
                case COMBO:
                    HudConfig.INSTANCE.comboX = Math.max(0, Math.min(width - 65, newX));
                    HudConfig.INSTANCE.comboY = Math.max(0, Math.min(height - 16, newY));
                    break;
                case ARMOR:
                    HudConfig.INSTANCE.armorX = Math.max(0, Math.min(width - 90, newX));
                    HudConfig.INSTANCE.armorY = Math.max(0, Math.min(height - 20, newY));
                    break;
                case TARGET:
                    HudConfig.INSTANCE.targetX = Math.max(0, Math.min(width - 140, newX));
                    HudConfig.INSTANCE.targetY = Math.max(0, Math.min(height - 42, newY));
                    break;
                case MINIMAP:
                    int mapSize = (int) MiniMap.INSTANCE.size.getValue().doubleValue();
                    HudConfig.INSTANCE.miniMapX = Math.max(0, Math.min(width - mapSize, newX));
                    HudConfig.INSTANCE.miniMapY = Math.max(0, Math.min(height - mapSize, newY));
                    break;
            }
        }

        super.render(poseStack, mouseX, mouseY, delta);
    }

    private void drawBoundingBox(PoseStack poseStack, Element elem, int x, int y, int w, int h, String name, int mx, int my, int accent) {
        boolean hover = mx >= x && mx <= x + w && my >= y && my <= y + h;
        int col = (draggingElement == elem || hover) ? accent : 0x88475569;
        float scale = HudConfig.INSTANCE.getScale(elem.id);
        int scaledW = (int) (w * scale);
        int scaledH = (int) (h * scale);

        RenderUtils.drawBorder(poseStack, x - 1, y - 1, scaledW + 2, scaledH + 2, 1, col);

        String tag = name + " [" + String.format("%.1f", scale) + "x]";
        font.drawShadow(poseStack, tag, x + 2, y - 8, hover ? 0xFFFFFFFF : 0xFF94A3B8);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int alignBtnW = 120;
            int alignBtnX = (width - alignBtnW) / 2;
            int alignBtnY = 42;
            if (mouseX >= alignBtnX && mouseX <= alignBtnX + alignBtnW && mouseY >= alignBtnY && mouseY <= alignBtnY + 16) {
                snapToGrid();
                return true;
            }

            int armX = HudConfig.INSTANCE.armorX == -1 ? (width / 2 + 95) : HudConfig.INSTANCE.armorX;
            int armY = HudConfig.INSTANCE.armorY == -1 ? (height - 22) : HudConfig.INSTANCE.armorY;
            int tarX = HudConfig.INSTANCE.targetX == -1 ? ((width - 140) / 2) : HudConfig.INSTANCE.targetX;
            int tarY = HudConfig.INSTANCE.targetY == -1 ? (height / 2 + 30) : HudConfig.INSTANCE.targetY;

            if (Keystrokes.INSTANCE != null && Keystrokes.INSTANCE.isEnabled() && isInside(mouseX, mouseY, HudConfig.INSTANCE.keystrokesX, HudConfig.INSTANCE.keystrokesY, 66, 75)) {
                startDrag(Element.KEYSTROKES, (int) mouseX, (int) mouseY, HudConfig.INSTANCE.keystrokesX, HudConfig.INSTANCE.keystrokesY);
                return true;
            } else if (FPSMod.INSTANCE != null && FPSMod.INSTANCE.isEnabled() && isInside(mouseX, mouseY, HudConfig.INSTANCE.fpsX, HudConfig.INSTANCE.fpsY, 55, 16)) {
                startDrag(Element.FPS, (int) mouseX, (int) mouseY, HudConfig.INSTANCE.fpsX, HudConfig.INSTANCE.fpsY);
                return true;
            } else if (CPSMod.INSTANCE != null && CPSMod.INSTANCE.isEnabled() && isInside(mouseX, mouseY, HudConfig.INSTANCE.cpsX, HudConfig.INSTANCE.cpsY, 65, 16)) {
                startDrag(Element.CPS, (int) mouseX, (int) mouseY, HudConfig.INSTANCE.cpsX, HudConfig.INSTANCE.cpsY);
                return true;
            } else if (ComboCounter.INSTANCE != null && ComboCounter.INSTANCE.isEnabled() && isInside(mouseX, mouseY, HudConfig.INSTANCE.comboX, HudConfig.INSTANCE.comboY, 65, 16)) {
                startDrag(Element.COMBO, (int) mouseX, (int) mouseY, HudConfig.INSTANCE.comboX, HudConfig.INSTANCE.comboY);
                return true;
            } else if (ArmorHUD.INSTANCE != null && ArmorHUD.INSTANCE.isEnabled() && isInside(mouseX, mouseY, armX, armY, 90, 20)) {
                HudConfig.INSTANCE.armorX = armX;
                HudConfig.INSTANCE.armorY = armY;
                startDrag(Element.ARMOR, (int) mouseX, (int) mouseY, armX, armY);
                return true;
            } else if (TargetHUD.INSTANCE != null && TargetHUD.INSTANCE.isEnabled() && isInside(mouseX, mouseY, tarX, tarY, 140, 42)) {
                HudConfig.INSTANCE.targetX = tarX;
                HudConfig.INSTANCE.targetY = tarY;
                startDrag(Element.TARGET, (int) mouseX, (int) mouseY, tarX, tarY);
                return true;
            } else if (MiniMap.INSTANCE != null && MiniMap.INSTANCE.isEnabled()) {
                int mapSize = (int) MiniMap.INSTANCE.size.getValue().doubleValue();
                if (isInside(mouseX, mouseY, HudConfig.INSTANCE.miniMapX, HudConfig.INSTANCE.miniMapY, mapSize, mapSize)) {
                    startDrag(Element.MINIMAP, (int) mouseX, (int) mouseY, HudConfig.INSTANCE.miniMapX, HudConfig.INSTANCE.miniMapY);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void snapToGrid() {
        HudConfig.INSTANCE.fpsX = round10(HudConfig.INSTANCE.fpsX);
        HudConfig.INSTANCE.fpsY = round10(HudConfig.INSTANCE.fpsY);
        HudConfig.INSTANCE.cpsX = round10(HudConfig.INSTANCE.cpsX);
        HudConfig.INSTANCE.cpsY = round10(HudConfig.INSTANCE.cpsY);
        HudConfig.INSTANCE.comboX = round10(HudConfig.INSTANCE.comboX);
        HudConfig.INSTANCE.comboY = round10(HudConfig.INSTANCE.comboY);
        HudConfig.INSTANCE.keystrokesX = round10(HudConfig.INSTANCE.keystrokesX);
        HudConfig.INSTANCE.keystrokesY = round10(HudConfig.INSTANCE.keystrokesY);
        HudConfig.INSTANCE.miniMapX = round10(HudConfig.INSTANCE.miniMapX);
        HudConfig.INSTANCE.miniMapY = round10(HudConfig.INSTANCE.miniMapY);
    }

    private int round10(int val) {
        return Math.round(val / 10.0f) * 10;
    }

    private void startDrag(Element el, int mx, int my, int elX, int elY) {
        draggingElement = el;
        dragOffsetX = mx - elX;
        dragOffsetY = my - elY;
    }

    private boolean isInside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingElement = null;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            if (minecraft != null) {
                minecraft.setScreen(ClickGuiScreen.INSTANCE);
            }
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
