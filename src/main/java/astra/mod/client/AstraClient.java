package astra.mod.client;

import astra.mod.client.module.ModuleManager;
import astra.mod.client.ui.clickgui.ClickGuiScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class AstraClient implements ClientModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("astra-client");
    public static KeyMapping clickGuiKey;

    private boolean initialized = false;
    private int titleTickTimer = 0;

    @Override
    public void onInitializeClient() {
        // Register Builtin PvP Resource Pack as ALWAYS_ENABLED
        FabricLoader.getInstance().getModContainer("astra").ifPresent(modContainer -> {
            boolean registered = ResourceManagerHelper.registerBuiltinResourcePack(
                    new ResourceLocation("astra", "astra_pvp"),
                    modContainer,
                    ResourcePackActivationType.ALWAYS_ENABLED
            );
            LOGGER.info("Astra PvP Built-in Pack registered: " + registered);
        });

        // Keybind for ClickGUI (Right Shift)
        clickGuiKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.astra.clickgui",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.astra"
        ));

        // Register 3D World Render Event for 3D Box ESP & Tracers
        net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.LAST.register(context -> {
            astra.mod.client.util.Render3D.renderWorld(context.matrixStack(), context.camera(), context.tickDelta());
        });

        // Client Tick Handler
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Set Window Icon, PvP Pack export, and Initial Title once window is ready
            if (!initialized && client.getWindow() != null) {
                applyWindowCustomizations(client);
                ensurePvPPackExported(client);
                astra.mod.client.config.ConfigManager.INSTANCE.load();
                initialized = true;
            }

            // Periodically update Window Title with FPS & player name
            if (initialized && ++titleTickTimer >= 20) {
                titleTickTimer = 0;
                updateWindowTitle(client);
            }

            if (client.player != null) {
                // Update modules
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

    private void ensurePvPPackExported(Minecraft mc) {
        try {
            File packDir = new File(mc.gameDirectory, "resourcepacks");
            if (!packDir.exists()) packDir.mkdirs();

            File targetZip = new File(packDir, "Astra_PvP_Pack.zip");
            if (!targetZip.exists()) {
                InputStream zipStream = AstraClient.class.getResourceAsStream("/resourcepacks/Astra_PvP_Pack.zip");
                if (zipStream != null) {
                    try (FileOutputStream fos = new FileOutputStream(targetZip)) {
                        byte[] buffer = new byte[8192];
                        int read;
                        while ((read = zipStream.read(buffer)) != -1) {
                            fos.write(buffer, 0, read);
                        }
                    }
                    LOGGER.info("Successfully exported Astra_PvP_Pack.zip to resourcepacks directory!");
                }
            }

            // Ensure PvP pack is enabled in options.resourcePacks
            if (mc.options != null && mc.options.resourcePacks != null) {
                if (!mc.options.resourcePacks.contains("Astra_PvP_Pack.zip")) {
                    mc.options.resourcePacks.add("Astra_PvP_Pack.zip");
                }
                if (!mc.options.resourcePacks.contains("fabric/astra:astra_pvp")) {
                    mc.options.resourcePacks.add("fabric/astra:astra_pvp");
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to export/enable PvP pack: " + e.getMessage());
        }
    }

    private void applyWindowCustomizations(Minecraft mc) {
        try {
            InputStream icon16 = AstraClient.class.getResourceAsStream("/assets/astra/icons/icon_16.png");
            InputStream icon32 = AstraClient.class.getResourceAsStream("/assets/astra/icons/icon_32.png");
            if (icon16 != null && icon32 != null) {
                mc.getWindow().setIcon(icon16, icon32);
                LOGGER.info("Applied custom Astra client icon to window!");
            }
        } catch (Exception e) {
            LOGGER.warn("Could not apply custom window icon: " + e.getMessage());
        }

        updateWindowTitle(mc);
    }

    private void updateWindowTitle(Minecraft mc) {
        try {
            if (mc.getWindow() != null) {
                String fps = (mc.fpsString != null && mc.fpsString.contains(" ")) ? mc.fpsString.split(" ")[0] : "60";
                String user = (mc.getUser() != null) ? mc.getUser().getName() : "Player";
                String title = "Astra Client 1.16.5 | " + fps + " FPS | " + user;
                mc.getWindow().setTitle(title);
            }
        } catch (Exception ignored) {}
    }
}
