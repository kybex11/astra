package astra.mod.client.module.impl.render;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.NumberSetting;

public class ItemPhysics extends Module {
    public static ItemPhysics INSTANCE;

    public final NumberSetting spinSpeed = new NumberSetting("Скорость", "Скорость вращения предметов", 8.0, 0.0, 30.0, 1.0);

    public ItemPhysics() {
        super("ItemPhysics", "Выпавшие предметы красиво вращаются", Category.RENDER);
        INSTANCE = this;
        addSettings(spinSpeed);
        setEnabled(false);
    }
}
