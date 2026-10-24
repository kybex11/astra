package astra.mod.client.cosmetics;

import astra.mod.client.theme.ThemeManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;

public class CosmeticRenderLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    public CosmeticRenderLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player,
                       float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        CosmeticManager cm = CosmeticManager.INSTANCE;
        if (player.isInvisible()) return;

        int accent = ThemeManager.INSTANCE.getAccent();
        float r = ((accent >> 16) & 0xFF) / 255.0f;
        float g = ((accent >> 8) & 0xFF) / 255.0f;
        float b = (accent & 0xFF) / 255.0f;

        VertexConsumer lines = buffer.getBuffer(RenderType.lines());

        // 1. 3D Floating Glowing Halo
        if (cm.isHaloEnabled()) {
            poseStack.pushPose();
            getParentModel().head.translateAndRotate(poseStack);

            // Floating bobbing animation
            double bob = Math.sin((ageInTicks + partialTicks) * 0.1) * 0.04;
            poseStack.translate(0.0, -0.65 + bob, 0.0);
            poseStack.mulPose(Vector3f.XP.rotationDegrees(90.0f));

            Matrix4f mat = poseStack.last().pose();
            int segs = 24;
            float radius = 0.35f;

            for (int i = 0; i < segs; i++) {
                double a1 = 2.0 * Math.PI * i / segs;
                double a2 = 2.0 * Math.PI * (i + 1) / segs;
                float x1 = (float) (Math.cos(a1) * radius);
                float y1 = (float) (Math.sin(a1) * radius);
                float x2 = (float) (Math.cos(a2) * radius);
                float y2 = (float) (Math.sin(a2) * radius);

                lines.vertex(mat, x1, y1, 0.0f).color(r, g, b, 0.9f).normal(poseStack.last().normal(), 0, 1, 0).endVertex();
                lines.vertex(mat, x2, y2, 0.0f).color(r, g, b, 0.9f).normal(poseStack.last().normal(), 0, 1, 0).endVertex();
            }

            poseStack.popPose();
        }

        // 2. 3D Animated Flapping Wings
        if (cm.isWingsEnabled()) {
            poseStack.pushPose();
            getParentModel().body.translateAndRotate(poseStack);
            poseStack.translate(0.0, 0.12, 0.16);

            float flap = (float) Math.sin((ageInTicks + partialTicks) * 0.18f) * 22.0f;

            // Left Wing
            poseStack.pushPose();
            poseStack.mulPose(Vector3f.YP.rotationDegrees(-flap - 18.0f));
            drawWing(poseStack, lines, r, g, b, false);
            poseStack.popPose();

            // Right Wing
            poseStack.pushPose();
            poseStack.mulPose(Vector3f.YP.rotationDegrees(flap + 18.0f));
            drawWing(poseStack, lines, r, g, b, true);
            poseStack.popPose();

            poseStack.popPose();
        }

        // 3. 3D Devil Horns
        if (cm.isHornsEnabled()) {
            poseStack.pushPose();
            getParentModel().head.translateAndRotate(poseStack);

            Matrix4f mat = poseStack.last().pose();
            // Left Horn
            lines.vertex(mat, -0.22f, -0.55f, -0.05f).color(0.95f, 0.15f, 0.2f, 1.0f).normal(poseStack.last().normal(), 0, 1, 0).endVertex();
            lines.vertex(mat, -0.32f, -0.75f, 0.05f).color(0.95f, 0.15f, 0.2f, 1.0f).normal(poseStack.last().normal(), 0, 1, 0).endVertex();

            // Right Horn
            lines.vertex(mat, 0.22f, -0.55f, -0.05f).color(0.95f, 0.15f, 0.2f, 1.0f).normal(poseStack.last().normal(), 0, 1, 0).endVertex();
            lines.vertex(mat, 0.32f, -0.75f, 0.05f).color(0.95f, 0.15f, 0.2f, 1.0f).normal(poseStack.last().normal(), 0, 1, 0).endVertex();

            poseStack.popPose();
        }
    }

    private void drawWing(PoseStack poseStack, VertexConsumer buffer, float r, float g, float b, boolean mirrored) {
        Matrix4f mat = poseStack.last().pose();
        float sign = mirrored ? -1.0f : 1.0f;

        float ox = 0.0f;
        float oy = 0.0f;
        float oz = 0.0f;

        float p1x = sign * 0.45f;
        float p1y = -0.4f;
        float p1z = 0.15f;

        float p2x = sign * 0.75f;
        float p2y = -0.2f;
        float p2z = 0.25f;

        float p3x = sign * 0.55f;
        float p3y = 0.25f;
        float p3z = 0.18f;

        // Wing wireframe outline and struts
        buffer.vertex(mat, ox, oy, oz).color(r, g, b, 0.95f).normal(poseStack.last().normal(), 0, 1, 0).endVertex();
        buffer.vertex(mat, p1x, p1y, p1z).color(r, g, b, 0.95f).normal(poseStack.last().normal(), 0, 1, 0).endVertex();

        buffer.vertex(mat, p1x, p1y, p1z).color(r, g, b, 0.95f).normal(poseStack.last().normal(), 0, 1, 0).endVertex();
        buffer.vertex(mat, p2x, p2y, p2z).color(r, g, b, 0.95f).normal(poseStack.last().normal(), 0, 1, 0).endVertex();

        buffer.vertex(mat, p2x, p2y, p2z).color(r, g, b, 0.95f).normal(poseStack.last().normal(), 0, 1, 0).endVertex();
        buffer.vertex(mat, p3x, p3y, p3z).color(r, g, b, 0.95f).normal(poseStack.last().normal(), 0, 1, 0).endVertex();

        buffer.vertex(mat, p3x, p3y, p3z).color(r, g, b, 0.95f).normal(poseStack.last().normal(), 0, 1, 0).endVertex();
        buffer.vertex(mat, ox, oy, oz).color(r, g, b, 0.95f).normal(poseStack.last().normal(), 0, 1, 0).endVertex();

        // Inner ribs
        buffer.vertex(mat, ox, oy, oz).color(r, g, b, 0.75f).normal(poseStack.last().normal(), 0, 1, 0).endVertex();
        buffer.vertex(mat, p2x, p2y, p2z).color(r, g, b, 0.75f).normal(poseStack.last().normal(), 0, 1, 0).endVertex();
    }
}
