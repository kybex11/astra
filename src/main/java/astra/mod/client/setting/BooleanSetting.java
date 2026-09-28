package astra.mod.client.setting;

public class BooleanSetting extends Setting<Boolean> {
    public BooleanSetting(String name, String description, boolean defaultValue) {
        super(name, description, defaultValue);
    }

    public BooleanSetting(String name, boolean defaultValue) {
        this(name, name, defaultValue);
    }

    public void toggle() {
        this.value = !this.value;
    }
}
