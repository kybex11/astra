package astra.mod.client.util;

import astra.mod.client.module.impl.render.ESP;
import astra.mod.client.theme.ThemeManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class Render3D {

    public static void renderWorld(PoseStack poseStack, Camera camera, float tickDelta) {
        ESP esp = ESP.INSTANCE;
        if (esp == null || !esp.isEnabled()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        Vec3 camPos = camera.getPosition();
        Matrix4f matrix = poseStack.last().pose();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.lineWidth(1.5f);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();

        int accent = ThemeManager.INSTANCE.getAccent();
        float r = ((accent >> 16) & 0xFF) / 255.0f;
        float g = ((accent >> 8) & 0xFF) / 255.0f;
        float b = (accent & 0xFF) / 255.0f;

        for (Entity e : mc.level.entitiesForRendering()) {
            if (!(e instanceof LivingEntity) || e == mc.player) continue;
            LivingEntity living = (LivingEntity) e;
            if (!living.isAlive() || living.isDeadOrDying()) continue;

            if (!esp.shouldGlow(e)) continue;

            // Accurate bounding box tracked with interpolation
            net.minecraft.world.phys.AABB bb = e.getBoundingBox();
            double dx = 0.0;
            double dy = 0.0;
            double dz = 0.0;

            // Check for sudden teleport / spawn / riding anomalies
            if (Math.abs(e.getX() - e.xOld) < 8.0 && Math.abs(e.getY() - e.yOld) < 8.0 && Math.abs(e.getZ() - e.zOld) < 8.0) {
                dx = MthLerp(tickDelta, e.xOld, e.getX()) - e.getX();
                dy = MthLerp(tickDelta, e.yOld, e.getY()) - e.getY();
                dz = MthLerp(tickDelta, e.zOld, e.getZ()) - e.getZ();
            }

            net.minecraft.world.phys.AABB box = bb.move(dx - camPos.x, dy - camPos.y, dz - camPos.z);

            float minX = (float) box.minX;
            float maxX = (float) box.maxX;
            float minY = (float) box.minY;
            float maxY = (float) box.maxY;
            float minZ = (float) box.minZ;
            float maxZ = (float) box.maxZ;

            // 1. Draw 3D Box (if enabled)
            if (esp.box3d.getValue()) {
                buffer.begin(1, DefaultVertexFormat.POSITION_COLOR); // 1 = GL_LINES
                drawBoxLines(buffer, matrix, minX, minY, minZ, maxX, maxY, maxZ, r, g, b, 0.85f);
                buffer.end();
                BufferUploader.end(buffer);
            }

            // 2. Draw Tracers (if enabled)
            if (esp.tracers.getValue()) {
                float startX = 0.0f;
                float startY = 0.0f;
                float startZ = 0.0f;

                if (mc.options.getCameraType() != net.minecraft.client.CameraType.FIRST_PERSON && mc.player != null) {
                    startX = (float) (MthLerp(tickDelta, mc.player.xOld, mc.player.getX()) - camPos.x);
                    startY = (float) (MthLerp(tickDelta, mc.player.yOld, mc.player.getY()) + mc.player.getEyeHeight() * 0.5 - camPos.y);
                    startZ = (float) (MthLerp(tickDelta, mc.player.zOld, mc.player.getZ()) - camPos.z);
                }

                Vec3 center = box.getCenter();
                buffer.begin(1, DefaultVertexFormat.POSITION_COLOR);
                buffer.vertex(matrix, startX, startY, startZ).color(r, g, b, 0.7f).endVertex();
                buffer.vertex(matrix, (float) center.x, (float) center.y, (float) center.z).color(r, g, b, 0.7f).endVertex();
                buffer.end();
                BufferUploader.end(buffer);
            }

            // 3. Draw Health Bar along side of entity box
            if (esp.healthBar.getValue()) {
                float health = living.getHealth() + living.getAbsorptionAmount();
                float maxHealth = Math.max(1.0f, living.getMaxHealth());
                float hpPercent = Math.max(0.0f, Math.min(1.0f, health / maxHealth));

                float barX = minX - 0.06f;
                float barH = (maxY - minY) * hpPercent;

                buffer.begin(1, DefaultVertexFormat.POSITION_COLOR);
                // Background dark bar
                buffer.vertex(matrix, barX, minY, minZ).color(0.1f, 0.1f, 0.1f, 0.8f).endVertex();
                buffer.vertex(matrix, barX, maxY, minZ).color(0.1f, 0.1f, 0.1f, 0.8f).endVertex();

                // Colored health
                float hr = (1.0f - hpPercent);
                float hg = hpPercent;
                buffer.vertex(matrix, barX, minY, minZ).color(hr, hg, 0.1f, 1.0f).endVertex();
                buffer.vertex(matrix, barX, minY + barH, minZ).color(hr, hg, 0.1f, 1.0f).endVertex();
                buffer.end();
                BufferUploader.end(buffer);
            }
        }

        RenderSystem.lineWidth(1.0f);
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    private static void drawBoxLines(BufferBuilder buffer, Matrix4f mat, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float r, float g, float b, float a) {
        // Bottom square
        buffer.vertex(mat, minX, minY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(mat, maxX, minY, minZ).color(r, g, b, a).endVertex();

        buffer.vertex(mat, maxX, minY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(mat, maxX, minY, maxZ).color(r, g, b, a).endVertex();

        buffer.vertex(mat, maxX, minY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(mat, minX, minY, maxZ).color(r, g, b, a).endVertex();

        buffer.vertex(mat, minX, minY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(mat, minX, minY, minZ).color(r, g, b, a).endVertex();

        // Top square
        buffer.vertex(mat, minX, maxY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(mat, maxX, maxY, minZ).color(r, g, b, a).endVertex();

        buffer.vertex(mat, maxX, maxY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(mat, maxX, maxY, maxZ).color(r, g, b, a).endVertex();

        buffer.vertex(mat, maxX, maxY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(mat, minX, maxY, maxZ).color(r, g, b, a).endVertex();

        buffer.vertex(mat, minX, maxY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(mat, minX, maxY, minZ).color(r, g, b, a).endVertex();

        // Vertical pillars
        buffer.vertex(mat, minX, minY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(mat, minX, maxY, minZ).color(r, g, b, a).endVertex();

        buffer.vertex(mat, maxX, minY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(mat, maxX, maxY, minZ).color(r, g, b, a).endVertex();

        buffer.vertex(mat, maxX, minY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(mat, maxX, maxY, maxZ).color(r, g, b, a).endVertex();

        buffer.vertex(mat, minX, minY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(mat, minX, maxY, maxZ).color(r, g, b, a).endVertex();
    }

    private static double MthLerp(float delta, double start, double end) {
        return start + delta * (end - start);
    }
}
