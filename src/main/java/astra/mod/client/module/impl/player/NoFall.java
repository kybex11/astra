package astra.mod.client.module.impl.player;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.ModeSetting;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

public class NoFall extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "Способ обхода урона", "Packet", "Packet", "Catch");

    public NoFall() {
        super("NoFall", "Защищает от урона при падении с высоты", Category.PLAYER);
        addSettings(mode);
    }

    @Override
    public void onTick() {
        if (mc.player != null && mc.player.fallDistance > 2.5f) {
            if (mc.getConnection() != null && mode.is("Packet")) {
                mc.getConnection().send(new ServerboundMovePlayerPacket(true));
            }
        }
    }
}
