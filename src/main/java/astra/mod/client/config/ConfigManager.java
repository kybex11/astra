package astra.mod.client.config;

import astra.mod.client.module.Module;
import astra.mod.client.module.ModuleManager;
import astra.mod.client.setting.BooleanSetting;
import astra.mod.client.setting.ModeSetting;
import astra.mod.client.setting.NumberSetting;
import astra.mod.client.setting.Setting;
import astra.mod.client.setting.StringSetting;
import astra.mod.client.theme.ThemeManager;
import astra.mod.client.ui.hud.HudConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Map;

public class ConfigManager {
    public static final ConfigManager INSTANCE = new ConfigManager();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private File getConfigFile() {
        File dir = new File(Minecraft.getInstance().gameDirectory, "config");
        if (!dir.exists()) dir.mkdirs();
        return new File(dir, "astra_config.json");
    }

    public void save() {
        try {
            JsonObject root = new JsonObject();

            // 1. Save HUD positions
            JsonObject hud = new JsonObject();
            hud.addProperty("fpsX", HudConfig.INSTANCE.fpsX);
            hud.addProperty("fpsY", HudConfig.INSTANCE.fpsY);
            hud.addProperty("cpsX", HudConfig.INSTANCE.cpsX);
            hud.addProperty("cpsY", HudConfig.INSTANCE.cpsY);
            hud.addProperty("comboX", HudConfig.INSTANCE.comboX);
            hud.addProperty("comboY", HudConfig.INSTANCE.comboY);
            hud.addProperty("keystrokesX", HudConfig.INSTANCE.keystrokesX);
            hud.addProperty("keystrokesY", HudConfig.INSTANCE.keystrokesY);
            hud.addProperty("armorX", HudConfig.INSTANCE.armorX);
            hud.addProperty("armorY", HudConfig.INSTANCE.armorY);
            hud.addProperty("targetX", HudConfig.INSTANCE.targetX);
            hud.addProperty("targetY", HudConfig.INSTANCE.targetY);
            hud.addProperty("miniMapX", HudConfig.INSTANCE.miniMapX);
            hud.addProperty("miniMapY", HudConfig.INSTANCE.miniMapY);
            hud.addProperty("coordsX", HudConfig.INSTANCE.coordsX);
            hud.addProperty("coordsY", HudConfig.INSTANCE.coordsY);
            hud.addProperty("pingX", HudConfig.INSTANCE.pingX);
            hud.addProperty("pingY", HudConfig.INSTANCE.pingY);
            hud.addProperty("directionX", HudConfig.INSTANCE.directionX);
            hud.addProperty("directionY", HudConfig.INSTANCE.directionY);
            hud.addProperty("momentumX", HudConfig.INSTANCE.momentumX);
            hud.addProperty("momentumY", HudConfig.INSTANCE.momentumY);
            hud.addProperty("saturationX", HudConfig.INSTANCE.saturationX);
            hud.addProperty("saturationY", HudConfig.INSTANCE.saturationY);
            hud.addProperty("totemX", HudConfig.INSTANCE.totemX);
            hud.addProperty("totemY", HudConfig.INSTANCE.totemY);
            hud.addProperty("itemCountX", HudConfig.INSTANCE.itemCountX);
            hud.addProperty("itemCountY", HudConfig.INSTANCE.itemCountY);
            hud.addProperty("reachX", HudConfig.INSTANCE.reachX);
            hud.addProperty("reachY", HudConfig.INSTANCE.reachY);
            hud.addProperty("stopwatchX", HudConfig.INSTANCE.stopwatchX);
            hud.addProperty("stopwatchY", HudConfig.INSTANCE.stopwatchY);
            hud.addProperty("effectsX", HudConfig.INSTANCE.effectsX);
            hud.addProperty("effectsY", HudConfig.INSTANCE.effectsY);
            hud.addProperty("bossBarX", HudConfig.INSTANCE.bossBarX);
            hud.addProperty("bossBarY", HudConfig.INSTANCE.bossBarY);

            JsonObject scales = new JsonObject();
            for (Map.Entry<String, Float> entry : HudConfig.INSTANCE.elementScales.entrySet()) {
                scales.addProperty(entry.getKey(), entry.getValue());
            }
            hud.add("scales", scales);

            root.add("hud", hud);

            // 2. Save Module states, keybinds & settings
            JsonObject modules = new JsonObject();
            for (Module m : ModuleManager.INSTANCE.getModules()) {
                JsonObject modJson = new JsonObject();
                modJson.addProperty("enabled", m.isEnabled());
                modJson.addProperty("keyCode", m.getKeyCode());

                JsonObject settings = new JsonObject();
                for (Setting<?> s : m.getSettings()) {
                    if (s instanceof BooleanSetting) {
                        settings.addProperty(s.getName(), ((BooleanSetting) s).getValue());
                    } else if (s instanceof NumberSetting) {
                        settings.addProperty(s.getName(), ((NumberSetting) s).getValue());
                    } else if (s instanceof ModeSetting) {
                        settings.addProperty(s.getName(), ((ModeSetting) s).getValue());
                    } else if (s instanceof StringSetting) {
                        settings.addProperty(s.getName(), ((StringSetting) s).getValue());
                    }
                }
                modJson.add("settings", settings);
                modules.add(m.getName(), modJson);
            }
            root.add("modules", modules);

            // 3. Save Theme & Crosshair Canvas
            root.addProperty("theme", ThemeManager.INSTANCE.getCurrentTheme().name());
            if (astra.mod.client.module.impl.render.CustomCrosshair.INSTANCE != null) {
                root.addProperty("crosshairCanvas", astra.mod.client.module.impl.render.CustomCrosshair.INSTANCE.serializeCanvas());
            }

            try (FileWriter writer = new FileWriter(getConfigFile())) {
                GSON.toJson(root, writer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void load() {
        File file = getConfigFile();
        if (!file.exists()) return;

        try (FileReader reader = new FileReader(file)) {
            JsonObject root = new JsonParser().parse(reader).getAsJsonObject();

            // 1. Load HUD
            if (root.has("hud")) {
                JsonObject hud = root.getAsJsonObject("hud");
                if (hud.has("fpsX")) HudConfig.INSTANCE.fpsX = hud.get("fpsX").getAsInt();
                if (hud.has("fpsY")) HudConfig.INSTANCE.fpsY = hud.get("fpsY").getAsInt();
                if (hud.has("cpsX")) HudConfig.INSTANCE.cpsX = hud.get("cpsX").getAsInt();
                if (hud.has("cpsY")) HudConfig.INSTANCE.cpsY = hud.get("cpsY").getAsInt();
                if (hud.has("comboX")) HudConfig.INSTANCE.comboX = hud.get("comboX").getAsInt();
                if (hud.has("comboY")) HudConfig.INSTANCE.comboY = hud.get("comboY").getAsInt();
                if (hud.has("keystrokesX")) HudConfig.INSTANCE.keystrokesX = hud.get("keystrokesX").getAsInt();
                if (hud.has("keystrokesY")) HudConfig.INSTANCE.keystrokesY = hud.get("keystrokesY").getAsInt();
                if (hud.has("armorX")) HudConfig.INSTANCE.armorX = hud.get("armorX").getAsInt();
                if (hud.has("armorY")) HudConfig.INSTANCE.armorY = hud.get("armorY").getAsInt();
                if (hud.has("targetX")) HudConfig.INSTANCE.targetX = hud.get("targetX").getAsInt();
                if (hud.has("targetY")) HudConfig.INSTANCE.targetY = hud.get("targetY").getAsInt();
                if (hud.has("miniMapX")) HudConfig.INSTANCE.miniMapX = hud.get("miniMapX").getAsInt();
                if (hud.has("miniMapY")) HudConfig.INSTANCE.miniMapY = hud.get("miniMapY").getAsInt();
                if (hud.has("coordsX")) HudConfig.INSTANCE.coordsX = hud.get("coordsX").getAsInt();
                if (hud.has("coordsY")) HudConfig.INSTANCE.coordsY = hud.get("coordsY").getAsInt();
                if (hud.has("pingX")) HudConfig.INSTANCE.pingX = hud.get("pingX").getAsInt();
                if (hud.has("pingY")) HudConfig.INSTANCE.pingY = hud.get("pingY").getAsInt();
                if (hud.has("directionX")) HudConfig.INSTANCE.directionX = hud.get("directionX").getAsInt();
                if (hud.has("directionY")) HudConfig.INSTANCE.directionY = hud.get("directionY").getAsInt();
                if (hud.has("momentumX")) HudConfig.INSTANCE.momentumX = hud.get("momentumX").getAsInt();
                if (hud.has("momentumY")) HudConfig.INSTANCE.momentumY = hud.get("momentumY").getAsInt();
                if (hud.has("saturationX")) HudConfig.INSTANCE.saturationX = hud.get("saturationX").getAsInt();
                if (hud.has("saturationY")) HudConfig.INSTANCE.saturationY = hud.get("saturationY").getAsInt();
                if (hud.has("totemX")) HudConfig.INSTANCE.totemX = hud.get("totemX").getAsInt();
                if (hud.has("totemY")) HudConfig.INSTANCE.totemY = hud.get("totemY").getAsInt();
                if (hud.has("itemCountX")) HudConfig.INSTANCE.itemCountX = hud.get("itemCountX").getAsInt();
                if (hud.has("itemCountY")) HudConfig.INSTANCE.itemCountY = hud.get("itemCountY").getAsInt();
                if (hud.has("reachX")) HudConfig.INSTANCE.reachX = hud.get("reachX").getAsInt();
                if (hud.has("reachY")) HudConfig.INSTANCE.reachY = hud.get("reachY").getAsInt();
                if (hud.has("stopwatchX")) HudConfig.INSTANCE.stopwatchX = hud.get("stopwatchX").getAsInt();
                if (hud.has("stopwatchY")) HudConfig.INSTANCE.stopwatchY = hud.get("stopwatchY").getAsInt();
                if (hud.has("effectsX")) HudConfig.INSTANCE.effectsX = hud.get("effectsX").getAsInt();
                if (hud.has("effectsY")) HudConfig.INSTANCE.effectsY = hud.get("effectsY").getAsInt();
                if (hud.has("bossBarX")) HudConfig.INSTANCE.bossBarX = hud.get("bossBarX").getAsInt();
                if (hud.has("bossBarY")) HudConfig.INSTANCE.bossBarY = hud.get("bossBarY").getAsInt();

                if (hud.has("scales")) {
                    JsonObject scales = hud.getAsJsonObject("scales");
                    for (Map.Entry<String, com.google.gson.JsonElement> entry : scales.entrySet()) {
                        HudConfig.INSTANCE.elementScales.put(entry.getKey(), entry.getValue().getAsFloat());
                    }
                }
            }

            // 2. Load Modules
            if (root.has("modules")) {
                JsonObject modules = root.getAsJsonObject("modules");
                for (Module m : ModuleManager.INSTANCE.getModules()) {
                    if (modules.has(m.getName())) {
                        JsonObject modJson = modules.getAsJsonObject(m.getName());
                        if (modJson.has("enabled")) {
                            m.setEnabled(modJson.get("enabled").getAsBoolean());
                        }
                        if (modJson.has("keyCode")) {
                            m.setKeyCode(modJson.get("keyCode").getAsInt());
                        }

                        if (modJson.has("settings")) {
                            JsonObject settings = modJson.getAsJsonObject("settings");
                            for (Setting<?> s : m.getSettings()) {
                                if (settings.has(s.getName())) {
                                    if (s instanceof BooleanSetting) {
                                        ((BooleanSetting) s).setValue(settings.get(s.getName()).getAsBoolean());
                                    } else if (s instanceof NumberSetting) {
                                        ((NumberSetting) s).setValue(settings.get(s.getName()).getAsDouble());
                                    } else if (s instanceof ModeSetting) {
                                        ((ModeSetting) s).setValue(settings.get(s.getName()).getAsString());
                                    } else if (s instanceof StringSetting) {
                                        ((StringSetting) s).setValue(settings.get(s.getName()).getAsString());
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. Load Theme & Crosshair Canvas
            if (root.has("theme")) {
                try {
                    ThemeManager.INSTANCE.setTheme(ThemeManager.Theme.valueOf(root.get("theme").getAsString()));
                } catch (Exception ignored) {}
            }
            if (root.has("crosshairCanvas") && astra.mod.client.module.impl.render.CustomCrosshair.INSTANCE != null) {
                astra.mod.client.module.impl.render.CustomCrosshair.INSTANCE.deserializeCanvas(root.get("crosshairCanvas").getAsString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
