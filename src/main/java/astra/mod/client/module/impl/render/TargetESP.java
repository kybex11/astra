package astra.mod.client.module.impl.render;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.BooleanSetting;
import astra.mod.client.setting.NumberSetting;
import astra.mod.client.theme.ThemeManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.opengl.GL11;

public class TargetESP extends Module {
    public static TargetESP INSTANCE;

    public final NumberSetting radius = new NumberSetting("Радиус", "Радиус кольца под целью", 0.75, 0.4, 1.5, 0.05);
    public final NumberSetting height = new NumberSetting("Высота", "Высота кольца над землей", 0.1, 0.0, 2.0, 0.1);
    public final BooleanSetting pulse = new BooleanSetting("Пульсация", "Плавная анимация дыхания", true);

    private float rot = 0.0f;

    public TargetESP() {
        super("TargetESP", "Светящееся вращающееся 3D кольцо под текущей целью атаки", Category.RENDER);
        INSTANCE = this;
        addSettings(radius, height, pulse);
        setEnabled(true);
    }

    @Override
    public void onTick() {
        rot += 4.0f;
        if (rot >= 360.0f) rot -= 360.0f;
    }

    public void render3D(PoseStack poseStack, Camera camera, float tickDelta) {
        if (!isEnabled()) return;

        LivingEntity target = null;
        if (astra.mod.client.module.impl.combat.KillAura.INSTANCE != null && astra.mod.client.module.impl.combat.KillAura.INSTANCE.isEnabled()) {
            target = astra.mod.client.module.impl.combat.KillAura.INSTANCE.currentTarget;
        }
        if (target == null && mc.hitResult instanceof EntityHitResult) {
            Entity e = ((EntityHitResult) mc.hitResult).getEntity();
            if (e instanceof LivingEntity) target = (LivingEntity) e;
        }

        if (target == null || !target.isAlive() || target == mc.player) return;

        Vec3 camPos = camera.getPosition();
        Matrix4f mat = poseStack.last().pose();

        double tx = target.xOld + (target.getX() - target.xOld) * tickDelta - camPos.x;
        double ty = target.yOld + (target.getY() - target.yOld) * tickDelta - camPos.y + height.getValue();
        double tz = target.zOld + (target.getZ() - target.zOld) * tickDelta - camPos.z;

        float r = radius.getValue().floatValue();
        if (pulse.getValue()) {
            r += (float) (Math.sin((System.currentTimeMillis() % 2000) / 2000.0 * Math.PI * 2) * 0.08);
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();

        int accent = ThemeManager.INSTANCE.getAccent();
        float cr = ((accent >> 16) & 0xFF) / 255.0f;
        float cg = ((accent >> 8) & 0xFF) / 255.0f;
        float cb = (accent & 0xFF) / 255.0f;

        int segs = 36;
        buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i < segs; i++) {
            double angle = 2.0 * Math.PI * i / segs + Math.toRadians(rot);
            float px = (float) (tx + Math.cos(angle) * r);
            float pz = (float) (tz + Math.sin(angle) * r);
            buffer.vertex(mat, px, (float) ty, pz).color(cr, cg, cb, 0.9f).endVertex();
        }
        buffer.end();
        com.mojang.blaze3d.vertex.BufferUploader.end(buffer);

        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }
}
