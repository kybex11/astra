package astra.mod.client;

import astra.mod.client.module.ModuleManager;
import astra.mod.client.ui.shadcn.ShadcnGuiScreen;
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
    public static KeyMapping worldMapKey;

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

        // Keybind for Big World Map (M)
        worldMapKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.astra.worldmap",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_M,
                "category.astra"
        ));

        // Astra Client Presence Network Handshake for Tab badges & client detection
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            try {
                net.minecraft.network.FriendlyByteBuf buf = new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
                buf.writeUUID(client.getUser().getGameProfile().getId());
                buf.writeUtf(client.getUser().getName(), 32);
                sender.sendPacket(astra.mod.client.presence.AstraPresenceManager.CHANNEL_PRESENCE, buf);
            } catch (Exception ignored) {}
        });

        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(
                astra.mod.client.presence.AstraPresenceManager.CHANNEL_PRESENCE,
                (client, handler, buf, responseSender) -> {
                    try {
                        java.util.UUID uuid = buf.readUUID();
                        String name = buf.readUtf(32);
                        client.execute(() -> astra.mod.client.presence.AstraPresenceManager.INSTANCE.registerUser(uuid, name));
                    } catch (Exception ignored) {}
                }
        );

        // Register 3D World Render Event for 3D Box ESP, Tracers, Trails & XRay
        net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.LAST.register(context -> {
            astra.mod.client.util.Render3D.renderWorld(context.matrixStack(), context.camera(), context.tickDelta());
            if (astra.mod.client.module.impl.render.Trails.INSTANCE != null) {
                astra.mod.client.module.impl.render.Trails.INSTANCE.render3D(context.matrixStack(), context.camera(), context.tickDelta());
            }
            if (astra.mod.client.module.impl.render.XRay.INSTANCE != null) {
                astra.mod.client.module.impl.render.XRay.INSTANCE.render3D(context.matrixStack(), context.camera(), context.tickDelta());
            }
            if (astra.mod.client.module.impl.render.Waypoints.INSTANCE != null) {
                astra.mod.client.module.impl.render.Waypoints.INSTANCE.render3D(context.matrixStack(), context.camera(), context.tickDelta());
            }
            if (astra.mod.client.module.impl.render.JumpCircles.INSTANCE != null) {
                astra.mod.client.module.impl.render.JumpCircles.INSTANCE.render3D(context.matrixStack(), context.camera(), context.tickDelta());
            }
            if (astra.mod.client.module.impl.render.TargetESP.INSTANCE != null) {
                astra.mod.client.module.impl.render.TargetESP.INSTANCE.render3D(context.matrixStack(), context.camera(), context.tickDelta());
            }
        });

        // Client Tick Handler
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Set Window Icon, PvP Pack export, and Initial Title once window is ready
            if (!initialized && client.getWindow() != null) {
                astra.mod.client.storage.AstraDatabase.INSTANCE.init();
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
                        if (astra.mod.client.module.impl.render.ClickGuiModule.INSTANCE != null) {
                            astra.mod.client.module.impl.render.ClickGuiModule.INSTANCE.openGui();
                        } else {
                            client.setScreen(astra.mod.client.web.WebGuiScreen.INSTANCE);
                        }
                    }
                }

                while (worldMapKey.consumeClick()) {
                    if (client.screen == null) {
                        client.setScreen(astra.mod.client.ui.map.WorldMapScreen.INSTANCE);
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
