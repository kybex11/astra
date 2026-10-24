package astra.mod.mixin;

import astra.mod.client.ui.PauseMenuHandler;
import astra.mod.client.ui.clickgui.ParticleEngine;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PauseScreen.class)
public abstract class PauseScreenMixin extends Screen {
    protected PauseScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void onInit(CallbackInfo ci) {
        ParticleEngine.INSTANCE.init(this.width, this.height);
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(PoseStack poseStack, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ci.cancel();
        PauseMenuHandler.render(this, poseStack, mouseX, mouseY, delta);
    }
}
