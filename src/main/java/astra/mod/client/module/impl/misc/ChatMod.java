package astra.mod.client.module.impl.misc;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.NumberSetting;
import org.lwjgl.glfw.GLFW;

public class ChatMod extends Module {
    public static ChatMod INSTANCE;

    public final NumberSetting width = new NumberSetting("Ширина", "Ширина окна чата", 1.0, 0.3, 1.0, 0.05);
    public final NumberSetting clearKey = new NumberSetting("Очистить", "Клавиша очистки чата (код)", 261.0, 0.0, 350.0, 1.0);

    private boolean wasDown = false;

    public ChatMod() {
        super("Chat", "Настройки чата: ширина и очистка по клавише", Category.MISC);
        INSTANCE = this;
        addSettings(width, clearKey);
        setEnabled(true);
    }

    @Override
    public void onTick() {
        if (mc.options != null && isEnabled()) {
            mc.options.chatWidth = width.getValue();
        }
        if (mc.screen != null || mc.getWindow() == null) {
            wasDown = false;
            return;
        }
        int code = clearKey.getValue().intValue();
        boolean down = code > 0 && GLFW.glfwGetKey(mc.getWindow().getWindow(), code) == GLFW.GLFW_PRESS;
        if (down && !wasDown) {
            try {
                if (mc.gui != null && mc.gui.getChat() != null) {
                    mc.gui.getChat().clearMessages(false);
                }
            } catch (Exception ignored) {}
        }
        wasDown = down;
    }
}
