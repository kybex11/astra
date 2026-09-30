package astra.mod.client.module.impl.movement;

import astra.mod.client.Category;
import astra.mod.client.module.Module;

public class ToggleSneak extends Module {
    public static ToggleSneak INSTANCE;

    public ToggleSneak() {
        super("ToggleSneak", "Постоянное приседание без удержания Shift", Category.MOVEMENT);
        INSTANCE = this;
        setEnabled(false);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.options == null) return;
        if (mc.screen != null) {
            mc.options.keyShift.setDown(false);
            return;
        }
        mc.options.keyShift.setDown(true);
    }

    @Override
    public void onDisable() {
        try {
            if (mc.options != null) mc.options.keyShift.setDown(false);
        } catch (Exception ignored) {}
    }
}
