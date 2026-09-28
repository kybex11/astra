package astra.mod.client.module.impl.render;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.BooleanSetting;

public class TargetHUD extends Module {
    public static TargetHUD INSTANCE;

    public final BooleanSetting showArmor = new BooleanSetting("ShowArmor", "Показывать броню цели", true);
    public final BooleanSetting gradientHealth = new BooleanSetting("Gradient", "Градиентная полоса здоровья", true);

    public TargetHUD() {
        super("TargetHUD", "Информационная панель текущей цели в бою", Category.HUD);
        INSTANCE = this;
        addSettings(showArmor, gradientHealth);
        setEnabled(true);
    }
}
