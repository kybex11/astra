package astra.mod.client.module.impl.render;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.BooleanSetting;

public class ArmorHUD extends Module {
    public static ArmorHUD INSTANCE;

    public final BooleanSetting showDurability = new BooleanSetting("Durability", "Показывать прочность", true);
    public final BooleanSetting horizontal = new BooleanSetting("Horizontal", "Горизонтальное отображение", true);

    public ArmorHUD() {
        super("ArmorHUD", "Отображает надетую броню и предмет в руке с прочностью", Category.HUD);
        INSTANCE = this;
        addSettings(showDurability, horizontal);
        setEnabled(true);
    }
}
