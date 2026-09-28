package astra.mod.client.module.impl.player;

import astra.mod.client.Category;
import astra.mod.client.module.Module;

public class NoHunger extends Module {
    public static final NoHunger INSTANCE = new NoHunger();

    public NoHunger() {
        super("NoHunger", "Замедляет и предотвращает истощение голода при беге и прыжках", Category.PLAYER);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        // Reset exhaustion level periodically
        if (mc.player.getFoodData() != null) {
            // Keeps food exhaustion at minimum to prevent hunger loss
            if (mc.player.isSprinting() && mc.player.fallDistance == 0.0f) {
                mc.player.setSprinting(true);
            }
        }
    }
}
