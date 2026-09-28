package astra.mod.client.module.impl.render;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.ModeSetting;
import astra.mod.client.setting.NumberSetting;
import astra.mod.client.theme.ThemeManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class Trails extends Module {
    public static Trails INSTANCE;

    public final ModeSetting mode = new ModeSetting("Стиль", "Тип визуала шлейфа", "3D Ribbon", "3D Ribbon", "Neon Line", "Cosmic Strip");
    public final ModeSetting colorMode = new ModeSetting("Цвет", "Цветовая палитра", "Rainbow", "Rainbow", "Cosmic", "Accent", "Cyan Glow");
    public final NumberSetting length = new NumberSetting("Длина", "Длина шлейфа (точек)", 35.0, 10.0, 100.0, 5.0);
    public final NumberSetting height = new NumberSetting("Высота", "Высота ленты шлейфа", 0.6, 0.2, 1.8, 0.1);

    public static class TrailPoint {
        public final Vec3 pos;
        public final long time;

        public TrailPoint(Vec3 pos, long time) {
            this.pos = pos;
            this.time = time;
        }
    }

    private final List<TrailPoint> points = new ArrayList<>();

    public Trails() {
        super("Trails", "Эффектный светящийся 3D неоновый шлейф (3D Ribbon Trail) за персонажем", Category.RENDER);
        INSTANCE = this;
        addSettings(mode, colorMode, length, height);
        setEnabled(true);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null) return;

        Vec3 pos = mc.player.position();
        long now = System.currentTimeMillis();

        if (points.isEmpty() || points.get(points.size() - 1).pos.distanceTo(pos) > 0.04) {
            points.add(new TrailPoint(pos, now));
        }

        int maxLen = length.getValue().intValue();
        while (points.size() > maxLen) {
            points.remove(0);
        }

        points.removeIf(p -> now - p.time > 2000);
    }

    public void render3D(PoseStack poseStack, Camera camera, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        if (!isEnabled() || mc.player == null || mc.options.getCameraType().isFirstPerson()) return;

        Vec3 camPos = camera.getPosition();
        long now = System.currentTimeMillis();
        float ribbonH = height.getValue().floatValue();

        // Get player interpolated current root position
        double curX = net.minecraft.util.Mth.lerp(partialTicks, mc.player.xOld, mc.player.getX());
        double curY = net.minecraft.util.Mth.lerp(partialTicks, mc.player.yOld, mc.player.getY());
        double curZ = net.minecraft.util.Mth.lerp(partialTicks, mc.player.zOld, mc.player.getZ());
        Vec3 currentPos = new Vec3(curX, curY, curZ);

        List<Vec3> renderPoints = new ArrayList<>();
        for (TrailPoint p : points) {
            renderPoints.add(p.pos);
        }
        renderPoints.add(currentPos);

        if (renderPoints.size() < 2) return;

        poseStack.pushPose();
        // Un-rotate camera rotation so trail ribbon is anchored in real 3D world space
        poseStack.mulPose(com.mojang.math.Vector3f.YP.rotationDegrees(-camera.getYRot() - 180.0F));
        poseStack.mulPose(com.mojang.math.Vector3f.XP.rotationDegrees(-camera.getXRot()));
        com.mojang.math.Matrix4f worldMatrix = poseStack.last().pose();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();

        if (!mode.is("Neon Line")) {
            // Draw 3D glowing volumetric ribbon
            buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

            for (int i = 0; i < renderPoints.size(); i++) {
                Vec3 p = renderPoints.get(i);
                float progress = (float) i / (float) (renderPoints.size() - 1);
                float alpha = progress * 0.85f;

                int color = getColor(i, now);
                int r = (color >> 16) & 0xFF;
                int g = (color >> 8) & 0xFF;
                int b = color & 0xFF;
                int a = (int) (alpha * 255);

                // Calculate direction and 3D normal vector
                Vec3 next = (i < renderPoints.size() - 1) ? renderPoints.get(i + 1) : p;
                Vec3 prev = (i > 0) ? renderPoints.get(i - 1) : p;
                Vec3 dir = next.subtract(prev);
                if (dir.lengthSqr() < 0.0001) dir = new Vec3(0, 0, 1);
                else dir = dir.normalize();

                Vec3 up = new Vec3(0, 1, 0);
                Vec3 normal = dir.cross(up).normalize().scale(0.12);

                float px = (float) (p.x - camPos.x);
                float py = (float) (p.y - camPos.y + 0.15);
                float pz = (float) (p.z - camPos.z);

                buffer.vertex(worldMatrix, px - (float) normal.x, py, pz - (float) normal.z).color(r, g, b, (int) (a * 0.35)).endVertex();
                buffer.vertex(worldMatrix, px + (float) normal.x, py + ribbonH, pz + (float) normal.z).color(r, g, b, a).endVertex();
            }

            tesselator.end();
        }

        // Draw upper sharp glowing edge line
        GL11.glLineWidth(2.5f);
        buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i < renderPoints.size(); i++) {
            Vec3 p = renderPoints.get(i);
            float progress = (float) i / (float) (renderPoints.size() - 1);
            int color = getColor(i, now);
            int r = (color >> 16) & 0xFF;
            int g = (color >> 8) & 0xFF;
            int b = color & 0xFF;
            int a = (int) (progress * 255);

            float px = (float) (p.x - camPos.x);
            float py = (float) (p.y - camPos.y + 0.15 + (mode.is("Neon Line") ? 0.3 : ribbonH));
            float pz = (float) (p.z - camPos.z);

            buffer.vertex(worldMatrix, px, py, pz).color(r, g, b, a).endVertex();
        }
        tesselator.end();

        poseStack.popPose();

        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    private int getColor(int index, long time) {
        switch (colorMode.getValue()) {
            case "Rainbow":
                return ThemeManager.getRainbow(index * 60);
            case "Cosmic":
                float sin = (float) Math.sin((time + index * 60) * 0.003);
                int cr = (int) (140 + 100 * sin);
                int cg = 50;
                int cb = (int) (220 + 35 * Math.cos((time + index * 60) * 0.003));
                return (cr << 16) | (cg << 8) | cb;
            case "Cyan Glow":
                return 0x06B6D4;
            case "Accent":
            default:
                return ThemeManager.INSTANCE.getAccent();
        }
    }
}
