package astra.mod;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import astra.mod.ui.UserInterface;

public class Astra implements ModInitializer {
	public static final String MOD_ID = "astra";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	// Ui Entry
	private static final UserInterface UI = new UserInterface();

	@Override
	public void onInitialize() {
		LOGGER.info("Astra initialized!");
		LOGGER.info("Starting package initialization");


	}

	public static ResourceLocation id(String path) {
		return new ResourceLocation(MOD_ID, path);
	}
}
