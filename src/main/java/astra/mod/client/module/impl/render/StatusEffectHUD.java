package astra.mod.client.module.impl.render;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.BooleanSetting;

public class StatusEffectHUD extends Module {
    public static StatusEffectHUD INSTANCE;

    public final BooleanSetting showDuration = new BooleanSetting("Длительность", "Показывать оставшееся время эффектов", true);
    public final BooleanSetting showAmplifier = new BooleanSetting("Уровень", "Показывать уровень эффекта (II, III...)", true);

    public StatusEffectHUD() {
        super("StatusEffectHUD", "Активные эффекты зелий с таймером", Category.HUD);
        INSTANCE = this;
        addSettings(showDuration, showAmplifier);
        setEnabled(true);
    }

    public static String formatDuration(int ticks) {
        int totalSec = Math.max(0, ticks / 20);
        int m = totalSec / 60;
        int s = totalSec % 60;
        return String.format("%d:%02d", m, s);
    }
}
