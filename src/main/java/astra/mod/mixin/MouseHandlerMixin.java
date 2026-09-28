package astra.mod.mixin;

import astra.mod.client.module.impl.render.Keystrokes;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    @Inject(method = "onPress", at = @At("HEAD"))
    private void onMousePress(long window, int button, int action, int mods, CallbackInfo ci) {
        if (action == 1 && Keystrokes.INSTANCE != null) { // 1 = GLFW_PRESS
            Keystrokes.INSTANCE.registerClick(button);
        }
    }
}
