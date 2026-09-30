package astra.mod.client.module.impl.movement;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.BooleanSetting;

public class ToggleSprint extends Module {
    public static ToggleSprint INSTANCE;

    public final BooleanSetting omni = new BooleanSetting("Omni", "Спринт вбок и назад тоже", true);
    public final BooleanSetting stopOnHunger = new BooleanSetting("Голод", "Не спринтовать при голоде <= 6", true);

    public ToggleSprint() {
        super("ToggleSprint", "Постоянный спринт без удержания W", Category.MOVEMENT);
        INSTANCE = this;
        addSettings(omni, stopOnHunger);
        setEnabled(false);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.player.input == null) return;
        if (mc.player.isShiftKeyDown()) return;
        if (stopOnHunger.getValue() && mc.player.getFoodData().getFoodLevel() <= 6) return;

        boolean moving = omni.getValue()
                ? (mc.player.input.forwardImpulse != 0 || mc.player.input.leftImpulse != 0)
                : mc.player.input.hasForwardImpulse();

        if (moving && !mc.player.horizontalCollision) {
            mc.player.setSprinting(true);
        }
    }
}
