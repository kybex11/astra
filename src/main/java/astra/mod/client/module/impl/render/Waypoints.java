package astra.mod.client.module.impl.render;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.BooleanSetting;
import astra.mod.client.setting.NumberSetting;
import astra.mod.client.theme.ThemeManager;
import astra.mod.client.waypoints.WaypointManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

public class Waypoints extends Module {
    public static Waypoints INSTANCE;

    public final NumberSetting maxDistance = new NumberSetting("Дистанция", "Дальность отображения точек", 256.0, 32.0, 1024.0, 16.0);
    public final BooleanSetting showBeam = new BooleanSetting("Луч", "Вертикальный луч до неба", true);
    public final BooleanSetting showLabel = new BooleanSetting("Название", "Название и дистанция над точкой", true);
    public final BooleanSetting clearAll = new BooleanSetting("Очистить все", "Удалить все точки (выкл. после очистки)", false);
    public final NumberSetting addKey = new NumberSetting("Клавиша +", "Клавиша добавления точки (код)", 66.0, 0.0, 350.0, 1.0);

    private long lastAddMs = 0L;
    private boolean wasKeyDown = false;

    public Waypoints() {
        super("Waypoints", "Путевые точки в мире с лучом и дистанцией", Category.RENDER);
        INSTANCE = this;
        addSettings(maxDistance, showBeam, showLabel, clearAll, addKey);
        setEnabled(true);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.screen != null) {
            wasKeyDown = false;
            return;
        }
        if (clearAll.getValue()) {
            WaypointManager.clear();
            clearAll.setValue(false);
        }
        int code = addKey.getValue().intValue();
        boolean down = code > 0 && GLFW.glfwGetKey(mc.getWindow().getWindow(), code) == GLFW.GLFW_PRESS;
        if (down && !wasKeyDown) {
            long now = System.currentTimeMillis();
            if (now - lastAddMs > 500) {
                lastAddMs = now;
                addCurrent();
            }
        }
        wasKeyDown = down;
    }

    private void addCurrent() {
        try {
            String dim = mc.level.dimension().location().getPath();
            int n = WaypointManager.getAll().size() + 1;
            WaypointManager.add(new WaypointManager.Waypoint(
                    "Point " + n, mc.player.getX(), mc.player.getY(), mc.player.getZ(), dim));
        } catch (Exception ignored) {}
    }

    public void render3D(PoseStack poseStack, Camera camera, float tickDelta) {
        if (!isEnabled()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        String curDim;
        try {
            curDim = mc.level.dimension().location().getPath();
        } catch (Exception e) {
            return;
        }

        Vec3 camPos = camera.getPosition();
        double maxDist = maxDistance.getValue();
        int accent = ThemeManager.INSTANCE.getAccent();
        float ar = ((accent >> 16) & 0xFF) / 255.0f;
        float ag = ((accent >> 8) & 0xFF) / 255.0f;
        float ab = (accent & 0xFF) / 255.0f;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        RenderSystem.disableDepthTest();

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        Matrix4f matrix = poseStack.last().pose();

        for (WaypointManager.Waypoint wp : WaypointManager.getAll()) {
            if (!curDim.equalsIgnoreCase(wp.dim)) continue;
            double dx = wp.x - camPos.x;
            double dy = wp.y - camPos.y;
            double dz = wp.z - camPos.z;
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (dist > maxDist || dist < 1.0) continue;

            float x = (float) dx;
            float y = (float) dy;
            float z = (float) dz;

            // Маркер-бокс 1x1x1
            buffer.begin(1, DefaultVertexFormat.POSITION_COLOR);
            drawBoxLines(buffer, matrix, x - 0.5f, y, z - 0.5f, x + 0.5f, y + 1.0f, z + 0.5f, ar, ag, ab, 0.9f);
            buffer.end();
            BufferUploader.end(buffer);

            // Луч вверх
            if (showBeam.getValue()) {
                buffer.begin(1, DefaultVertexFormat.POSITION_COLOR);
                buffer.vertex(matrix, x, y, z).color(ar, ag, ab, 0.5f).endVertex();
                buffer.vertex(matrix, x, y + 64.0f, z).color(ar, ag, ab, 0.05f).endVertex();
                buffer.end();
                BufferUploader.end(buffer);
            }

            // Подпись с дистанцией
            if (showLabel.getValue()) {
                drawLabel(poseStack, camera, mc, wp, x, y + 1.6f, z, (int) Math.round(dist));
            }
        }

        RenderSystem.enableDepthTest();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    private void drawLabel(PoseStack poseStack, Camera camera, Minecraft mc, WaypointManager.Waypoint wp, float x, float y, float z, int dist) {
        try {
            String text = wp.name + " §7[" + dist + "m]";
            Font font = mc.font;
            float textW = font.width(text);

            poseStack.pushPose();
            poseStack.translate(x, y, z);
            poseStack.mulPose(camera.rotation());
            float s = 0.025f;
            poseStack.scale(-s, -s, s);

            Matrix4f mat = poseStack.last().pose();
            MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
            font.drawInBatch(text, -textW / 2.0f, 0.0f, 0xFFFFFFFF, true, mat, buffers, false, 0, 15728880);
            buffers.endBatch();

            poseStack.popPose();
        } catch (Exception ignored) {}
    }

    private static void drawBoxLines(BufferBuilder buffer, Matrix4f mat, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float r, float g, float b, float a) {
        buffer.vertex(mat, minX, minY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(mat, maxX, minY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(mat, maxX, minY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(mat, maxX, minY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(mat, maxX, minY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(mat, minX, minY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(mat, minX, minY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(mat, minX, minY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(mat, minX, maxY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(mat, maxX, maxY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(mat, maxX, maxY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(mat, maxX, maxY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(mat, maxX, maxY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(mat, minX, maxY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(mat, minX, maxY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(mat, minX, maxY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(mat, minX, minY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(mat, minX, maxY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(mat, maxX, minY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(mat, maxX, maxY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(mat, maxX, minY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(mat, maxX, maxY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(mat, minX, minY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(mat, minX, maxY, maxZ).color(r, g, b, a).endVertex();
    }
}
