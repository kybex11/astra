package astra.mod.client.module.impl.render;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.BooleanSetting;

public class FastRender extends Module {
    public static FastRender INSTANCE;

    public final BooleanSetting entityCulling = new BooleanSetting("EntityCulling", "Не рендерить скрытых за стенами сущностей для FPS", true);
    public final BooleanSetting fastMath = new BooleanSetting("FastMath", "Оптимизация математических тригонометрических расчетов", true);
    public final BooleanSetting reduceShadows = new BooleanSetting("ReduceShadows", "Упрощение теней сущностей для повышения FPS", true);

    public FastRender() {
        super("FastRender", "Комплексная оптимизация игры, устранение фризов и Entity Culling", Category.RENDER);
        INSTANCE = this;
        addSettings(entityCulling, fastMath, reduceShadows);
        setEnabled(true);
    }
}
