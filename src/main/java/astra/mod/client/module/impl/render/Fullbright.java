package astra.mod.client.module.impl.render;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.NumberSetting;

public class Fullbright extends Module {
    public final NumberSetting brightness = new NumberSetting("Яркость", "Уровень яркости (Lighting Mod)", 100.0, 1.0, 100.0, 1.0);

    private double previousGamma = 1.0;

    public Fullbright() {
        super("Fullbright", "Максимальная яркость, позволяющая видеть в темноте", Category.RENDER);
        addSettings(brightness);
    }

    @Override
    public void onEnable() {
        if (mc.options != null) {
            previousGamma = mc.options.gamma;
            mc.options.gamma = brightness.getValue();
        }
    }

    @Override
    public void onDisable() {
        if (mc.options != null) {
            mc.options.gamma = Math.min(1.0, Math.max(0.0, previousGamma));
        }
    }

    @Override
    public void onTick() {
        if (mc.options != null && mc.options.gamma < brightness.getValue()) {
            mc.options.gamma = brightness.getValue();
        }
    }
}
