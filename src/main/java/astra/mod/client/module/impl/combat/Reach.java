package astra.mod.client.module.impl.combat;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.NumberSetting;

public class Reach extends Module {
    public static Reach INSTANCE;

    public final NumberSetting reachDistance = new NumberSetting("Reach", "Дистанция атаки (блоки)", 3.8, 3.0, 6.0, 0.1);

    public Reach() {
        super("Reach", "Увеличивает дистанцию удара по противникам", Category.COMBAT);
        INSTANCE = this;
        addSettings(reachDistance);
    }
}
