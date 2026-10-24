package astra.mod.client.presence;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AstraPresenceManager {
    public static final AstraPresenceManager INSTANCE = new AstraPresenceManager();
    public static final ResourceLocation CHANNEL_PRESENCE = new ResourceLocation("astra", "presence");

    private final Set<UUID> astraUsers = Collections.synchronizedSet(new HashSet<>());
    private final Set<String> astraNames = Collections.synchronizedSet(new HashSet<>());
    private boolean showBadge = true;

    public void registerUser(UUID uuid, String name) {
        if (uuid != null) astraUsers.add(uuid);
        if (name != null && !name.trim().isEmpty()) astraNames.add(name.toLowerCase().trim());
    }

    public boolean isAstraUser(PlayerInfo info) {
        if (info == null || info.getProfile() == null) return false;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && info.getProfile().getId().equals(mc.player.getUUID())) {
            return true;
        }
        if (astraUsers.contains(info.getProfile().getId())) return true;
        String name = info.getProfile().getName();
        return name != null && astraNames.contains(name.toLowerCase());
    }

    public Component formatTabName(PlayerInfo info, Component original) {
        if (!showBadge || !isAstraUser(info)) {
            return original;
        }

        MutableComponent badge = new TextComponent("★ ")
                .withStyle(ChatFormatting.AQUA);

        Component baseName = original != null ? original : new TextComponent(info.getProfile().getName());
        return badge.append(baseName);
    }

    public boolean isShowBadge() {
        return showBadge;
    }

    public void setShowBadge(boolean showBadge) {
        this.showBadge = showBadge;
    }

    public void clear() {
        astraUsers.clear();
        astraNames.clear();
    }
}
