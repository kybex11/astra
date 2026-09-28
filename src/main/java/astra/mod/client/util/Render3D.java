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
import com.mojang.math.Vector3f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
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

        poseStack.pushPose();
        // Un-rotate camera rotation so matrix is purely world-aligned relative to camera eye
        poseStack.mulPose(Vector3f.YP.rotationDegrees(-camera.getYRot() - 180.0F));
        poseStack.mulPose(Vector3f.XP.rotationDegrees(-camera.getXRot()));
        Matrix4f worldMatrix = poseStack.last().pose();

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
                drawBoxLines(buffer, worldMatrix, minX, minY, minZ, maxX, maxY, maxZ, r, g, b, 0.85f);
                buffer.end();
                BufferUploader.end(buffer);
            }

            // 2. Draw Tracers (if enabled) - originates directly from center/crosshair (0,0,0)
            if (esp.tracers.getValue()) {
                buffer.begin(1, DefaultVertexFormat.POSITION_COLOR);
                buffer.vertex(worldMatrix, 0.0f, 0.0f, 0.0f).color(r, g, b, 0.7f).endVertex();
                buffer.vertex(worldMatrix, (float) (lerpX - camPos.x), (float) (lerpY + h * 0.5f - camPos.y), (float) (lerpZ - camPos.z)).color(r, g, b, 0.7f).endVertex();
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

                float hpR = Math.max(0.0f, Math.min(1.0f, (1.0f - hpPercent) * 2.0f));
                float hpG = Math.max(0.0f, Math.min(1.0f, hpPercent * 2.0f));
                float hpB = 0.15f;

                buffer.begin(1, DefaultVertexFormat.POSITION_COLOR);
                // Health bar background line
                buffer.vertex(worldMatrix, barX, minY, minZ).color(0.1f, 0.1f, 0.1f, 0.7f).endVertex();
                buffer.vertex(worldMatrix, barX, maxY, minZ).color(0.1f, 0.1f, 0.1f, 0.7f).endVertex();
                // Active health line
                buffer.vertex(worldMatrix, barX, minY, minZ).color(hpR, hpG, hpB, 0.95f).endVertex();
                buffer.vertex(worldMatrix, barX, minY + barH, minZ).color(hpR, hpG, hpB, 0.95f).endVertex();
                buffer.end();
                BufferUploader.end(buffer);
            }
        }

        poseStack.popPose();

        RenderSystem.enableDepthTest();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    private static void drawBoxLines(BufferBuilder buffer, Matrix4f m, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float r, float g, float b, float a) {
        // Bottom rect
        buffer.vertex(m, minX, minY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(m, maxX, minY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(m, maxX, minY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(m, maxX, minY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(m, maxX, minY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(m, minX, minY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(m, minX, minY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(m, minX, minY, minZ).color(r, g, b, a).endVertex();

        // Top rect
        buffer.vertex(m, minX, maxY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(m, maxX, maxY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(m, maxX, maxY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(m, maxX, maxY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(m, maxX, maxY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(m, minX, maxY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(m, minX, maxY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(m, minX, maxY, minZ).color(r, g, b, a).endVertex();

        // 4 Vertical Pillars
        buffer.vertex(m, minX, minY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(m, minX, maxY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(m, maxX, minY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(m, maxX, maxY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(m, maxX, minY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(m, maxX, maxY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(m, minX, minY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(m, minX, maxY, maxZ).color(r, g, b, a).endVertex();
    }

    private static double MthLerp(float delta, double start, double end) {
        return start + delta * (end - start);
    }
}
