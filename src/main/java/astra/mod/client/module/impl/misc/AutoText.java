package astra.mod.client.module.impl.misc;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.NumberSetting;
import astra.mod.client.setting.StringSetting;
import org.lwjgl.glfw.GLFW;

public class AutoText extends Module {
    public static AutoText INSTANCE;

    public final StringSetting text1 = new StringSetting("Текст 1", "Сообщение первого макроса", "gl hf!");
    public final NumberSetting key1 = new NumberSetting("Клавиша 1", "Клавиша первого макроса (код)", 71.0, 0.0, 350.0, 1.0);
    public final StringSetting text2 = new StringSetting("Текст 2", "Сообщение второго макроса", "gg!");
    public final NumberSetting key2 = new NumberSetting("Клавиша 2", "Клавиша второго макроса (код)", 72.0, 0.0, 350.0, 1.0);
    public final StringSetting text3 = new StringSetting("Текст 3", "Сообщение третьего макроса", "re?");
    public final NumberSetting key3 = new NumberSetting("Клавиша 3", "Клавиша третьего макроса (код)", 74.0, 0.0, 350.0, 1.0);
    public final StringSetting text4 = new StringSetting("Текст 4", "Сообщение четвёртого макроса", "wp!");
    public final NumberSetting key4 = new NumberSetting("Клавиша 4", "Клавиша четвёртого макроса (код)", 75.0, 0.0, 350.0, 1.0);

    private final boolean[] wasDown = new boolean[4];

    public AutoText() {
        super("AutoText", "Макросы: отправка сообщений по горячим клавишам", Category.MISC);
        INSTANCE = this;
        addSettings(text1, key1, text2, key2, text3, key3, text4, key4);
        setEnabled(true);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.screen != null || mc.getWindow() == null) return;
        long handle = mc.getWindow().getWindow();
        checkSlot(0, handle, key1.getValue().intValue(), text1.getValue());
        checkSlot(1, handle, key2.getValue().intValue(), text2.getValue());
        checkSlot(2, handle, key3.getValue().intValue(), text3.getValue());
        checkSlot(3, handle, key4.getValue().intValue(), text4.getValue());
    }

    private void checkSlot(int idx, long handle, int keyCode, String text) {
        boolean down = keyCode > 0 && GLFW.glfwGetKey(handle, keyCode) == GLFW.GLFW_PRESS;
        if (down && !wasDown[idx]) {
            if (text != null && !text.trim().isEmpty()) {
                try {
                    mc.player.chat(text.trim());
                } catch (Exception ignored) {}
            }
        }
        wasDown[idx] = down;
    }
}
