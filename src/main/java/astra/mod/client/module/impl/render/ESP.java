package astra.mod.client.module.impl.render;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.BooleanSetting;
import astra.mod.client.setting.ModeSetting;

public class ESP extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "Вид подсветки", "Glow", "Box", "Glow");
    private final BooleanSetting players = new BooleanSetting("Players", "Подсвечивать игроков", true);
    private final BooleanSetting mobs = new BooleanSetting("Mobs", "Подсвечивать мобов", false);

    public ESP() {
        super("ESP", "Подсветка сущностей сквозь стены", Category.RENDER);
        addSettings(mode, players, mobs);
    }
}
