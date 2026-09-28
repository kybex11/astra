package astra.mod.client.module.impl.movement;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.ModeSetting;
import astra.mod.client.setting.NumberSetting;

public class Speed extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "Тип разгона", "Legit", "Legit", "Vanilla");
    private final NumberSetting multiplier = new NumberSetting("Factor", "Множитель скорости", 1.5, 1.1, 3.0, 0.1);

    public Speed() {
        super("Speed", "Увеличивает скорость передвижения", Category.MOVEMENT);
        addSettings(mode, multiplier);
    }

    @Override
    public void onTick() {
        if (mc.player != null && mc.player.input != null && mc.player.input.hasForwardImpulse()) {
            if (mode.is("Vanilla") && mc.player.isOnGround()) {
                double factor = multiplier.getValue();
                mc.player.setDeltaMovement(
                        mc.player.getDeltaMovement().x * factor,
                        mc.player.getDeltaMovement().y,
                        mc.player.getDeltaMovement().z * factor
                );
            }
        }
    }
}
