package astra.mod.client.module.impl.combat;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.BooleanSetting;
import astra.mod.client.setting.NumberSetting;

public class KillAura extends Module {
    private final NumberSetting range = new NumberSetting("Range", "Радиус атаки", 3.8, 2.0, 6.0, 0.1);
    private final NumberSetting aps = new NumberSetting("APS", "Ударов в секунду", 12.0, 1.0, 20.0, 1.0);
    private final BooleanSetting playersOnly = new BooleanSetting("Players", "Атаковать только игроков", true);

    public KillAura() {
        super("KillAura", "Автоматически атакует врагов в радиусе действия", Category.COMBAT);
        addSettings(range, aps, playersOnly);
    }
}
