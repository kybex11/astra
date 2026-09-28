package astra.mod.client.module.impl.combat;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.NumberSetting;

public class Velocity extends Module {
    public static Velocity INSTANCE;

    public final NumberSetting horizontal = new NumberSetting("Horizontal", "Горизонтальное откидывание (%)", 0.0, 0.0, 100.0, 5.0);
    public final NumberSetting vertical = new NumberSetting("Vertical", "Вертикальное откидывание (%)", 0.0, 0.0, 100.0, 5.0);

    public Velocity() {
        super("Velocity", "Регулировка отдачи и нокбэка от ударов (Anti-KB)", Category.COMBAT);
        INSTANCE = this;
        addSettings(horizontal, vertical);
        setEnabled(true);
    }
}
