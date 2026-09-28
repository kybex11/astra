package astra.mod.client.ui.hud;

import astra.mod.client.module.Module;
import astra.mod.client.module.ModuleManager;
import astra.mod.client.module.impl.render.*;
import astra.mod.client.theme.ThemeManager;
import astra.mod.client.util.RenderUtils;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class HudRenderer {
    public static final HudRenderer INSTANCE = new HudRenderer();

    public void render(PoseStack poseStack, float tickDelta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.player == null) return;

        Font font = mc.font;
        int accent = ThemeManager.INSTANCE.getAccent();
        int accentSec = ThemeManager.INSTANCE.getAccentSecondary();

        // 1. Lunar-Style Keystrokes
        renderKeystrokes(poseStack, font, accent);

        // 2. Lunar-Style FPS, CPS & Combo Pills
        renderPills(poseStack, font, accent);

        // 3. Lunar-Style ArmorHUD
        renderArmorHUD(poseStack, font);

        // 4. Lunar-Style TargetHUD
        renderTargetHUD(poseStack, font, accent, accentSec);

        // 5. MiniMap / Full Rect Radar
        renderMiniMap(poseStack, font, accent);

        // 7. Active Modules (ArrayList) on the right side
        renderArrayList(poseStack, font, mc);
    }

    private void renderKeystrokes(PoseStack poseStack, Font font, int accent) {
        Keystrokes ks = Keystrokes.INSTANCE;
        if (ks == null || !ks.isEnabled()) return;

        Minecraft mc = Minecraft.getInstance();
        long window = mc.getWindow().getWindow();

        float scale = HudConfig.INSTANCE.getScale("keystrokes");
        poseStack.pushPose();
        poseStack.scale(scale, scale, 1.0f);

        int startX = (int) (HudConfig.INSTANCE.keystrokesX / scale);
        int startY = (int) (HudConfig.INSTANCE.keystrokesY / scale);

        boolean w = InputConstants.isKeyDown(window, GLFW.GLFW_KEY_W);
        boolean a = InputConstants.isKeyDown(window, GLFW.GLFW_KEY_A);
        boolean s = InputConstants.isKeyDown(window, GLFW.GLFW_KEY_S);
        boolean d = InputConstants.isKeyDown(window, GLFW.GLFW_KEY_D);
        boolean lmb = mc.options.keyAttack.isDown() || (GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS);
        boolean rmb = mc.options.keyUse.isDown() || (GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS);
        boolean space = InputConstants.isKeyDown(window, GLFW.GLFW_KEY_SPACE);

        // Row 1: W
        drawKey(poseStack, font, "W", startX + 22, startY, 20, 20, w, accent);

        // Row 2: A S D
        drawKey(poseStack, font, "A", startX, startY + 22, 20, 20, a, accent);
        drawKey(poseStack, font, "S", startX + 22, startY + 22, 20, 20, s, accent);
        drawKey(poseStack, font, "D", startX + 44, startY + 22, 20, 20, d, accent);

        int nextY = startY + 44;

        // Row 3: LMB & RMB
        if (ks.showMouse.getValue()) {
            String lText = ks.showCPS.getValue() ? (ks.getLeftCPS() + " CPS") : "LMB";
            String rText = ks.showCPS.getValue() ? (ks.getRightCPS() + " CPS") : "RMB";
            drawKey(poseStack, font, lText, startX, nextY, 31, 20, lmb, accent);
            drawKey(poseStack, font, rText, startX + 33, nextY, 31, 20, rmb, accent);
            nextY += 22;
        }

        // Row 4: Space
        if (ks.showSpace.getValue()) {
            drawKey(poseStack, font, "—", startX, nextY, 64, 13, space, accent);
        }

        poseStack.popPose();
    }

    private void drawKey(PoseStack poseStack, Font font, String text, int x, int y, int w, int h, boolean pressed, int accent) {
        int bg = pressed ? (0xCC000000 | (accent & 0x00FFFFFF)) : 0xAA0F172A;
        int border = pressed ? (0xFF000000 | (accent & 0x00FFFFFF)) : 0x44334155;
        int textCol = pressed ? 0xFFFFFFFF : 0xFFCBD5E1;

        RenderUtils.drawBorderedRect(poseStack, x, y, w, h, 1, bg, border);
        int textW = font.width(text);
        font.drawShadow(poseStack, text, x + (w - textW) / 2.0f, y + (h - 8) / 2.0f, textCol);
    }

    private void renderPills(PoseStack poseStack, Font font, int accent) {
        Minecraft mc = Minecraft.getInstance();

        // FPS Pill
        if (FPSMod.INSTANCE != null && FPSMod.INSTANCE.isEnabled()) {
            float scale = HudConfig.INSTANCE.getScale("fps");
            poseStack.pushPose();
            poseStack.scale(scale, scale, 1.0f);
            int fpsPillX = (int) (HudConfig.INSTANCE.fpsX / scale);
            int fpsPillY = (int) (HudConfig.INSTANCE.fpsY / scale);
            String fps = mc.fpsString.split(" ")[0] + " FPS";
            int w = font.width(fps) + 12;
            RenderUtils.drawBorderedRect(poseStack, fpsPillX, fpsPillY, w, 15, 1, 0xAA0F172A, 0x44334155);
            RenderUtils.drawRect(poseStack, fpsPillX, fpsPillY, 2, 15, accent);
            font.drawShadow(poseStack, fps, fpsPillX + 6, fpsPillY + 4, 0xFFFFFFFF);
            poseStack.popPose();
        }

        // CPS Pill
        if (CPSMod.INSTANCE != null && CPSMod.INSTANCE.isEnabled() && Keystrokes.INSTANCE != null) {
            float scale = HudConfig.INSTANCE.getScale("cps");
            poseStack.pushPose();
            poseStack.scale(scale, scale, 1.0f);
            int cpsPillX = (int) (HudConfig.INSTANCE.cpsX / scale);
            int cpsPillY = (int) (HudConfig.INSTANCE.cpsY / scale);
            String cps = Keystrokes.INSTANCE.getLeftCPS() + " | " + Keystrokes.INSTANCE.getRightCPS() + " CPS";
            int w = font.width(cps) + 12;
            RenderUtils.drawBorderedRect(poseStack, cpsPillX, cpsPillY, w, 15, 1, 0xAA0F172A, 0x44334155);
            RenderUtils.drawRect(poseStack, cpsPillX, cpsPillY, 2, 15, accent);
            font.drawShadow(poseStack, cps, cpsPillX + 6, cpsPillY + 4, 0xFFFFFFFF);
            poseStack.popPose();
        }

        // Combo Counter Pill
        if (ComboCounter.INSTANCE != null && ComboCounter.INSTANCE.isEnabled()) {
            float scale = HudConfig.INSTANCE.getScale("combo");
            poseStack.pushPose();
            poseStack.scale(scale, scale, 1.0f);
            int comboPillX = (int) (HudConfig.INSTANCE.comboX / scale);
            int comboPillY = (int) (HudConfig.INSTANCE.comboY / scale);
            int combo = ComboCounter.INSTANCE.getCombo();
            String comboText = "Combo: " + combo;
            int w = font.width(comboText) + 12;
            RenderUtils.drawBorderedRect(poseStack, comboPillX, comboPillY, w, 15, 1, 0xAA0F172A, 0x44334155);
            RenderUtils.drawRect(poseStack, comboPillX, comboPillY, 2, 15, combo > 0 ? 0xFF10B981 : accent);
            font.drawShadow(poseStack, comboText, comboPillX + 6, comboPillY + 4, combo > 0 ? 0xFF34D399 : 0xFFFFFFFF);
            poseStack.popPose();
        }
    }

    private void renderArmorHUD(PoseStack poseStack, Font font) {
        ArmorHUD ah = ArmorHUD.INSTANCE;
        if (ah == null || !ah.isEnabled()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();

        List<ItemStack> items = new ArrayList<>();
        for (int i = 3; i >= 0; i--) {
            ItemStack stack = mc.player.inventory.armor.get(i);
            if (!stack.isEmpty()) items.add(stack);
        }
        if (!mc.player.getMainHandItem().isEmpty()) {
            items.add(mc.player.getMainHandItem());
        }

        if (items.isEmpty()) return;

        float scale = HudConfig.INSTANCE.getScale("armor");
        poseStack.pushPose();
        poseStack.scale(scale, scale, 1.0f);

        int startX = (int) ((HudConfig.INSTANCE.armorX == -1 ? (screenW / 2 + 95) : HudConfig.INSTANCE.armorX) / scale);
        int startY = (int) ((HudConfig.INSTANCE.armorY == -1 ? (screenH - 22) : HudConfig.INSTANCE.armorY) / scale);

        RenderSystem.enableBlend();
        for (ItemStack stack : items) {
            mc.getItemRenderer().renderAndDecorateItem(stack, startX, startY);
            mc.getItemRenderer().renderGuiItemDecorations(font, stack, startX, startY);

            if (ah.showDurability.getValue() && stack.isDamageableItem()) {
                int max = stack.getMaxDamage();
                int current = max - stack.getDamageValue();
                int percent = (int) Math.round(((double) current / (double) max) * 100.0);
                String durStr = percent + "%";
                font.drawShadow(poseStack, durStr, startX + 2, startY - 7, getDurabilityColor(percent));
            }

            startX += 18;
        }
        poseStack.popPose();
    }

    private int getDurabilityColor(int percent) {
        if (percent > 60) return 0xFF10B981;
        if (percent > 25) return 0xFFF59E0B;
        return 0xFFEF4444;
    }

    private void renderTargetHUD(PoseStack poseStack, Font font, int accent, int accentSec) {
        TargetHUD th = TargetHUD.INSTANCE;
        if (th == null || !th.isEnabled()) return;

        Minecraft mc = Minecraft.getInstance();
        LivingEntity entity = null;
        if (astra.mod.client.module.impl.combat.KillAura.INSTANCE != null && astra.mod.client.module.impl.combat.KillAura.INSTANCE.isEnabled() && astra.mod.client.module.impl.combat.KillAura.INSTANCE.currentTarget != null) {
            entity = astra.mod.client.module.impl.combat.KillAura.INSTANCE.currentTarget;
        } else if (mc.hitResult instanceof EntityHitResult) {
            Entity target = ((EntityHitResult) mc.hitResult).getEntity();
            if (target instanceof LivingEntity) {
                entity = (LivingEntity) target;
            }
        }

        if (entity != null) {
            float scale = HudConfig.INSTANCE.getScale("target");
            poseStack.pushPose();
            poseStack.scale(scale, scale, 1.0f);

            int screenW = mc.getWindow().getGuiScaledWidth();
            int screenH = mc.getWindow().getGuiScaledHeight();

            int cardW = 140;
            int cardH = 42;
            int cardX = (int) ((HudConfig.INSTANCE.targetX == -1 ? ((screenW - cardW) / 2) : HudConfig.INSTANCE.targetX) / scale);
            int cardY = (int) ((HudConfig.INSTANCE.targetY == -1 ? (screenH / 2 + 30) : HudConfig.INSTANCE.targetY) / scale);

            RenderUtils.drawBorderedRect(poseStack, cardX, cardY, cardW, cardH, 1, 0xDD0F172A, 0xFF334155);

            String name = entity.getName().getString();
            font.drawShadow(poseStack, name, cardX + 10, cardY + 8, 0xFFFFFFFF);

            float dist = (float) Math.round(mc.player.distanceTo(entity) * 10.0f) / 10.0f;
            String distStr = dist + "m";
            font.drawShadow(poseStack, distStr, cardX + cardW - font.width(distStr) - 10, cardY + 8, 0xFF94A3B8);

            float health = Math.max(0.0f, entity.getHealth());
            float maxHealth = Math.max(1.0f, entity.getMaxHealth());
            double healthProgress = Math.min(1.0, health / maxHealth);

            int barX = cardX + 10;
            int barY = cardY + 24;
            int barW = cardW - 20;
            int barH = 8;

            RenderUtils.drawRect(poseStack, barX, barY, barW, barH, 0xFF1E293B);
            int fillW = (int) Math.round(barW * healthProgress);
            if (fillW > 0) {
                RenderUtils.drawGradientH(poseStack, barX, barY, fillW, barH, accent, accentSec);
            }

            String hpText = (int) health + " HP";
            font.drawShadow(poseStack, hpText, barX + (barW - font.width(hpText)) / 2.0f, barY, 0xFFFFFFFF);
            poseStack.popPose();
        }
    }

    private void renderArrayList(PoseStack poseStack, Font font, Minecraft mc) {
        HUD hud = ModuleManager.INSTANCE.getModule(HUD.class);
        if (hud == null || !hud.isEnabled() || !hud.arrayList.getValue()) return;

        List<Module> activeModules = ModuleManager.INSTANCE.getModules().stream()
                .filter(Module::isEnabled)
                .filter(m -> !(m instanceof HUD))
                .sorted(Comparator.comparingInt(m -> -font.width(m.getName())))
                .collect(Collectors.toList());

        int screenW = mc.getWindow().getGuiScaledWidth();
        int currentY = 8;
        int offset = 0;

        for (Module m : activeModules) {
            String text = m.getName();
            int textW = font.width(text);
            int cardW = textW + 10;
            int cardH = 13;
            int cardX = screenW - cardW - 6;

            int color = ThemeManager.INSTANCE.getCurrentTheme() == ThemeManager.Theme.CHROMA
                    ? ThemeManager.getRainbow(offset * 200)
                    : ThemeManager.INSTANCE.getAccent();

            RenderUtils.drawRect(poseStack, cardX, currentY, cardW, cardH, 0xAA0F172A);
            RenderUtils.drawRect(poseStack, cardX + cardW - 2, currentY, 2, cardH, color);

            font.drawShadow(poseStack, text, cardX + 4, currentY + 3, color);

            currentY += cardH + 2;
            offset++;
        }
    }

    private void renderMiniMap(PoseStack poseStack, Font font, int accent) {
        MiniMap mm = MiniMap.INSTANCE;
        if (mm == null || !mm.isEnabled()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        float scale = HudConfig.INSTANCE.getScale("minimap");
        poseStack.pushPose();
        poseStack.scale(scale, scale, 1.0f);

        int mapX = (int) (HudConfig.INSTANCE.miniMapX / scale);
        int mapY = (int) (HudConfig.INSTANCE.miniMapY / scale);
        int mapSize = (int) mm.size.getValue().doubleValue();
        float radius = mapSize / 2.0f;
        float cx = mapX + radius;
        float cy = mapY + radius;
        double range = mm.range.getValue();

        // 1. Full Square container utilization
        RenderUtils.drawBorderedRect(poseStack, mapX, mapY, mapSize, mapSize, 1, 0xF00B0F19, 0xFF334155);

        // 2. Surface Mapping
        int playerX = (int) mc.player.getX();
        int playerY = (int) mc.player.getY();
        int playerZ = (int) mc.player.getZ();
        int intRange = (int) range;
        int step = Math.max(1, intRange / 24);

        for (int rX = -intRange; rX <= intRange; rX += step) {
            for (int rZ = -intRange; rZ <= intRange; rZ += step) {
                int worldX = playerX + rX;
                int worldZ = playerZ + rZ;
                net.minecraft.core.BlockPos pos = new net.minecraft.core.BlockPos(worldX, playerY, worldZ);

                net.minecraft.world.level.block.state.BlockState state = mc.level.getBlockState(pos);
                int mapCol = state.getMaterial().getColor().col;
                if (mapCol == 0) {
                    mapCol = mc.level.getBlockState(pos.below()).getMaterial().getColor().col;
                }
                if (mapCol == 0) mapCol = 0x2D3748;

                float px = cx + ((float) rX / (float) intRange) * (radius - 4);
                float py = cy + ((float) rZ / (float) intRange) * (radius - 4);
                int pixelSize = Math.max(1, (int) Math.ceil((radius - 4) / (intRange / (float) step)));

                if (px >= mapX + 2 && px + pixelSize <= mapX + mapSize - 2 && py >= mapY + 2 && py + pixelSize <= mapY + mapSize - 2) {
                    RenderUtils.drawRect(poseStack, (int) px, (int) py, pixelSize, pixelSize, 0xCC000000 | mapCol);
                }
            }
        }

        // Radar grid
        RenderUtils.drawLine(poseStack, mapX + 4, cy, mapX + mapSize - 4, cy, 1.0f, 0x33CBD5E1);
        RenderUtils.drawLine(poseStack, cx, mapY + 4, cx, mapY + mapSize - 4, 1.0f, 0x33CBD5E1);

        // Cardinal directions
        font.drawShadow(poseStack, "N", cx - font.width("N") / 2.0f, mapY + 3, accent);
        font.drawShadow(poseStack, "S", cx - font.width("S") / 2.0f, mapY + mapSize - 11, 0xFF94A3B8);
        font.drawShadow(poseStack, "W", mapX + 3, cy - 4, 0xFF94A3B8);
        font.drawShadow(poseStack, "E", mapX + mapSize - 9, cy - 4, 0xFF94A3B8);

        // 3. Render Entities with clear player icons
        for (Entity e : mc.level.entitiesForRendering()) {
            if (e == mc.player || !(e instanceof LivingEntity)) continue;
            LivingEntity living = (LivingEntity) e;
            if (!living.isAlive() || living.isDeadOrDying()) continue;

            boolean isPlayer = living instanceof Player;
            boolean isMonster = living instanceof net.minecraft.world.entity.monster.Monster;
            boolean isAnimal = living instanceof net.minecraft.world.entity.animal.Animal;

            if (isPlayer && !mm.showPlayers.getValue()) continue;
            if (isMonster && !mm.showMobs.getValue()) continue;
            if (isAnimal && !mm.showAnimals.getValue()) continue;
            if (!isPlayer && !isMonster && !isAnimal) continue;

            double dx = e.getX() - mc.player.getX();
            double dz = e.getZ() - mc.player.getZ();

            if (Math.abs(dx) <= range && Math.abs(dz) <= range) {
                float maxPixelR = radius - 6.0f;
                float px = (float) (cx + (dx / range) * maxPixelR);
                float py = (float) (cy + (dz / range) * maxPixelR);

                if (isPlayer) {
                    // Bright Cyan-Purple player marker with outline
                    RenderUtils.drawBorderedRect(poseStack, (int) px - 2, (int) py - 2, 5, 5, 1, 0xFFEF4444, 0xFFFFFFFF);
                    String pName = living.getName().getString();
                    poseStack.pushPose();
                    poseStack.translate(px, py - 8, 0);
                    poseStack.scale(0.55f, 0.55f, 1.0f);
                    font.drawShadow(poseStack, pName, -font.width(pName) / 2.0f, 0, 0xFFFFFFFF);
                    poseStack.popPose();
                } else {
                    int dotColor = isMonster ? 0xFFF59E0B : 0xFF38BDF8;
                    RenderUtils.drawRect(poseStack, (int) px - 1, (int) py - 1, 3, 3, dotColor);
                }
            }
        }

        // 4. Player center marker & view needle
        RenderUtils.drawRect(poseStack, (int) cx - 2, (int) cy - 2, 4, 4, 0xFF10B981);
        double rad = Math.toRadians(mc.player.yRot + 90.0f);
        float nx = (float) (cx + Math.cos(rad) * 7.0f);
        float ny = (float) (cy + Math.sin(rad) * 7.0f);
        RenderUtils.drawLine(poseStack, cx, cy, nx, ny, 1.5f, 0xFF10B981);

        poseStack.popPose();
    }
}
