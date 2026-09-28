package astra.mod.client.module.impl.render;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.BooleanSetting;
import astra.mod.client.setting.NumberSetting;

public class MiniMap extends Module {
    public static MiniMap INSTANCE;

    public final NumberSetting range = new NumberSetting("Range", "Радиус отображения радара (блоки)", 32.0, 16.0, 64.0, 4.0);
    public final NumberSetting size = new NumberSetting("Size", "Размер миникарты (пиксели)", 80.0, 60.0, 120.0, 5.0);
    public final BooleanSetting showPlayers = new BooleanSetting("Players", "Отображать игроков", true);
    public final BooleanSetting showMobs = new BooleanSetting("Mobs", "Отображать монстров", true);
    public final BooleanSetting showAnimals = new BooleanSetting("Animals", "Отображать животных", false);

    public MiniMap() {
        super("MiniMap", "Миникарта / Радар с отображением сущностей и направлений", Category.HUD);
        INSTANCE = this;
        addSettings(range, size, showPlayers, showMobs, showAnimals);
        setEnabled(true);
    }
}
