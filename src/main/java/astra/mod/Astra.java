package astra.mod;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Astra implements ModInitializer {
    public static final String MOD_ID = "astra";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Astra Client Mod core initialized!");
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
