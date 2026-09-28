package astra.mod.client.module.impl.render;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.BooleanSetting;
import astra.mod.client.setting.ModeSetting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;

public class ESP extends Module {
    public static ESP INSTANCE;

    public final ModeSetting mode = new ModeSetting("Mode", "Стиль подсветки", "Glow", "Glow", "Box");
    public final BooleanSetting players = new BooleanSetting("Players", "Подсвечивать игроков", true);
    public final BooleanSetting mobs = new BooleanSetting("Mobs", "Подсвечивать враждебных мобов", false);

    public ESP() {
        super("ESP", "Подсветка силуэтов игроков и сущностей сквозь стены", Category.RENDER);
        INSTANCE = this;
        addSettings(mode, players, mobs);
        setEnabled(true);
    }

    public boolean shouldGlow(Entity entity) {
        if (!isEnabled() || mc.player == null) return false;
        if (entity == mc.player) return false;

        if (entity instanceof Player && players.getValue()) {
            return true;
        }

        if (entity instanceof Monster && mobs.getValue()) {
            return true;
        }

        return false;
    }
}
