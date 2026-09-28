package astra.mod.client.module.impl.render;

import astra.mod.client.Category;
import astra.mod.client.module.Module;

public class DarkTheme extends Module {
    public static DarkTheme INSTANCE;

    public DarkTheme() {
        super("DarkTheme", "Заменяет стандартный фон меню Minecraft на стильную темную тему", Category.RENDER);
        INSTANCE = this;
        setEnabled(true);
    }
}
