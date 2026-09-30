package astra.mod.client.module.impl.render;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.NumberSetting;

public class FOVChanger extends Module {
    public static FOVChanger INSTANCE;

    public final NumberSetting fov = new NumberSetting("FOV", "Угол обзора камеры", 90.0, 30.0, 120.0, 1.0);

    private double previousFov = 90.0;
    private boolean captured = false;

    public FOVChanger() {
        super("FOVChanger", "Свой угол обзора камеры", Category.RENDER);
        INSTANCE = this;
        addSettings(fov);
        setEnabled(false);
    }

    @Override
    public void onEnable() {
        if (mc.options != null) {
            previousFov = mc.options.fov;
            captured = true;
        }
    }

    @Override
    public void onTick() {
        if (mc.options != null && isEnabled()) {
            mc.options.fov = fov.getValue();
        }
    }

    @Override
    public void onDisable() {
        if (mc.options != null && captured) {
            mc.options.fov = previousFov;
            captured = false;
        }
    }
}
