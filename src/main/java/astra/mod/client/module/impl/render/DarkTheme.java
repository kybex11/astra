package astra.mod.client.module.impl.render;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.BooleanSetting;

public class DarkTheme extends Module {
    public static DarkTheme INSTANCE;

    public final BooleanSetting particles = new BooleanSetting("Particles", "Фоновые частицы созвездий в меню", true);
    public final BooleanSetting deepDark = new BooleanSetting("DeepObsidian", "Глубокий обсидиановый градиент", true);

    public DarkTheme() {
        super("DarkTheme", "Заменяет стандартный фон меню Minecraft на стильную темную тему", Category.RENDER);
        INSTANCE = this;
        addSettings(particles, deepDark);
        setEnabled(true);
    }
}
