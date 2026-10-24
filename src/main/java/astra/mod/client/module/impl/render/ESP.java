package astra.mod.client.module.impl.render;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.BooleanSetting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;

public class ESP extends Module {
    public static ESP INSTANCE;

    public final BooleanSetting box3d = new BooleanSetting("3DBox", "Отображать 3D боксы сквозь стены", true);
    public final BooleanSetting healthBar = new BooleanSetting("HealthBar", "Индикатор здоровья на теле игрока", true);
    public final BooleanSetting nametags = new BooleanSetting("Nametags", "Имя и точные HP над головой", true);
    public final BooleanSetting tracers = new BooleanSetting("Tracers", "Линии до игроков от центра экрана", true);
    public final BooleanSetting detectAdmins = new BooleanSetting("Admins", "Детектировать админов и спектаторов", true);
    public final BooleanSetting players = new BooleanSetting("Players", "Подсвечивать игроков", true);
    public final BooleanSetting mobs = new BooleanSetting("Mobs", "Подсвечивать монстров", true);

    public ESP() {
        super("ESP", "Подсветка 3D боксов, хитбоксов и трейсеров сквозь стены", Category.RENDER);
        INSTANCE = this;
        addSettings(box3d, healthBar, nametags, tracers, detectAdmins, players, mobs);
        setEnabled(true);
    }

    public boolean shouldGlow(Entity entity) {
        if (!isEnabled() || mc.player == null) return false;
        if (entity == mc.player) return false;

        if (entity instanceof Player) {
            Player p = (Player) entity;
            if (p.isSpectator() && detectAdmins.getValue()) return true;
            return players.getValue();
        }

        if (entity instanceof Monster) {
            return mobs.getValue();
        }

        return false;
    }
}
