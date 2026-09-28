package astra.mod.client.ui.crosshair;

import astra.mod.client.module.impl.render.CustomCrosshair;
import astra.mod.client.theme.ThemeManager;
import astra.mod.client.util.RenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import org.lwjgl.glfw.GLFW;

public class CrosshairEditorScreen extends Screen {
    public static final CrosshairEditorScreen INSTANCE = new CrosshairEditorScreen();

    private enum Tool {
        PENCIL("✏️ Карандаш"),
        ERASER("🧹 Ластик");

        final String label;
        Tool(String label) { this.label = label; }
    }

    private Tool currentTool = Tool.PENCIL;
    private boolean symmetry4X = true;
    private int selectedColor = 1; // 1 = Accent, or ARGB hex
    private boolean isMouseDown = false;
    private int mouseButton = 0;

    private static final int[] PALETTE = new int[]{
            1, // Dynamic Theme Accent
            0xFFFFFFFF, // Pure White
            0xFFEF4444, // Crimson Red
            0xFF06B6D4, // Neon Cyan
            0xFF10B981, // Emerald Green
            0xFFF59E0B, // Golden Yellow
            0xFFA855F7, // Neon Purple
            0xFFEC4899  // Hot Pink
    };

    public CrosshairEditorScreen() {
        super(new TextComponent("Crosshair Canvas Editor"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        int accent = ThemeManager.INSTANCE.getAccent();

        // 1. Dark Backdrop
        RenderUtils.drawRect(poseStack, 0, 0, width, height, 0xDD080B11);

        // 2. Main Window Container
        int winW = Math.min(580, width - 20);
        int winH = Math.min(360, height - 20);
        int winX = (width - winW) / 2;
        int winY = (height - winH) / 2;

        RenderUtils.drawRoundedBorderedRect(poseStack, winX, winY, winW, winH, 6, 1, 0xF80B0F19, 0xFF334155);

        // Header
        RenderUtils.drawRect(poseStack, winX, winY, winW, 28, 0xFF0F172A);
        RenderUtils.drawRect(poseStack, winX, winY + 27, winW, 1, 0xFF334155);
        font.drawShadow(poseStack, "★ РЕДАКТОР ПРИЦЕЛА (ХОЛСТ 32×32)", winX + 12, winY + 10, accent);

        // Save & Close Button
        int saveW = 85;
        int saveX = winX + winW - saveW - 10;
        int saveY = winY + 5;
        boolean saveHover = mouseX >= saveX && mouseX <= saveX + saveW && mouseY >= saveY && mouseY <= saveY + 18;
        RenderUtils.drawRoundedBorderedRect(poseStack, saveX, saveY, saveW, 18, 4, 1, saveHover ? 0xEE1E1B4B : 0xAA0F172A, saveHover ? accent : 0xFF334155);
        font.drawShadow(poseStack, "✓ Сохранить", saveX + 10, saveY + 5, saveHover ? 0xFFFFFFFF : 0xFFCBD5E1);

        // 3. 32x32 Drawing Grid
        int cellSize = 8;
        int gridPixelSize = 32 * cellSize; // 256px
        int gridX = winX + 20;
        int gridY = winY + 38;

        RenderUtils.drawBorderedRect(poseStack, gridX - 1, gridY - 1, gridPixelSize + 2, gridPixelSize + 2, 1, 0xFF05070B, 0xFF334155);

        CustomCrosshair mod = CustomCrosshair.INSTANCE;

        // Draw grid cells and pixels
        for (int x = 0; x < 32; x++) {
            for (int y = 0; y < 32; y++) {
                int px = gridX + x * cellSize;
                int py = gridY + y * cellSize;

                int pixel = mod.canvas[x][y];
                int cellColor = (pixel == 0) ? (((x + y) % 2 == 0) ? 0xFF0B0F19 : 0xFF0F1422) : (pixel == 1 ? accent : pixel);

                RenderUtils.drawRect(poseStack, px, py, cellSize - 1, cellSize - 1, cellColor);
            }
        }

        // Center crosshair guides
        int midX = gridX + 16 * cellSize;
        int midY = gridY + 16 * cellSize;
        RenderUtils.drawRect(poseStack, midX - 1, gridY, 1, gridPixelSize, 0x446366F1);
        RenderUtils.drawRect(poseStack, gridX, midY - 1, gridPixelSize, 1, 0x446366F1);

        // Handle drawing while dragging
        if (isMouseDown && mouseX >= gridX && mouseX < gridX + gridPixelSize && mouseY >= gridY && mouseY < gridY + gridPixelSize) {
            int cx = (mouseX - gridX) / cellSize;
            int cy = (mouseY - gridY) / cellSize;
            applyDraw(cx, cy, mouseButton == 0 ? (currentTool == Tool.PENCIL ? selectedColor : 0) : 0);
        }

        // 4. Right Control Panel
        int panelX = gridX + gridPixelSize + 20;
        int panelY = winY + 38;
        int panelW = winW - (gridPixelSize + 50);

        // Tools
        font.drawShadow(poseStack, "ИНСТРУМЕНТЫ", panelX, panelY, 0xFF94A3B8);

        int toolBtnW = (panelW - 6) / 2;
        boolean pencilActive = (currentTool == Tool.PENCIL);
        RenderUtils.drawRoundedBorderedRect(poseStack, panelX, panelY + 12, toolBtnW, 20, 4, 1, pencilActive ? 0xDD1E1B4B : 0xAA0F172A, pencilActive ? accent : 0xFF334155);
        font.drawShadow(poseStack, "✏️ Кисть", panelX + 8, panelY + 17, pencilActive ? 0xFFFFFFFF : 0xFF94A3B8);

        boolean eraserActive = (currentTool == Tool.ERASER);
        RenderUtils.drawRoundedBorderedRect(poseStack, panelX + toolBtnW + 6, panelY + 12, toolBtnW, 20, 4, 1, eraserActive ? 0xDD1E1B4B : 0xAA0F172A, eraserActive ? accent : 0xFF334155);
        font.drawShadow(poseStack, "🧹 Ластик", panelX + toolBtnW + 12, panelY + 17, eraserActive ? 0xFFFFFFFF : 0xFF94A3B8);

        // Symmetry Toggle
        int symY = panelY + 38;
        RenderUtils.drawRoundedBorderedRect(poseStack, panelX, symY, panelW, 20, 4, 1, symmetry4X ? 0xDD1E1B4B : 0xAA0F172A, symmetry4X ? accent : 0xFF334155);
        font.drawShadow(poseStack, "Симметрия 4X: " + (symmetry4X ? "[ВКЛ ✓]" : "[ВЫКЛ]"), panelX + 8, symY + 6, symmetry4X ? 0xFFFFFFFF : 0xFF94A3B8);

        // Color Palette
        int palY = symY + 28;
        font.drawShadow(poseStack, "ПАЛИТРА ЦВЕТОВ", panelX, palY, 0xFF94A3B8);

        int colorSize = (panelW - 21) / 4;
        for (int i = 0; i < PALETTE.length; i++) {
            int cx = panelX + (i % 4) * (colorSize + 7);
            int cy = palY + 12 + (i / 4) * (colorSize + 7);
            int c = PALETTE[i];
            int drawC = (c == 1) ? accent : c;

            boolean isSel = (selectedColor == c);
            RenderUtils.drawRoundedBorderedRect(poseStack, cx, cy, colorSize, colorSize, 3, isSel ? 2 : 1, drawC, isSel ? 0xFFFFFFFF : 0xFF334155);
            if (isSel) {
                font.drawShadow(poseStack, "•", cx + colorSize / 2 - 2, cy + colorSize / 2 - 4, 0xFF000000);
            }
        }

        // Quick Presets
        int preY = palY + 70;
        font.drawShadow(poseStack, "ПРЕСЕТЫ", panelX, preY, 0xFF94A3B8);

        int pBtnW = (panelW - 6) / 2;
        drawPresetButton(poseStack, panelX, preY + 12, pBtnW, "Крест", mouseX, mouseY, accent);
        drawPresetButton(poseStack, panelX + pBtnW + 6, preY + 12, pBtnW, "Точка", mouseX, mouseY, accent);
        drawPresetButton(poseStack, panelX, preY + 34, pBtnW, "CS Круг", mouseX, mouseY, accent);
        drawPresetButton(poseStack, panelX + pBtnW + 6, preY + 34, pBtnW, "Очистить", mouseX, mouseY, 0xFFEF4444);

        // Live Scale 1:1 Preview
        int prevY = preY + 62;
        font.drawShadow(poseStack, "ПРЕВЬЮ 1:1 В ИГРЕ", panelX, prevY, 0xFF94A3B8);
        int prevBoxSize = 44;
        RenderUtils.drawBorderedRect(poseStack, panelX, prevY + 12, prevBoxSize, prevBoxSize, 1, 0xFF05070B, 0xFF334155);

        int prevMidX = panelX + prevBoxSize / 2;
        int prevMidY = prevY + 12 + prevBoxSize / 2;

        for (int x = 0; x < 32; x++) {
            for (int y = 0; y < 32; y++) {
                int pixel = mod.canvas[x][y];
                if (pixel != 0) {
                    int col = (pixel == 1) ? accent : pixel;
                    RenderUtils.drawRect(poseStack, prevMidX - 16 + x, prevMidY - 16 + y, 1, 1, col);
                }
            }
        }

        super.render(poseStack, mouseX, mouseY, delta);
    }

    private void drawPresetButton(PoseStack poseStack, int x, int y, int w, String label, int mouseX, int mouseY, int accent) {
        boolean hover = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + 18;
        RenderUtils.drawRoundedBorderedRect(poseStack, x, y, w, 18, 4, 1, hover ? 0xDD1E1B4B : 0xAA0F172A, hover ? accent : 0xFF334155);
        font.drawShadow(poseStack, label, x + (w - font.width(label)) / 2.0f, y + 5, hover ? 0xFFFFFFFF : 0xFFCBD5E1);
    }

    private void applyDraw(int x, int y, int color) {
        if (x < 0 || x >= 32 || y < 0 || y >= 32) return;
        CustomCrosshair mod = CustomCrosshair.INSTANCE;
        mod.canvas[x][y] = color;

        if (symmetry4X) {
            mod.canvas[31 - x][y] = color;
            mod.canvas[x][31 - y] = color;
            mod.canvas[31 - x][31 - y] = color;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        isMouseDown = true;
        mouseButton = button;

        int winW = Math.min(580, width - 20);
        int winH = Math.min(360, height - 20);
        int winX = (width - winW) / 2;
        int winY = (height - winH) / 2;

        // Save & Close
        int saveW = 85;
        int saveX = winX + winW - saveW - 10;
        int saveY = winY + 5;
        if (mouseX >= saveX && mouseX <= saveX + saveW && mouseY >= saveY && mouseY <= saveY + 18) {
            astra.mod.client.config.ConfigManager.INSTANCE.save();
            onClose();
            return true;
        }

        int cellSize = 8;
        int gridPixelSize = 32 * cellSize;
        int gridX = winX + 20;
        int gridY = winY + 38;

        // Click on Canvas
        if (mouseX >= gridX && mouseX < gridX + gridPixelSize && mouseY >= gridY && mouseY < gridY + gridPixelSize) {
            int cx = (int) ((mouseX - gridX) / cellSize);
            int cy = (int) ((mouseY - gridY) / cellSize);
            applyDraw(cx, cy, button == 0 ? (currentTool == Tool.PENCIL ? selectedColor : 0) : 0);
            return true;
        }

        // Tools
        int panelX = gridX + gridPixelSize + 20;
        int panelY = winY + 38;
        int panelW = winW - (gridPixelSize + 50);
        int toolBtnW = (panelW - 6) / 2;

        if (mouseX >= panelX && mouseX <= panelX + toolBtnW && mouseY >= panelY + 12 && mouseY <= panelY + 32) {
            currentTool = Tool.PENCIL;
            return true;
        }
        if (mouseX >= panelX + toolBtnW + 6 && mouseX <= panelX + panelW && mouseY >= panelY + 12 && mouseY <= panelY + 32) {
            currentTool = Tool.ERASER;
            return true;
        }

        // Symmetry
        int symY = panelY + 38;
        if (mouseX >= panelX && mouseX <= panelX + panelW && mouseY >= symY && mouseY <= symY + 20) {
            symmetry4X = !symmetry4X;
            return true;
        }

        // Palette
        int palY = symY + 28;
        int colorSize = (panelW - 21) / 4;
        for (int i = 0; i < PALETTE.length; i++) {
            int cx = panelX + (i % 4) * (colorSize + 7);
            int cy = palY + 12 + (i / 4) * (colorSize + 7);
            if (mouseX >= cx && mouseX <= cx + colorSize && mouseY >= cy && mouseY <= cy + colorSize) {
                selectedColor = PALETTE[i];
                currentTool = Tool.PENCIL;
                return true;
            }
        }

        // Presets
        int preY = palY + 70;
        int pBtnW = (panelW - 6) / 2;

        if (mouseX >= panelX && mouseX <= panelX + pBtnW && mouseY >= preY + 12 && mouseY <= preY + 30) {
            loadPresetCross();
            return true;
        }
        if (mouseX >= panelX + pBtnW + 6 && mouseX <= panelX + panelW && mouseY >= preY + 12 && mouseY <= preY + 30) {
            loadPresetDot();
            return true;
        }
        if (mouseX >= panelX && mouseX <= panelX + pBtnW && mouseY >= preY + 34 && mouseY <= preY + 52) {
            loadPresetCircle();
            return true;
        }
        if (mouseX >= panelX + pBtnW + 6 && mouseX <= panelX + panelW && mouseY >= preY + 34 && mouseY <= preY + 52) {
            clearCanvas();
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void clearCanvas() {
        for (int x = 0; x < 32; x++) {
            for (int y = 0; y < 32; y++) {
                CustomCrosshair.INSTANCE.canvas[x][y] = 0;
            }
        }
    }

    private void loadPresetCross() {
        clearCanvas();
        CustomCrosshair.INSTANCE.initDefaultCanvas();
    }

    private void loadPresetDot() {
        clearCanvas();
        CustomCrosshair.INSTANCE.canvas[15][15] = 1;
        CustomCrosshair.INSTANCE.canvas[16][15] = 1;
        CustomCrosshair.INSTANCE.canvas[15][16] = 1;
        CustomCrosshair.INSTANCE.canvas[16][16] = 1;
    }

    private void loadPresetCircle() {
        clearCanvas();
        for (int x = 0; x < 32; x++) {
            for (int y = 0; y < 32; y++) {
                double dist = Math.sqrt((x - 15.5) * (x - 15.5) + (y - 15.5) * (y - 15.5));
                if (dist >= 5.0 && dist <= 7.0) {
                    CustomCrosshair.INSTANCE.canvas[x][y] = 1;
                }
            }
        }
        CustomCrosshair.INSTANCE.canvas[15][15] = 1;
        CustomCrosshair.INSTANCE.canvas[16][15] = 1;
        CustomCrosshair.INSTANCE.canvas[15][16] = 1;
        CustomCrosshair.INSTANCE.canvas[16][16] = 1;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        isMouseDown = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            astra.mod.client.config.ConfigManager.INSTANCE.save();
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
