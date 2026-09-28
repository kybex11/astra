package astra.mod.client.module.impl.combat;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.BooleanSetting;
import astra.mod.client.setting.NumberSetting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;

public class EntityLauncher extends Module {

    private final NumberSetting launchPower = new NumberSetting("Launch Power", "Сила подброса вверх", 2.0, 0.5, 5.0, 0.1);
    private final NumberSetting range = new NumberSetting("Range", "Радиус действия", 5.0, 1.0, 10.0, 0.5);
    private final BooleanSetting playersOnly = new BooleanSetting("Players Only", "Только игроки", false);

    public EntityLauncher() {
        super("Entity Launcher", "Подбрасывает сущностей в небо", Category.COMBAT, GLFW.GLFW_KEY_J);
        addSettings(launchPower, range, playersOnly);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null) return;

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity == mc.player) continue;
            if (playersOnly.getValue() && !(entity instanceof Player)) continue;
            if (!(entity instanceof LivingEntity)) continue;
            if (mc.player.distanceTo(entity) > range.getValue()) continue;

            entity.setDeltaMovement(
                    entity.getDeltaMovement().x,
                    launchPower.getValue(),
                    entity.getDeltaMovement().z
            );
            entity.hurtMarked = true;
        }
    }
}
