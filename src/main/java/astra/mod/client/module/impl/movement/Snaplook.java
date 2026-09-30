package astra.mod.client.module.impl.movement;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.NumberSetting;
import org.lwjgl.glfw.GLFW;

public class Snaplook extends Module {
    public static Snaplook INSTANCE;

    public final NumberSetting holdKey = new NumberSetting("Клавиша", "Клавиша взгляда назад (код)", 86.0, 0.0, 350.0, 1.0);

    private static boolean flipped = false;
    public static float snapYaw = 0.0f;
    public static float snapPitch = 0.0f;

    public Snaplook() {
        super("Snaplook", "Быстрый взгляд назад на удержание клавиши", Category.MOVEMENT);
        INSTANCE = this;
        addSettings(holdKey);
        setEnabled(true);
    }

    public static boolean isFlipped() {
        return flipped;
    }

    @Override
    public void onTick() {
        if (!isEnabled() || mc.player == null || mc.screen != null) {
            flipped = false;
            return;
        }
        int code = holdKey.getValue().intValue();
        boolean down = code > 0 && mc.getWindow() != null
                && GLFW.glfwGetKey(mc.getWindow().getWindow(), code) == GLFW.GLFW_PRESS;
        flipped = down;
        if (down) {
            snapYaw = mc.player.yRot + 180.0f;
            snapPitch = mc.player.xRot;
        }
    }

    @Override
    public void onDisable() {
        flipped = false;
    }
}
