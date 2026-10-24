package astra.mod.client.module.impl.movement;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.BooleanSetting;
import astra.mod.client.setting.ModeSetting;
import astra.mod.client.setting.NumberSetting;
import net.minecraft.world.phys.Vec3;

public class AntiSwimming extends Module {
    public static final AntiSwimming INSTANCE = new AntiSwimming();

    public final ModeSetting mode = new ModeSetting("Режим", "Режим движения по воде", "Solid", "Solid", "Dolphin", "SpeedBoost");
    public final NumberSetting speed = new NumberSetting("Скорость", "Скорость ходьбы по воде", 1.2, 1.0, 3.0, 0.1);
    public final BooleanSetting noSlowdown = new BooleanSetting("NoSlowdown", "Убирает замедление в воде", true);

    public AntiSwimming() {
        super("AntiSwimming", "Хождение по воде как по твердой поверхности (Jesus / WaterWalk)", Category.MOVEMENT);
        addSettings(mode, speed, noSlowdown);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null) return;

        if (mc.player.isInWater() || mc.player.isInLava()) {
            Vec3 motion = mc.player.getDeltaMovement();
            if (mode.is("Solid")) {
                if (mc.options.keyJump.isDown()) {
                    mc.player.setDeltaMovement(motion.x * speed.getValue(), 0.35, motion.z * speed.getValue());
                } else if (mc.options.keyShift.isDown()) {
                    mc.player.setDeltaMovement(motion.x * speed.getValue(), -0.15, motion.z * speed.getValue());
                } else {
                    mc.player.setDeltaMovement(motion.x * speed.getValue(), 0.0, motion.z * speed.getValue());
                    mc.player.setOnGround(true);
                    mc.player.fallDistance = 0.0f;
                }
            } else if (mode.is("Dolphin")) {
                mc.player.setDeltaMovement(motion.x * 1.3, mc.player.isInWater() ? 0.3 : motion.y, motion.z * 1.3);
            } else if (mode.is("SpeedBoost")) {
                mc.player.setDeltaMovement(motion.x * speed.getValue(), motion.y, motion.z * speed.getValue());
            }
        }
    }
}
