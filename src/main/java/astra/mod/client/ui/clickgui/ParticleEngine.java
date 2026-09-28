package astra.mod.client.ui.clickgui;

import astra.mod.client.theme.ThemeManager;
import astra.mod.client.util.RenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ParticleEngine {
    public static final ParticleEngine INSTANCE = new ParticleEngine();

    private final List<Particle> particles = new ArrayList<>();
    private final Random random = new Random();
    private int width;
    private int height;

    private static class Particle {
        float x, y;
        float vx, vy;
        float size;
        float alpha;

        Particle(float x, float y, float vx, float vy, float size, float alpha) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.size = size;
            this.alpha = alpha;
        }
    }

    public void init(int screenWidth, int screenHeight) {
        this.width = screenWidth;
        this.height = screenHeight;
        particles.clear();
        int count = Math.min(80, (screenWidth * screenHeight) / 10000);
        for (int i = 0; i < count; i++) {
            particles.add(new Particle(
                    random.nextFloat() * screenWidth,
                    random.nextFloat() * screenHeight,
                    (random.nextFloat() - 0.5f) * 0.6f,
                    (random.nextFloat() - 0.5f) * 0.6f,
                    random.nextFloat() * 2.5f + 1.0f,
                    random.nextFloat() * 0.6f + 0.2f
            ));
        }
    }

    public void render(PoseStack poseStack) {
        if (!ThemeManager.INSTANCE.isParticles()) return;

        int accent = ThemeManager.INSTANCE.getAccent() & 0x00FFFFFF;

        for (Particle p : particles) {
            p.x += p.vx;
            p.y += p.vy;

            if (p.x < 0) p.x = width;
            if (p.x > width) p.x = 0;
            if (p.y < 0) p.y = height;
            if (p.y > height) p.y = 0;

            int a = (int) (p.alpha * 255.0f);
            int color = (a << 24) | accent;

            int s = (int) Math.max(1, p.size);
            RenderUtils.drawRect(poseStack, (int) p.x, (int) p.y, s, s, color);
        }
    }
}
