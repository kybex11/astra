package astra.mod.client.module.impl.movement;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.BooleanSetting;

public class Sprint extends Module {
    private final BooleanSetting omniSprint = new BooleanSetting("Omni", "Спринт во все стороны", false);

    public Sprint() {
        super("Sprint", "Автоматически ускоряет бег персонажа", Category.MOVEMENT);
        addSettings(omniSprint);
    }

    @Override
    public void onTick() {
        if (mc.player != null && mc.player.input != null) {
            boolean canSprint = omniSprint.getValue()
                    ? (mc.player.input.forwardImpulse != 0 || mc.player.input.leftImpulse != 0)
                    : mc.player.input.hasForwardImpulse();

            if (canSprint && !mc.player.isShiftKeyDown() && !mc.player.horizontalCollision) {
                mc.player.setSprinting(true);
            }
        }
    }
}
