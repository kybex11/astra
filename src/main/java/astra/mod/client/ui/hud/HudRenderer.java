package astra.mod.client.ui.hud;

import astra.mod.client.module.Module;
import astra.mod.client.module.ModuleManager;
import astra.mod.client.module.impl.render.HUD;
import astra.mod.client.util.RenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class HudRenderer {
    public static final HudRenderer INSTANCE = new HudRenderer();

    public void render(PoseStack poseStack, float tickDelta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.player == null) return;

        HUD hud = ModuleManager.INSTANCE.getModule(HUD.class);
        if (hud == null || !hud.isEnabled()) return;

        Font font = mc.font;

        // 1. Watermark
        if (hud.watermark.getValue()) {
            String title = "ASTRA";
            String sub = " | 1.16.5";
            int totalW = font.width(title) + font.width(sub) + 14;
            int h = 18;
            int x = 8;
            int y = 8;

            // Card background & glowing border
            RenderUtils.drawBorderedRect(poseStack, x, y, totalW, h, 1, 0xCC111827, 0xFF4F46E5);
            // Left gradient indicator
            RenderUtils.drawGradientV(poseStack, x, y, 2, h, 0xFF6366F1, 0xFFA855F7);

            font.drawShadow(poseStack, title, x + 6, y + 5, 0xFF818CF8);
            font.drawShadow(poseStack, sub, x + 6 + font.width(title), y + 5, 0xFFCBD5E1);
        }

        // 2. Active Modules (ArrayList)
        if (hud.arrayList.getValue()) {
            List<Module> activeModules = ModuleManager.INSTANCE.getModules().stream()
                    .filter(Module::isEnabled)
                    .filter(m -> !(m instanceof HUD))
                    .sorted(Comparator.comparingInt(m -> -font.width(m.getName())))
                    .collect(Collectors.toList());

            int screenW = mc.getWindow().getGuiScaledWidth();
            int currentY = 8;

            int[] colors = new int[]{
                    0xFF6366F1, 0xFF7C3AED, 0xFF8B5CF6, 0xFFA855F7, 0xFFC084FC
            };

            int colorIndex = 0;
            for (Module m : activeModules) {
                String text = m.getName();
                int textW = font.width(text);
                int cardW = textW + 10;
                int cardH = 13;
                int cardX = screenW - cardW - 6;

                int color = colors[colorIndex % colors.length];
                colorIndex++;

                // Translucent module pill
                RenderUtils.drawRect(poseStack, cardX, currentY, cardW, cardH, 0xAA0F172A);
                // Right accent bar
                RenderUtils.drawRect(poseStack, cardX + cardW - 2, currentY, 2, cardH, color);

                font.drawShadow(poseStack, text, cardX + 4, currentY + 3, color);

                currentY += cardH + 2;
            }
        }
    }
}
