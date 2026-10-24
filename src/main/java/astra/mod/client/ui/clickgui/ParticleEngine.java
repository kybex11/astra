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
        int count = Math.max(80, Math.min(150, (screenWidth * screenHeight) / 3500));
        for (int i = 0; i < count; i++) {
            particles.add(new Particle(
                    random.nextFloat() * screenWidth,
                    random.nextFloat() * screenHeight,
                    (random.nextFloat() - 0.5f) * 0.9f,
                    (random.nextFloat() - 0.5f) * 0.9f,
                    random.nextFloat() * 2.2f + 1.2f,
                    random.nextFloat() * 0.6f + 0.35f
            ));
        }
    }

    public void render(PoseStack poseStack) {
        renderWithMouse(poseStack, -100, -100);
    }

    public void renderWithMouse(PoseStack poseStack, int mouseX, int mouseY) {
        if (!ThemeManager.INSTANCE.isParticles()) return;

        int accent = ThemeManager.INSTANCE.getAccent() & 0x00FFFFFF;
        float maxDist = 75.0f;
        float maxDistSq = maxDist * maxDist;

        // 1. Update and connect particles
        int size = particles.size();
        for (int i = 0; i < size; i++) {
            Particle p1 = particles.get(i);
            p1.x += p1.vx;
            p1.y += p1.vy;

            if (p1.x < -10) p1.x = width + 10;
            if (p1.x > width + 10) p1.x = -10;
            if (p1.y < -10) p1.y = height + 10;
            if (p1.y > height + 10) p1.y = -10;

            // Connect to neighboring particles (Neural Network Constellation)
            for (int j = i + 1; j < size; j++) {
                Particle p2 = particles.get(j);
                float dx = p1.x - p2.x;
                float dy = p1.y - p2.y;
                float distSq = dx * dx + dy * dy;

                if (distSq < maxDistSq) {
                    float dist = (float) Math.sqrt(distSq);
                    float factor = 1.0f - (dist / maxDist);
                    int lineAlpha = (int) (factor * Math.min(p1.alpha, p2.alpha) * 160.0f);
                    if (lineAlpha > 3) {
                        int lineColor = (lineAlpha << 24) | accent;
                        RenderUtils.drawLine(poseStack, p1.x, p1.y, p2.x, p2.y, 1.0f, lineColor);
                    }
                }
            }

            // Connect to mouse cursor
            if (mouseX >= 0 && mouseY >= 0) {
                float mdx = p1.x - mouseX;
                float mdy = p1.y - mouseY;
                float mDistSq = mdx * mdx + mdy * mdy;
                float mouseMaxDist = 80.0f;
                if (mDistSq < mouseMaxDist * mouseMaxDist) {
                    float dist = (float) Math.sqrt(mDistSq);
                    float factor = 1.0f - (dist / mouseMaxDist);
                    int lineAlpha = (int) (factor * 200.0f);
                    int lineColor = (lineAlpha << 24) | accent;
                    RenderUtils.drawLine(poseStack, p1.x, p1.y, (float) mouseX, (float) mouseY, 1.2f, lineColor);
                }
            }

            // Draw particle glowing dot
            int a = (int) (p1.alpha * 255.0f);
            int color = (a << 24) | 0x00FFFFFF;
            RenderUtils.drawCircle(poseStack, p1.x, p1.y, p1.size, (a << 24) | accent);
        }
    }
}

