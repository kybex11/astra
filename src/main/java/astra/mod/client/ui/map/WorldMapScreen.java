package astra.mod.client.ui.map;

import astra.mod.client.theme.ThemeManager;
import astra.mod.client.util.RenderUtils;
import astra.mod.client.waypoints.WaypointManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.block.state.BlockState;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class WorldMapScreen extends Screen {
    public static final WorldMapScreen INSTANCE = new WorldMapScreen();

    private double offsetX = 0;
    private double offsetZ = 0;
    private double zoom = 1.0; // 0.5x to 3.0x
    private boolean isDragging = false;
    private double lastMouseX = 0;
    private double lastMouseY = 0;

    public WorldMapScreen() {
        super(new TextComponent("World Map"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        super.init();
        if (minecraft != null && minecraft.player != null) {
            offsetX = 0;
            offsetZ = 0;
        }
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        int accent = ThemeManager.INSTANCE.getAccent();
        Minecraft mc = Minecraft.getInstance();

        // 1. Dark background
        RenderUtils.drawRect(poseStack, 0, 0, width, height, 0xF80B0F19);

        if (mc.player == null || mc.level == null) {
            super.render(poseStack, mouseX, mouseY, delta);
            return;
        }

        int centerX = width / 2;
        int centerY = height / 2;

        int playerBaseX = (int) (mc.player.getX() + offsetX);
        int playerBaseY = (int) mc.player.getY();
        int playerBaseZ = (int) (mc.player.getZ() + offsetZ);

        // 2. Render terrain grid
        int viewRadius = (int) (Math.min(width, height) / (2.0 * zoom));
        int step = Math.max(1, (int) (4 / zoom));

        for (int rx = -viewRadius; rx <= viewRadius; rx += step) {
            for (int rz = -viewRadius; rz <= viewRadius; rz += step) {
                int wx = playerBaseX + rx;
                int wz = playerBaseZ + rz;
                BlockPos pos = new BlockPos(wx, playerBaseY, wz);

                BlockState state = mc.level.getBlockState(pos);
                int mapCol = state.getMaterial().getColor().col;
                if (mapCol == 0) {
                    mapCol = mc.level.getBlockState(pos.below()).getMaterial().getColor().col;
                }
                if (mapCol == 0) mapCol = 0x222C3C;

                float screenX = (float) (centerX + rx * zoom);
                float screenY = (float) (centerY + rz * zoom);
                int pSize = (int) Math.ceil(step * zoom);

                if (screenX >= 0 && screenX + pSize <= width && screenY >= 30 && screenY + pSize <= height - 30) {
                    RenderUtils.drawRect(poseStack, (int) screenX, (int) screenY, pSize, pSize, 0xDD000000 | mapCol);
                }
            }
        }

        // 3. Grid overlay lines
        RenderUtils.drawLine(poseStack, centerX, 30, centerX, height - 30, 1.0f, 0x22CBD5E1);
        RenderUtils.drawLine(poseStack, 0, centerY, width, centerY, 1.0f, 0x22CBD5E1);

        // 4. Render Waypoints
        String curDim = "";
        try { curDim = mc.level.dimension().location().getPath(); } catch (Exception ignored) {}
        List<WaypointManager.Waypoint> waypoints = WaypointManager.getAll();
        for (WaypointManager.Waypoint wp : waypoints) {
            if (!curDim.isEmpty() && !curDim.equalsIgnoreCase(wp.dim)) continue;

            double dx = wp.x - (mc.player.getX() + offsetX);
            double dz = wp.z - (mc.player.getZ() + offsetZ);

            float wx = (float) (centerX + dx * zoom);
            float wy = (float) (centerY + dz * zoom);

            if (wx >= 10 && wx <= width - 10 && wy >= 35 && wy <= height - 35) {
                // Waypoint pin
                RenderUtils.drawBorderedRect(poseStack, (int) wx - 4, (int) wy - 4, 8, 8, 1, 0xFF38BDF8, 0xFFFFFFFF);
                font.drawShadow(poseStack, wp.name, wx + 6, wy - 4, 0xFF38BDF8);
                String coords = String.format("(%.0f, %.0f)", wp.x, wp.z);
                font.drawShadow(poseStack, coords, wx + 6, wy + 6, 0xFF94A3B8);
            }
        }

        // 5. Render Player Position
        float pScreenX = (float) (centerX - offsetX * zoom);
        float pScreenY = (float) (centerY - offsetZ * zoom);
        if (pScreenX >= 0 && pScreenX <= width && pScreenY >= 30 && pScreenY <= height - 30) {
            RenderUtils.drawBorderedRect(poseStack, (int) pScreenX - 4, (int) pScreenY - 4, 8, 8, 1, 0xFF10B981, 0xFFFFFFFF);
            double rad = Math.toRadians(mc.player.yRot + 90.0f);
            float needleX = (float) (pScreenX + Math.cos(rad) * 12.0f);
            float needleY = (float) (pScreenY + Math.sin(rad) * 12.0f);
            RenderUtils.drawLine(poseStack, pScreenX, pScreenY, needleX, needleY, 2.0f, 0xFF10B981);
        }

        // 6. Header & Controls UI
        RenderUtils.drawRect(poseStack, 0, 0, width, 28, 0xEE0F172A);
        RenderUtils.drawRect(poseStack, 0, 27, width, 1, 0xFF334155);
        font.drawShadow(poseStack, "★ ASTRA БОЛЬШАЯ КАРТА МИРА", 12, 10, accent);

        String info = String.format("Координаты: %.0f, %.0f, %.0f | Зум: %.1fx",
                mc.player.getX(), mc.player.getY(), mc.player.getZ(), zoom);
        font.drawShadow(poseStack, info, width - font.width(info) - 12, 10, 0xFF94A3B8);

        // Footer Help
        RenderUtils.drawRect(poseStack, 0, height - 24, width, 24, 0xEE0F172A);
        RenderUtils.drawRect(poseStack, 0, height - 24, width, 1, 0xFF334155);
        String help = "ЛКМ + Перетаскивание: Смещение карты | Колесо мыши: Зум | Двойной клик: Поставить точку | ПКМ по точке: Удалить";
        font.drawShadow(poseStack, help, 12, height - 16, 0xFF64748B);

        super.render(poseStack, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            isDragging = true;
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            return true;
        } else if (button == 1) { // Right click on waypoint to remove
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                int centerX = width / 2;
                int centerY = height / 2;
                String curDim = "";
                try { curDim = mc.level.dimension().location().getPath(); } catch (Exception ignored) {}

                for (WaypointManager.Waypoint wp : WaypointManager.getAll()) {
                    if (!curDim.isEmpty() && !curDim.equalsIgnoreCase(wp.dim)) continue;
                    double dx = wp.x - (mc.player.getX() + offsetX);
                    double dz = wp.z - (mc.player.getZ() + offsetZ);
                    float wx = (float) (centerX + dx * zoom);
                    float wy = (float) (centerY + dz * zoom);

                    if (Math.abs(mouseX - wx) <= 10 && Math.abs(mouseY - wy) <= 10) {
                        // Remove this waypoint
                        List<WaypointManager.Waypoint> all = new java.util.ArrayList<>(WaypointManager.getAll());
                        all.remove(wp);
                        WaypointManager.clear();
                        for (WaypointManager.Waypoint w : all) WaypointManager.add(w);
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            isDragging = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isDragging && button == 0) {
            offsetX -= (mouseX - lastMouseX) / zoom;
            offsetZ -= (mouseY - lastMouseY) / zoom;
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (delta > 0) {
            zoom = Math.min(3.0, zoom + 0.2);
        } else if (delta < 0) {
            zoom = Math.max(0.4, zoom - 0.2);
        }
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_M) {
            onClose();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_R) { // Reset view to player
            offsetX = 0;
            offsetZ = 0;
            zoom = 1.0;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
