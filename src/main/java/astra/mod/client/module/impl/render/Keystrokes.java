package astra.mod.client.module.impl.render;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.BooleanSetting;
import astra.mod.client.setting.NumberSetting;

import java.util.ArrayList;
import java.util.List;

public class Keystrokes extends Module {
    public static Keystrokes INSTANCE;

    public final BooleanSetting showCPS = new BooleanSetting("ShowCPS", "Отображать CPS на кнопках мыши", true);
    public final BooleanSetting showSpace = new BooleanSetting("ShowSpace", "Отображать клавишу Пробел", true);
    public final BooleanSetting showMouse = new BooleanSetting("ShowMouse", "Отображать ЛКМ и ПКМ", true);
    public final NumberSetting x = new NumberSetting("X", "Позиция X", 20.0, 0.0, 1000.0, 5.0);
    public final NumberSetting y = new NumberSetting("Y", "Позиция Y", 60.0, 0.0, 1000.0, 5.0);

    private final List<Long> leftClicks = new ArrayList<>();
    private final List<Long> rightClicks = new ArrayList<>();

    public Keystrokes() {
        super("Keystrokes", "Отображает нажатия клавиш WASD и мыши", Category.HUD);
        INSTANCE = this;
        addSettings(showCPS, showSpace, showMouse, x, y);
        setEnabled(true);
    }

    public void registerClick(int button) {
        long now = System.currentTimeMillis();
        if (button == 0) {
            leftClicks.add(now);
        } else if (button == 1) {
            rightClicks.add(now);
        }
    }

    public int getLeftCPS() {
        long now = System.currentTimeMillis();
        leftClicks.removeIf(time -> now - time > 1000);
        return leftClicks.size();
    }

    public int getRightCPS() {
        long now = System.currentTimeMillis();
        rightClicks.removeIf(time -> now - time > 1000);
        return rightClicks.size();
    }
}
