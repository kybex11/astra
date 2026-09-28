package astra.mod.mixin;

import astra.mod.client.module.ModuleManager;
import astra.mod.client.module.impl.render.Keystrokes;
import astra.mod.client.ui.PauseMenuHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.PauseScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    @Inject(method = "onPress", at = @At("HEAD"), cancellable = true)
    private void onMousePress(long window, int button, int action, int mods, CallbackInfo ci) {
        if (action == 1) { // 1 = GLFW_PRESS
            astra.mod.client.module.impl.render.KeySounds.INSTANCE.onMouse(button, action);
            if (Keystrokes.INSTANCE != null) {
                Keystrokes.INSTANCE.registerClick(button);
            }

            Minecraft mc = Minecraft.getInstance();
            if (mc.screen == null && mc.player != null) {
                ModuleManager.INSTANCE.handleMouse(button);
            }

            if (mc.screen instanceof PauseScreen) {
                double mouseX = mc.mouseHandler.xpos() * (double) mc.getWindow().getGuiScaledWidth() / (double) mc.getWindow().getScreenWidth();
                double mouseY = mc.mouseHandler.ypos() * (double) mc.getWindow().getGuiScaledHeight() / (double) mc.getWindow().getScreenHeight();
                if (PauseMenuHandler.handleClick(mc.screen, mouseX, mouseY, button)) {
                    ci.cancel();
                }
            }
        }
    }
}
