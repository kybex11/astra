package astra.mod.client.module.impl.render;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.BooleanSetting;

public class NoRender extends Module {
    public static NoRender INSTANCE;

    public final BooleanSetting weather = new BooleanSetting("NoWeather", "Отключает дождь и снег", true);
    public final BooleanSetting blindness = new BooleanSetting("NoBlindness", "Отключает слепоту и тьму", true);
    public final BooleanSetting hurtCam = new BooleanSetting("NoHurtCam", "Отключает тряску камеры от урона", true);
    public final BooleanSetting fire = new BooleanSetting("NoFireOverlay", "Убирает пламя на экране при горении", true);

    public NoRender() {
        super("NoRender", "Отключает мешающие визуальные эффекты и повышает FPS", Category.RENDER);
        INSTANCE = this;
        addSettings(weather, blindness, hurtCam, fire);
        setEnabled(true);
    }
}
