package astra.mod.client.module.impl.render;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.BooleanSetting;
import astra.mod.client.setting.ModeSetting;
import astra.mod.client.setting.NumberSetting;
import astra.mod.client.theme.ThemeManager;
import astra.mod.client.util.RenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;

public class CustomCrosshair extends Module {
    public static CustomCrosshair INSTANCE;

    public final ModeSetting style = new ModeSetting("Style", "Стиль прицела", "Cross", "Cross", "Dot", "Box", "Circle", "T-Shape", "Heart", "Chevron", "Arrow", "Triangle");
    public final ModeSetting colorMode = new ModeSetting("Color", "Цвет прицела", "Accent", "Accent", "White", "Rainbow", "Red", "Cyan", "Green");
    public final NumberSetting size = new NumberSetting("Size", "Длина лучей / размер", 4.0, 1.0, 15.0, 1.0);
    public final NumberSetting gap = new NumberSetting("Gap", "Зазор от центра", 2.0, 0.0, 10.0, 1.0);
    public final NumberSetting thickness = new NumberSetting("Thickness", "Толщина линий", 1.0, 1.0, 4.0, 1.0);
    public final BooleanSetting dot = new BooleanSetting("CenterDot", "Точка в центре", true);
    public final BooleanSetting outline = new BooleanSetting("Outline", "Черная обводка линий", true);

    public CustomCrosshair() {
        super("Crosshair", "Кастомизация прицела в стиле PvP клиентов", Category.RENDER);
        INSTANCE = this;
        addSettings(style, colorMode, size, gap, thickness, dot, outline);
    }

    public void render(PoseStack poseStack) {
        if (!isEnabled()) return;

        Minecraft mc = Minecraft.getInstance();
        int cx = mc.getWindow().getGuiScaledWidth() / 2;
        int cy = mc.getWindow().getGuiScaledHeight() / 2;

        int color = getCrosshairColor();
        int outlineColor = 0xFF000000;
        int s = size.getValue().intValue();
        int g = gap.getValue().intValue();
        int t = thickness.getValue().intValue();
        boolean hasOutline = outline.getValue();

        // Precise centering anchors
        int offX = t / 2;
        int offY = t / 2;

        // 1. Perfectly centered Center Dot
        if (dot.getValue() || style.is("Dot")) {
            int dotSize = Math.max(1, t);
            int dx = cx - offX;
            int dy = cy - offY;
            if (hasOutline) {
                RenderUtils.drawBorderedRect(poseStack, dx - 1, dy - 1, dotSize + 2, dotSize + 2, 1, color, outlineColor);
            } else {
                RenderUtils.drawRect(poseStack, dx, dy, dotSize, dotSize, color);
            }
            if (style.is("Dot")) return;
        }

        if (style.is("Cross") || style.is("T-Shape")) {
            // Left ray
            drawRay(poseStack, cx - offX - g - s, cy - offY, s, t, color, outlineColor, hasOutline);
            // Right ray
            drawRay(poseStack, cx - offX + t + g, cy - offY, s, t, color, outlineColor, hasOutline);
            // Bottom ray
            drawRay(poseStack, cx - offX, cy - offY + t + g, t, s, color, outlineColor, hasOutline);
            // Top ray (if not T-Shape)
            if (!style.is("T-Shape")) {
                drawRay(poseStack, cx - offX, cy - offY - g - s, t, s, color, outlineColor, hasOutline);
            }
        } else if (style.is("Box")) {
            int boxDim = (g + s) * 2;
            int bx = cx - (g + s);
            int by = cy - (g + s);
            if (hasOutline) {
                RenderUtils.drawBorderedRect(poseStack, bx - 1, by - 1, boxDim + 2, boxDim + 2, t + 1, 0x00000000, outlineColor);
            }
            RenderUtils.drawBorderedRect(poseStack, bx, by, boxDim, boxDim, t, 0x00000000, color);
        } else if (style.is("Circle")) {
            RenderUtils.drawCircle(poseStack, cx, cy, g + s, color);
        } else if (style.is("Chevron")) {
            for (int i = 0; i < s; i++) {
                drawRay(poseStack, cx - i - g, cy + i + g, t, t, color, outlineColor, hasOutline);
                drawRay(poseStack, cx + i + g, cy + i + g, t, t, color, outlineColor, hasOutline);
            }
        } else if (style.is("Arrow")) {
            drawRay(poseStack, cx - offX, cy - g - s, t, s * 2, color, outlineColor, hasOutline);
            for (int i = 0; i < s; i++) {
                drawRay(poseStack, cx - i - 1, cy - g - s + i, t, t, color, outlineColor, hasOutline);
                drawRay(poseStack, cx + i + 1, cy - g - s + i, t, t, color, outlineColor, hasOutline);
            }
        } else if (style.is("Heart")) {
            // Heart shape
            drawRay(poseStack, cx - 2, cy - 2, 2, 2, color, outlineColor, hasOutline);
            drawRay(poseStack, cx + 1, cy - 2, 2, 2, color, outlineColor, hasOutline);
            drawRay(poseStack, cx - 3, cy - 1, 7, 2, color, outlineColor, hasOutline);
            drawRay(poseStack, cx - 2, cy + 1, 5, 2, color, outlineColor, hasOutline);
            drawRay(poseStack, cx - 1, cy + 3, 3, 1, color, outlineColor, hasOutline);
            drawRay(poseStack, cx, cy + 4, 1, 1, color, outlineColor, hasOutline);
        } else if (style.is("Triangle")) {
            for (int i = 0; i < s; i++) {
                drawRay(poseStack, cx - i, cy + i + g, t, t, color, outlineColor, hasOutline);
                drawRay(poseStack, cx + i, cy + i + g, t, t, color, outlineColor, hasOutline);
            }
            drawRay(poseStack, cx - s, cy + s + g, s * 2, t, color, outlineColor, hasOutline);
        }
    }

    private void drawRay(PoseStack poseStack, int x, int y, int w, int h, int col, int outCol, boolean out) {
        if (out) {
            RenderUtils.drawBorderedRect(poseStack, x - 1, y - 1, w + 2, h + 2, 1, col, outCol);
        } else {
            RenderUtils.drawRect(poseStack, x, y, w, h, col);
        }
    }

    private int getCrosshairColor() {
        switch (colorMode.getValue()) {
            case "White": return 0xFFFFFFFF;
            case "Rainbow": return ThemeManager.getRainbow(0);
            case "Red": return 0xFFEF4444;
            case "Cyan": return 0xFF06B6D4;
            case "Green": return 0xFF10B981;
            case "Accent":
            default:
                return ThemeManager.INSTANCE.getAccent();
        }
    }
}
