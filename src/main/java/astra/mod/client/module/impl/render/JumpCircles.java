package astra.mod.client.module.impl.render;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.BooleanSetting;
import astra.mod.client.setting.ModeSetting;
import astra.mod.client.setting.NumberSetting;
import astra.mod.client.theme.ThemeManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JumpCircles extends Module {
    public static JumpCircles INSTANCE;

    public final ModeSetting colorMode = new ModeSetting("Цвет", "Цветовая палитра кругов", "Accent", "Accent", "Rainbow", "Cyan", "Crimson");
    public final NumberSetting maxRadius = new NumberSetting("Радиус", "Максимальный радиус круга", 1.8, 0.8, 3.5, 0.2);
    public final NumberSetting speed = new NumberSetting("Скорость", "Скорость расширения кругов", 1.5, 0.5, 3.0, 0.1);
    public final BooleanSetting jump = new BooleanSetting("При прыжке", "Создавать круг при прыжке", true);
    public final BooleanSetting land = new BooleanSetting("При приземлении", "Создавать круг при приземлении", true);

    public static class Circle {
        public final Vec3 pos;
        public float progress = 0.0f; // 0.0 to 1.0

        public Circle(Vec3 pos) {
            this.pos = pos;
        }
    }

    private final List<Circle> circles = new ArrayList<>();
    private boolean wasOnGround = true;

    public JumpCircles() {
        super("JumpCircles", "Эффектные расширяющиеся светящиеся 3D кольца под ногами при прыжке и приземлении", Category.RENDER);
        INSTANCE = this;
        addSettings(colorMode, maxRadius, speed, jump, land);
        setEnabled(true);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        boolean onGround = mc.player.isOnGround();

        // Detect jump
        if (jump.getValue() && wasOnGround && !onGround && mc.player.getDeltaMovement().y > 0.0) {
            circles.add(new Circle(new Vec3(mc.player.getX(), mc.player.getY(), mc.player.getZ())));
        }

        // Detect land
        if (land.getValue() && !wasOnGround && onGround && mc.player.fallDistance > 0.5f) {
            circles.add(new Circle(new Vec3(mc.player.getX(), mc.player.getY(), mc.player.getZ())));
        }

        wasOnGround = onGround;

        // Update circles
        float spd = speed.getValue().floatValue() * 0.035f;
        Iterator<Circle> it = circles.iterator();
        while (it.hasNext()) {
            Circle c = it.next();
            c.progress += spd;
            if (c.progress >= 1.0f) {
                it.remove();
            }
        }
    }

    public void render3D(PoseStack poseStack, Camera camera, float tickDelta) {
        if (!isEnabled() || circles.isEmpty()) return;

        Vec3 camPos = camera.getPosition();
        Matrix4f mat = poseStack.last().pose();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();

        int accent = ThemeManager.INSTANCE.getAccent();
        float baseR = ((accent >> 16) & 0xFF) / 255.0f;
        float baseG = ((accent >> 8) & 0xFF) / 255.0f;
        float baseB = (accent & 0xFF) / 255.0f;

        float maxR = maxRadius.getValue().floatValue();
        int segments = 32;

        for (Circle c : circles) {
            float curRadius = c.progress * maxR;
            float alpha = (1.0f - c.progress) * 0.85f;

            float r = baseR;
            float g = baseG;
            float b = baseB;

            if (colorMode.is("Rainbow")) {
                int col = ThemeManager.getRainbow((int) (c.progress * 300));
                r = ((col >> 16) & 0xFF) / 255.0f;
                g = ((col >> 8) & 0xFF) / 255.0f;
                b = (col & 0xFF) / 255.0f;
            } else if (colorMode.is("Cyan")) {
                r = 0.05f; g = 0.85f; b = 0.95f;
            } else if (colorMode.is("Crimson")) {
                r = 0.95f; g = 0.15f; b = 0.25f;
            }

            float cx = (float) (c.pos.x - camPos.x);
            float cy = (float) (c.pos.y - camPos.y + 0.02);
            float cz = (float) (c.pos.z - camPos.z);

            buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
            for (int i = 0; i <= segments; i++) {
                double angle = 2.0 * Math.PI * i / segments;
                float x = cx + (float) (Math.cos(angle) * curRadius);
                float z = cz + (float) (Math.sin(angle) * curRadius);
                buffer.vertex(mat, x, cy, z).color(r, g, b, alpha).endVertex();
            }
            buffer.end();
            com.mojang.blaze3d.vertex.BufferUploader.end(buffer);
        }

        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }
}
