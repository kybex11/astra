package astra.mod.client.module.impl.render;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.BooleanSetting;

public class FogCustomizer extends Module {
    public static FogCustomizer INSTANCE;

    public final BooleanSetting disableTerrainFog = new BooleanSetting("Без тумана", "Полностью убрать туман дальности", true);

    public FogCustomizer() {
        super("FogCustomizer", "Настройка тумана дальности", Category.RENDER);
        INSTANCE = this;
        addSettings(disableTerrainFog);
        setEnabled(false);
    }

    public boolean shouldCancelTerrainFog() {
        return isEnabled() && disableTerrainFog.getValue();
    }
}
