package astra.mod.client.module.impl.movement;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.NumberSetting;

public class FastClimb extends Module {
    public static final FastClimb INSTANCE = new FastClimb();

    private final NumberSetting speed = new NumberSetting("Скорость подъема", "Множитель скорости движения по лестницам", 0.35, 0.15, 0.8, 0.05);

    public FastClimb() {
        super("FastClimb", "Ускоренный подъем по лестницам и лианам", Category.MOVEMENT);
        addSettings(speed);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        if (mc.player.onClimbable() && mc.player.horizontalCollision) {
            mc.player.setDeltaMovement(mc.player.getDeltaMovement().x, speed.getValue(), mc.player.getDeltaMovement().z);
        }
    }
}
