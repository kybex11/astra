package astra.mod.client.module.impl.render;

import astra.mod.client.Category;
import astra.mod.client.module.Module;
import astra.mod.client.setting.ModeSetting;
import astra.mod.client.ui.clickgui.ClickGuiScreen;
import astra.mod.client.ui.shadcn.ShadcnGuiScreen;
import astra.mod.client.web.WebGuiScreen;
import org.lwjgl.glfw.GLFW;

public class ClickGuiModule extends Module {
    public static ClickGuiModule INSTANCE;

    public final ModeSetting style = new ModeSetting("Стиль", "Тип интерфейса меню", "Web (Ultralight)", "Web (Ultralight)", "Shadcn UI", "Classic");

    public ClickGuiModule() {
        super("ClickGUI", "Открывает графический интерфейс управления клиентом", Category.RENDER, GLFW.GLFW_KEY_RIGHT_SHIFT);
        INSTANCE = this;
        addSettings(style);
    }

    public void openGui() {
        if (mc.screen == null) {
            if (style.is("Web (Ultralight)")) {
                mc.setScreen(WebGuiScreen.INSTANCE);
            } else if (style.is("Shadcn UI")) {
                mc.setScreen(ShadcnGuiScreen.INSTANCE);
            } else {
                mc.setScreen(ClickGuiScreen.INSTANCE);
            }
        }
    }

    @Override
    public void onEnable() {
        openGui();
        setEnabled(false);
    }
}
