package astra.mod.client.module.impl.render;

import astra.mod.client.Category;
import astra.mod.client.module.Module;

public class FPSMod extends Module {
    public static FPSMod INSTANCE;

    public FPSMod() {
        super("FPS", "Счетчик кадров в секунду", Category.HUD);
        INSTANCE = this;
        setEnabled(true);
    }
}
