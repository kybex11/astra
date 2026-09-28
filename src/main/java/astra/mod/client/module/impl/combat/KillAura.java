package astra.mod.client.module.impl.combat;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.BooleanSetting;
import astra.mod.client.setting.NumberSetting;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;

public class KillAura extends Module {
    public static KillAura INSTANCE;

    public final NumberSetting range = new NumberSetting("Range", "Дистанция атаки", 3.8, 2.0, 6.0, 0.1);
    public final BooleanSetting cooldown = new BooleanSetting("1.9 Delay", "Максимальный урон при полной готовности оружия", true);
    public final NumberSetting cps = new NumberSetting("CPS", "Скорость ударов (если 1.9 Delay выключен)", 12.0, 1.0, 20.0, 1.0);
    public final BooleanSetting autoJump = new BooleanSetting("AutoJump", "Прыжок для критических ударов", true);
    public final BooleanSetting comboLock = new BooleanSetting("ComboLock", "Сброс спринта для максимального комбо-откидывания", true);
    public final BooleanSetting playersOnly = new BooleanSetting("PlayersOnly", "Атаковать только игроков", true);
    public final BooleanSetting mobs = new BooleanSetting("Mobs", "Атаковать враждебных мобов", false);

    private long lastAttackTime = 0L;
    public LivingEntity currentTarget = null;

    public KillAura() {
        super("KillAura", "Автоматически атакует врагов с умным таймингом комбо и критов", Category.COMBAT);
        INSTANCE = this;
        addSettings(range, cooldown, cps, autoJump, comboLock, playersOnly, mobs);
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
                Player p = (Player) living;
                if (p.isCreative() || p.isSpectator()) continue;
            } else {
                if (playersOnly.getValue()) continue;
                if (!mobs.getValue() || !(living instanceof Monster)) continue;
            }

            double distSq = mc.player.distanceToSqr(living);
            if (distSq <= closestDist) {
                closestDist = distSq;
                bestTarget = living;
            }
        }

        currentTarget = bestTarget;
        if (bestTarget == null) return;

        // Check attack readiness
        boolean canAttack = false;
        long now = System.currentTimeMillis();

        if (cooldown.getValue()) {
            canAttack = mc.player.getAttackStrengthScale(0.0f) >= 0.95f;
        } else {
            long delay = (long) (1000.0 / cps.getValue());
            canAttack = (now - lastAttackTime) >= delay;
        }

        if (canAttack) {
            // AutoJump for guaranteed critical hits
            if (autoJump.getValue() && mc.player.isOnGround() && !mc.player.isInWater()) {
                mc.player.setDeltaMovement(mc.player.getDeltaMovement().x, 0.42, mc.player.getDeltaMovement().z);
                mc.player.hasImpulse = true;
            }

            // Attack target
            if (mc.gameMode != null) {
                mc.gameMode.attack(mc.player, bestTarget);
                mc.player.swing(InteractionHand.MAIN_HAND);
                lastAttackTime = now;

                // Combo Lock (Sprint Reset on hit)
                if (comboLock.getValue()) {
                    mc.player.setSprinting(false);
                    if (mc.player.input != null && mc.player.input.hasForwardImpulse()) {
                        mc.player.setSprinting(true);
                    }
                }
            }
        }
    }

    @Override
    public void onDisable() {
        currentTarget = null;
    }
}
