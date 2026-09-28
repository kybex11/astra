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
}
