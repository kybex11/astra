package astra.mod.client.cosmetics;

import astra.mod.client.theme.ThemeManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;

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
        float ar = ((accent >> 16) & 0xFF) / 255.0f;
        float ag = ((accent >> 8) & 0xFF) / 255.0f;
        float ab = (accent & 0xFF) / 255.0f;

        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(CosmeticManager.ASTRA_CAPE));

        // 1. 3D Modern Minecraft Floating Halo (Volumetric 8-segment gold voxel ring)
        if (cm.isHaloEnabled()) {
            poseStack.pushPose();
            getParentModel().head.translateAndRotate(poseStack);

            // Floating bobbing & gentle rotation
            double bob = Math.sin((ageInTicks + partialTicks) * 0.08) * 0.04;
            float rotY = (ageInTicks + partialTicks) * 1.5f;

            poseStack.translate(0.0, -0.62 + bob, 0.0);
            poseStack.mulPose(Vector3f.YP.rotationDegrees(rotY));

            int segments = 8;
            float radius = 0.32f;
            float voxelThickness = 0.035f;

            for (int i = 0; i < segments; i++) {
                poseStack.pushPose();
                float angle = i * (360.0f / segments);
                poseStack.mulPose(Vector3f.YP.rotationDegrees(angle));
                poseStack.translate(radius, 0.0, 0.0);

                // Gold voxel block with glow
                drawVoxelBox(poseStack, vc, -voxelThickness, -voxelThickness, -0.06f,
                        voxelThickness, voxelThickness, 0.06f,
                        1.0f, 0.84f, 0.12f, 0.95f, packedLight);
                poseStack.popPose();
            }

            poseStack.popPose();
        }

        // 2. 3D Modern Minecraft Voxel Dragon Wings (Volumetric blocky bones & membranes)
        if (cm.isWingsEnabled()) {
            poseStack.pushPose();
            getParentModel().body.translateAndRotate(poseStack);
            poseStack.translate(0.0, 0.1, 0.14);

            float flap = (float) Math.sin((ageInTicks + partialTicks) * 0.16f) * 24.0f;

            // Left Wing
            poseStack.pushPose();
            poseStack.mulPose(Vector3f.YP.rotationDegrees(-flap - 16.0f));
            drawMinecraftWing(poseStack, vc, ar, ag, ab, false, packedLight);
            poseStack.popPose();

            // Right Wing
            poseStack.pushPose();
            poseStack.mulPose(Vector3f.YP.rotationDegrees(flap + 16.0f));
            drawMinecraftWing(poseStack, vc, ar, ag, ab, true, packedLight);
            poseStack.popPose();

            poseStack.popPose();
        }

        // 3. 3D Modern Minecraft Voxel Demon Horns (3-tier curved volumetric horns)
        if (cm.isHornsEnabled()) {
            poseStack.pushPose();
            getParentModel().head.translateAndRotate(poseStack);

            // Left Horn
            drawMinecraftHorn(poseStack, vc, -0.22f, packedLight);
            // Right Horn
            drawMinecraftHorn(poseStack, vc, 0.22f, packedLight);

            poseStack.popPose();
        }
    }

    private void drawMinecraftWing(PoseStack poseStack, VertexConsumer vc, float ar, float ag, float ab, boolean mirrored, int light) {
        float sign = mirrored ? -1.0f : 1.0f;

        float boneR = 0.12f;
        float boneG = 0.14f;
        float boneB = 0.20f;

        // 1. Shoulder Bone Cube (Volumetric 3D)
        drawVoxelBox(poseStack, vc, -0.04f, -0.04f, -0.04f, sign * 0.26f, 0.04f, 0.04f, boneR, boneG, boneB, 1.0f, light);

        // 2. Primary Arm Bone Cube (angles up and backwards)
        poseStack.pushPose();
        poseStack.translate(sign * 0.24f, 0.0, 0.0);
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(sign * -22.0f));
        poseStack.mulPose(Vector3f.XP.rotationDegrees(12.0f));

        drawVoxelBox(poseStack, vc, 0.0f, -0.035f, -0.035f, sign * 0.38f, 0.035f, 0.035f, boneR, boneG, boneB, 1.0f, light);

        // Bone Claw / Joint Spike
        drawVoxelBox(poseStack, vc, sign * 0.35f, -0.06f, -0.02f, sign * 0.40f, 0.08f, 0.02f, 0.85f, 0.15f, 0.25f, 1.0f, light);

        // 3. Outer Finger Bone
        poseStack.pushPose();
        poseStack.translate(sign * 0.36f, 0.0, 0.0);
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(sign * 48.0f));
        drawVoxelBox(poseStack, vc, 0.0f, -0.025f, -0.025f, sign * 0.44f, 0.025f, 0.025f, boneR, boneG, boneB, 1.0f, light);
        poseStack.popPose();

        // 4. Volumetric Wing Membranes (Thin 3D solid Minecraft slabs with accent glow)
        drawVoxelBox(poseStack, vc, 0.0f, 0.04f, -0.01f, sign * 0.35f, 0.36f, 0.01f, ar, ag, ab, 0.85f, light);
        drawVoxelBox(poseStack, vc, sign * 0.12f, 0.36f, -0.01f, sign * 0.45f, 0.62f, 0.01f, ar * 0.85f, ag * 0.85f, ab * 0.85f, 0.80f, light);

        poseStack.popPose();
    }

    private void drawMinecraftHorn(PoseStack poseStack, VertexConsumer vc, float xOffset, int light) {
        poseStack.pushPose();
        poseStack.translate(xOffset, -0.45, -0.08);

        float sign = (xOffset < 0) ? -1.0f : 1.0f;
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(sign * -18.0f));
        poseStack.mulPose(Vector3f.XP.rotationDegrees(-15.0f));

        // Tier 1: Base block (Obsidian crimson)
        drawVoxelBox(poseStack, vc, -0.045f, -0.07f, -0.045f, 0.045f, 0.0f, 0.045f, 0.22f, 0.05f, 0.08f, 1.0f, light);

        // Tier 2: Mid block (Crimson glow, angled back)
        poseStack.translate(0.0, -0.065, 0.02);
        poseStack.mulPose(Vector3f.XP.rotationDegrees(-18.0f));
        drawVoxelBox(poseStack, vc, -0.035f, -0.07f, -0.035f, 0.035f, 0.0f, 0.035f, 0.85f, 0.15f, 0.22f, 1.0f, light);

        // Tier 3: Sharp Tip block (Neon red tip)
        poseStack.translate(0.0, -0.065, 0.02);
        poseStack.mulPose(Vector3f.XP.rotationDegrees(-15.0f));
        drawVoxelBox(poseStack, vc, -0.022f, -0.06f, -0.022f, 0.022f, 0.0f, 0.022f, 1.0f, 0.25f, 0.35f, 1.0f, light);

        poseStack.popPose();
    }

    public static void drawVoxelBox(PoseStack poseStack, VertexConsumer vc,
                                   float minX, float minY, float minZ,
                                   float maxX, float maxY, float maxZ,
                                   float r, float g, float b, float a, int light) {
        Matrix4f mat = poseStack.last().pose();
        Matrix3f norm = poseStack.last().normal();

        // 1. TOP face (+Y, normal 0, 1, 0, light 1.0f)
        float topR = r, topG = g, topB = b;
        vc.vertex(mat, minX, maxY, maxZ).color(topR, topG, topB, a).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(norm, 0, 1, 0).endVertex();
        vc.vertex(mat, maxX, maxY, maxZ).color(topR, topG, topB, a).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(norm, 0, 1, 0).endVertex();
        vc.vertex(mat, maxX, maxY, minZ).color(topR, topG, topB, a).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(norm, 0, 1, 0).endVertex();
        vc.vertex(mat, minX, maxY, minZ).color(topR, topG, topB, a).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(norm, 0, 1, 0).endVertex();

        // 2. BOTTOM face (-Y, normal 0, -1, 0, light 0.55f)
        float botR = r * 0.55f, botG = g * 0.55f, botB = b * 0.55f;
        vc.vertex(mat, minX, minY, minZ).color(botR, botG, botB, a).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(norm, 0, -1, 0).endVertex();
        vc.vertex(mat, maxX, minY, minZ).color(botR, botG, botB, a).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(norm, 0, -1, 0).endVertex();
        vc.vertex(mat, maxX, minY, maxZ).color(botR, botG, botB, a).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(norm, 0, -1, 0).endVertex();
        vc.vertex(mat, minX, minY, maxZ).color(botR, botG, botB, a).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(norm, 0, -1, 0).endVertex();

        // 3. NORTH face (-Z, normal 0, 0, -1, light 0.8f)
        float nR = r * 0.8f, nG = g * 0.8f, nB = b * 0.8f;
        vc.vertex(mat, minX, maxY, minZ).color(nR, nG, nB, a).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(norm, 0, 0, -1).endVertex();
        vc.vertex(mat, maxX, maxY, minZ).color(nR, nG, nB, a).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(norm, 0, 0, -1).endVertex();
        vc.vertex(mat, maxX, minY, minZ).color(nR, nG, nB, a).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(norm, 0, 0, -1).endVertex();
        vc.vertex(mat, minX, minY, minZ).color(nR, nG, nB, a).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(norm, 0, 0, -1).endVertex();

        // 4. SOUTH face (+Z, normal 0, 0, 1, light 0.8f)
        float sR = r * 0.8f, sG = g * 0.8f, sB = b * 0.8f;
        vc.vertex(mat, minX, minY, maxZ).color(sR, sG, sB, a).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(norm, 0, 0, 1).endVertex();
        vc.vertex(mat, maxX, minY, maxZ).color(sR, sG, sB, a).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(norm, 0, 0, 1).endVertex();
        vc.vertex(mat, maxX, maxY, maxZ).color(sR, sG, sB, a).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(norm, 0, 0, 1).endVertex();
        vc.vertex(mat, minX, maxY, maxZ).color(sR, sG, sB, a).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(norm, 0, 0, 1).endVertex();

        // 5. WEST face (-X, normal -1, 0, 0, light 0.65f)
        float wR = r * 0.65f, wG = g * 0.65f, wB = b * 0.65f;
        vc.vertex(mat, minX, maxY, maxZ).color(wR, wG, wB, a).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(norm, -1, 0, 0).endVertex();
        vc.vertex(mat, minX, maxY, minZ).color(wR, wG, wB, a).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(norm, -1, 0, 0).endVertex();
        vc.vertex(mat, minX, minY, minZ).color(wR, wG, wB, a).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(norm, -1, 0, 0).endVertex();
        vc.vertex(mat, minX, minY, maxZ).color(wR, wG, wB, a).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(norm, -1, 0, 0).endVertex();

        // 6. EAST face (+X, normal 1, 0, 0, light 0.65f)
        float eR = r * 0.65f, eG = g * 0.65f, eB = b * 0.65f;
        vc.vertex(mat, maxX, minY, maxZ).color(eR, eG, eB, a).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(norm, 1, 0, 0).endVertex();
        vc.vertex(mat, maxX, minY, minZ).color(eR, eG, eB, a).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(norm, 1, 0, 0).endVertex();
        vc.vertex(mat, maxX, maxY, minZ).color(eR, eG, eB, a).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(norm, 1, 0, 0).endVertex();
        vc.vertex(mat, maxX, maxY, maxZ).color(eR, eG, eB, a).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(norm, 1, 0, 0).endVertex();
    }
}
