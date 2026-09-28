package astra.mod.client.module.impl.player;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.BooleanSetting;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

public class AntiFire extends Module {
    public static final AntiFire INSTANCE = new AntiFire();

    public final BooleanSetting instantExtinguish = new BooleanSetting("AutoExtinguish", "Мгновенное тушение огня", true);
    public final BooleanSetting packetSpam = new BooleanSetting("PacketCancel", "Пакетное предотвращение горения", true);

    public AntiFire() {
        super("AntiFire", "Защита от огня, лавы и эффекта горения (Auto-Extinguish)", Category.PLAYER);
        addSettings(instantExtinguish, packetSpam);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null) return;

        if (mc.player.isOnFire() || mc.player.getRemainingFireTicks() > 0) {
            if (instantExtinguish.getValue()) {
                mc.player.clearFire();
                mc.player.setRemainingFireTicks(0);
            }

            if (packetSpam.getValue() && mc.player.isOnGround() && mc.getConnection() != null) {
                // Send ground packets to rapidly clear burning tick on servers
                for (int i = 0; i < 20; i++) {
                    mc.getConnection().send(new ServerboundMovePlayerPacket(mc.player.isOnGround()));
                }
            }
        }
    }
}
