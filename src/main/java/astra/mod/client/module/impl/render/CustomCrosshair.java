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

    public final ModeSetting style = new ModeSetting("Style", "Стиль прицела", "Canvas (32x32)", "Canvas (32x32)", "Cross", "Dot", "Box", "Circle", "T-Shape", "Heart", "Chevron", "Arrow", "Triangle");
    public final ModeSetting colorMode = new ModeSetting("Color", "Цвет прицела", "Accent", "Accent", "White", "Rainbow", "Red", "Cyan", "Green", "Yellow", "Purple");
    public final NumberSetting pixelScale = new NumberSetting("Scale", "Масштаб холста", 1.0, 0.25, 3.0, 0.25);
    public final NumberSetting size = new NumberSetting("Size", "Длина лучей (для пресетов)", 4.0, 1.0, 15.0, 0.5);
    public final NumberSetting gap = new NumberSetting("Gap", "Зазор от центра", 2.0, 0.0, 10.0, 0.5);
    public final NumberSetting thickness = new NumberSetting("Thickness", "Толщина линий", 1.0, 0.5, 4.0, 0.5);
    public final BooleanSetting dot = new BooleanSetting("CenterDot", "Точка в центре", true);
    public final BooleanSetting outline = new BooleanSetting("Outline", "Черная обводка", true);

    public final int[][] canvas = new int[32][32];

    public CustomCrosshair() {
        super("Crosshair", "Кастомизация и рисование прицела на холсте 32x32", Category.RENDER);
        INSTANCE = this;
        addSettings(style, colorMode, pixelScale, size, gap, thickness, dot, outline);
        initDefaultCanvas();
    }

    public void initDefaultCanvas() {
        for (int i = 0; i < 32; i++) {
            for (int j = 0; j < 32; j++) {
                canvas[i][j] = 0;
            }
        }
        canvas[15][15] = 1; canvas[16][15] = 1;
        canvas[15][16] = 1; canvas[16][16] = 1;

        for (int i = 6; i <= 12; i++) {
            canvas[i][15] = 1; canvas[i][16] = 1;
            canvas[31 - i][15] = 1; canvas[31 - i][16] = 1;
            canvas[15][i] = 1; canvas[16][i] = 1;
            canvas[15][31 - i] = 1; canvas[16][31 - i] = 1;
        }
    }

    public void render(PoseStack poseStack) {
        if (!isEnabled()) return;

        Minecraft mc = Minecraft.getInstance();
        float cx = mc.getWindow().getGuiScaledWidth() / 2.0f;
        float cy = mc.getWindow().getGuiScaledHeight() / 2.0f;

        int color = getCrosshairColor();
        int outlineColor = 0xFF000000;
        boolean hasOutline = outline.getValue();

        if (style.is("Canvas (32x32)")) {
            float scale = pixelScale.getValue().floatValue();
            if (scale <= 0.05f) scale = 0.25f;

            poseStack.pushPose();
            poseStack.translate(cx, cy, 0.0);
            poseStack.scale(scale, scale, 1.0f);

            for (int x = 0; x < 32; x++) {
                for (int y = 0; y < 32; y++) {
                    int pixel = canvas[x][y];
                    if (pixel != 0) {
                        int px = x - 16;
                        int py = y - 16;
                        int col = (pixel == 1) ? color : pixel;

                        if (hasOutline) {
                            RenderUtils.drawBorderedRect(poseStack, px - 1, py - 1, 3, 3, 1, col, outlineColor);
                        } else {
                            RenderUtils.drawRect(poseStack, px, py, 1, 1, col);
                        }
                    }
                }
            }
            poseStack.popPose();
            return;
        }

        float s = size.getValue().floatValue();
        float g = gap.getValue().floatValue();
        float t = thickness.getValue().floatValue();
        float offX = t / 2.0f;
        float offY = t / 2.0f;

        if (dot.getValue() || style.is("Dot")) {
            float dotSize = Math.max(0.5f, t);
            float dx = cx - offX;
            float dy = cy - offY;
            if (hasOutline) {
                RenderUtils.drawBorderedRect(poseStack, (int)(dx - 1), (int)(dy - 1), (int)(dotSize + 2), (int)(dotSize + 2), 1, color, outlineColor);
            } else {
                RenderUtils.drawRect(poseStack, (int)dx, (int)dy, (int)dotSize, (int)dotSize, color);
            }
            if (style.is("Dot")) return;
        }

        if (style.is("Cross") || style.is("T-Shape")) {
            drawRay(poseStack, cx - offX - g - s, cy - offY, s, t, color, outlineColor, hasOutline);
            drawRay(poseStack, cx - offX + t + g, cy - offY, s, t, color, outlineColor, hasOutline);
            drawRay(poseStack, cx - offX, cy - offY + t + g, t, s, color, outlineColor, hasOutline);
            if (!style.is("T-Shape")) {
                drawRay(poseStack, cx - offX, cy - offY - g - s, t, s, color, outlineColor, hasOutline);
            }
        } else if (style.is("Box")) {
            float boxDim = (g + s) * 2;
            float bx = cx - (g + s);
            float by = cy - (g + s);
            if (hasOutline) {
                RenderUtils.drawBorderedRect(poseStack, (int)(bx - 1), (int)(by - 1), (int)(boxDim + 2), (int)(boxDim + 2), (int)(t + 1), 0x00000000, outlineColor);
            }
            RenderUtils.drawBorderedRect(poseStack, (int)bx, (int)by, (int)boxDim, (int)boxDim, (int)t, 0x00000000, color);
        } else if (style.is("Circle")) {
            RenderUtils.drawCircle(poseStack, cx, cy, g + s, color);
        } else if (style.is("Chevron")) {
            for (float i = 0; i < s; i++) {
                drawRay(poseStack, cx - i - g, cy + i + g, t, t, color, outlineColor, hasOutline);
                drawRay(poseStack, cx + i + g, cy + i + g, t, t, color, outlineColor, hasOutline);
            }
        } else if (style.is("Arrow")) {
            drawRay(poseStack, cx - offX, cy - g - s, t, s * 2, color, outlineColor, hasOutline);
            for (float i = 0; i < s; i++) {
                drawRay(poseStack, cx - i - 1, cy - g - s + i, t, t, color, outlineColor, hasOutline);
                drawRay(poseStack, cx + i + 1, cy - g - s + i, t, t, color, outlineColor, hasOutline);
            }
        } else if (style.is("Heart")) {
            drawRay(poseStack, cx - 2, cy - 2, 2, 2, color, outlineColor, hasOutline);
            drawRay(poseStack, cx + 1, cy - 2, 2, 2, color, outlineColor, hasOutline);
            drawRay(poseStack, cx - 3, cy - 1, 7, 2, color, outlineColor, hasOutline);
            drawRay(poseStack, cx - 2, cy + 1, 5, 2, color, outlineColor, hasOutline);
            drawRay(poseStack, cx - 1, cy + 3, 3, 1, color, outlineColor, hasOutline);
            drawRay(poseStack, cx, cy + 4, 1, 1, color, outlineColor, hasOutline);
        } else if (style.is("Triangle")) {
            for (float i = 0; i < s; i++) {
                drawRay(poseStack, cx - i, cy + i + g, t, t, color, outlineColor, hasOutline);
                drawRay(poseStack, cx + i, cy + i + g, t, t, color, outlineColor, hasOutline);
            }
            drawRay(poseStack, cx - s, cy + s + g, s * 2, t, color, outlineColor, hasOutline);
        }
    }

    private void drawRay(PoseStack poseStack, float x, float y, float w, float h, int col, int outCol, boolean out) {
        int rw = Math.max(1, Math.round(w));
        int rh = Math.max(1, Math.round(h));
        if (out) {
            RenderUtils.drawBorderedRect(poseStack, (int)(x - 1), (int)(y - 1), rw + 2, rh + 2, 1, col, outCol);
        } else {
            RenderUtils.drawRect(poseStack, (int)x, (int)y, rw, rh, col);
        }
    }

    public int getCrosshairColor() {
        switch (colorMode.getValue()) {
            case "White": return 0xFFFFFFFF;
            case "Rainbow": return ThemeManager.getRainbow(0);
            case "Red": return 0xFFEF4444;
            case "Cyan": return 0xFF06B6D4;
            case "Green": return 0xFF10B981;
            case "Yellow": return 0xFFF59E0B;
            case "Purple": return 0xFFA855F7;
            case "Accent":
            default:
                return ThemeManager.INSTANCE.getAccent();
        }
    }

    public String serializeCanvas() {
        StringBuilder sb = new StringBuilder();
        for (int x = 0; x < 32; x++) {
            for (int y = 0; y < 32; y++) {
                sb.append(canvas[x][y]).append(",");
            }
        }
        return sb.toString();
    }

    public void deserializeCanvas(String data) {
        if (data == null || data.trim().isEmpty()) return;
        String[] parts = data.split(",");
        int idx = 0;
        for (int x = 0; x < 32 && idx < parts.length; x++) {
            for (int y = 0; y < 32 && idx < parts.length; y++) {
                try {
                    canvas[x][y] = Integer.parseInt(parts[idx++]);
                } catch (Exception ignored) {}
            }
        }
    }
}
