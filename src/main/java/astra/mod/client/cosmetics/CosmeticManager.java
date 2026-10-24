package astra.mod.client.cosmetics;

import astra.mod.mixin.MinecraftAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.resources.ResourceLocation;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class CosmeticManager {
    public static final CosmeticManager INSTANCE = new CosmeticManager();
    public static final ResourceLocation ASTRA_CAPE = new ResourceLocation("astra", "textures/capes/astra_cape.png");

    private String systemNick = "";
    private boolean capeEnabled = true;
    private boolean wingsEnabled = true;
    private boolean haloEnabled = true;
    private boolean hornsEnabled = false;
    private String wingsStyle = "Dragon"; // Dragon, Angel, Neon

    public boolean isWingsEnabled() {
        return wingsEnabled;
    }

    public void setWingsEnabled(boolean wingsEnabled) {
        this.wingsEnabled = wingsEnabled;
    }

    public boolean isHaloEnabled() {
        return haloEnabled;
    }

    public void setHaloEnabled(boolean haloEnabled) {
        this.haloEnabled = haloEnabled;
    }

    public boolean isHornsEnabled() {
        return hornsEnabled;
    }

    public void setHornsEnabled(boolean hornsEnabled) {
        this.hornsEnabled = hornsEnabled;
    }

    public String getWingsStyle() {
        return wingsStyle;
    }

    public void setWingsStyle(String wingsStyle) {
        this.wingsStyle = wingsStyle;
    }

    public boolean hasCustomName() {
        return systemNick != null && !systemNick.trim().isEmpty();
    }

    public String getCustomName() {
        return systemNick;
    }

    public void setSystemNick(String name) {
        if (name == null || name.trim().isEmpty()) {
            this.systemNick = "";
            return;
        }
        this.systemNick = name.trim();
        try {
            Minecraft mc = Minecraft.getInstance();
            String uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + this.systemNick).getBytes(StandardCharsets.UTF_8)).toString();
            User newUser = new User(this.systemNick, uuid, "0", "mojang");
            if (mc instanceof MinecraftAccessor) {
                ((MinecraftAccessor) mc).setUser(newUser);
            }

            // Auto-reconnect to current server if in multiplayer
            if (mc.getCurrentServer() != null && mc.level != null && !mc.isLocalServer()) {
                net.minecraft.client.multiplayer.ServerData serverData = mc.getCurrentServer();
                mc.level.disconnect();
                mc.clearLevel();
                mc.setScreen(new net.minecraft.client.gui.screens.ConnectScreen(
                        new net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen(new net.minecraft.client.gui.screens.TitleScreen()),
                        mc,
                        serverData
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isCapeEnabled() {
        return capeEnabled;
    }

    public void setCapeEnabled(boolean capeEnabled) {
        this.capeEnabled = capeEnabled;
    }
}
