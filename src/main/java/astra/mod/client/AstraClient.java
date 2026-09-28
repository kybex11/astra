package astra.mod.client;

import astra.mod.client.module.ModuleManager;
import astra.mod.client.ui.clickgui.ClickGuiScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class AstraClient implements ClientModInitializer {
    public static KeyMapping clickGuiKey;

    @Override
    public void onInitializeClient() {
        clickGuiKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.astra.clickgui",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.astra"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) {
                // Update active modules
                ModuleManager.INSTANCE.onTick(client);

                // Check key mapping click
                while (clickGuiKey.consumeClick()) {
                    if (client.screen == null) {
                        client.setScreen(ClickGuiScreen.INSTANCE);
                    }
                }
            }
        });
    }
}
