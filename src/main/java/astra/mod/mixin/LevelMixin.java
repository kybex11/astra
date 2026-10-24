package astra.mod.mixin;

import astra.mod.client.module.impl.render.TimeChanger;
import astra.mod.client.module.impl.render.WeatherChanger;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public class LevelMixin {
    @Inject(method = "getDayTime", at = @At("HEAD"), cancellable = true)
    private void onGetDayTime(CallbackInfoReturnable<Long> cir) {
        if (TimeChanger.INSTANCE != null && TimeChanger.INSTANCE.isEnabled()) {
            cir.setReturnValue(TimeChanger.INSTANCE.getTargetTime());
        }
    }

    @Inject(method = "getRainLevel", at = @At("HEAD"), cancellable = true)
    private void onGetRainLevel(float tickDelta, CallbackInfoReturnable<Float> cir) {
        if (WeatherChanger.INSTANCE != null && WeatherChanger.INSTANCE.isEnabled()) {
            if (WeatherChanger.INSTANCE.mode.is("Clear")) {
                cir.setReturnValue(0.0f);
            } else if (WeatherChanger.INSTANCE.mode.is("Rain") || WeatherChanger.INSTANCE.mode.is("Thunder")) {
                cir.setReturnValue(1.0f);
            }
        }
    }
}
