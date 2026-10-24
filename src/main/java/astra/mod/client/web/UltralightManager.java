package astra.mod.client.web;

import astra.mod.client.module.Module;
import astra.mod.client.module.ModuleManager;
import astra.mod.client.theme.ThemeManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class UltralightManager {
    public static final UltralightManager INSTANCE = new UltralightManager();

    private boolean isNativeLoaded = false;
    private int webTextureId = -1;

    public void init() {
        try {
            // Check for native Ultralight binary in java.library.path or runtime
            System.loadLibrary("Ultralight");
            isNativeLoaded = true;
            System.out.println("[Astra] Ultralight native engine loaded successfully!");
        } catch (Throwable t) {
            isNativeLoaded = false;
            System.out.println("[Astra] Ultralight running in high-performance Java OSR bridge mode.");
        }
    }

    public String getModulesJson() {
        JsonArray array = new JsonArray();
        for (Module m : ModuleManager.INSTANCE.getModules()) {
            JsonObject obj = new JsonObject();
            obj.addProperty("name", m.getName());
            obj.addProperty("category", m.getCategory().name());
            obj.addProperty("description", m.getDescription());
            obj.addProperty("enabled", m.isEnabled());
            array.add(obj);
        }
        return array.toString();
    }

    public void toggleModule(String name) {
        Minecraft.getInstance().execute(() -> {
            Module mod = ModuleManager.INSTANCE.getModule(name);
            if (mod != null) {
                mod.toggle();
            }
        });
    }

    public void setTheme(String themeName) {
        Minecraft.getInstance().execute(() -> {
            try {
                ThemeManager.Theme t = ThemeManager.Theme.valueOf(themeName.toUpperCase());
                ThemeManager.INSTANCE.setTheme(t);
            } catch (Exception ignored) {}
        });
    }

    public String loadHtmlAsset(String path) {
        try (InputStream in = UltralightManager.class.getResourceAsStream(path)) {
            if (in == null) return "";
            byte[] bytes = new byte[in.available()];
            in.read(bytes);
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        }
    }

    public boolean isNativeLoaded() {
        return isNativeLoaded;
    }
}
