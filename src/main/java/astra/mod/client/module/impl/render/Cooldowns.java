package astra.mod.client.module.impl.render;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.BooleanSetting;

public class Cooldowns extends Module {
    public static Cooldowns INSTANCE;

    public final BooleanSetting showPercent = new BooleanSetting("Проценты", "Показывать процент перезарядки", false);

    public Cooldowns() {
        super("Cooldowns", "Перезарядка предметов хотбара над инвентарём", Category.HUD);
        INSTANCE = this;
        addSettings(showPercent);
        setEnabled(true);
    }
}
