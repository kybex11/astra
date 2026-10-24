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
        MINIMAP("minimap"),
        COORDS("coords"),
        PING("ping"),
        DIRECTION("direction"),
        MOMENTUM("momentum"),
        SATURATION("saturation"),
        TOTEM("totem"),
        ITEMCOUNT("itemcount"),
        REACH("reach"),
        STOPWATCH("stopwatch"),
        EFFECTS("effects"),
        BOSSBAR("bossbar");

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
        if (Coordinates.INSTANCE != null && Coordinates.INSTANCE.isEnabled()
                && isInside(mx, my, HudConfig.INSTANCE.coordsX, HudConfig.INSTANCE.coordsY, 150, 12)) {
            return Element.COORDS;
        }
        if (PingMod.INSTANCE != null && PingMod.INSTANCE.isEnabled()
                && isInside(mx, my, HudConfig.INSTANCE.pingX, HudConfig.INSTANCE.pingY, 70, 12)) {
            return Element.PING;
        }
        if (DirectionHUD.INSTANCE != null && DirectionHUD.INSTANCE.isEnabled()
                && isInside(mx, my, HudConfig.INSTANCE.directionX, HudConfig.INSTANCE.directionY, 110, 12)) {
            return Element.DIRECTION;
        }
        if (MomentumMod.INSTANCE != null && MomentumMod.INSTANCE.isEnabled()
                && isInside(mx, my, HudConfig.INSTANCE.momentumX, HudConfig.INSTANCE.momentumY, 80, 12)) {
            return Element.MOMENTUM;
        }
        if (SaturationMod.INSTANCE != null && SaturationMod.INSTANCE.isEnabled()
                && isInside(mx, my, HudConfig.INSTANCE.saturationX, HudConfig.INSTANCE.saturationY, 90, 12)) {
            return Element.SATURATION;
        }
        if (TotemCounter.INSTANCE != null && TotemCounter.INSTANCE.isEnabled()
                && isInside(mx, my, HudConfig.INSTANCE.totemX, HudConfig.INSTANCE.totemY, 70, 12)) {
            return Element.TOTEM;
        }
        if (ItemCounter.INSTANCE != null && ItemCounter.INSTANCE.isEnabled()
                && isInside(mx, my, HudConfig.INSTANCE.itemCountX, HudConfig.INSTANCE.itemCountY, 80, 12)) {
            return Element.ITEMCOUNT;
        }
        if (ReachDisplay.INSTANCE != null && ReachDisplay.INSTANCE.isEnabled()
                && isInside(mx, my, HudConfig.INSTANCE.reachX, HudConfig.INSTANCE.reachY, 80, 12)) {
            return Element.REACH;
        }
        if (StopwatchMod.INSTANCE != null && StopwatchMod.INSTANCE.isEnabled()
                && isInside(mx, my, HudConfig.INSTANCE.stopwatchX, HudConfig.INSTANCE.stopwatchY, 80, 12)) {
            return Element.STOPWATCH;
        }
        if (StatusEffectHUD.INSTANCE != null && StatusEffectHUD.INSTANCE.isEnabled()) {
            int fx = HudConfig.INSTANCE.effectsX == -1 ? (width - 118) : HudConfig.INSTANCE.effectsX;
            int fy = HudConfig.INSTANCE.effectsY == -1 ? 8 : HudConfig.INSTANCE.effectsY;
            if (isInside(mx, my, fx, fy, 118, 60)) {
                return Element.EFFECTS;
            }
        }
        if (BossBarMod.INSTANCE != null && BossBarMod.INSTANCE.shouldShowCustom()) {
            int bx = HudConfig.INSTANCE.bossBarX == -1 ? (width - 182) / 2 : HudConfig.INSTANCE.bossBarX;
            if (isInside(mx, my, bx, HudConfig.INSTANCE.bossBarY, 182, 16)) {
                return Element.BOSSBAR;
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
        if (Coordinates.INSTANCE != null && Coordinates.INSTANCE.isEnabled()) {
            drawBoundingBox(poseStack, Element.COORDS, HudConfig.INSTANCE.coordsX, HudConfig.INSTANCE.coordsY, 150, 12, "Coords", mouseX, mouseY, accent);
        }
        if (PingMod.INSTANCE != null && PingMod.INSTANCE.isEnabled()) {
            drawBoundingBox(poseStack, Element.PING, HudConfig.INSTANCE.pingX, HudConfig.INSTANCE.pingY, 70, 12, "Ping", mouseX, mouseY, accent);
        }
        if (DirectionHUD.INSTANCE != null && DirectionHUD.INSTANCE.isEnabled()) {
            drawBoundingBox(poseStack, Element.DIRECTION, HudConfig.INSTANCE.directionX, HudConfig.INSTANCE.directionY, 110, 12, "Direction", mouseX, mouseY, accent);
        }
        if (MomentumMod.INSTANCE != null && MomentumMod.INSTANCE.isEnabled()) {
            drawBoundingBox(poseStack, Element.MOMENTUM, HudConfig.INSTANCE.momentumX, HudConfig.INSTANCE.momentumY, 80, 12, "Speed", mouseX, mouseY, accent);
        }
        if (SaturationMod.INSTANCE != null && SaturationMod.INSTANCE.isEnabled()) {
            drawBoundingBox(poseStack, Element.SATURATION, HudConfig.INSTANCE.saturationX, HudConfig.INSTANCE.saturationY, 90, 12, "Food", mouseX, mouseY, accent);
        }
        if (TotemCounter.INSTANCE != null && TotemCounter.INSTANCE.isEnabled()) {
            drawBoundingBox(poseStack, Element.TOTEM, HudConfig.INSTANCE.totemX, HudConfig.INSTANCE.totemY, 70, 12, "Totems", mouseX, mouseY, accent);
        }
        if (ItemCounter.INSTANCE != null && ItemCounter.INSTANCE.isEnabled()) {
            drawBoundingBox(poseStack, Element.ITEMCOUNT, HudConfig.INSTANCE.itemCountX, HudConfig.INSTANCE.itemCountY, 80, 12, "Items", mouseX, mouseY, accent);
        }
        if (ReachDisplay.INSTANCE != null && ReachDisplay.INSTANCE.isEnabled()) {
            drawBoundingBox(poseStack, Element.REACH, HudConfig.INSTANCE.reachX, HudConfig.INSTANCE.reachY, 80, 12, "Reach", mouseX, mouseY, accent);
        }
        if (StopwatchMod.INSTANCE != null && StopwatchMod.INSTANCE.isEnabled()) {
            drawBoundingBox(poseStack, Element.STOPWATCH, HudConfig.INSTANCE.stopwatchX, HudConfig.INSTANCE.stopwatchY, 80, 12, "Timer", mouseX, mouseY, accent);
        }
        if (StatusEffectHUD.INSTANCE != null && StatusEffectHUD.INSTANCE.isEnabled()) {
            int fx = HudConfig.INSTANCE.effectsX == -1 ? (width - 118) : HudConfig.INSTANCE.effectsX;
            int fy = HudConfig.INSTANCE.effectsY == -1 ? 8 : HudConfig.INSTANCE.effectsY;
            drawBoundingBox(poseStack, Element.EFFECTS, fx, fy, 118, 60, "Effects", mouseX, mouseY, accent);
        }
        if (BossBarMod.INSTANCE != null && BossBarMod.INSTANCE.shouldShowCustom()) {
            int bx = HudConfig.INSTANCE.bossBarX == -1 ? (width - 182) / 2 : HudConfig.INSTANCE.bossBarX;
            drawBoundingBox(poseStack, Element.BOSSBAR, bx, HudConfig.INSTANCE.bossBarY, 182, 16, "BossBar", mouseX, mouseY, accent);
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
                case COORDS:
                    HudConfig.INSTANCE.coordsX = Math.max(0, Math.min(width - 150, newX));
                    HudConfig.INSTANCE.coordsY = Math.max(0, Math.min(height - 12, newY));
                    break;
                case PING:
                    HudConfig.INSTANCE.pingX = Math.max(0, Math.min(width - 70, newX));
                    HudConfig.INSTANCE.pingY = Math.max(0, Math.min(height - 12, newY));
                    break;
                case DIRECTION:
                    HudConfig.INSTANCE.directionX = Math.max(0, Math.min(width - 110, newX));
                    HudConfig.INSTANCE.directionY = Math.max(0, Math.min(height - 12, newY));
                    break;
                case MOMENTUM:
                    HudConfig.INSTANCE.momentumX = Math.max(0, Math.min(width - 80, newX));
                    HudConfig.INSTANCE.momentumY = Math.max(0, Math.min(height - 12, newY));
                    break;
                case SATURATION:
                    HudConfig.INSTANCE.saturationX = Math.max(0, Math.min(width - 90, newX));
                    HudConfig.INSTANCE.saturationY = Math.max(0, Math.min(height - 12, newY));
                    break;
                case TOTEM:
                    HudConfig.INSTANCE.totemX = Math.max(0, Math.min(width - 70, newX));
                    HudConfig.INSTANCE.totemY = Math.max(0, Math.min(height - 12, newY));
                    break;
                case ITEMCOUNT:
                    HudConfig.INSTANCE.itemCountX = Math.max(0, Math.min(width - 80, newX));
                    HudConfig.INSTANCE.itemCountY = Math.max(0, Math.min(height - 12, newY));
                    break;
                case REACH:
                    HudConfig.INSTANCE.reachX = Math.max(0, Math.min(width - 80, newX));
                    HudConfig.INSTANCE.reachY = Math.max(0, Math.min(height - 12, newY));
                    break;
                case STOPWATCH:
                    HudConfig.INSTANCE.stopwatchX = Math.max(0, Math.min(width - 80, newX));
                    HudConfig.INSTANCE.stopwatchY = Math.max(0, Math.min(height - 12, newY));
                    break;
                case EFFECTS:
                    HudConfig.INSTANCE.effectsX = Math.max(0, Math.min(width - 118, newX));
                    HudConfig.INSTANCE.effectsY = Math.max(0, Math.min(height - 60, newY));
                    break;
                case BOSSBAR:
                    HudConfig.INSTANCE.bossBarX = Math.max(0, Math.min(width - 182, newX));
                    HudConfig.INSTANCE.bossBarY = Math.max(0, Math.min(height - 16, newY));
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
            if (Coordinates.INSTANCE != null && Coordinates.INSTANCE.isEnabled()
                    && isInside(mouseX, mouseY, HudConfig.INSTANCE.coordsX, HudConfig.INSTANCE.coordsY, 150, 12)) {
                startDrag(Element.COORDS, (int) mouseX, (int) mouseY, HudConfig.INSTANCE.coordsX, HudConfig.INSTANCE.coordsY);
                return true;
            }
            if (PingMod.INSTANCE != null && PingMod.INSTANCE.isEnabled()
                    && isInside(mouseX, mouseY, HudConfig.INSTANCE.pingX, HudConfig.INSTANCE.pingY, 70, 12)) {
                startDrag(Element.PING, (int) mouseX, (int) mouseY, HudConfig.INSTANCE.pingX, HudConfig.INSTANCE.pingY);
                return true;
            }
            if (DirectionHUD.INSTANCE != null && DirectionHUD.INSTANCE.isEnabled()
                    && isInside(mouseX, mouseY, HudConfig.INSTANCE.directionX, HudConfig.INSTANCE.directionY, 110, 12)) {
                startDrag(Element.DIRECTION, (int) mouseX, (int) mouseY, HudConfig.INSTANCE.directionX, HudConfig.INSTANCE.directionY);
                return true;
            }
            if (MomentumMod.INSTANCE != null && MomentumMod.INSTANCE.isEnabled()
                    && isInside(mouseX, mouseY, HudConfig.INSTANCE.momentumX, HudConfig.INSTANCE.momentumY, 80, 12)) {
                startDrag(Element.MOMENTUM, (int) mouseX, (int) mouseY, HudConfig.INSTANCE.momentumX, HudConfig.INSTANCE.momentumY);
                return true;
            }
            if (SaturationMod.INSTANCE != null && SaturationMod.INSTANCE.isEnabled()
                    && isInside(mouseX, mouseY, HudConfig.INSTANCE.saturationX, HudConfig.INSTANCE.saturationY, 90, 12)) {
                startDrag(Element.SATURATION, (int) mouseX, (int) mouseY, HudConfig.INSTANCE.saturationX, HudConfig.INSTANCE.saturationY);
                return true;
            }
            if (TotemCounter.INSTANCE != null && TotemCounter.INSTANCE.isEnabled()
                    && isInside(mouseX, mouseY, HudConfig.INSTANCE.totemX, HudConfig.INSTANCE.totemY, 70, 12)) {
                startDrag(Element.TOTEM, (int) mouseX, (int) mouseY, HudConfig.INSTANCE.totemX, HudConfig.INSTANCE.totemY);
                return true;
            }
            if (ItemCounter.INSTANCE != null && ItemCounter.INSTANCE.isEnabled()
                    && isInside(mouseX, mouseY, HudConfig.INSTANCE.itemCountX, HudConfig.INSTANCE.itemCountY, 80, 12)) {
                startDrag(Element.ITEMCOUNT, (int) mouseX, (int) mouseY, HudConfig.INSTANCE.itemCountX, HudConfig.INSTANCE.itemCountY);
                return true;
            }
            if (ReachDisplay.INSTANCE != null && ReachDisplay.INSTANCE.isEnabled()
                    && isInside(mouseX, mouseY, HudConfig.INSTANCE.reachX, HudConfig.INSTANCE.reachY, 80, 12)) {
                startDrag(Element.REACH, (int) mouseX, (int) mouseY, HudConfig.INSTANCE.reachX, HudConfig.INSTANCE.reachY);
                return true;
            }
            if (StopwatchMod.INSTANCE != null && StopwatchMod.INSTANCE.isEnabled()
                    && isInside(mouseX, mouseY, HudConfig.INSTANCE.stopwatchX, HudConfig.INSTANCE.stopwatchY, 80, 12)) {
                startDrag(Element.STOPWATCH, (int) mouseX, (int) mouseY, HudConfig.INSTANCE.stopwatchX, HudConfig.INSTANCE.stopwatchY);
                return true;
            }
            if (StatusEffectHUD.INSTANCE != null && StatusEffectHUD.INSTANCE.isEnabled()) {
                int fx = HudConfig.INSTANCE.effectsX == -1 ? (width - 118) : HudConfig.INSTANCE.effectsX;
                int fy = HudConfig.INSTANCE.effectsY == -1 ? 8 : HudConfig.INSTANCE.effectsY;
                if (isInside(mouseX, mouseY, fx, fy, 118, 60)) {
                    HudConfig.INSTANCE.effectsX = fx;
                    HudConfig.INSTANCE.effectsY = fy;
                    startDrag(Element.EFFECTS, (int) mouseX, (int) mouseY, fx, fy);
                    return true;
                }
            }
            if (BossBarMod.INSTANCE != null && BossBarMod.INSTANCE.shouldShowCustom()) {
                int bx = HudConfig.INSTANCE.bossBarX == -1 ? (width - 182) / 2 : HudConfig.INSTANCE.bossBarX;
                if (isInside(mouseX, mouseY, bx, HudConfig.INSTANCE.bossBarY, 182, 16)) {
                    HudConfig.INSTANCE.bossBarX = bx;
                    startDrag(Element.BOSSBAR, (int) mouseX, (int) mouseY, bx, HudConfig.INSTANCE.bossBarY);
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
        HudConfig.INSTANCE.coordsX = round10(HudConfig.INSTANCE.coordsX);
        HudConfig.INSTANCE.coordsY = round10(HudConfig.INSTANCE.coordsY);
        HudConfig.INSTANCE.pingX = round10(HudConfig.INSTANCE.pingX);
        HudConfig.INSTANCE.pingY = round10(HudConfig.INSTANCE.pingY);
        HudConfig.INSTANCE.directionX = round10(HudConfig.INSTANCE.directionX);
        HudConfig.INSTANCE.directionY = round10(HudConfig.INSTANCE.directionY);
        HudConfig.INSTANCE.momentumX = round10(HudConfig.INSTANCE.momentumX);
        HudConfig.INSTANCE.momentumY = round10(HudConfig.INSTANCE.momentumY);
        HudConfig.INSTANCE.saturationX = round10(HudConfig.INSTANCE.saturationX);
        HudConfig.INSTANCE.saturationY = round10(HudConfig.INSTANCE.saturationY);
        HudConfig.INSTANCE.totemX = round10(HudConfig.INSTANCE.totemX);
        HudConfig.INSTANCE.totemY = round10(HudConfig.INSTANCE.totemY);
        HudConfig.INSTANCE.itemCountX = round10(HudConfig.INSTANCE.itemCountX);
        HudConfig.INSTANCE.itemCountY = round10(HudConfig.INSTANCE.itemCountY);
        HudConfig.INSTANCE.reachX = round10(HudConfig.INSTANCE.reachX);
        HudConfig.INSTANCE.reachY = round10(HudConfig.INSTANCE.reachY);
        HudConfig.INSTANCE.stopwatchX = round10(HudConfig.INSTANCE.stopwatchX);
        HudConfig.INSTANCE.stopwatchY = round10(HudConfig.INSTANCE.stopwatchY);
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
