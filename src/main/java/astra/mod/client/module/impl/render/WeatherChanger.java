package astra.mod.client.module.impl.render;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.ModeSetting;

public class WeatherChanger extends Module {
    public static WeatherChanger INSTANCE;

    public final ModeSetting mode = new ModeSetting("Weather", "Погода", "Clear", "Clear", "Rain", "Thunder");

    public WeatherChanger() {
        super("Weather", "Локальное изменение погоды на клиенте", Category.RENDER);
        INSTANCE = this;
        addSettings(mode);
    }
}
