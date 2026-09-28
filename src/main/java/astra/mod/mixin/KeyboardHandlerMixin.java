package astra.mod.mixin;

import astra.mod.client.module.ModuleManager;
import astra.mod.client.ui.clickgui.ClickGuiScreen;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {
    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    private void onKeyPress(long window, int key, int scancode, int action, int mods, CallbackInfo ci) {
        if (action == 1) { // GLFW_PRESS
            astra.mod.client.module.impl.render.KeySounds.INSTANCE.onKey(key, action);
            Minecraft mc = Minecraft.getInstance();
            if (key == GLFW.GLFW_KEY_RIGHT_SHIFT) {
                if (mc.screen == null) {
                    mc.setScreen(ClickGuiScreen.INSTANCE);
                    ci.cancel();
                    return;
                } else if (mc.screen instanceof ClickGuiScreen) {
                    mc.screen.onClose();
                    ci.cancel();
                    return;
                }
            }

            if (mc.screen == null && mc.player != null) {
                ModuleManager.INSTANCE.handleKey(key);
            }
        }
    }
}
