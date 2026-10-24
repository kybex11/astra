package astra.mod.client.module.impl.render;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.BooleanSetting;

public class HUD extends Module {
    public final BooleanSetting watermark = new BooleanSetting("Watermark", "Отображать логотип чита", true);
    public final BooleanSetting arrayList = new BooleanSetting("ArrayList", "Список активных модулей", true);

    public HUD() {
        super("HUD", "Отображение графических элементов интерфейса на экране", Category.HUD);
        addSettings(watermark, arrayList);
        setEnabled(true);
    }
}
