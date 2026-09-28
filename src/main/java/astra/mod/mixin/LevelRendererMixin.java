package astra.mod.mixin;

import astra.mod.client.module.impl.render.ESP;
import astra.mod.client.module.impl.render.Trails;
import astra.mod.client.module.impl.render.XRay;
import astra.mod.client.theme.ThemeManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @Inject(method = "renderLevel", at = @At("TAIL"))
    private void onRenderLevel(PoseStack poseStack, float partialTicks, long finishTimeNano, boolean drawBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projectionMatrix, CallbackInfo ci) {
        if (Trails.INSTANCE != null) {
            Trails.INSTANCE.render3D(poseStack, camera, partialTicks);
        }

        if (XRay.INSTANCE != null) {
            XRay.INSTANCE.render3D(poseStack, camera, partialTicks);
        }

        astra_renderTracers(poseStack, camera, partialTicks);
    }

    @Unique
    private void astra_renderTracers(PoseStack poseStack, Camera camera, float partialTicks) {
        ESP esp = ESP.INSTANCE;
        if (esp == null || !esp.isEnabled() || !esp.tracers.getValue()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        Vec3 camPos = camera.getPosition();

        // Start tracer from bottom/chest of player model
        double startX = Mth.lerp(partialTicks, mc.player.xOld, mc.player.getX()) - camPos.x;
        double startY = Mth.lerp(partialTicks, mc.player.yOld, mc.player.getY()) - camPos.y + 0.25;
        double startZ = Mth.lerp(partialTicks, mc.player.zOld, mc.player.getZ()) - camPos.z;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glLineWidth(1.8f);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(GL11.GL_LINES, DefaultVertexFormat.POSITION_COLOR);

        for (Entity e : mc.level.entitiesForRendering()) {
            if (e == mc.player || !(e instanceof Player)) continue;
            Player p = (Player) e;
            if (!p.isAlive()) continue;

            double targetX = Mth.lerp(partialTicks, p.xOld, p.getX()) - camPos.x;
            double targetY = Mth.lerp(partialTicks, p.yOld, p.getY()) - camPos.y + p.getEyeHeight() * 0.5;
            double targetZ = Mth.lerp(partialTicks, p.zOld, p.getZ()) - camPos.z;

            double dist = mc.player.distanceTo(p);
            int color;
            if (dist < 10.0) {
                color = 0xFFEF4444; // Red (close danger)
            } else if (dist < 25.0) {
                color = 0xFFF59E0B; // Yellow
            } else {
                color = ThemeManager.INSTANCE.getAccent();
            }

            int r = (color >> 16) & 0xFF;
            int g = (color >> 8) & 0xFF;
            int b = color & 0xFF;

            buffer.vertex(startX, startY, startZ).color(r, g, b, 200).endVertex();
            buffer.vertex(targetX, targetY, targetZ).color(r, g, b, 240).endVertex();
        }

        tesselator.end();

        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }
}
