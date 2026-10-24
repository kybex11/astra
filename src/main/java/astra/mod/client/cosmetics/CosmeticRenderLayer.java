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
import net.minecraft.resources.ResourceLocation;

public class CosmeticRenderLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    public static final ResourceLocation WHITE_TEXTURE = new ResourceLocation("astra", "textures/white.png");

    public CosmeticRenderLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player,
                       float limbAngle, float limbDistance, float partialTicks, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        CosmeticManager cm = CosmeticManager.INSTANCE;
        if (player.isInvisible()) return;

        int accent = ThemeManager.INSTANCE.getAccent();
        float ar = ((accent >> 16) & 0xFF) / 255.0f;
        float ag = ((accent >> 8) & 0xFF) / 255.0f;
        float ab = (accent & 0xFF) / 255.0f;

        // Render with pure white texture so vertex colors are 100% solid, crisp and free of cape textures
        VertexConsumer vc = buffer.getBuffer(RenderType.entityCutoutNoCull(WHITE_TEXTURE));

        // 1. 3D Floating Golden Halo
        if (cm.isHaloEnabled()) {
            poseStack.pushPose();
            getParentModel().head.translateAndRotate(poseStack);

            // Floating bobbing & rotation over the top of the head (top of head is Y = -0.50f)
            double bob = Math.sin((ageInTicks + partialTicks) * 0.08) * 0.035;
            float rotY = (ageInTicks + partialTicks) * 2.0f;

            poseStack.translate(0.0, -0.70 + bob, 0.0);
            poseStack.mulPose(Vector3f.YP.rotationDegrees(rotY));

            int segments = 8;
            float radius = 0.28f;

            for (int i = 0; i < segments; i++) {
                poseStack.pushPose();
                float angle = i * (360.0f / segments);
                poseStack.mulPose(Vector3f.YP.rotationDegrees(angle));
                poseStack.translate(radius, 0.0, 0.0);

                // Pure gold voxel block
                drawVoxelBox(poseStack, vc, -0.03f, -0.02f, -0.06f, 0.03f, 0.02f, 0.06f,
                        1.0f, 0.85f, 0.12f, 1.0f, packedLight);
                poseStack.popPose();
            }

            poseStack.popPose();
        }

        // 2. 3D Modern Minecraft Voxel Dragon Wings
        if (cm.isWingsEnabled()) {
            poseStack.pushPose();
            getParentModel().body.translateAndRotate(poseStack);

            // Origin on player's upper back (Z = 0.135, Y = 0.15)
            poseStack.translate(0.0, 0.15, 0.135);

            float flap = (float) Math.sin((ageInTicks + partialTicks) * 0.14f) * 22.0f;

            // Left Wing
            poseStack.pushPose();
            poseStack.mulPose(Vector3f.YP.rotationDegrees(-flap - 16.0f));
            drawWing(poseStack, vc, ar, ag, ab, packedLight);
            poseStack.popPose();

            // Right Wing (Perfect symmetrical mirror using scale X = -1)
            poseStack.pushPose();
            poseStack.scale(-1.0f, 1.0f, 1.0f);
            poseStack.mulPose(Vector3f.YP.rotationDegrees(-flap - 16.0f));
            drawWing(poseStack, vc, ar, ag, ab, packedLight);
            poseStack.popPose();

            poseStack.popPose();
        }

        // 3. 3D Modern Minecraft Voxel Demon Horns
        if (cm.isHornsEnabled()) {
            poseStack.pushPose();
            getParentModel().head.translateAndRotate(poseStack);

            // Left Horn
            drawHorn(poseStack, vc, -0.18f, packedLight);
            // Right Horn
            drawHorn(poseStack, vc, 0.18f, packedLight);

            poseStack.popPose();
        }
    }

    private void drawWing(PoseStack poseStack, VertexConsumer vc, float ar, float ag, float ab, int light) {
        float boneR = 0.14f;
        float boneG = 0.14f;
        float boneB = 0.18f;
        float clawR = 1.0f;
        float clawG = 0.85f;
        float clawB = 0.15f;

        // Base anchor block
        drawVoxelBox(poseStack, vc, 0.0f, -0.03f, 0.0f, 0.10f, 0.03f, 0.05f, boneR, boneG, boneB, 1.0f, light);

        // Main Bone (extends OUT +X and UP -Y):
        drawVoxelBox(poseStack, vc, 0.08f, -0.08f, 0.01f, 0.20f, 0.02f, 0.05f, boneR, boneG, boneB, 1.0f, light);
        drawVoxelBox(poseStack, vc, 0.18f, -0.18f, 0.02f, 0.32f, -0.06f, 0.06f, boneR, boneG, boneB, 1.0f, light);
        drawVoxelBox(poseStack, vc, 0.30f, -0.28f, 0.03f, 0.45f, -0.16f, 0.07f, boneR, boneG, boneB, 1.0f, light);

        // Joint Claw Spike (pointing up -Y)
        drawVoxelBox(poseStack, vc, 0.40f, -0.38f, 0.03f, 0.46f, -0.26f, 0.07f, clawR, clawG, clawB, 1.0f, light);

        // Forearm Bone (extends further OUT +X and slightly down +Y):
        drawVoxelBox(poseStack, vc, 0.45f, -0.27f, 0.04f, 0.60f, -0.18f, 0.07f, boneR, boneG, boneB, 1.0f, light);
        drawVoxelBox(poseStack, vc, 0.58f, -0.19f, 0.04f, 0.74f, -0.08f, 0.07f, boneR, boneG, boneB, 1.0f, light);
        drawVoxelBox(poseStack, vc, 0.72f, -0.09f, 0.04f, 0.88f, 0.04f, 0.07f, boneR, boneG, boneB, 1.0f, light);

        // Wing Tip Claw
        drawVoxelBox(poseStack, vc, 0.86f, 0.02f, 0.04f, 0.94f, 0.12f, 0.07f, clawR, clawG, clawB, 1.0f, light);

        // Solid 3D Wing Membranes (accent colored volumetric slabs):
        drawVoxelBox(poseStack, vc, 0.08f, -0.06f, 0.02f, 0.32f, 0.18f, 0.04f, ar, ag, ab, 0.92f, light);
        drawVoxelBox(poseStack, vc, 0.30f, -0.16f, 0.03f, 0.58f, 0.12f, 0.05f, ar * 0.9f, ag * 0.9f, ab * 0.9f, 0.90f, light);
        drawVoxelBox(poseStack, vc, 0.56f, -0.08f, 0.04f, 0.85f, 0.06f, 0.06f, ar * 0.8f, ag * 0.8f, ab * 0.8f, 0.88f, light);

        // Lower Wing Scallops
        drawVoxelBox(poseStack, vc, 0.18f, 0.18f, 0.02f, 0.28f, 0.28f, 0.04f, ar, ag, ab, 0.92f, light);
        drawVoxelBox(poseStack, vc, 0.40f, 0.12f, 0.03f, 0.50f, 0.24f, 0.05f, ar * 0.9f, ag * 0.9f, ab * 0.9f, 0.90f, light);
        drawVoxelBox(poseStack, vc, 0.65f, 0.06f, 0.04f, 0.75f, 0.18f, 0.06f, ar * 0.8f, ag * 0.8f, ab * 0.8f, 0.88f, light);
    }

    private void drawHorn(PoseStack poseStack, VertexConsumer vc, float xOffset, int light) {
        poseStack.pushPose();
        // Top front temple of head
        poseStack.translate(xOffset, -0.48, -0.15);

        float sign = (xOffset < 0) ? -1.0f : 1.0f;
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(sign * -16.0f));
        poseStack.mulPose(Vector3f.XP.rotationDegrees(-15.0f));

        // Tier 1: Base block (Dark obsidian)
        drawVoxelBox(poseStack, vc, -0.035f, -0.06f, -0.035f, 0.035f, 0.0f, 0.035f, 0.16f, 0.05f, 0.07f, 1.0f, light);

        // Tier 2: Mid block (Ruby crimson, angled back)
        poseStack.translate(0.0, -0.055, 0.02);
        poseStack.mulPose(Vector3f.XP.rotationDegrees(-20.0f));
        drawVoxelBox(poseStack, vc, -0.028f, -0.06f, -0.028f, 0.028f, 0.0f, 0.028f, 0.85f, 0.10f, 0.20f, 1.0f, light);

        // Tier 3: Sharp Tip block (Neon red tip)
        poseStack.translate(0.0, -0.055, 0.02);
        poseStack.mulPose(Vector3f.XP.rotationDegrees(-20.0f));
        drawVoxelBox(poseStack, vc, -0.018f, -0.05f, -0.018f, 0.018f, 0.0f, 0.018f, 1.0f, 0.25f, 0.35f, 1.0f, light);

        poseStack.popPose();
    }

    public static void drawVoxelBox(PoseStack poseStack, VertexConsumer vc,
                                   float minX, float minY, float minZ,
                                   float maxX, float maxY, float maxZ,
                                   float r, float g, float b, float a, int light) {
        Matrix4f mat = poseStack.last().pose();
        Matrix3f norm = poseStack.last().normal();

        float x1 = Math.min(minX, maxX);
        float x2 = Math.max(minX, maxX);
        float y1 = Math.min(minY, maxY);
        float y2 = Math.max(minY, maxY);
        float z1 = Math.min(minZ, maxZ);
        float z2 = Math.max(minZ, maxZ);

        // 1. TOP face (+Y, normal 0, 1, 0, light factor 1.0f)
        float topR = r, topG = g, topB = b;
        vc.vertex(mat, x1, y2, z2).color(topR, topG, topB, a).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(norm, 0, 1, 0).endVertex();
        vc.vertex(mat, x2, y2, z2).color(topR, topG, topB, a).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(norm, 0, 1, 0).endVertex();
        vc.vertex(mat, x2, y2, z1).color(topR, topG, topB, a).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(norm, 0, 1, 0).endVertex();
        vc.vertex(mat, x1, y2, z1).color(topR, topG, topB, a).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(norm, 0, 1, 0).endVertex();

        // 2. BOTTOM face (-Y, normal 0, -1, 0, light factor 0.55f)
        float botR = r * 0.55f, botG = g * 0.55f, botB = b * 0.55f;
        vc.vertex(mat, x1, y1, z1).color(botR, botG, botB, a).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(norm, 0, -1, 0).endVertex();
        vc.vertex(mat, x2, y1, z1).color(botR, botG, botB, a).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(norm, 0, -1, 0).endVertex();
        vc.vertex(mat, x2, y1, z2).color(botR, botG, botB, a).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(norm, 0, -1, 0).endVertex();
        vc.vertex(mat, x1, y1, z2).color(botR, botG, botB, a).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(norm, 0, -1, 0).endVertex();

        // 3. NORTH face (-Z, normal 0, 0, -1, light factor 0.8f)
        float nR = r * 0.8f, nG = g * 0.8f, nB = b * 0.8f;
        vc.vertex(mat, x1, y2, z1).color(nR, nG, nB, a).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(norm, 0, 0, -1).endVertex();
        vc.vertex(mat, x2, y2, z1).color(nR, nG, nB, a).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(norm, 0, 0, -1).endVertex();
        vc.vertex(mat, x2, y1, z1).color(nR, nG, nB, a).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(norm, 0, 0, -1).endVertex();
        vc.vertex(mat, x1, y1, z1).color(nR, nG, nB, a).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(norm, 0, 0, -1).endVertex();

        // 4. SOUTH face (+Z, normal 0, 0, 1, light factor 0.8f)
        float sR = r * 0.8f, sG = g * 0.8f, sB = b * 0.8f;
        vc.vertex(mat, x1, y1, z2).color(sR, sG, sB, a).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(norm, 0, 0, 1).endVertex();
        vc.vertex(mat, x2, y1, z2).color(sR, sG, sB, a).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(norm, 0, 0, 1).endVertex();
        vc.vertex(mat, x2, y2, z2).color(sR, sG, sB, a).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(norm, 0, 0, 1).endVertex();
        vc.vertex(mat, x1, y2, z2).color(sR, sG, sB, a).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(norm, 0, 0, 1).endVertex();

        // 5. WEST face (-X, normal -1, 0, 0, light factor 0.65f)
        float wR = r * 0.65f, wG = g * 0.65f, wB = b * 0.65f;
        vc.vertex(mat, x1, y2, z2).color(wR, wG, wB, a).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(norm, -1, 0, 0).endVertex();
        vc.vertex(mat, x1, y2, z1).color(wR, wG, wB, a).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(norm, -1, 0, 0).endVertex();
        vc.vertex(mat, x1, y1, z1).color(wR, wG, wB, a).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(norm, -1, 0, 0).endVertex();
        vc.vertex(mat, x1, y1, z2).color(wR, wG, wB, a).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(norm, -1, 0, 0).endVertex();

        // 6. EAST face (+X, normal 1, 0, 0, light factor 0.65f)
        float eR = r * 0.65f, eG = g * 0.65f, eB = b * 0.65f;
        vc.vertex(mat, x2, y1, z2).color(eR, eG, eB, a).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(norm, 1, 0, 0).endVertex();
        vc.vertex(mat, x2, y1, z1).color(eR, eG, eB, a).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(norm, 1, 0, 0).endVertex();
        vc.vertex(mat, x2, y2, z1).color(eR, eG, eB, a).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(norm, 1, 0, 0).endVertex();
        vc.vertex(mat, x2, y2, z2).color(eR, eG, eB, a).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(norm, 1, 0, 0).endVertex();
    }
}
