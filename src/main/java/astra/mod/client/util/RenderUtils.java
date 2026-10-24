package astra.mod.client.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Matrix4f;
import net.minecraft.client.gui.GuiComponent;

public class RenderUtils {

    public static void drawRect(PoseStack poseStack, int x, int y, int width, int height, int color) {
        GuiComponent.fill(poseStack, x, y, x + width, y + height, color);
    }

    public static void drawBorder(PoseStack poseStack, int x, int y, int width, int height, int thickness, int color) {
        // Top
        drawRect(poseStack, x, y, width, thickness, color);
        // Bottom
        drawRect(poseStack, x, y + height - thickness, width, thickness, color);
        // Left
        drawRect(poseStack, x, y + thickness, thickness, height - (thickness * 2), color);
        // Right
        drawRect(poseStack, x + width - thickness, y + thickness, thickness, height - (thickness * 2), color);
    }

    public static void drawBorderedRect(PoseStack poseStack, int x, int y, int width, int height, int thickness, int bgColor, int borderColor) {
        drawRect(poseStack, x, y, width, height, bgColor);
        drawBorder(poseStack, x, y, width, height, thickness, borderColor);
    }

    public static void drawGradientV(PoseStack poseStack, int x, int y, int width, int height, int colorTop, int colorBottom) {
        float a1 = (float) (colorTop >> 24 & 255) / 255.0F;
        float r1 = (float) (colorTop >> 16 & 255) / 255.0F;
        float g1 = (float) (colorTop >> 8 & 255) / 255.0F;
        float b1 = (float) (colorTop & 255) / 255.0F;

        float a2 = (float) (colorBottom >> 24 & 255) / 255.0F;
        float r2 = (float) (colorBottom >> 16 & 255) / 255.0F;
        float g2 = (float) (colorBottom >> 8 & 255) / 255.0F;
        float b2 = (float) (colorBottom & 255) / 255.0F;

        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        Matrix4f matrix = poseStack.last().pose();

        bufferBuilder.begin(7, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(matrix, (float) (x + width), (float) y, 0.0F).color(r1, g1, b1, a1).endVertex();
        bufferBuilder.vertex(matrix, (float) x, (float) y, 0.0F).color(r1, g1, b1, a1).endVertex();
        bufferBuilder.vertex(matrix, (float) x, (float) (y + height), 0.0F).color(r2, g2, b2, a2).endVertex();
        bufferBuilder.vertex(matrix, (float) (x + width), (float) (y + height), 0.0F).color(r2, g2, b2, a2).endVertex();
        bufferBuilder.end();
        BufferUploader.end(bufferBuilder);

        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static void drawGradientH(PoseStack poseStack, int x, int y, int width, int height, int colorLeft, int colorRight) {
        float a1 = (float) (colorLeft >> 24 & 255) / 255.0F;
        float r1 = (float) (colorLeft >> 16 & 255) / 255.0F;
        float g1 = (float) (colorLeft >> 8 & 255) / 255.0F;
        float b1 = (float) (colorLeft & 255) / 255.0F;

        float a2 = (float) (colorRight >> 24 & 255) / 255.0F;
        float r2 = (float) (colorRight >> 16 & 255) / 255.0F;
        float g2 = (float) (colorRight >> 8 & 255) / 255.0F;
        float b2 = (float) (colorRight & 255) / 255.0F;

        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        Matrix4f matrix = poseStack.last().pose();

        bufferBuilder.begin(7, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(matrix, (float) (x + width), (float) y, 0.0F).color(r2, g2, b2, a2).endVertex();
        bufferBuilder.vertex(matrix, (float) x, (float) y, 0.0F).color(r1, g1, b1, a1).endVertex();
        bufferBuilder.vertex(matrix, (float) x, (float) (y + height), 0.0F).color(r1, g1, b1, a1).endVertex();
        bufferBuilder.vertex(matrix, (float) (x + width), (float) (y + height), 0.0F).color(r2, g2, b2, a2).endVertex();
        bufferBuilder.end();
        BufferUploader.end(bufferBuilder);

        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static void drawSwitch(PoseStack poseStack, int x, int y, int width, int height, boolean active) {
        int trackColor = active ? 0xFF6366F1 : 0xFF2D3342;
        int knobColor = 0xFFFFFFFF;
        int borderColor = active ? 0xFF818CF8 : 0xFF3E4759;

        // Background capsule
        drawBorderedRect(poseStack, x, y, width, height, 1, trackColor, borderColor);

        // Knob
        int knobSize = height - 4;
        int knobX = active ? (x + width - knobSize - 2) : (x + 2);
        int knobY = y + 2;
        drawRect(poseStack, knobX, knobY, knobSize, knobSize, knobColor);
    }

    public static void drawProgressBar(PoseStack poseStack, int x, int y, int width, int height, double progress, int bgColor, int fillColor) {
        drawRect(poseStack, x, y, width, height, bgColor);
        int fillWidth = (int) Math.round(width * Math.max(0.0, Math.min(1.0, progress)));
        if (fillWidth > 0) {
            drawGradientH(poseStack, x, y, fillWidth, height, fillColor, 0xFF818CF8);
        }
    }

    public static void drawLine(PoseStack poseStack, float x1, float y1, float x2, float y2, float lineWidth, int color) {
        float a = (float) (color >> 24 & 255) / 255.0F;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;

        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.lineWidth(lineWidth);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        Matrix4f matrix = poseStack.last().pose();

        buffer.begin(1, DefaultVertexFormat.POSITION_COLOR); // 1 = GL_LINES
        buffer.vertex(matrix, x1, y1, 0.0F).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x2, y2, 0.0F).color(r, g, b, a).endVertex();
        buffer.end();
        BufferUploader.end(buffer);

        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static void drawRoundedRect(PoseStack poseStack, float x, float y, float width, float height, float radius, int color) {
        float a = (float) (color >> 24 & 255) / 255.0F;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;

        radius = Math.max(0, Math.min(radius, Math.min(width / 2.0f, height / 2.0f)));

        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        Matrix4f matrix = poseStack.last().pose();

        buffer.begin(7, DefaultVertexFormat.POSITION_COLOR); // GL_QUADS

        // Center body
        buffer.vertex(matrix, x + radius, y, 0.0F).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + width - radius, y, 0.0F).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + width - radius, y + height, 0.0F).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + radius, y + height, 0.0F).color(r, g, b, a).endVertex();

        // Left body
        buffer.vertex(matrix, x, y + radius, 0.0F).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + radius, y + radius, 0.0F).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + radius, y + height - radius, 0.0F).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x, y + height - radius, 0.0F).color(r, g, b, a).endVertex();

        // Right body
        buffer.vertex(matrix, x + width - radius, y + radius, 0.0F).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + width, y + radius, 0.0F).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + width, y + height - radius, 0.0F).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + width - radius, y + height - radius, 0.0F).color(r, g, b, a).endVertex();

        buffer.end();
        BufferUploader.end(buffer);

        // Render rounded 4 corners with triangle fans
        drawCorner(matrix, buffer, x + radius, y + radius, radius, 180, 270, r, g, b, a);
        drawCorner(matrix, buffer, x + width - radius, y + radius, radius, 270, 360, r, g, b, a);
        drawCorner(matrix, buffer, x + width - radius, y + height - radius, radius, 0, 90, r, g, b, a);
        drawCorner(matrix, buffer, x + radius, y + height - radius, radius, 90, 180, r, g, b, a);

        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    private static void drawCorner(Matrix4f matrix, BufferBuilder buffer, float cx, float cy, float radius, int startAngle, int endAngle, float r, float g, float b, float a) {
        buffer.begin(6, DefaultVertexFormat.POSITION_COLOR); // GL_TRIANGLE_FAN
        buffer.vertex(matrix, cx, cy, 0.0F).color(r, g, b, a).endVertex();
        int step = 15;
        for (int angle = startAngle; angle <= endAngle; angle += step) {
            double rad = Math.toRadians(angle);
            float px = (float) (cx + Math.cos(rad) * radius);
            float py = (float) (cy + Math.sin(rad) * radius);
            buffer.vertex(matrix, px, py, 0.0F).color(r, g, b, a).endVertex();
        }
        buffer.end();
        BufferUploader.end(buffer);
    }

    public static void drawRoundedBorderedRect(PoseStack poseStack, float x, float y, float width, float height, float radius, float borderWidth, int bgColor, int borderColor) {
        drawRoundedRect(poseStack, x, y, width, height, radius, borderColor);
        drawRoundedRect(poseStack, x + borderWidth, y + borderWidth, width - borderWidth * 2, height - borderWidth * 2, Math.max(0, radius - borderWidth), bgColor);
    }

    public static void drawCircle(PoseStack poseStack, float cx, float cy, float radius, int color) {
        float a = (float) (color >> 24 & 255) / 255.0F;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;

        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        Matrix4f matrix = poseStack.last().pose();

        buffer.begin(6, DefaultVertexFormat.POSITION_COLOR); // GL_TRIANGLE_FAN
        buffer.vertex(matrix, cx, cy, 0.0F).color(r, g, b, a).endVertex();
        int step = 10;
        for (int angle = 0; angle <= 360; angle += step) {
            double rad = Math.toRadians(angle);
            float px = (float) (cx + Math.cos(rad) * radius);
            float py = (float) (cy + Math.sin(rad) * radius);
            buffer.vertex(matrix, px, py, 0.0F).color(r, g, b, a).endVertex();
        }
        buffer.end();
        BufferUploader.end(buffer);

        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }
}
