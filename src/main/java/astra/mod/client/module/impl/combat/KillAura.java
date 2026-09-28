package astra.mod.client.module.impl.combat;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.BooleanSetting;
import astra.mod.client.setting.NumberSetting;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;

import java.util.Random;

public class KillAura extends Module {
    public static KillAura INSTANCE;

    public final NumberSetting range = new NumberSetting("Range", "Радиус атаки (блоки)", 3.0, 2.0, 3.2, 0.05);
    public final BooleanSetting cooldown = new BooleanSetting("1.9 Delay", "Максимальный урон при полной готовности оружия", true);
    public final NumberSetting cps = new NumberSetting("CPS", "Скорость ударов (если 1.9 Delay выключен)", 10.0, 1.0, 16.0, 1.0);
    public final BooleanSetting legitAim = new BooleanSetting("SmoothAim", "Плавное человеческое наведение (AntiCheat Bypass)", true);
    public final BooleanSetting raytrace = new BooleanSetting("Raytrace", "Проверка видимости цели (не бить сквозь стены)", true);
    public final BooleanSetting autoJump = new BooleanSetting("AutoJump", "Прыжок для критических ударов", false);
    public final BooleanSetting comboLock = new BooleanSetting("ComboLock", "Сброс спринта для максимального комбо", true);
    public final BooleanSetting targetPlayers = new BooleanSetting("Players", "Атаковать игроков", true);
    public final BooleanSetting targetMonsters = new BooleanSetting("Monsters", "Атаковать враждебных мобов", true);
    public final BooleanSetting targetAnimals = new BooleanSetting("Animals", "Атаковать животных", false);

    private long lastAttackTime = 0L;
    public LivingEntity currentTarget = null;
    private final Random random = new Random();

    public KillAura() {
        super("KillAura", "Автоматически атакует врагов с умным таймингом комбо и критов", Category.COMBAT);
        INSTANCE = this;
        addSettings(range, cooldown, cps, legitAim, raytrace, autoJump, comboLock, targetPlayers, targetMonsters, targetAnimals);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null || mc.player.isDeadOrDying()) {
            currentTarget = null;
            return;
        }

        double maxDist = range.getValue();
        LivingEntity bestTarget = null;
        double closestDist = maxDist * maxDist;

        for (Entity e : mc.level.entitiesForRendering()) {
            if (!(e instanceof LivingEntity) || e == mc.player) continue;
            LivingEntity living = (LivingEntity) e;
            if (!living.isAlive() || living.isDeadOrDying()) continue;

            if (living instanceof Player) {
                if (!targetPlayers.getValue()) continue;
                Player p = (Player) living;
                if (p.isCreative() || p.isSpectator()) continue;
            } else if (living instanceof Monster) {
                if (!targetMonsters.getValue()) continue;
            } else if (living instanceof Animal) {
                if (!targetAnimals.getValue()) continue;
            } else {
                if (!targetMonsters.getValue() && !targetAnimals.getValue()) continue;
            }

            if (raytrace.getValue() && !mc.player.canSee(living)) continue;

            double distSq = mc.player.distanceToSqr(living);
            if (distSq <= closestDist) {
                closestDist = distSq;
                bestTarget = living;
            }
        }

        currentTarget = bestTarget;
        if (bestTarget == null) return;

        // Human-like smooth rotation with exact Mouse Sensitivity GCD Quantization (Bypasses Matrix & Grim ka.abt.ses)
        float[] targetRots = getRotations(mc.player, bestTarget);
        if (legitAim.getValue()) {
            float f = (float) (mc.options.sensitivity * 0.6F + 0.2F);
            float gcd = f * f * f * 1.2F;
            if (gcd < 0.0001f) gcd = 0.001f;

            float yawDiff = Mth.wrapDegrees(targetRots[0] - mc.player.yRot);
            float pitchDiff = Mth.wrapDegrees(targetRots[1] - mc.player.xRot);

            float speed = 20.0f + random.nextFloat() * 8.0f;
            float stepYaw = Mth.clamp(yawDiff, -speed, speed);
            float stepPitch = Mth.clamp(pitchDiff, -speed * 0.5f, speed * 0.5f);

            int yawSteps = Math.round(stepYaw / gcd);
            int pitchSteps = Math.round(stepPitch / gcd);

            mc.player.yRot += yawSteps * gcd;
            mc.player.xRot = Mth.clamp(mc.player.xRot + pitchSteps * gcd, -90.0f, 90.0f);
        } else {
            mc.player.yRot = targetRots[0];
            mc.player.xRot = targetRots[1];
        }

        // FOV check: only attack if facing target (< 30 deg)
        float currentYawDiff = Math.abs(Mth.wrapDegrees(targetRots[0] - mc.player.yRot));
        if (currentYawDiff > 30.0f) return;

        // Strict reach double-check
        if (mc.player.distanceTo(bestTarget) > range.getValue()) return;

        // Attack timing check
        boolean canAttack;
        long now = System.currentTimeMillis();

        if (cooldown.getValue()) {
            canAttack = mc.player.getAttackStrengthScale(0.0f) >= 0.92f;
        } else {
            long delay = (long) (1000.0 / (cps.getValue() + (random.nextDouble() * 2.0 - 1.0)));
            canAttack = (now - lastAttackTime) >= delay;
        }

        if (canAttack) {
            // AutoJump for critical hits
            if (autoJump.getValue() && mc.player.isOnGround() && !mc.player.isInWater()) {
                mc.player.setDeltaMovement(mc.player.getDeltaMovement().x, 0.42, mc.player.getDeltaMovement().z);
                mc.player.hasImpulse = true;
            }

            // Execute Attack
            if (mc.gameMode != null) {
                mc.gameMode.attack(mc.player, bestTarget);
                mc.player.swing(InteractionHand.MAIN_HAND);
                mc.player.resetAttackStrengthTicker();
                lastAttackTime = now + random.nextInt(35);

                // Combo sprint reset
                if (comboLock.getValue()) {
                    mc.player.setSprinting(false);
                    if (mc.player.input != null && mc.player.input.hasForwardImpulse()) {
                        mc.player.setSprinting(true);
                    }
                }
            }
        }
    }

    private float[] getRotations(Player player, LivingEntity target) {
        double diffX = target.getX() - player.getX();
        double diffY = (target.getY() + target.getEyeHeight() * 0.65D) - (player.getY() + player.getEyeHeight());
        double diffZ = target.getZ() - player.getZ();
        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float yaw = (float) (Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F);
        float pitch = (float) -Math.toDegrees(Math.atan2(diffY, diffXZ));

        return new float[]{
                player.yRot + Mth.wrapDegrees(yaw - player.yRot),
                player.xRot + Mth.wrapDegrees(pitch - player.xRot)
        };
    }

    @Override
    public void onDisable() {
        currentTarget = null;
    }
}
