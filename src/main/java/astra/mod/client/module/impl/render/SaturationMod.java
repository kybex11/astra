package astra.mod.client.module.impl.render;

import astra.mod.client.Category;
import astra.mod.client.module.Module;

public class SaturationMod extends Module {
    public static SaturationMod INSTANCE;

    public SaturationMod() {
        super("Saturation", "Уровень сытости и насыщения", Category.HUD);
        INSTANCE = this;
        setEnabled(true);
    }

    public static String getText() {
        try {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.player == null) return "—";
            int food = mc.player.getFoodData().getFoodLevel();
            float sat = mc.player.getFoodData().getSaturationLevel();
            return "Food: " + food + " | Sat: " + (Math.round(sat * 10.0f) / 10.0f);
        } catch (Exception e) {
            return "—";
        }
    }
}
