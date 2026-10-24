package astra.mod.client.module.impl.combat;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.BooleanSetting;
import astra.mod.client.setting.NumberSetting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class AimAssist extends Module {
    public static final AimAssist INSTANCE = new AimAssist();

    private final NumberSetting speed = new NumberSetting("Скорость", "Скорость доводки к цели (1-50)", 10.0, 1.0, 50.0, 1.0);
    private final BooleanSetting fastSnap = new BooleanSetting("Быстрая наводка", "Мгновенный захват цели без задержки (Snap)", true);
    private final BooleanSetting bowAim = new BooleanSetting("Аим для лука", "Упреждение цели и расчет баллистики/гравитации стрелы", true);
    private final NumberSetting bowRange = new NumberSetting("Дальность лука", "Дистанция захвата с луком", 48.0, 16.0, 96.0, 4.0);
    private final NumberSetting range = new NumberSetting("Дистанция (Melee)", "Максимальная дистанция ближнего боя", 5.0, 3.0, 8.0, 0.5);
    private final NumberSetting fov = new NumberSetting("Угол обзора (FOV)", "Угол захвата цели перед игроком", 90.0, 20.0, 360.0, 5.0);
    private final BooleanSetting clickOnly = new BooleanSetting("Только при атаке", "Доводить прицел только при зажатой ЛКМ/ПКМ", false);

    public AimAssist() {
        super("AimAssist", "Автоматическая доводка и мгновенный снап на врага (ближний бой и лук)", Category.COMBAT);
        addSettings(speed, fastSnap, bowAim, bowRange, range, fov, clickOnly);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null) return;

        ItemStack held = mc.player.getMainHandItem();
        boolean hasBow = (held.getItem() instanceof BowItem) || (held.getItem() instanceof CrossbowItem);

        if (clickOnly.getValue()) {
            boolean attackDown = mc.options.keyAttack.isDown();
            boolean useDown = mc.options.keyUse.isDown();
            if (!attackDown && !(hasBow && useDown)) return;
        }

        double maxDist = (hasBow && bowAim.getValue()) ? bowRange.getValue() : range.getValue();
        Player bestTarget = null;
        double closestDist = Double.MAX_VALUE;

        Vec3 eyePos = mc.player.getEyePosition(1.0f);

        for (Player other : mc.level.players()) {
            if (other == mc.player || other.isDeadOrDying() || other.isInvisible()) continue;

            double dist = mc.player.distanceTo(other);
            if (dist > maxDist) continue;

            Vec3 targetEye = other.getEyePosition(1.0f);
            double diffX = targetEye.x - eyePos.x;
            double diffY = targetEye.y - eyePos.y;
            double diffZ = targetEye.z - eyePos.z;
            double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

            float targetYaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0f;
            float yawDiff = Math.abs(wrapDegrees(targetYaw - mc.player.yRot));

            if (yawDiff <= fov.getValue() / 2.0f && dist < closestDist) {
                closestDist = dist;
                bestTarget = other;
            }
        }

        if (bestTarget != null) {
            float targetYaw;
            float targetPitch;

            if (hasBow && bowAim.getValue()) {
                // Ballistics & Gravity Prediction
                int useTicks = mc.player.getTicksUsingItem();
                float pull = (held.getItem() instanceof BowItem) ? BowItem.getPowerForTime(useTicks) : 1.0f;
                if (pull < 0.15f) pull = 1.0f; // Default full power prediction

                float arrowVelocity = pull * 3.0f;
                double dist = mc.player.distanceTo(bestTarget);
                double travelTicks = dist / (arrowVelocity * 0.99);

                // Target velocity prediction
                double predX = bestTarget.getX() + (bestTarget.getX() - bestTarget.xOld) * travelTicks;
                double predZ = bestTarget.getZ() + (bestTarget.getZ() - bestTarget.zOld) * travelTicks;

                // Parabolic gravity drop compensation (g = 0.05 blocks/tick^2)
                double gravityDrop = 0.5 * 0.05 * (travelTicks * travelTicks);
                double predY = bestTarget.getY() + bestTarget.getEyeHeight() * 0.75 + gravityDrop;

                double diffX = predX - eyePos.x;
                double diffY = predY - eyePos.y;
                double diffZ = predZ - eyePos.z;
                double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

                targetYaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0f;
                targetPitch = (float) -Math.toDegrees(Math.atan2(diffY, diffXZ));
            } else {
                Vec3 targetEye = bestTarget.getEyePosition(1.0f);
                double diffX = targetEye.x - eyePos.x;
                double diffY = targetEye.y - eyePos.y;
                double diffZ = targetEye.z - eyePos.z;
                double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

                targetYaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0f;
                targetPitch = (float) -Math.toDegrees(Math.atan2(diffY, diffXZ));
            }

            float yawDiff = wrapDegrees(targetYaw - mc.player.yRot);
            float pitchDiff = targetPitch - mc.player.xRot;

            if (fastSnap.getValue()) {
                // Instant / snappy lock
                mc.player.yRot = targetYaw;
                mc.player.xRot = targetPitch;
            } else {
                float step = (float) (speed.getValue() * 0.8f);
                mc.player.yRot += Math.max(-step, Math.min(step, yawDiff));
                mc.player.xRot += Math.max(-step, Math.min(step, pitchDiff));
            }
        }
    }

    private float wrapDegrees(float angle) {
        angle = angle % 360.0f;
        if (angle >= 180.0f) angle -= 360.0f;
        if (angle < -180.0f) angle += 360.0f;
        return angle;
    }
}
