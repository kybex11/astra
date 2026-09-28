package astra.mod.client.cosmetics;

import net.minecraft.resources.ResourceLocation;

public class CosmeticManager {
    public static final CosmeticManager INSTANCE = new CosmeticManager();
    public static final ResourceLocation ASTRA_CAPE = new ResourceLocation("astra", "textures/capes/astra_cape.png");

    private String customNick = "";
    private boolean capeEnabled = true;

    public boolean hasCustomName() {
        return customNick != null && !customNick.trim().isEmpty();
    }

    public String getCustomName() {
        return customNick;
    }

    public void setCustomName(String customNick) {
        this.customNick = customNick == null ? "" : customNick.trim();
    }

    public boolean isCapeEnabled() {
        return capeEnabled;
    }

    public void setCapeEnabled(boolean capeEnabled) {
        this.capeEnabled = capeEnabled;
    }
}
