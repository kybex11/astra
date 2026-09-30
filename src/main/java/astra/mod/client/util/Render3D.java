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

            // Exact lerped position offset from camera
            double lerpX = MthLerp(tickDelta, e.xOld, e.getX());
            double lerpY = MthLerp(tickDelta, e.yOld, e.getY());
            double lerpZ = MthLerp(tickDelta, e.zOld, e.getZ());

            float w = e.getBbWidth() / 2.0f;
            float h = e.getBbHeight();

            float minX = (float) (lerpX - w - camPos.x);
            float maxX = (float) (lerpX + w - camPos.x);
            float minY = (float) (lerpY - camPos.y);
            float maxY = (float) (lerpY + h - camPos.y);
            float minZ = (float) (lerpZ - w - camPos.z);
            float maxZ = (float) (lerpZ + w - camPos.z);

            // 1. Draw 3D Box (if enabled)
            if (esp.box3d.getValue()) {
                buffer.begin(1, DefaultVertexFormat.POSITION_COLOR); // 1 = GL_LINES
                drawBoxLines(buffer, matrix, minX, minY, minZ, maxX, maxY, maxZ, r, g, b, 0.85f);
                buffer.end();
                BufferUploader.end(buffer);
            }

            // 2. Draw Tracers (if enabled)
            if (esp.tracers.getValue()) {
                double playerX = MthLerp(tickDelta, mc.player.xOld, mc.player.getX()) - camPos.x;
                double playerY = MthLerp(tickDelta, mc.player.yOld, mc.player.getY()) - camPos.y + mc.player.getEyeHeight() * 0.5;
                double playerZ = MthLerp(tickDelta, mc.player.zOld, mc.player.getZ()) - camPos.z;

                buffer.begin(1, DefaultVertexFormat.POSITION_COLOR);
                buffer.vertex(matrix, (float) playerX, (float) playerY, (float) playerZ).color(r, g, b, 0.7f).endVertex();
                buffer.vertex(matrix, (float) (lerpX - camPos.x), (float) (lerpY + h * 0.5f - camPos.y), (float) (lerpZ - camPos.z)).color(r, g, b, 0.7f).endVertex();
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
