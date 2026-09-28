package astra.mod.client.module.impl.render;

import astra.mod.client.Category;
import astra.mod.client.module.Module;

public class Fullbright extends Module {
    private double previousGamma = 1.0;

    public Fullbright() {
        super("Fullbright", "Максимальная яркость, позволяющая видеть в темноте", Category.RENDER);
    }

    @Override
    public void onEnable() {
        if (mc.options != null) {
            previousGamma = mc.options.gamma;
            mc.options.gamma = 100.0;
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
        if (mc.options != null && mc.options.gamma < 10.0) {
            mc.options.gamma = 100.0;
        }
    }
}
