package astra.mod.client.setting;

public class NumberSetting extends Setting<Double> {
    private final double min;
    private final double max;
    private final double step;

    public NumberSetting(String name, String description, double defaultValue, double min, double max, double step) {
        super(name, description, defaultValue);
        this.min = min;
        this.max = max;
        this.step = step;
    }

    public NumberSetting(String name, double defaultValue, double min, double max, double step) {
        this(name, name, defaultValue, min, max, step);
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getStep() {
        return step;
    }

    @Override
    public void setValue(Double value) {
        double clamped = Math.max(min, Math.min(max, value));
        double stepped = Math.round(clamped / step) * step;
        super.setValue(stepped);
    }
}
