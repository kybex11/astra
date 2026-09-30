package astra.mod.client.module.impl.render;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.BooleanSetting;
import astra.mod.client.setting.NumberSetting;

public class BossBarMod extends Module {
    public static BossBarMod INSTANCE;

    public final BooleanSetting showCustom = new BooleanSetting("Своя полоса", "Рисовать свою полосу босса сверху", true);
    public final BooleanSetting hideVanilla = new BooleanSetting("Скрыть ваниллу", "Скрывать ванильную полосу босса", true);
    public final NumberSetting scale = new NumberSetting("Масштаб", "Размер полосы босса", 1.0, 0.5, 1.5, 0.1);

    public BossBarMod() {
        super("BossBar", "Настройка полосы здоровья боссов", Category.RENDER);
        INSTANCE = this;
        addSettings(showCustom, hideVanilla, scale);
        setEnabled(true);
    }

    public boolean shouldHideVanilla() {
        return isEnabled() && hideVanilla.getValue();
    }

    public boolean shouldShowCustom() {
        return isEnabled() && showCustom.getValue();
    }

    public float getScale() {
        if (!isEnabled()) return 1.0f;
        return scale.getValue().floatValue();
    }
}
