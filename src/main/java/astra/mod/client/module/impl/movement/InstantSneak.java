package astra.mod.client.module.impl.movement;

import astra.mod.client.Category;
import astra.mod.client.module.Module;

public class InstantSneak extends Module {
    public static InstantSneak INSTANCE;

    public InstantSneak() {
        super("InstantSneak", "Резкий шифт: мгновенное изменение высоты камеры без плавности (Old Sneak)", Category.MOVEMENT);
        INSTANCE = this;
        setEnabled(true);
    }
}
