package astra.mod.client.module.impl.render;

import astra.mod.client.Category;
import astra.mod.client.module.Module;

public class ComboCounter extends Module {
    public static final ComboCounter INSTANCE = new ComboCounter();

    private int combo = 0;
    private long lastHitTime = 0;

    public ComboCounter() {
        super("ComboCounter", "Отображает счетчик серии комбо ударов по противнику", Category.HUD);
        setEnabled(true);
    }

    public void onHit() {
        long now = System.currentTimeMillis();
        if (now - lastHitTime < 2500) {
            combo++;
        } else {
            combo = 1;
        }
        lastHitTime = now;
    }

    public void onTakeDamage() {
        combo = 0;
    }

    public int getCombo() {
        if (System.currentTimeMillis() - lastHitTime > 2500) {
            combo = 0;
        }
        return combo;
    }
}
