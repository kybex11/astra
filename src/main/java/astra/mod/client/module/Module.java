package astra.mod.client.module;

import astra.mod.client.Category;
import astra.mod.client.setting.Setting;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Module {
    protected final Minecraft mc = Minecraft.getInstance();
    private final String name;
    private final String description;
    private final Category category;
    private int keyCode;
    private boolean enabled;
    private final List<Setting<?>> settings = new ArrayList<>();

    public Module(String name, String description, Category category) {
        this(name, description, category, GLFW.GLFW_KEY_UNKNOWN);
    }

    public Module(String name, String description, Category category, int keyCode) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.keyCode = keyCode;
        this.enabled = false;
    }

    public void addSettings(Setting<?>... settings) {
        this.settings.addAll(Arrays.asList(settings));
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            if (this.enabled) {
                onEnable();
            } else {
                onDisable();
            }
        }
    }

    public void onEnable() {}

    public void onDisable() {}

    public void onTick() {}

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Category getCategory() {
        return category;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public List<Setting<?>> getSettings() {
        return settings;
    }
}
