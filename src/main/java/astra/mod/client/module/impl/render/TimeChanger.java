package astra.mod.client.module.impl.render;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.ModeSetting;
import astra.mod.client.setting.NumberSetting;

public class TimeChanger extends Module {
    public static TimeChanger INSTANCE;

    public final ModeSetting mode = new ModeSetting("Time", "Время суток", "Day", "Day", "Night", "Sunset", "Custom");
    public final NumberSetting customTime = new NumberSetting("Ticks", "Пользовательское время", 6000.0, 0.0, 24000.0, 500.0);

    public TimeChanger() {
        super("TimeChanger", "Локальное изменение времени суток на клиенте", Category.RENDER);
        INSTANCE = this;
        addSettings(mode, customTime);
    }

    public long getTargetTime() {
        if (mode.is("Day")) return 6000L;
        if (mode.is("Sunset")) return 12000L;
        if (mode.is("Night")) return 18000L;
        return customTime.getValue().longValue();
    }
}
