package astra.mod.client.module.impl.render;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.NumberSetting;

public class MobSize extends Module {
    public static MobSize INSTANCE;

    public final NumberSetting scale = new NumberSetting("Размер", "Масштаб мобов (не игроков)", 1.0, 0.3, 2.5, 0.1);

    public MobSize() {
        super("MobSize", "Изменение размера мобов", Category.RENDER);
        INSTANCE = this;
        addSettings(scale);
        setEnabled(false);
    }
}
