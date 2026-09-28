package astra.mod.client.module.impl.render;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.ui.clickgui.ClickGuiScreen;
import org.lwjgl.glfw.GLFW;

public class ClickGuiModule extends Module {
    public ClickGuiModule() {
        super("ClickGUI", "Открывает графический интерфейс управления читом", Category.RENDER, GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    @Override
    public void onEnable() {
        if (mc.screen == null) {
            mc.setScreen(ClickGuiScreen.INSTANCE);
        }
        // Immediately reset module state so it acts as an activator
        setEnabled(false);
    }
}
