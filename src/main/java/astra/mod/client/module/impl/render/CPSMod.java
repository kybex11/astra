package astra.mod.client.module.impl.render;

import astra.mod.client.Category;
import astra.mod.client.module.Module;

public class CPSMod extends Module {
    public static CPSMod INSTANCE;

    public CPSMod() {
        super("CPS", "Индикатор количества кликов в секунду", Category.HUD);
        INSTANCE = this;
        setEnabled(true);
    }
}
