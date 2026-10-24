package astra.mod.client.module.impl.render;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.NumberSetting;

public class DamageTint extends Module {
    public static DamageTint INSTANCE;

    public final NumberSetting threshold = new NumberSetting("Порог HP", "Ниже скольки HP показывать красный виньетку", 6.0, 2.0, 20.0, 1.0);

    public DamageTint() {
        super("DamageTint", "Красное затемнение экрана при низком здоровье", Category.RENDER);
        INSTANCE = this;
        addSettings(threshold);
        setEnabled(true);
    }
}
