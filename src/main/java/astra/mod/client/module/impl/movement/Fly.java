package astra.mod.client.module.impl.movement;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.NumberSetting;

public class Fly extends Module {
    private final NumberSetting speed = new NumberSetting("Speed", "Скорость полёта", 1.0, 0.1, 5.0, 0.1);

    public Fly() {
        super("Fly", "Позволяет летать как в креативе", Category.MOVEMENT);
        addSettings(speed);
    }

    @Override
    public void onEnable() {
        if (mc.player != null) {
            mc.player.abilities.mayfly = true;
            mc.player.abilities.flying = true;
        }
    }

    @Override
    public void onTick() {
        if (mc.player != null) {
            mc.player.abilities.mayfly = true;
            mc.player.abilities.setFlyingSpeed(speed.getValue().floatValue() * 0.05f);
        }
    }

    @Override
    public void onDisable() {
        if (mc.player != null && !mc.player.isCreative()) {
            mc.player.abilities.mayfly = false;
            mc.player.abilities.flying = false;
        }
    }
}
