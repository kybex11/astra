package astra.mod.client.ui.shadcn;

import com.mojang.blaze3d.vertex.PoseStack;
import astra.mod.client.util.RenderUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ParticleSystem {
    private final List<Particle> particles = new ArrayList<>();
    private final Random random = new Random();
    private int width, height;

    public void init(int width, int height) {
        this.width = width;
        this.height = height;
        particles.clear();
        for (int i = 0; i < 80; i++) {
            particles.add(createParticle());
        }
    }

    private Particle createParticle() {
        return new Particle(
            random.nextFloat() * width,
            random.nextFloat() * height,
            (random.nextFloat() - 0.5f) * 0.5f,
            (random.nextFloat() - 0.5f) * 0.5f,
            random.nextFloat() * 2 + 1,
            random.nextInt(0x44) + 0x22
        );
    }

    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        if (particles.isEmpty()) return;

        for (Particle p : particles) {
            p.x += p.vx * delta * 60;
            p.y += p.vy * delta * 60;

            if (p.x < 0) p.x = width;
            if (p.x > width) p.x = 0;
            if (p.y < 0) p.y = height;
            if (p.y > height) p.y = 0;

            float dx = mouseX - p.x;
            float dy = mouseY - p.y;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);
            if (dist < 100) {
                p.x -= dx * 0.02f;
                p.y -= dy * 0.02f;
            }

            int alpha = (int) (p.alpha * (0.5f + 0.5f * Math.sin(System.currentTimeMillis() * 0.003 + p.x * 0.01)));
            int color = (alpha << 24) | (0x6366F1 & 0x00FFFFFF);

            RenderUtils.drawCircle(poseStack, p.x, p.y, p.size, color);
        }

        for (int i = 0; i < particles.size(); i++) {
            for (int j = i + 1; j < particles.size(); j++) {
                Particle a = particles.get(i);
                Particle b = particles.get(j);
                float dx = a.x - b.x;
                float dy = a.y - b.y;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                if (dist < 80) {
                    int alpha = (int) ((1 - dist / 80) * 0x22);
                    int color = (alpha << 24) | (0x6366F1 & 0x00FFFFFF);
                    RenderUtils.drawLine(poseStack, a.x, a.y, b.x, b.y, 1, color);
                }
            }
        }
    }

    private static class Particle {
        float x, y, vx, vy, size, alpha;

        Particle(float x, float y, float vx, float vy, float size, float alpha) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.size = size;
            this.alpha = alpha;
        }
    }
}
