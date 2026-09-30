package astra.mod.client.module.impl.render;

import astra.mod.client.Category;
import astra.mod.client.module.Module;

public class MomentumMod extends Module {
    public static MomentumMod INSTANCE;

    private double lastX = 0.0;
    private double lastZ = 0.0;
    private boolean hasLast = false;
    private double smoothed = 0.0;

    public MomentumMod() {
        super("Momentum", "Текущая скорость передвижения в блоках/сек", Category.HUD);
        INSTANCE = this;
        setEnabled(true);
    }

    @Override
    public void onTick() {
        if (mc.player == null) {
            hasLast = false;
            smoothed = 0.0;
            return;
        }
        double x = mc.player.getX();
        double z = mc.player.getZ();
        if (hasLast) {
            double dx = x - lastX;
            double dz = z - lastZ;
            double dist = Math.sqrt(dx * dx + dz * dz);
            double bps = dist * 20.0;
            if (bps > 40.0) bps = 0.0; // телепорт — сброс
            smoothed += (bps - smoothed) * 0.25;
            if (smoothed < 0.05) smoothed = 0.0;
        }
        lastX = x;
        lastZ = z;
        hasLast = true;
    }

    public double getSpeed() {
        return Math.round(smoothed * 10.0) / 10.0;
    }
}
