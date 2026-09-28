package astra.mod.client.module.impl.render;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.BooleanSetting;
import astra.mod.client.setting.ModeSetting;
import astra.mod.client.setting.NumberSetting;

public class CustomCrosshair extends Module {
    public static CustomCrosshair INSTANCE;

    public final ModeSetting style = new ModeSetting("Style", "Стиль прицела", "Cross", "Cross", "Dot", "Box");
    public final NumberSetting size = new NumberSetting("Size", "Размер", 4.0, 1.0, 12.0, 1.0);
    public final NumberSetting gap = new NumberSetting("Gap", "Зазор", 2.0, 0.0, 8.0, 1.0);
    public final BooleanSetting dot = new BooleanSetting("CenterDot", "Точка в центре", true);

    public CustomCrosshair() {
        super("Crosshair", "Кастомизация прицела в стиле PvP клиентов", Category.RENDER);
        INSTANCE = this;
        addSettings(style, size, gap, dot);
    }
}
