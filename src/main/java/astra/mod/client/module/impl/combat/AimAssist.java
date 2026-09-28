package astra.mod.client.module.impl.combat;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.BooleanSetting;
import astra.mod.client.setting.NumberSetting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class AimAssist extends Module {
    public static final AimAssist INSTANCE = new AimAssist();

    private final NumberSetting speed = new NumberSetting("Скорость наводки", "Плавность доводки прицела к цели", 4.0, 1.0, 15.0, 0.5);
    private final NumberSetting range = new NumberSetting("Дистанция", "Максимальная дистанция захвата цели", 4.5, 3.0, 8.0, 0.5);
    private final NumberSetting fov = new NumberSetting("Угол обзора (FOV)", "Угол захвата цели перед игроком", 60.0, 20.0, 180.0, 5.0);
    private final BooleanSetting clickOnly = new BooleanSetting("Только при атаке", "Доводить прицел только при зажатой ЛКМ", true);

    public AimAssist() {
        super("AimAssist", "Плавная автоматическая доводка прицела на противника", Category.COMBAT);
        addSettings(speed, range, fov, clickOnly);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null) return;
        if (clickOnly.getValue() && !mc.options.keyAttack.isDown()) return;

        Player bestTarget = null;
        double closestDist = Double.MAX_VALUE;

        for (Player other : mc.level.players()) {
            if (other == mc.player || other.isDeadOrDying() || other.isInvisible()) continue;

            double dist = mc.player.distanceTo(other);
            if (dist > range.getValue()) continue;

            Vec3 eyePos = mc.player.getEyePosition(1.0f);
            Vec3 targetEye = other.getEyePosition(1.0f);

            double diffX = targetEye.x - eyePos.x;
            double diffY = targetEye.y - eyePos.y;
            double diffZ = targetEye.z - eyePos.z;
            double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

            float targetYaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0f;
            float targetPitch = (float) -Math.toDegrees(Math.atan2(diffY, diffXZ));

            float yawDiff = Math.abs(wrapDegrees(targetYaw - mc.player.yRot));
            if (yawDiff <= fov.getValue() / 2.0f && dist < closestDist) {
                closestDist = dist;
                bestTarget = other;
            }
        }

        if (bestTarget != null) {
            Vec3 eyePos = mc.player.getEyePosition(1.0f);
            Vec3 targetEye = bestTarget.getEyePosition(1.0f);

            double diffX = targetEye.x - eyePos.x;
            double diffY = targetEye.y - eyePos.y;
            double diffZ = targetEye.z - eyePos.z;
            double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

            float targetYaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0f;
            float targetPitch = (float) -Math.toDegrees(Math.atan2(diffY, diffXZ));

            float yawDiff = wrapDegrees(targetYaw - mc.player.yRot);
            float pitchDiff = targetPitch - mc.player.xRot;

            float step = (float) (speed.getValue() * 0.4f);
            mc.player.yRot += Math.max(-step, Math.min(step, yawDiff));
            mc.player.xRot += Math.max(-step, Math.min(step, pitchDiff));
        }
    }

    private float wrapDegrees(float angle) {
        angle = angle % 360.0f;
        if (angle >= 180.0f) angle -= 360.0f;
        if (angle < -180.0f) angle += 360.0f;
        return angle;
    }
}
