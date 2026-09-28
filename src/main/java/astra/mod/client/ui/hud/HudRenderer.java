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

        // 2. Lunar-Style FPS & CPS Pills (top-left, cleanly styled)
        renderPills(poseStack, font, accent);

        // 3. Lunar-Style ArmorHUD
        renderArmorHUD(poseStack, font);

        // 4. Lunar-Style TargetHUD
        renderTargetHUD(poseStack, font, accent, accentSec);

        // 5. Custom Crosshair
        renderCrosshair(poseStack, mc);

        // 6. Active Modules (ArrayList) on the right side
        renderArrayList(poseStack, font, mc);
    }

    private void renderKeystrokes(PoseStack poseStack, Font font, int accent) {
        Keystrokes ks = Keystrokes.INSTANCE;
        if (ks == null || !ks.isEnabled()) return;

        Minecraft mc = Minecraft.getInstance();
        long window = mc.getWindow().getWindow();

        int startX = ks.x.getValue().intValue();
        int startY = ks.y.getValue().intValue();

        boolean w = InputConstants.isKeyDown(window, GLFW.GLFW_KEY_W);
        boolean a = InputConstants.isKeyDown(window, GLFW.GLFW_KEY_A);
        boolean s = InputConstants.isKeyDown(window, GLFW.GLFW_KEY_S);
        boolean d = InputConstants.isKeyDown(window, GLFW.GLFW_KEY_D);
        boolean lmb = InputConstants.isKeyDown(window, GLFW.GLFW_MOUSE_BUTTON_LEFT);
        boolean rmb = InputConstants.isKeyDown(window, GLFW.GLFW_MOUSE_BUTTON_RIGHT);
        boolean space = InputConstants.isKeyDown(window, GLFW.GLFW_KEY_SPACE);

        // Row 1: W
        drawKey(poseStack, font, "W", startX + 22, startY, 20, 20, w, accent);

        // Row 2: A S D
        drawKey(poseStack, font, "A", startX, startY + 22, 20, 20, a, accent);
        drawKey(poseStack, font, "S", startX + 22, startY + 22, 20, 20, s, accent);
        drawKey(poseStack, font, "D", startX + 44, startY + 22, 20, 20, d, accent);

        int nextY = startY + 44;

        // Row 3: LMB & RMB (with CPS)
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
        int pillX = 8;
        int pillY = 8;

        // FPS Pill
        if (FPSMod.INSTANCE != null && FPSMod.INSTANCE.isEnabled()) {
            String fps = mc.fpsString.split(" ")[0] + " FPS";
            int w = font.width(fps) + 12;
            RenderUtils.drawBorderedRect(poseStack, pillX, pillY, w, 15, 1, 0xAA0F172A, 0x44334155);
            RenderUtils.drawRect(poseStack, pillX, pillY, 2, 15, accent);
            font.drawShadow(poseStack, fps, pillX + 6, pillY + 4, 0xFFFFFFFF);
            pillX += w + 6;
        }

        // CPS Pill
        if (CPSMod.INSTANCE != null && CPSMod.INSTANCE.isEnabled() && Keystrokes.INSTANCE != null) {
            String cps = Keystrokes.INSTANCE.getLeftCPS() + " | " + Keystrokes.INSTANCE.getRightCPS() + " CPS";
            int w = font.width(cps) + 12;
            RenderUtils.drawBorderedRect(poseStack, pillX, pillY, w, 15, 1, 0xAA0F172A, 0x44334155);
            RenderUtils.drawRect(poseStack, pillX, pillY, 2, 15, accent);
            font.drawShadow(poseStack, cps, pillX + 6, pillY + 4, 0xFFFFFFFF);
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
        // Equipped Armor (helmet, chest, legs, boots)
        for (int i = 3; i >= 0; i--) {
            ItemStack stack = mc.player.inventory.armor.get(i);
            if (!stack.isEmpty()) items.add(stack);
        }
        // Main hand item
        if (!mc.player.getMainHandItem().isEmpty()) {
            items.add(mc.player.getMainHandItem());
        }

        if (items.isEmpty()) return;

        int startX = screenW / 2 + 95;
        int startY = screenH - 22;

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

                int screenW = mc.getWindow().getGuiScaledWidth();
                int screenH = mc.getWindow().getGuiScaledHeight();

                int cardW = 140;
                int cardH = 42;
                int cardX = (screenW - cardW) / 2;
                int cardY = screenH / 2 + 30;

                // Card background
                RenderUtils.drawBorderedRect(poseStack, cardX, cardY, cardW, cardH, 1, 0xDD0F172A, 0xFF334155);

                // Target Name & Distance
                String name = entity.getName().getString();
                font.drawShadow(poseStack, name, cardX + 10, cardY + 8, 0xFFFFFFFF);

                float dist = (float) Math.round(mc.player.distanceTo(entity) * 10.0f) / 10.0f;
                String distStr = dist + "m";
                font.drawShadow(poseStack, distStr, cardX + cardW - font.width(distStr) - 10, cardY + 8, 0xFF94A3B8);

                // Health Bar
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

                // Health text overlay
                String hpText = (int) health + " HP";
                font.drawShadow(poseStack, hpText, barX + (barW - font.width(hpText)) / 2.0f, barY, 0xFFFFFFFF);
            }
        }

    private void renderCrosshair(PoseStack poseStack, Minecraft mc) {
        CustomCrosshair ch = CustomCrosshair.INSTANCE;
        if (ch == null || !ch.isEnabled()) return;

        int cx = mc.getWindow().getGuiScaledWidth() / 2;
        int cy = mc.getWindow().getGuiScaledHeight() / 2;

        int size = ch.size.getValue().intValue();
        int gap = ch.gap.getValue().intValue();
        int color = 0xFF000000 | (ThemeManager.INSTANCE.getAccent() & 0x00FFFFFF);

        // Center dot
        if (ch.dot.getValue()) {
            RenderUtils.drawRect(poseStack, cx, cy, 1, 1, 0xFFFFFFFF);
        }

        if (ch.style.is("Cross")) {
            // Top
            RenderUtils.drawRect(poseStack, cx, cy - gap - size, 1, size, color);
            // Bottom
            RenderUtils.drawRect(poseStack, cx, cy + gap + 1, 1, size, color);
            // Left
            RenderUtils.drawRect(poseStack, cx - gap - size, cy, size, 1, color);
            // Right
            RenderUtils.drawRect(poseStack, cx + gap + 1, cy, size, 1, color);
        } else if (ch.style.is("Box")) {
            RenderUtils.drawBorder(poseStack, cx - size, cy - size, size * 2 + 1, size * 2 + 1, 1, color);
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

            // Module pill background
            RenderUtils.drawRect(poseStack, cardX, currentY, cardW, cardH, 0xAA0F172A);
            // Right accent bar
            RenderUtils.drawRect(poseStack, cardX + cardW - 2, currentY, 2, cardH, color);

            font.drawShadow(poseStack, text, cardX + 4, currentY + 3, color);

            currentY += cardH + 2;
            offset++;
        }
    }
}
