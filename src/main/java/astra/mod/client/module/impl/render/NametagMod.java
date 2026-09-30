package astra.mod.client.module.impl.render;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.BooleanSetting;
import astra.mod.client.setting.NumberSetting;

public class NametagMod extends Module {
    public static NametagMod INSTANCE;

    public final NumberSetting scale = new NumberSetting("Масштаб", "Размер никнеймов игроков", 1.0, 0.5, 2.0, 0.1);
    public final NumberSetting maxDistance = new NumberSetting("Дистанция", "Видеть ники на расстоянии (0 = ванилла)", 0.0, 0.0, 128.0, 4.0);
    public final BooleanSetting showOwn = new BooleanSetting("Свой ник", "Показывать собственный никнейм", false);

    public NametagMod() {
        super("Nametags", "Настройка отображения никнеймов", Category.RENDER);
        INSTANCE = this;
        addSettings(scale, maxDistance, showOwn);
        setEnabled(false);
    }
}
