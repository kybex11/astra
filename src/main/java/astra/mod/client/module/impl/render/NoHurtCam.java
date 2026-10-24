package astra.mod.client.module.impl.render;

import astra.mod.client.Category;
import astra.mod.client.module.Module;

public class NoHurtCam extends Module {
    public static NoHurtCam INSTANCE;

    public NoHurtCam() {
        super("NoHurtCam", "Отключает тряску камеры при получении урона", Category.RENDER);
        INSTANCE = this;
        setEnabled(true);
    }
}
