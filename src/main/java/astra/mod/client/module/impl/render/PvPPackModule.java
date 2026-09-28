package astra.mod.client.module.impl.render;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import net.minecraft.Util;

import java.io.File;

public class PvPPackModule extends Module {
    public PvPPackModule() {
        super("PvPPack", "Открывает папку ресурс-паков со встроенным Astra PvP Pack", Category.RENDER);
    }

    @Override
    public void onEnable() {
        if (mc != null) {
            File packDir = new File(mc.gameDirectory, "resourcepacks");
            if (!packDir.exists()) {
                packDir.mkdirs();
            }
            Util.getPlatform().openFile(packDir);
        }
        setEnabled(false);
    }
}
