package astra.mod.client.module.impl.render;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.BooleanSetting;

public class StopwatchMod extends Module {
    public static StopwatchMod INSTANCE;

    public final BooleanSetting showMillis = new BooleanSetting("Миллисекунды", "Показывать десятые доли секунды", true);

    private long startTime = 0L;
    private long stoppedAt = 0L;
    private boolean running = false;

    public StopwatchMod() {
        super("Stopwatch", "Внутриигровой секундомер", Category.HUD);
        INSTANCE = this;
        addSettings(showMillis);
        setEnabled(false);
    }

    @Override
    public void onEnable() {
        startTime = System.currentTimeMillis();
        stoppedAt = 0L;
        running = true;
    }

    @Override
    public void onDisable() {
        running = false;
        stoppedAt = System.currentTimeMillis();
    }

    public String getText() {
        long now = running ? System.currentTimeMillis() : (stoppedAt == 0L ? System.currentTimeMillis() : stoppedAt);
        long elapsed = Math.max(0L, now - startTime);
        long minutes = elapsed / 60000L;
        long seconds = (elapsed % 60000L) / 1000L;
        long tenths = (elapsed % 1000L) / 100L;
        String base = String.format("%02d:%02d", minutes, seconds);
        if (showMillis.getValue()) base += "." + tenths;
        return base;
    }
}
