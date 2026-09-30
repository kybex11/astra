package astra.mod.client.module.impl.render;

import astra.mod.client.Category;
import astra.mod.client.module.Module;

public class PingMod extends Module {
    public static PingMod INSTANCE;

    public PingMod() {
        super("Ping", "Задержка соединения с сервером в мс", Category.HUD);
        INSTANCE = this;
        setEnabled(true);
    }

    public static String getPingText() {
        try {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.getConnection() == null || mc.player == null) return "—";
            if (mc.hasSingleplayerServer()) return "0 ms";
            net.minecraft.client.multiplayer.PlayerInfo info =
                    mc.getConnection().getPlayerInfo(mc.player.getGameProfile().getId());
            if (info == null) return "—";
            return info.getLatency() + " ms";
        } catch (Exception e) {
            return "—";
        }
    }
}
