package astra.mod.client.config;

import astra.mod.client.module.Module;
import astra.mod.client.module.ModuleManager;
import astra.mod.client.setting.BooleanSetting;
import astra.mod.client.setting.ModeSetting;
import astra.mod.client.setting.NumberSetting;
import astra.mod.client.setting.Setting;
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
                    }
                }
                modJson.add("settings", settings);
                modules.add(m.getName(), modJson);
            }
            root.add("modules", modules);

            // 3. Save Theme
            root.addProperty("theme", ThemeManager.INSTANCE.getCurrentTheme().name());

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
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. Load Theme
            if (root.has("theme")) {
                try {
                    ThemeManager.INSTANCE.setTheme(ThemeManager.Theme.valueOf(root.get("theme").getAsString()));
                } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
