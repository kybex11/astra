package astra.mod.client.setting;

public class StringSetting extends Setting<String> {
    private final int maxLength;

    public StringSetting(String name, String description, String defaultValue) {
        this(name, description, defaultValue, 64);
    }

    public StringSetting(String name, String description, String defaultValue, int maxLength) {
        super(name, description, defaultValue);
        this.maxLength = maxLength;
    }

    public StringSetting(String name, String defaultValue) {
        this(name, name, defaultValue, 64);
    }

    @Override
    public void setValue(String value) {
        if (value == null) value = "";
        if (value.length() > maxLength) value = value.substring(0, maxLength);
        super.setValue(value);
    }

    public int getMaxLength() {
        return maxLength;
    }
}
