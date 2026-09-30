package astra.mod.client.module.impl.render;

import astra.mod.client.Category;
import astra.mod.client.module.Module;

public class ReachDisplay extends Module {
    public static ReachDisplay INSTANCE;

    private double lastReach = 0.0;
    private long lastHitTime = 0L;

    public ReachDisplay() {
        super("ReachDisplay", "Дистанция последнего удара по противнику", Category.HUD);
        INSTANCE = this;
        setEnabled(true);
    }

    public void record(double distance) {
        lastReach = Math.round(distance * 10.0) / 10.0;
        lastHitTime = System.currentTimeMillis();
    }

    public String getText() {
        if (System.currentTimeMillis() - lastHitTime > 5000) return "Reach: —";
        return "Reach: " + lastReach + " m";
    }
}
