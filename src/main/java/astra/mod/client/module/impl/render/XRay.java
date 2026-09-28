package astra.mod.client.module.impl.render;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.BooleanSetting;
import astra.mod.client.setting.NumberSetting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class XRay extends Module {
    public static final XRay INSTANCE = new XRay();

    public final BooleanSetting diamonds = new BooleanSetting("💎 Алмазы", true);
    public final BooleanSetting netherite = new BooleanSetting("🟣 Древние обломки", true);
    public final BooleanSetting gold = new BooleanSetting("🟡 Золото", true);
    public final BooleanSetting emeralds = new BooleanSetting("🟢 Изумруды", true);
    public final BooleanSetting iron = new BooleanSetting("⚪ Железо", false);
    public final BooleanSetting copper = new BooleanSetting("🟤 Медь", false);
    public final BooleanSetting redstone = new BooleanSetting("🔴 Редстоун", false);
    public final BooleanSetting lapis = new BooleanSetting("🔵 Лазурит", false);
    public final BooleanSetting chests = new BooleanSetting("📦 Сундуки / Шалкеры", true);
    public final BooleanSetting spawners = new BooleanSetting("🔥 Спавнеры", true);

    public final NumberSetting radius = new NumberSetting("Радиус поиска", 26.0, 10.0, 48.0, 2.0);
    public final BooleanSetting boxOutline = new BooleanSetting("Контур блоков", true);
    public final BooleanSetting boxFill = new BooleanSetting("Заливка блоков", true);
    public final BooleanSetting tracers = new BooleanSetting("Линии к ценным рудам", true);
    public final NumberSetting fillOpacity = new NumberSetting("Яркость заливки", 45.0, 10.0, 120.0, 5.0);

    public enum OreTarget {
        DIAMOND("Алмазы", 0xFF00E5FF, true),
        NETHERITE("Древние обломки", 0xFFA855F7, true),
        GOLD("Золото", 0xFFF59E0B, false),
        EMERALD("Изумруды", 0xFF10B981, true),
        IRON("Железо", 0xFFCBD5E1, false),
        COPPER("Медь", 0xFFD97706, false),
        REDSTONE("Редстоун", 0xFFEF4444, false),
        LAPIS("Лазурит", 0xFF3B82F6, false),
        CHEST("Сундук/Шалкер", 0xFFF97316, true),
        SPAWNER("Спавнер", 0xFFFF0055, true);

        public final String title;
        public final int color;
        public final boolean highPriority;

        OreTarget(String title, int color, boolean highPriority) {
            this.title = title;
            this.color = color;
            this.highPriority = highPriority;
        }
    }

    public static class FoundBlock {
        public final BlockPos pos;
        public final OreTarget target;
        public double distSq;

        public FoundBlock(BlockPos pos, OreTarget target, double distSq) {
            this.pos = pos;
            this.target = target;
            this.distSq = distSq;
        }
    }

    private volatile List<FoundBlock> foundBlocks = Collections.emptyList();
    private boolean isScanning = false;
    private int scanCooldown = 0;

    public XRay() {
        super("XRay", "Выборочная подсветка руд и ценных блоков в мире (Селективный X-Ray)", Category.RENDER);

        addSettings(
                diamonds, netherite, gold, emeralds, iron,
                copper, redstone, lapis, chests, spawners,
                radius, boxOutline, boxFill, tracers, fillOpacity
        );
    }

    @Override
    public void onEnable() {
        scanCooldown = 0;
        triggerScan();
    }

    @Override
    public void onDisable() {
        foundBlocks = Collections.emptyList();
    }

    @Override
    public void onTick() {
        Minecraft mc = Minecraft.getInstance();
        if (!isEnabled() || mc.level == null || mc.player == null) return;

        if (++scanCooldown >= 10) { // Scan every 10 ticks (0.5s)
            scanCooldown = 0;
            triggerScan();
        }
    }

    private void triggerScan() {
        if (isScanning) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        isScanning = true;
        BlockPos center = mc.player.blockPosition();
        int r = ((Double) radius.getValue()).intValue();
        Level level = mc.level;

        CompletableFuture.runAsync(() -> {
            try {
                List<FoundBlock> scanned = new ArrayList<>();
                int rSq = r * r;

                for (int x = -r; x <= r; x++) {
                    for (int y = -r; y <= r; y++) {
                        for (int z = -r; z <= r; z++) {
                            int distSq = x * x + y * y + z * z;
                            if (distSq > rSq) continue;

                            BlockPos pos = center.offset(x, y, z);
                            if (!level.hasChunkAt(pos)) continue;

                            BlockState state = level.getBlockState(pos);
                            if (state.isAir()) continue;

                            OreTarget target = matchTarget(state.getBlock());
                            if (target != null) {
                                scanned.add(new FoundBlock(pos, target, distSq));
                            }
                        }
                    }
                }

                // Sort by distance and cap to 300 to ensure fluid 240+ FPS
                scanned.sort(Comparator.comparingDouble(a -> a.distSq));
                if (scanned.size() > 300) {
                    scanned = scanned.subList(0, 300);
                }

                foundBlocks = scanned;
            } catch (Exception ignored) {
            } finally {
                isScanning = false;
            }
        });
    }

    private OreTarget matchTarget(Block block) {
        String path = Registry.BLOCK.getKey(block).getPath().toLowerCase();

        if (diamonds.getValue() && path.contains("diamond") && (path.contains("ore") || path.contains("block"))) {
            return OreTarget.DIAMOND;
        }
        if (netherite.getValue() && (path.contains("ancient_debris") || path.contains("netherite_block"))) {
            return OreTarget.NETHERITE;
        }
        if (gold.getValue() && path.contains("gold") && (path.contains("ore") || path.contains("block"))) {
            return OreTarget.GOLD;
        }
        if (emeralds.getValue() && path.contains("emerald") && (path.contains("ore") || path.contains("block"))) {
            return OreTarget.EMERALD;
        }
        if (iron.getValue() && path.contains("iron") && (path.contains("ore") || path.contains("block") || path.contains("raw"))) {
            return OreTarget.IRON;
        }
        if (copper.getValue() && path.contains("copper") && (path.contains("ore") || path.contains("block") || path.contains("raw"))) {
            return OreTarget.COPPER;
        }
        if (redstone.getValue() && path.contains("redstone") && (path.contains("ore") || path.contains("block"))) {
            return OreTarget.REDSTONE;
        }
        if (lapis.getValue() && path.contains("lapis") && (path.contains("ore") || path.contains("block"))) {
            return OreTarget.LAPIS;
        }
        if (chests.getValue() && (path.contains("chest") || path.contains("shulker_box") || path.contains("barrel"))) {
            return OreTarget.CHEST;
        }
        if (spawners.getValue() && path.contains("spawner")) {
            return OreTarget.SPAWNER;
        }
        return null;
    }

    public void render3D(PoseStack poseStack, Camera camera, float partialTicks) {
        if (!isEnabled()) return;
        List<FoundBlock> list = foundBlocks;
        if (list.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        Vec3 camPos = camera.getPosition();

        poseStack.pushPose();
        // Un-rotate camera view so all vertices are rigidly fixed to real 3D world space
        poseStack.mulPose(Vector3f.YP.rotationDegrees(-camera.getYRot() - 180.0F));
        poseStack.mulPose(Vector3f.XP.rotationDegrees(-camera.getXRot()));
        Matrix4f worldMatrix = poseStack.last().pose();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();

        int fillA = ((Double) fillOpacity.getValue()).intValue();

        // 1. Render Filled Boxes
        if (boxFill.getValue()) {
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR);
            for (FoundBlock fb : list) {
                BlockPos p = fb.pos;
                float minX = (float) (p.getX() - camPos.x);
                float minY = (float) (p.getY() - camPos.y);
                float minZ = (float) (p.getZ() - camPos.z);
                float maxX = minX + 1.0f;
                float maxY = minY + 1.0f;
                float maxZ = minZ + 1.0f;

                int col = fb.target.color;
                int r = (col >> 16) & 0xFF;
                int g = (col >> 8) & 0xFF;
                int b = col & 0xFF;

                drawBoxQuads(buffer, worldMatrix, minX, minY, minZ, maxX, maxY, maxZ, r, g, b, fillA);
            }
            tesselator.end();
        }

        // 2. Render Box Wireframe Outlines
        if (boxOutline.getValue()) {
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glLineWidth(1.8f);
            buffer.begin(GL11.GL_LINES, DefaultVertexFormat.POSITION_COLOR);

            for (FoundBlock fb : list) {
                BlockPos p = fb.pos;
                float minX = (float) (p.getX() - camPos.x);
                float minY = (float) (p.getY() - camPos.y);
                float minZ = (float) (p.getZ() - camPos.z);
                float maxX = minX + 1.0f;
                float maxY = minY + 1.0f;
                float maxZ = minZ + 1.0f;

                int col = fb.target.color;
                int r = (col >> 16) & 0xFF;
                int g = (col >> 8) & 0xFF;
                int b = col & 0xFF;

                drawBoxLines(buffer, worldMatrix, minX, minY, minZ, maxX, maxY, maxZ, r, g, b, 230);
            }
            tesselator.end();
            GL11.glDisable(GL11.GL_LINE_SMOOTH);
        }

        // 3. Render Tracers to High Priority Ores
        if (tracers.getValue()) {
            float startX = (float) (Mth.lerp(partialTicks, mc.player.xOld, mc.player.getX()) - camPos.x);
            float startY = (float) (Mth.lerp(partialTicks, mc.player.yOld, mc.player.getY()) - camPos.y + 0.25);
            float startZ = (float) (Mth.lerp(partialTicks, mc.player.zOld, mc.player.getZ()) - camPos.z);

            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glLineWidth(1.5f);
            buffer.begin(GL11.GL_LINES, DefaultVertexFormat.POSITION_COLOR);

            int tracerCount = 0;
            for (FoundBlock fb : list) {
                if (!fb.target.highPriority && fb.distSq > 16 * 16) continue;
                if (++tracerCount > 30) break; // Limit tracers to 30 cleanest lines

                BlockPos p = fb.pos;
                float tx = (float) (p.getX() + 0.5 - camPos.x);
                float ty = (float) (p.getY() + 0.5 - camPos.y);
                float tz = (float) (p.getZ() + 0.5 - camPos.z);

                int col = fb.target.color;
                int r = (col >> 16) & 0xFF;
                int g = (col >> 8) & 0xFF;
                int b = col & 0xFF;

                buffer.vertex(worldMatrix, startX, startY, startZ).color(r, g, b, 180).endVertex();
                buffer.vertex(worldMatrix, tx, ty, tz).color(r, g, b, 240).endVertex();
            }

            tesselator.end();
            GL11.glDisable(GL11.GL_LINE_SMOOTH);
        }

        poseStack.popPose();

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    private static void drawBoxLines(BufferBuilder b, Matrix4f mat, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, int r, int g, int blue, int a) {
        // Bottom
        b.vertex(mat, minX, minY, minZ).color(r, g, blue, a).endVertex();
        b.vertex(mat, maxX, minY, minZ).color(r, g, blue, a).endVertex();
        b.vertex(mat, maxX, minY, minZ).color(r, g, blue, a).endVertex();
        b.vertex(mat, maxX, minY, maxZ).color(r, g, blue, a).endVertex();
        b.vertex(mat, maxX, minY, maxZ).color(r, g, blue, a).endVertex();
        b.vertex(mat, minX, minY, maxZ).color(r, g, blue, a).endVertex();
        b.vertex(mat, minX, minY, maxZ).color(r, g, blue, a).endVertex();
        b.vertex(mat, minX, minY, minZ).color(r, g, blue, a).endVertex();

        // Top
        b.vertex(mat, minX, maxY, minZ).color(r, g, blue, a).endVertex();
        b.vertex(mat, maxX, maxY, minZ).color(r, g, blue, a).endVertex();
        b.vertex(mat, maxX, maxY, minZ).color(r, g, blue, a).endVertex();
        b.vertex(mat, maxX, maxY, maxZ).color(r, g, blue, a).endVertex();
        b.vertex(mat, maxX, maxY, maxZ).color(r, g, blue, a).endVertex();
        b.vertex(mat, minX, maxY, maxZ).color(r, g, blue, a).endVertex();
        b.vertex(mat, minX, maxY, maxZ).color(r, g, blue, a).endVertex();
        b.vertex(mat, minX, maxY, minZ).color(r, g, blue, a).endVertex();

        // Pillars
        b.vertex(mat, minX, minY, minZ).color(r, g, blue, a).endVertex();
        b.vertex(mat, minX, maxY, minZ).color(r, g, blue, a).endVertex();
        b.vertex(mat, maxX, minY, minZ).color(r, g, blue, a).endVertex();
        b.vertex(mat, maxX, maxY, minZ).color(r, g, blue, a).endVertex();
        b.vertex(mat, maxX, minY, maxZ).color(r, g, blue, a).endVertex();
        b.vertex(mat, maxX, maxY, maxZ).color(r, g, blue, a).endVertex();
        b.vertex(mat, minX, minY, maxZ).color(r, g, blue, a).endVertex();
        b.vertex(mat, minX, maxY, maxZ).color(r, g, blue, a).endVertex();
    }

    private static void drawBoxQuads(BufferBuilder b, Matrix4f mat, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, int r, int g, int blue, int a) {
        // Bottom (Y-)
        b.vertex(mat, minX, minY, minZ).color(r, g, blue, a).endVertex();
        b.vertex(mat, maxX, minY, minZ).color(r, g, blue, a).endVertex();
        b.vertex(mat, maxX, minY, maxZ).color(r, g, blue, a).endVertex();
        b.vertex(mat, minX, minY, maxZ).color(r, g, blue, a).endVertex();

        // Top (Y+)
        b.vertex(mat, minX, maxY, minZ).color(r, g, blue, a).endVertex();
        b.vertex(mat, minX, maxY, maxZ).color(r, g, blue, a).endVertex();
        b.vertex(mat, maxX, maxY, maxZ).color(r, g, blue, a).endVertex();
        b.vertex(mat, maxX, maxY, minZ).color(r, g, blue, a).endVertex();

        // North (Z-)
        b.vertex(mat, minX, minY, minZ).color(r, g, blue, a).endVertex();
        b.vertex(mat, minX, maxY, minZ).color(r, g, blue, a).endVertex();
        b.vertex(mat, maxX, maxY, minZ).color(r, g, blue, a).endVertex();
        b.vertex(mat, maxX, minY, minZ).color(r, g, blue, a).endVertex();

        // South (Z+)
        b.vertex(mat, minX, minY, maxZ).color(r, g, blue, a).endVertex();
        b.vertex(mat, maxX, minY, maxZ).color(r, g, blue, a).endVertex();
        b.vertex(mat, maxX, maxY, maxZ).color(r, g, blue, a).endVertex();
        b.vertex(mat, minX, maxY, maxZ).color(r, g, blue, a).endVertex();

        // West (X-)
        b.vertex(mat, minX, minY, minZ).color(r, g, blue, a).endVertex();
        b.vertex(mat, minX, minY, maxZ).color(r, g, blue, a).endVertex();
        b.vertex(mat, minX, maxY, maxZ).color(r, g, blue, a).endVertex();
        b.vertex(mat, minX, maxY, minZ).color(r, g, blue, a).endVertex();

        // East (X+)
        b.vertex(mat, maxX, minY, minZ).color(r, g, blue, a).endVertex();
        b.vertex(mat, maxX, maxY, minZ).color(r, g, blue, a).endVertex();
        b.vertex(mat, maxX, maxY, maxZ).color(r, g, blue, a).endVertex();
        b.vertex(mat, maxX, minY, maxZ).color(r, g, blue, a).endVertex();
    }
}
