package astra.mod.client.module.impl.combat;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.NumberSetting;

public class AutoClicker extends Module {
    private final NumberSetting cps = new NumberSetting("CPS", "Кликов в секунду", 14.0, 1.0, 20.0, 1.0);

    public AutoClicker() {
        super("AutoClicker", "Автоматически кликает ЛКМ при зажатии", Category.COMBAT);
        addSettings(cps);
    }
}
